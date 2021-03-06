package com.lothrazar.storagenetwork.item.remote;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;

public class ContainerNetworkRemote extends ContainerNetwork {

  private TileMain root;
  private ItemStack remote;

  public ContainerNetworkRemote(int id, PlayerInventory pInv) {
    super(SsnRegistry.REMOTE, id);
    this.player = pInv.player;
    this.world = player.world;
    //    if (world.isRemote) {
    //      Triple<String, Integer, ItemStack> result = UtilInventory.getCurioRemote(Minecraft.getInstance().player, SsnRegistry.INVENTORY_REMOTE);
    //      this.remote = result.getRight();
    //    }
    //    else {
    Triple<String, Integer, ItemStack> result = UtilInventory.getCurioRemote(pInv.player, SsnRegistry.INVENTORY_REMOTE);
    this.remote = result.getRight();
    //    } 
    DimPos dp = DimPos.getPosStored(remote);
    if (dp == null) {
      StorageNetwork.LOGGER.error(world.isRemote + "=client||Remote opening with null pos Stored {} ", result);
    }
    else {
      StorageNetwork.log("CONTAINER FFFFFF" + dp.getBlockPos());
      this.root = dp.getTileEntity(TileMain.class, world);
    }
    if (root == null) {
      //maybe the table broke after doing this, rare case
      StorageNetwork.log("CONTAINER NETWORK REMOTE null tile");
    }
    this.playerInv = pInv;
    bindPlayerInvo(this.playerInv);
    bindHotbar();
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    //does not store itemstack inventory, and opens from curios so no security here. unless it dissapears
    return !remote.isEmpty();
  }

  @Override
  public TileMain getTileMain() {
    return root;
  }

  @Override
  public void slotChanged() {}

  @Override
  public boolean isCrafting() {
    return false;
  }
}
