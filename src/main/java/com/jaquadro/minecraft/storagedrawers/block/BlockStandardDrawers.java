package com.jaquadro.minecraft.storagedrawers.block;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;
import com.jaquadro.minecraft.storagedrawers.block.dynamic.StatusModelData;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawersStandard;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockStandardDrawers extends BlockDrawers {

    public static final PropertyEnum<EnumBasicDrawer> BLOCK = PropertyEnum.create("block", EnumBasicDrawer.class);


    @SideOnly(Side.CLIENT)
    private StatusModelData[] statusInfo;

    public BlockStandardDrawers(String registryName, String blockName) {
        super(Material.WOOD, registryName, blockName);
    }

    @Override
    protected void initDefaultState() {
        super.initDefaultState();
        setDefaultState(getDefaultState().withProperty(BLOCK, EnumBasicDrawer.FULL2));
    }

    @Override
    public int getDrawerCount(IBlockState state) {
        if (state != null && state.getBlock() instanceof BlockDrawers) {
            EnumBasicDrawer info = state.getValue(BLOCK);
            if (info != null)
                return info.getDrawerCount();
        }

        return 0;
    }

    @Override
    public boolean isHalfDepth(IBlockState state) {
        if (state != null && state.getBlock() instanceof BlockDrawers) {
            EnumBasicDrawer info = state.getValue(BLOCK);
            if (info != null)
                return info.isHalfDepth();
        }

        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initDynamic() {
        statusInfo = new StatusModelData[EnumBasicDrawer.values().length];
        for (EnumBasicDrawer type : EnumBasicDrawer.values()) {
            ResourceLocation location = new ResourceLocation(StorageDrawers.MOD_ID + ":models/dynamic/basicDrawers_" + type.getName() + ".json");
            statusInfo[type.getMetadata()] = new StatusModelData(type.getDrawerCount(), location);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StatusModelData getStatusInfo(IBlockState state) {
        if (state != null) {
            EnumBasicDrawer info = state.getValue(BLOCK);
            if (info != null)
                return statusInfo[info.getMetadata()];
        }

        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return isOpaqueCube(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        try {
            return switch (state.getValue(BLOCK)) {
                case FULL1, FULL2, FULL4 -> true;
                default -> false;
            };
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        switch (state.getValue(BLOCK)) {
            case FULL1, FULL2, FULL4 -> {
                return true;
            }
            default -> {
                TileEntityDrawers tile = getTileEntity(world, pos);
                return (tile != null && tile.getDirection() == face.getOpposite().getIndex());
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        switch (blockState.getValue(BLOCK)) {
            case FULL1, FULL2, FULL4 -> {
                return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
            }
            default -> {
                TileEntityDrawers tile = getTileEntity(blockAccess, pos);
                if (tile != null && tile.getDirection() == side.getIndex())
                    return true;
                return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(BLOCK, EnumBasicDrawer.byMetadata(meta));
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BLOCK, EnumBasicDrawer.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BLOCK).getMetadata();
    }

    @Override
    protected int getDrawerSlot(int drawerCount, int side, float hitX, float hitY, float hitZ) {
        if (drawerCount == 1)
            return 0;
        if (drawerCount == 2)
            return hitTop(hitY) ? 0 : 1;

        if (hitLeft(side, hitX, hitZ))
            return hitTop(hitY) ? 0 : 1;
        else
            return hitTop(hitY) ? 2 : 3;
    }

    @Override
    public TileEntityDrawers createNewTileEntity(World world, int meta) {
        IBlockState state = getStateFromMeta(meta);
        EnumBasicDrawer type = state.getValue(BLOCK);

        return TileEntityDrawersStandard.createEntity(type.getDrawerCount());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{BLOCK, FACING}, new IUnlistedProperty[]{STATE_MODEL});
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return super.getActualState(state, worldIn, pos).withProperty(BLOCK, state.getValue(BLOCK));
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityDrawersStandard.Legacy)
            ((TileEntityDrawersStandard.Legacy) tile).replaceWithCurrent();
    }
}
