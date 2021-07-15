package com.lothrazar.storagenetwork.block.cable;

import com.google.common.collect.Maps;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.block.BaseBlock;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockCable extends BaseBlock {

  public BlockCable(String registryName) {
    super(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.2F), registryName);
    setDefaultState(stateContainer.getBaseState()
        .with(NORTH, EnumConnectType.NONE).with(EAST, EnumConnectType.NONE)
        .with(SOUTH, EnumConnectType.NONE).with(WEST, EnumConnectType.NONE)
        .with(UP, EnumConnectType.NONE).with(DOWN, EnumConnectType.NONE));
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity != null) {
        IItemHandler items = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        if (items != null) {
          for (int i = 0; i < items.getSlots(); ++i) {
            InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), items.getStackInSlot(i));
          }
          worldIn.updateComparatorOutputLevel(pos, this);
        }
      }
      super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
  }

  public static BlockState cleanBlockState(BlockState state) {
    for (Direction d : Direction.values()) {
      EnumProperty<EnumConnectType> prop = FACING_TO_PROPERTY_MAP.get(d);
      if (state.get(prop) == EnumConnectType.INVENTORY) {
        //dont replace cable types only inv types
        state = state.with(prop, EnumConnectType.NONE);
      }
    }
    return state;
  }

  private static final EnumProperty<EnumConnectType> DOWN = EnumProperty.create("down", EnumConnectType.class);
  private static final EnumProperty<EnumConnectType> UP = EnumProperty.create("up", EnumConnectType.class);
  private static final EnumProperty<EnumConnectType> NORTH = EnumProperty.create("north", EnumConnectType.class);
  private static final EnumProperty<EnumConnectType> SOUTH = EnumProperty.create("south", EnumConnectType.class);
  private static final EnumProperty<EnumConnectType> WEST = EnumProperty.create("west", EnumConnectType.class);
  private static final EnumProperty<EnumConnectType> EAST = EnumProperty.create("east", EnumConnectType.class);
  public static final Map<Direction, EnumProperty<EnumConnectType>> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (p) -> {
    p.put(Direction.NORTH, NORTH);
    p.put(Direction.EAST, EAST);
    p.put(Direction.SOUTH, SOUTH);
    p.put(Direction.WEST, WEST);
    p.put(Direction.UP, UP);
    p.put(Direction.DOWN, DOWN);
  });
  private static final double top = 16;
  private static final double bot = 0;
  private static final double C = 8;
  private static final double w = 2;
  private static final double sm = C - w;
  private static final double lg = C + w;
  //(double x1, double y1, double z1, double x2, double y2, double z2)
  private static final VoxelShape AABB = Block.makeCuboidShape(sm, sm, sm, lg, lg, lg);
  //Y for updown
  private static final VoxelShape AABB_UP = Block.makeCuboidShape(sm, sm, sm, lg, top, lg);
  private static final VoxelShape AABB_DOWN = Block.makeCuboidShape(sm, bot, sm, lg, lg, lg);
  //Z for n-s
  private static final VoxelShape AABB_NORTH = Block.makeCuboidShape(sm, sm, bot, lg, lg, lg);
  private static final VoxelShape AABB_SOUTH = Block.makeCuboidShape(sm, sm, sm, lg, lg, top);
  //X for e-w
  private static final VoxelShape AABB_WEST = Block.makeCuboidShape(bot, sm, sm, lg, lg, lg);
  private static final VoxelShape AABB_EAST = Block.makeCuboidShape(sm, sm, sm, top, lg, lg);

  private boolean shapeConnects(BlockState state, EnumProperty<EnumConnectType> dirctionProperty) {
    return state.get(dirctionProperty).equals(EnumConnectType.CABLE)
        || state.get(dirctionProperty).equals(EnumConnectType.INVENTORY);
  }

  @Override
  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
    return ShapeCache.getOrCreate(state, this::createShape);
  }

  private VoxelShape createShape(BlockState state) {
    VoxelShape shape = AABB;
    if (shapeConnects(state, UP)) {
      shape = VoxelShapes.combine(shape, AABB_UP, IBooleanFunction.OR);
    }
    if (shapeConnects(state, DOWN)) {
      shape = VoxelShapes.combine(shape, AABB_DOWN, IBooleanFunction.OR);
    }
    if (state.get(WEST).equals(EnumConnectType.CABLE)) {
      shape = VoxelShapes.combine(shape, AABB_WEST, IBooleanFunction.OR);
    }
    if (state.get(EAST).equals(EnumConnectType.CABLE)) {
      shape = VoxelShapes.combine(shape, AABB_EAST, IBooleanFunction.OR);
    }
    if (state.get(NORTH).equals(EnumConnectType.CABLE)) {
      shape = VoxelShapes.combine(shape, AABB_NORTH, IBooleanFunction.OR);
    }
    if (state.get(SOUTH).equals(EnumConnectType.CABLE)) {
      shape = VoxelShapes.combine(shape, AABB_SOUTH, IBooleanFunction.OR);
    }
    return shape;
  }

  @Override
  public BlockRenderType getRenderType(BlockState bs) {
    return BlockRenderType.MODEL;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean allowsMovement(BlockState state, IBlockReader world, BlockPos pos, PathType type) {
    return false;
  }

  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new TileCable();
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState stateIn, LivingEntity placer, ItemStack stack) {
    BlockState facingState;
    for (Direction d : Direction.values()) {
      BlockPos posoff = pos.offset(d);
      facingState = worldIn.getBlockState(posoff);
      TileEntity tileOffset = worldIn.getTileEntity(posoff);
      IConnectable cap = null;
      if (tileOffset != null) {
        cap = tileOffset.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY).orElse(null);
      }
      if (cap != null
          || facingState.getBlock() == SsnRegistry.MAIN) {
        stateIn = stateIn.with(FACING_TO_PROPERTY_MAP.get(d), EnumConnectType.CABLE);
        worldIn.setBlockState(pos, stateIn);
      }
    }
  }

  @Override
  protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
    super.fillStateContainer(builder);
    builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
  }

  @Override
  public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
    EnumProperty<EnumConnectType> property = FACING_TO_PROPERTY_MAP.get(facing);
    if (facingState.getBlock() == SsnRegistry.MAIN
        || facingState.getBlock() instanceof BlockCable) {
      //      StorageNetwork.log("plain cable" + facingState.getBlock());
      return stateIn.with(property, EnumConnectType.CABLE);
    } //
      //based on capability you have, edit connection type
    TileEntity tileOffset = world.getTileEntity(facingPos);
    IConnectable cap = null;
    if (tileOffset != null) {
      cap = tileOffset.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY).orElse(null);
    }
    if (cap != null) {
      //      StorageNetwork.log("EARLY EXIT block" + facingState.getBlock() + " has cap " + cap);
      if (cap.getMainPos() != null) {
        //its a network bock of some type, knows where network is but not exactly inventory
        return stateIn.with(property, EnumConnectType.INVENTORY);
      }
      return stateIn.with(property, EnumConnectType.CABLE);
    }
    //if i have zero other inventories, and this is one now, ok go invo
    if (!this.hasInventoryAlready(stateIn)
        && isInventory(stateIn, facing, facingState, world, currentPos, facingPos)) {
      return stateIn.with(property, EnumConnectType.INVENTORY);
    }
    return stateIn.with(property, EnumConnectType.NONE);
  }

  //only one inventory allowed per link cable eh
  private boolean hasInventoryAlready(BlockState stateIn) {
    for (Direction d : Direction.values()) {
      if (stateIn.get(FACING_TO_PROPERTY_MAP.get(d)).isInventory()) {
        return true;
      }
    }
    return false;
  }

  private static boolean isInventory(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
    if (facing == null) {
      return false;
    }
    if (!TileMain.isTargetAllowed(facingState)) {
      return false;
    }
    TileEntity neighbor = world.getTileEntity(facingPos);
    if (neighbor != null
        && neighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite()).orElse(null) != null) {
      return true;
    }
    return false;
  }
}
