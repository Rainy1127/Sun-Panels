package net.rainy.sun_panels.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.rainy.sun_panels.compat.ModCompatManager;
import net.rainy.sun_panels.compat.mekanism.MekanismEnergyAdapter;
import net.rainy.sun_panels.menu.SolarPanelMenu;
import net.rainy.sun_panels.registry.ModBlockEntities;
import net.rainy.sun_panels.registry.ModBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseSolarPanelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SKY_BLOCKED = 0;
    public static final int SKY_DARK = 1;
    public static final int SKY_SUN = 2;

    private static final Direction[] DIRECTIONS = Direction.values();
    // Sky visibility only changes when blocks or time change, so it is re-evaluated once per second
    // (staggered per panel) instead of every tick.
    private static final int SKY_CHECK_INTERVAL = 20;
    // At night vanilla darkens sky light by 11, so the effective sky brightness bottoms out at 4.
    private static final int NIGHT_LIGHT = 4;
    private static final int MAX_LIGHT = 15;

    protected final CustomEnergyStorage energyStorage;
    protected final int generationRate;
    protected int currentGeneration;

    // What other blocks see through the capability: extract-only, so pipes and machines
    // can never push energy back into a panel.
    private final IEnergyStorage exposedStorage;

    private int skyCheckCountdown;
    private int cachedGeneration;
    @Nullable
    private BlockCapabilityCache<IEnergyStorage, Direction>[] outputCaches;

    private int skyState = SKY_BLOCKED;

    // Read-only values for the status screen, synced by vanilla data slots only when they change.
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case SolarPanelMenu.DATA_ENERGY -> energyStorage.getEnergyStored();
                case SolarPanelMenu.DATA_CAPACITY -> energyStorage.getMaxEnergyStored();
                case SolarPanelMenu.DATA_OUTPUT -> currentGeneration;
                case SolarPanelMenu.DATA_MAX_OUTPUT -> generationRate;
                case SolarPanelMenu.DATA_SKY -> skyState;
                case SolarPanelMenu.DATA_TIER -> getTierIndex();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return SolarPanelMenu.DATA_COUNT;
        }
    };

    public BaseSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity, int generationRate) {
        super(type, pos, state);
        this.energyStorage = new CustomEnergyStorage(capacity, generationRate, capacity);
        this.exposedStorage = new ExtractOnlyStorage(this.energyStorage);
        this.generationRate = generationRate;
        this.currentGeneration = 0;
        this.skyCheckCountdown = Math.floorMod(pos.hashCode(), SKY_CHECK_INTERVAL);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.BASIC_SOLAR_PANEL.get(),
                (panel, side) -> panel.getExposedStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ADVANCED_SOLAR_PANEL.get(),
                (panel, side) -> panel.getExposedStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ELITE_SOLAR_PANEL.get(),
                (panel, side) -> panel.getExposedStorage());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BaseSolarPanelBlockEntity panel) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (--panel.skyCheckCountdown <= 0) {
            panel.skyCheckCountdown = SKY_CHECK_INTERVAL;
            panel.cachedGeneration = panel.computeGeneration(level, pos);
            panel.skyState = panel.cachedGeneration > 0 ? SKY_SUN
                    : level.canSeeSky(pos.above()) ? SKY_DARK : SKY_BLOCKED;
        }

        panel.currentGeneration = panel.cachedGeneration;
        if (panel.cachedGeneration > 0 && panel.energyStorage.receiveEnergy(panel.cachedGeneration, false) > 0) {
            panel.setChanged();
        }

        panel.pushEnergy(serverLevel, pos);
    }

    /**
     * Uses the sky light level of the space above the panel instead of requiring that space to be air.
     * Sky light passes through transparent blocks such as glass without losing strength,
     * so a glass block directly on top of the panel no longer stops generation.
     * <p>
     * Output follows the effective sky brightness (sky light minus the current darkening from time of day
     * and weather): full output in clear daylight, a smooth ramp at dawn, dusk and in rain, nothing at night.
     */
    protected int computeGeneration(Level level, BlockPos pos) {
        if (!level.dimensionType().hasSkyLight()) {
            return 0;
        }

        BlockPos abovePos = pos.above();
        if (!level.canSeeSky(abovePos)) {
            return 0;
        }

        int effectiveSky = Math.min(MAX_LIGHT, level.getBrightness(LightLayer.SKY, abovePos) - level.getSkyDarken());
        if (effectiveSky <= NIGHT_LIGHT) {
            return 0;
        }
        return Math.max(1, Math.round(generationRate * (float) (effectiveSky - NIGHT_LIGHT) / (MAX_LIGHT - NIGHT_LIGHT)));
    }

    @SuppressWarnings("unchecked")
    private void pushEnergy(ServerLevel level, BlockPos pos) {
        int available = energyStorage.getEnergyStored();
        if (available <= 0) {
            return;
        }

        if (outputCaches == null) {
            outputCaches = new BlockCapabilityCache[DIRECTIONS.length];
            for (Direction direction : DIRECTIONS) {
                outputCaches[direction.ordinal()] = BlockCapabilityCache.create(
                        Capabilities.EnergyStorage.BLOCK, level, pos.relative(direction), direction.getOpposite(),
                        () -> !this.isRemoved(), () -> {});
            }
        }

        for (Direction direction : DIRECTIONS) {
            IEnergyStorage target = outputCaches[direction.ordinal()].getCapability();
            if (target == null || !target.canReceive()) {
                continue;
            }

            int accepted = target.receiveEnergy(available, false);
            if (accepted > 0) {
                energyStorage.extractEnergy(accepted, false);
                available -= accepted;
                setChanged();
                if (available <= 0) {
                    return;
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        outputCaches = null;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("CurrentGeneration", currentGeneration);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Energy")) {
            energyStorage.setEnergy(tag.getInt("Energy"));
        }
        if (tag.contains("CurrentGeneration")) {
            currentGeneration = tag.getInt("CurrentGeneration");
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("CurrentGeneration", currentGeneration);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static class CustomEnergyStorage extends net.neoforged.neoforge.energy.EnergyStorage {
        public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        public void setEnergy(int energy) {
            this.energy = Math.max(0, Math.min(capacity, energy));
        }

        public int getMaxReceive() {
            return maxReceive;
        }

        public int getMaxExtract() {
            return maxExtract;
        }
    }

    private static final class ExtractOnlyStorage implements IEnergyStorage {
        private final CustomEnergyStorage storage;

        private ExtractOnlyStorage(CustomEnergyStorage storage) {
            this.storage = storage;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return storage.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return storage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return storage.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }

    public CustomEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public IEnergyStorage getExposedStorage() {
        return exposedStorage;
    }

    public int getGenerationRate() {
        return generationRate;
    }

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    public ContainerData getContainerData() {
        return data;
    }

    /** 0 = basic, 1 = advanced, 2 = elite; used by the screen for the title and accent. */
    public int getTierIndex() {
        BlockState state = getBlockState();
        if (state.is(ModBlocks.ELITE_SOLAR_PANEL.get())) {
            return 2;
        }
        return state.is(ModBlocks.ADVANCED_SOLAR_PANEL.get()) ? 1 : 0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new SolarPanelMenu(containerId, inventory, this, data);
    }

    public MekanismEnergyAdapter getMekanismEnergyAdapter() {
        if (!ModCompatManager.isModLoaded(ModCompatManager.MEKANISM_ID)) {
            return null;
        }
        return new MekanismEnergyAdapter(this.energyStorage);
    }
}
