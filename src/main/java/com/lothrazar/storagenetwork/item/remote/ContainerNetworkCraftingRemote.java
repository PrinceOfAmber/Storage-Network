package com.lothrazar.storagenetwork.item.remote;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.block.request.SlotCraftingNetwork;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.gui.NetworkCraftingInventory;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class ContainerNetworkCraftingRemote extends ContainerNetwork {

  Map<Integer, ItemStack> matrixStacks = new HashMap<>();
  private TileMain root;
  private ItemStack remote;

  public ContainerNetworkCraftingRemote(int id, PlayerInventory pInv) {
    super(SsnRegistry.CRAFTINGREMOTE, id);
    this.remote = pInv.player.getHeldItemMainhand();
    this.player = pInv.player;
    this.world = player.world;
    DimPos dp = DimPos.getPosStored(remote);
    if (dp == null) {
      StorageNetwork.LOGGER.error("Remote opening with null pos Stored {} ", remote);
    }
    this.root = dp.getTileEntity(TileMain.class, world);
    matrix = new NetworkCraftingInventory(this, matrixStacks);
    this.playerInv = pInv;
    SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(this, playerInv.player, matrix, resultInventory, 0, 101, 128);
    slotCraftOutput.setTileMain(getTileMain());
    addSlot(slotCraftOutput);
    bindGrid();
    bindPlayerInvo(this.playerInv);
    bindHotbar();
    for (int i = 0; i < matrix.getSizeInventory(); i++) {
      if (remote.hasTag() && remote.getTag().contains("matrix" + i)) {
        CompoundNBT tag = remote.getTag().getCompound("matrix" + i);
        ItemStack stackSaved = ItemStack.read(tag);
        if (!stackSaved.isEmpty()) {
          matrix.setInventorySlotContents(i, stackSaved);
        }
      }
    }
    onCraftMatrixChanged(matrix);
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    return remote == player.getHeldItemMainhand();
  }

  @Override
  public TileMain getTileMain() {
    if (root == null) {
      DimPos dp = DimPos.getPosStored(remote);
      if (dp != null) {
        root = dp.getTileEntity(TileMain.class, world);
      }
    }
    return root;
  }

  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    if (recipeLocked) {
      //      StorageNetwork.log("recipe locked so onCraftMatrixChanged cancelled");
      return;
    }
    //    findMatchingRecipe(matrix);
    super.onCraftMatrixChanged(inventoryIn);
  }

  @Override
  public void onContainerClosed(PlayerEntity playerIn) {
    super.onContainerClosed(playerIn);
    ItemStack me;
    for (int i = 0; i < matrix.getSizeInventory(); i++) {
      me = matrix.getStackInSlot(i);
      CompoundNBT here = me.write(new CompoundNBT());
      this.remote.getTag().put("matrix" + i, here);
    }
  }

  @Override
  public void slotChanged() {}

  @Override
  public boolean isCrafting() {
    return true;
  }
}
