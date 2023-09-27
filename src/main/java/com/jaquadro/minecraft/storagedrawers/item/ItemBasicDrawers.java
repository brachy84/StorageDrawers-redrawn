package com.jaquadro.minecraft.storagedrawers.item;

import com.jaquadro.minecraft.chameleon.resources.IItemMeshResolver;
import com.jaquadro.minecraft.chameleon.resources.IItemVariantProvider;
import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemBasicDrawers extends ItemDrawers implements IItemMeshResolver, IItemVariantProvider {

    @SideOnly(Side.CLIENT)
    private MeshDefinition meshResolver;

    private final Function<ItemStack, String> nameFunction;

    public ItemBasicDrawers(Block block) {
        this(block, stack -> EnumBasicDrawer.byMetadata(stack.getMetadata()).getUnlocalizedName());
    }

    protected ItemBasicDrawers(Block block, Function<ItemStack, String> function) {
        super(block);
        setHasSubtypes(true);
        nameFunction = function;
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull ItemStack stack) {
        return super.getTranslationKey() + "." + nameFunction.apply(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemMeshDefinition getMeshResolver() {
        if (meshResolver == null)
            meshResolver = new MeshDefinition();
        return meshResolver;
    }

    @Override
    public List<ResourceLocation> getItemVariants() {
        ResourceLocation location = ForgeRegistries.ITEMS.getKey(this);
        List<ResourceLocation> variants = new ArrayList<>();

        for (EnumBasicDrawer type : EnumBasicDrawer.values())
            for (BlockPlanks.EnumType material : BlockPlanks.EnumType.values())
                variants.add(new ResourceLocation(location.getNamespace(), location.getPath() + '_' + type.getName() + '_' + material.getName()));

        return variants;
    }

    @SideOnly(Side.CLIENT)
    private class MeshDefinition implements ItemMeshDefinition {

        @Override
        public ModelResourceLocation getModelLocation(@NotNull ItemStack stack) {
            if (stack.isEmpty())
                return null;

            EnumBasicDrawer drawer = EnumBasicDrawer.byMetadata(stack.getMetadata());

            String material = "oak";
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("material"))
                material = stack.getTagCompound().getString("material");

            String key = StorageDrawers.MOD_ID + ":basicDrawers_" + drawer + "_" + material;
            return new ModelResourceLocation(key, "inventory");
        }
    }
}
