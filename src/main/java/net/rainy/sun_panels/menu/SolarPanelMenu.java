package net.rainy.sun_panels.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.rainy.sun_panels.block.entity.BaseSolarPanelBlockEntity;
import net.rainy.sun_panels.registry.ModMenuTypes;

/**
 * Read-only status menu shared by all three panel tiers. Every value travels through vanilla data slots,
 * so the client is only updated when a number actually changes and no custom packets are needed.
 */
public class SolarPanelMenu extends AbstractContainerMenu {
    public static final int DATA_ENERGY = 0;
    public static final int DATA_CAPACITY = 1;
    public static final int DATA_OUTPUT = 2;
    public static final int DATA_MAX_OUTPUT = 3;
    public static final int DATA_SKY = 4;
    public static final int DATA_TIER = 5;
    public static final int DATA_COUNT = 6;

    public static final int INVENTORY_Y = 84;
    private static final int MAIN_INVENTORY_SLOTS = 27;
    private static final int PLAYER_SLOTS = 36;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final Block block;

    /** Client constructor, used by the menu type. The extra data buffer is not needed. */
    public SolarPanelMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL, Blocks.AIR);
    }

    /** Server constructor. */
    public SolarPanelMenu(int containerId, Inventory inventory, BaseSolarPanelBlockEntity panel, ContainerData data) {
        this(containerId, inventory, data, ContainerLevelAccess.create(panel.getLevel(), panel.getBlockPos()),
                panel.getBlockState().getBlock());
    }

    private SolarPanelMenu(int containerId, Inventory inventory, ContainerData data, ContainerLevelAccess access, Block block) {
        super(ModMenuTypes.SOLAR_PANEL_MENU.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.data = data;
        this.access = access;
        this.block = block;
        addDataSlots(data);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, INVENTORY_Y + 58));
        }
    }

    public int energy() {
        return data.get(DATA_ENERGY);
    }

    public int capacity() {
        return data.get(DATA_CAPACITY);
    }

    public int output() {
        return data.get(DATA_OUTPUT);
    }

    public int maxOutput() {
        return data.get(DATA_MAX_OUTPUT);
    }

    public int skyState() {
        return data.get(DATA_SKY);
    }

    public int tierIndex() {
        return data.get(DATA_TIER);
    }

    @Override
    public boolean stillValid(Player player) {
        return block == Blocks.AIR || stillValid(access, player, block);
    }

    /** Shift-click moves items between the main inventory and the hotbar; the panel has no slots of its own. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < MAIN_INVENTORY_SLOTS
                ? moveItemStackTo(stack, MAIN_INVENTORY_SLOTS, PLAYER_SLOTS, false)
                : moveItemStackTo(stack, 0, MAIN_INVENTORY_SLOTS, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
