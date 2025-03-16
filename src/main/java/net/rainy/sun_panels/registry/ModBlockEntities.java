package net.rainy.sun_panels.registry;


import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rainy.sun_panels.Sun_Panels;
import net.rainy.sun_panels.block.entity.AdvancedSolarPanelBlockEntity;
import net.rainy.sun_panels.block.entity.BasicSolarPanelBlockEntity;
import net.rainy.sun_panels.block.entity.EliteSolarPanelBlockEntity;
import net.rainy.sun_panels.block.entity.pipe.AdvancedEnergyPipeBlockEntity;
import net.rainy.sun_panels.block.entity.pipe.BasicEnergyPipeBlockEntity;
import net.rainy.sun_panels.block.entity.pipe.EliteEnergyPipeBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Sun_Panels.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicSolarPanelBlockEntity>> BASIC_SOLAR_PANEL =
            BLOCK_ENTITIES.register("basic_solar_panel",
                    () -> BlockEntityType.Builder.of(
                            BasicSolarPanelBlockEntity::new,
                            ModBlocks.BASIC_SOLAR_PANEL.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedSolarPanelBlockEntity>> ADVANCED_SOLAR_PANEL =
            BLOCK_ENTITIES.register("advanced_solar_panel",
                    () -> BlockEntityType.Builder.of(
                            AdvancedSolarPanelBlockEntity::new,
                            ModBlocks.ADVANCED_SOLAR_PANEL.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EliteSolarPanelBlockEntity>> ELITE_SOLAR_PANEL =
            BLOCK_ENTITIES.register("elite_solar_panel",
                    () -> BlockEntityType.Builder.of(
                            EliteSolarPanelBlockEntity::new,
                            ModBlocks.ELITE_SOLAR_PANEL.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicEnergyPipeBlockEntity>> BASIC_ENERGY_PIPE =
            BLOCK_ENTITIES.register("basic_energy_pipe",
                    () -> BlockEntityType.Builder.of(BasicEnergyPipeBlockEntity::new,
                            ModBlocks.BASIC_ENERGY_PIPE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedEnergyPipeBlockEntity>> ADVANCED_ENERGY_PIPE =
            BLOCK_ENTITIES.register("advanced_energy_pipe",
                    () -> BlockEntityType.Builder.of(AdvancedEnergyPipeBlockEntity::new,
                            ModBlocks.ADVANCED_ENERGY_PIPE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EliteEnergyPipeBlockEntity>> ELITE_ENERGY_PIPE =
            BLOCK_ENTITIES.register("elite_energy_pipe",
                    () -> BlockEntityType.Builder.of(EliteEnergyPipeBlockEntity::new,
                            ModBlocks.ELITE_ENERGY_PIPE.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}