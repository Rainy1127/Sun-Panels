package net.rainy.sun_panels.menu;


import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.rainy.sun_panels.block.entity.BasicSolarPanelBlockEntity;
import net.rainy.sun_panels.registry.ModBlocks;
import net.rainy.sun_panels.registry.ModMenuTypes;

import javax.annotation.Nullable;

public class SolarPanelMenu extends AbstractContainerMenu {
    public final BasicSolarPanelBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;

    // Client constructor
    public SolarPanelMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()),
                ContainerLevelAccess.create(inventory.player.level(), extraData.readBlockPos()));
    }

    // Server constructor
    public SolarPanelMenu(int containerId, Inventory inventory, BlockEntity entity, ContainerLevelAccess levelAccess) {
        super(ModMenuTypes.SOLAR_PANEL_MENU.get(), containerId);
        this.blockEntity = (BasicSolarPanelBlockEntity) entity;
        this.levelAccess = levelAccess;

        // Add player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, ModBlocks.BASIC_SOLAR_PANEL.get());
    }

    // Handle shift-clicking items
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 36) {
                if (!this.moveItemStackTo(itemstack1, 36, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    // Getter methods for the screen to access
    public int getEnergyStored() {
        return this.blockEntity.getEnergyStorage().getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return this.blockEntity.getEnergyStorage().getMaxEnergyStored();
    }

    public int getCurrentGeneration() {
        return this.blockEntity.getCurrentGeneration();
    }

    public int getMaxGeneration() {
        return this.blockEntity.getMaxGeneration();
    }
}