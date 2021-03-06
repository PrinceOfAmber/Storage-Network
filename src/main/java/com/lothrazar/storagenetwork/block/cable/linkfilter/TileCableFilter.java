package com.lothrazar.storagenetwork.block.cable.linkfilter;

import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableLink;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileCableFilter extends TileCableWithFacing implements ITickableTileEntity, INamedContainerProvider {

  protected CapabilityConnectableLink capability;

  public TileCableFilter() {
    super(SsnRegistry.FILTERKABELTILE);
    this.capability = new CapabilityConnectableLink(this);
  }

  @Override
  public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
    return new ContainerCableFilter(i, world, pos, playerInventory, playerEntity);
  }

  @Override
  public ITextComponent getDisplayName() {
    return new StringTextComponent(getType().getRegistryName().getPath());
  }

  @Override
  public void read(BlockState bs, CompoundNBT compound) {
    super.read(bs, compound);
    this.capability.deserializeNBT(compound.getCompound("capability"));
  }

  @Override
  public CompoundNBT write(CompoundNBT compound) {
    CompoundNBT result = super.write(compound);
    result.put("capability", capability.serializeNBT());
    return result;
  }

  @Override
  public void setDirection(Direction direction) {
    super.setDirection(direction);
    this.capability.setInventoryFace(direction);
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
    if (capability == StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY) {
      LazyOptional<CapabilityConnectableLink> cap = LazyOptional.of(() -> this.capability);
      return cap.cast();
    }
    return super.getCapability(capability, facing);
  }

  @Override
  public void tick() {
    super.refreshDirection();
  }
}
