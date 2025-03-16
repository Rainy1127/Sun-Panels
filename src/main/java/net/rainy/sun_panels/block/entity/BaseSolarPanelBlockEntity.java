package net.rainy.sun_panels.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.rainy.sun_panels.block.entity.AdvancedSolarPanelBlockEntity;
import net.rainy.sun_panels.block.entity.BasicSolarPanelBlockEntity;
import net.rainy.sun_panels.block.entity.EliteSolarPanelBlockEntity;
import net.rainy.sun_panels.registry.ModBlockEntities;
import net.rainy.sun_panels.compat.mekanism.MekanismEnergyAdapter;
import net.rainy.sun_panels.Sun_Panels;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.rainy.sun_panels.compat.mekanism.MekanismCompat;
import net.rainy.sun_panels.compat.ModCompatManager;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSolarPanelBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final CustomEnergyStorage energyStorage;
    protected final int generationRate;
    protected int currentGeneration;

    // Container data for the GUI
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> currentGeneration;
                case 3 -> generationRate;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Only allow setting the current generation
            if (index == 2) {
                currentGeneration = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public BaseSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity, int generationRate) {
        super(type, pos, state);
        this.energyStorage = new CustomEnergyStorage(capacity, generationRate, capacity);
        this.generationRate = generationRate;
        this.currentGeneration = 0;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register capabilities for BasicSolarPanelBlockEntity
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.BASIC_SOLAR_PANEL.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof BasicSolarPanelBlockEntity basicSolarPanel) {
                        return basicSolarPanel.getEnergyStorage();
                    }
                    return null;
                }
        );

        // Register capabilities for AdvancedSolarPanelBlockEntity
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ADVANCED_SOLAR_PANEL.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof AdvancedSolarPanelBlockEntity advancedSolarPanel) {
                        return advancedSolarPanel.getEnergyStorage();
                    }
                    return null;
                }
        );

        // Register capabilities for EliteSolarPanelBlockEntity
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ELITE_SOLAR_PANEL.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof EliteSolarPanelBlockEntity eliteSolarPanel) {
                        return eliteSolarPanel.getEnergyStorage();
                    }
                    return null;
                }
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BaseSolarPanelBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        // Reset current generation
        blockEntity.currentGeneration = 0;

        // Check if it's daytime and has direct line of sight to the sky
        // Adding proper daylight check to prevent generation at night
        boolean isDaytime = level.isDay();
        
        // Check if there's no block above the solar panel
        BlockPos abovePos = pos.above();
        boolean hasNoBlockAbove = level.isEmptyBlock(abovePos);
        
        // If there's no block above, check if it has sky access
        boolean canSeeSky = hasNoBlockAbove && level.canSeeSky(abovePos);
        int skyLight = level.getBrightness(LightLayer.SKY, abovePos) - level.getSkyDarken();

        if (isDaytime && canSeeSky && skyLight > 0) {
            // Calculate generation based on light level (max at noon)
            float timeOfDay = level.getTimeOfDay(1.0F);
            float multiplier = 1.0F;

            // Reduce generation at dawn/dusk
            if (timeOfDay < 0.25F || timeOfDay > 0.75F) {
                multiplier = 0.5F;
            }

            // Generate energy - use skyLight without sky darken value to get actual light level
            int energyToGenerate = Math.round(blockEntity.generationRate * multiplier * skyLight / 15.0F);
            blockEntity.energyStorage.receiveEnergy(energyToGenerate, false);

            // Update current generation
            blockEntity.currentGeneration = energyToGenerate;

            // Add debug message to verify energy generation and Mekanism integration
            if (level.getGameTime() % 200 == 0) { // Log every 10 seconds (200 ticks)
                StringBuilder logMessage = new StringBuilder();
                logMessage.append(String.format(
                        "Solar Panel at %s generating %d RF/t (Stored: %d/%d RF",
                        pos,
                        blockEntity.currentGeneration,
                        blockEntity.energyStorage.getEnergyStored(),
                        blockEntity.energyStorage.getMaxEnergyStored()
                ));

                // Add Mekanism energy info if available
                if (ModCompatManager.isModLoaded(ModCompatManager.MEKANISM_ID)) {
                    MekanismEnergyAdapter adapter = blockEntity.getMekanismEnergyAdapter();
                    if (adapter != null) {
                        logMessage.append(String.format(
                                ", Mekanism: %.1f/%.1f J",
                                adapter.getEnergy(),
                                adapter.getMaxEnergy()
                        ));
                    }
                }

                // Add Tech Reborn energy info if available
                if (ModCompatManager.isModLoaded(ModCompatManager.TECH_REBORN_ID)) {
                    double euStored = blockEntity.energyStorage.getEnergyStored() * 0.25; // Convert to EU
                    double euMax = blockEntity.energyStorage.getMaxEnergyStored() * 0.25; // Convert to EU
                    logMessage.append(String.format(
                            ", Tech Reborn: %.1f/%.1f EU",
                            euStored,
                            euMax
                    ));
                }

                logMessage.append(")");
                LOGGER.info(logMessage.toString());
            }

            // Mark block entity as changed
            blockEntity.setChanged();
        }

        // Transfer energy to adjacent blocks
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);

            if (adjacentEntity != null) {
                IEnergyStorage adjacentStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, adjacentPos, direction.getOpposite());

                if (adjacentStorage != null && adjacentStorage.canReceive()) {
                    int energyToTransfer = blockEntity.energyStorage.extractEnergy(blockEntity.generationRate, true);
                    int energyAccepted = adjacentStorage.receiveEnergy(energyToTransfer, false);
                    blockEntity.energyStorage.extractEnergy(energyAccepted, false);

                    if (energyAccepted > 0) {
                        blockEntity.setChanged();
                    }
                }
            }
        }
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

    // Custom energy storage class that allows setting energy directly
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

    // Getter for energy storage
    public CustomEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    // Getter for generation rate
    public int getGenerationRate() {
        return generationRate;
    }

    // Getter for current generation
    public int getCurrentGeneration() {
        return currentGeneration;
    }

    // Getter for container data
    public ContainerData getContainerData() {
        return data;
    }

    /**
     * Creates a Mekanism energy adapter for this solar panel's energy storage.
     * This allows the solar panel to work with Mekanism's energy system.
     *
     * @return A Mekanism energy adapter for this solar panel, or null if Mekanism is not loaded
     */
    public MekanismEnergyAdapter getMekanismEnergyAdapter() {
        if (!ModCompatManager.isModLoaded(ModCompatManager.MEKANISM_ID)) {
            LOGGER.debug("Attempted to get Mekanism adapter but Mekanism is not loaded");
            return null;
        }
        return new MekanismEnergyAdapter(this.energyStorage);
    }
}