package net.rainy.sun_panels.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.rainy.sun_panels.energy.ExtractOnlyEnergyHandler;
import net.rainy.sun_panels.energy.PanelEnergyHandler;
import net.rainy.sun_panels.menu.SolarPanelMenu;
import net.rainy.sun_panels.registry.SPRegistries;

public class SolarPanelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SKY_BLOCKED = 0;
    public static final int SKY_DARK = 1;
    public static final int SKY_SUN = 2;

    private static final Direction[] DIRECTIONS = Direction.values();
    // Sky visibility and daylight change slowly, so they are re-checked once per second (staggered per panel).
    private static final int SKY_CHECK_INTERVAL = 20;

    private final SolarTier tier;
    private final PanelEnergyHandler energy;
    private final EnergyHandler exposed;
    private final ContainerData menuData;

    private int skyCheckCountdown;
    private int currentOutput;
    private int skyState = SKY_BLOCKED;
    private BlockCapabilityCache<EnergyHandler, Direction>[] outputs;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(SPRegistries.SOLAR_PANEL_BE.get(), pos, state);
        this.tier = state.getBlock() instanceof SolarPanelBlock block ? block.tier() : SolarTier.BASIC;
        this.energy = new PanelEnergyHandler(tier.capacity(), this::setChanged);
        this.exposed = new ExtractOnlyEnergyHandler(energy);
        this.skyCheckCountdown = Math.floorMod(pos.hashCode(), SKY_CHECK_INTERVAL);
        this.menuData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case SolarPanelMenu.DATA_ENERGY -> energy.getEnergy();
                    case SolarPanelMenu.DATA_CAPACITY -> energy.getCapacity();
                    case SolarPanelMenu.DATA_OUTPUT -> currentOutput;
                    case SolarPanelMenu.DATA_MAX_OUTPUT -> tier.generation();
                    case SolarPanelMenu.DATA_SKY -> skyState;
                    case SolarPanelMenu.DATA_TIER -> tier.ordinal();
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
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity panel) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (--panel.skyCheckCountdown <= 0) {
            panel.skyCheckCountdown = SKY_CHECK_INTERVAL;
            panel.refreshSky(level, pos);
        }
        panel.energy.generate(panel.currentOutput);
        panel.pushEnergy(serverLevel, pos);
    }

    /**
     * Sky access is judged by the sky light of the space above the panel, not by that space being air.
     * Sky light passes through glass and other transparent blocks at full strength, so covered panels work.
     */
    private void refreshSky(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        if (!level.canSeeSky(above)) {
            skyState = SKY_BLOCKED;
            currentOutput = 0;
            return;
        }
        currentOutput = SolarMath.output(tier.generation(), level.getEffectiveSkyBrightness(above));
        skyState = currentOutput > 0 ? SKY_SUN : SKY_DARK;
    }

    @SuppressWarnings("unchecked")
    private void pushEnergy(ServerLevel level, BlockPos pos) {
        int available = energy.getEnergy();
        if (available <= 0) {
            return;
        }
        if (outputs == null) {
            outputs = new BlockCapabilityCache[DIRECTIONS.length];
            for (Direction direction : DIRECTIONS) {
                outputs[direction.ordinal()] = BlockCapabilityCache.create(Capabilities.Energy.BLOCK, level,
                        pos.relative(direction), direction.getOpposite(), () -> !this.isRemoved(), () -> {});
            }
        }
        for (Direction direction : DIRECTIONS) {
            EnergyHandler target = outputs[direction.ordinal()].getCapability();
            if (target != null) {
                available -= EnergyHandlerUtil.move(energy, target, available, null);
                if (available <= 0) {
                    return;
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        outputs = null;
    }

    public SolarTier getTier() {
        return tier;
    }

    public EnergyHandler getExposedEnergy() {
        return exposed;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energy.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy.deserialize(input);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SolarPanelMenu(containerId, inventory, this, menuData);
    }
}
