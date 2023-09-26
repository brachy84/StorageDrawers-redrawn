package com.jaquadro.minecraft.storagedrawers.item;

import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributesModifiable;
import com.jaquadro.minecraft.storagedrawers.block.BlockStandardDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawersStandard;
import com.jaquadro.minecraft.storagedrawers.config.SDConfig;
import com.jaquadro.minecraft.storagedrawers.core.ModBlocks;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemDrawers extends ItemBlock {

    public ItemDrawers(Block block) {
        super(block);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState))
            return false;

        TileEntityDrawers tile = (TileEntityDrawers) world.getTileEntity(pos);
        if (tile != null) {
            if (side != EnumFacing.UP && side != EnumFacing.DOWN)
                tile.setDirection(side.ordinal());

            if (tile instanceof TileEntityDrawersStandard) {
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("tile"))
                    tile.readFromPortableNBT(stack.getTagCompound().getCompoundTag("tile"));

                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("material"))
                    tile.setMaterial(stack.getTagCompound().getString("material"));

                tile.setIsSealed(false);
            }

            if (SDConfig.general.defaultQuantify) {
                IDrawerAttributes attributes = tile.getDrawerAttributes();
                if (attributes instanceof IDrawerAttributesModifiable)
                    ((IDrawerAttributesModifiable) attributes).setIsShowingQuantity(true);
            }
        }

        return true;
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, @Nullable World world, List<String> list, ITooltipFlag advanced) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("material")) {
            String key = itemStack.getTagCompound().getString("material");
            list.add(I18n.format("storagedrawers.material", I18n.format("storagedrawers.material." + key)));
        }

        list.add(I18n.format("storagedrawers.drawers.description", getCapacityForBlock(itemStack)));

        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("tile"))
            list.add(ChatFormatting.YELLOW + I18n.format("storagedrawers.drawers.sealed"));
    }

    private int getCapacityForBlock(@Nonnull ItemStack itemStack) {
        Block block = Block.getBlockFromItem(itemStack.getItem());

        if (block instanceof BlockStandardDrawers) {
            EnumBasicDrawer info = EnumBasicDrawer.byMetadata(itemStack.getMetadata());
            return info.getBaseStorage();
        } else if (block == ModBlocks.compDrawers) {
            return SDConfig.blocks.compdrawers.baseStorage;
        }

        return 0;
    }
}
