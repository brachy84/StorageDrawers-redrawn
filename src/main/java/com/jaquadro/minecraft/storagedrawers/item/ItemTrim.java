package com.jaquadro.minecraft.storagedrawers.item;

import com.jaquadro.minecraft.chameleon.resources.IItemMeshMapper;
import com.jaquadro.minecraft.chameleon.resources.IItemVariantProvider;
import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockTrim;
import com.jaquadro.minecraft.storagedrawers.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ItemTrim extends ItemMultiTexture implements IItemMeshMapper, IItemVariantProvider {

    public ItemTrim(Block block) {
        super(block, block, new Mapper() {
            @Override
            @Nonnull
            public String apply(@Nonnull ItemStack input) {
                return BlockPlanks.EnumType.byMetadata(input.getMetadata()).getTranslationKey();
            }
        });
    }

    protected ItemTrim(Block block, Mapper mapper) {
        super(block, block, mapper);
    }

    @Override
    public boolean doesSneakBypassUse(@Nonnull ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        return block instanceof BlockDrawers && ((BlockDrawers) block).retrimType() != null;
    }

    @Override
    public List<ResourceLocation> getItemVariants() {
        ResourceLocation location = ForgeRegistries.ITEMS.getKey(this);
        List<ResourceLocation> variants = new ArrayList<>();

        for (BlockPlanks.EnumType woodType : BlockPlanks.EnumType.values())
            variants.add(new ResourceLocation(location.getNamespace(), location.getPath() + '_' + woodType.getName()));

        return variants;
    }

    @Override
    public List<Pair<ItemStack, ModelResourceLocation>> getMeshMappings() {
        List<Pair<ItemStack, ModelResourceLocation>> mappings = new ArrayList<Pair<ItemStack, ModelResourceLocation>>();

        for (BlockPlanks.EnumType woodType : BlockPlanks.EnumType.values()) {
            IBlockState state = block.getDefaultState().withProperty(BlockTrim.VARIANT, woodType);
            ModelResourceLocation location = new ModelResourceLocation(ModBlocks.trim.getRegistryName().toString() + '_' + woodType.getName(), "inventory");
            mappings.add(Pair.of(new ItemStack(this, 1, block.getMetaFromState(state)), location));
        }

        return mappings;
    }
}
