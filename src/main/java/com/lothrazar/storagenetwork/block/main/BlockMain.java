package com.lothrazar.storagenetwork.block.main;

import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.BaseBlock;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockMain extends BaseBlock {

  public BlockMain() {
    super(Material.IRON, "master");
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    this.updateConnection(worldIn, pos, state);
    if (worldIn.isRemote) {
      return;
    }
    TileEntity tileAtPos = worldIn.getTileEntity(pos);
    if (tileAtPos != null) {
      ((TileMain) tileAtPos).refreshNetwork();
    }
  }

  @Override
  public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos,
      PlayerEntity playerIn, Hand hand, BlockRayTraceResult result) {
    if (worldIn.isRemote) {
      return ActionResultType.SUCCESS;
    }
    TileEntity tileHere = worldIn.getTileEntity(pos);
    if (!(tileHere instanceof TileMain)) {
      return ActionResultType.PASS;
    }
    //    float hitX, float hitY, float hitZ;
    if (hand == Hand.MAIN_HAND && playerIn.getHeldItem(hand).isEmpty()) {
      displayConnections(playerIn, tileHere);
      return ActionResultType.SUCCESS;
    }
    return ActionResultType.PASS;
  }

  private void displayConnections(PlayerEntity playerIn, TileEntity tileHere) {
    TileMain tileMain = (TileMain) tileHere;
    int total = tileMain.getConnectablePositions().size();
    if (total == 0) {
      return;
    }
    playerIn.sendMessage(
        new TranslationTextComponent(TextFormatting.LIGHT_PURPLE +
            UtilTileEntity.lang("chat.main.emptyslots") + tileMain.emptySlots()),
        playerIn.getUniqueID());
    playerIn.sendMessage(new TranslationTextComponent(TextFormatting.DARK_AQUA +
        UtilTileEntity.lang("chat.main.connectables") + total), playerIn.getUniqueID());
    Map<String, Integer> mapNamesToCount = new HashMap<>();
    Iterator<DimPos> iter = tileMain.getConnectablePositions().iterator();
    Block bl;
    DimPos p;
    String blockName;
    while (iter.hasNext()) {
      p = iter.next();
      bl = p.getBlockState().getBlock();
      //getTranslatedName client only thanks mojang lol
      blockName = (new TranslationTextComponent(bl.getTranslationKey())).getString();
      int count = mapNamesToCount.get(blockName) != null ? (mapNamesToCount.get(blockName) + 1) : 1;
      mapNamesToCount.put(blockName, count);
    }
    List<Entry<String, Integer>> listDisplayStrings = Lists.newArrayList();
    for (Entry<String, Integer> e : mapNamesToCount.entrySet()) {
      listDisplayStrings.add(e);
    }
    Collections.sort(listDisplayStrings, new Comparator<Entry<String, Integer>>() {

      @Override
      public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
        return Integer.compare(o2.getValue(), o1.getValue());
      }
    });
    for (Entry<String, Integer> e : listDisplayStrings) {
      playerIn.sendMessage(new TranslationTextComponent(TextFormatting.AQUA + "    " + e.getValue() + ": " + e.getKey()), playerIn.getUniqueID());
    }
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new TileMain();
  }
}
