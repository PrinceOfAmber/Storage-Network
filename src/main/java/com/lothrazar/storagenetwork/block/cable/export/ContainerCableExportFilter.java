package com.lothrazar.storagenetwork.block.cable.export;

import com.lothrazar.storagenetwork.block.cable.ContainerCable;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.item.ItemUpgrade;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCableExportFilter extends ContainerCable {

  public final TileCableExport tile;
  public CapabilityConnectableAutoIO cap;

  public ContainerCableExportFilter(int windowId, World world, BlockPos pos, PlayerInventory playerInv, PlayerEntity player) {
    super(SsnRegistry.FILTEREXPORTCONTAINER, windowId);
    tile = (TileCableExport) world.getTileEntity(pos);
    this.cap = tile.ioStorage;
    for (int i = 0; i < cap.upgrades.getSlots(); i++) {
      this.addSlot(new SlotItemHandler(cap.upgrades, i, 98 + i * SQ, 6) {

        @Override
        public int getSlotStackLimit() {
          return 1;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
          return stack.getItem() instanceof ItemUpgrade;
        }
      });
    }
    this.bindPlayerInvo(playerInv);
  }

  @Override
  public ItemStack transferStackInSlot(PlayerEntity player, int slotIndex) {
    Slot slot = this.inventorySlots.get(slotIndex);
    //in range [4,39] means its coming FROM inventory
    // [0,3] is the filter list
    if (slot != null && slot.getHasStack()) {
      ItemStack stackInSlot = slot.getStack();
      if (stackInSlot.getItem() instanceof ItemUpgrade) {
        if (4 <= slotIndex && slotIndex <= 39) {
          //FROM inventory to upgrade slots 
          if (!this.mergeItemStack(stackInSlot, 0, 4, true)) { // SsnRegistry.UPGRADE_COUNT
            return ItemStack.EMPTY;
          }
        }
        else if (0 <= slotIndex && slotIndex <= 3) {
          //FROM upgrade slots TO inventory 
          if (!this.mergeItemStack(stackInSlot, 0, 35, true)) {
            return ItemStack.EMPTY;
          }
        }
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    return true;
  }
}
