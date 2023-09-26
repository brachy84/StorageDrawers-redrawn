package com.jaquadro.minecraft.storagedrawers.core;

import com.jaquadro.minecraft.chameleon.Chameleon;
import com.jaquadro.minecraft.chameleon.resources.ModelRegistry;
import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;
import com.jaquadro.minecraft.storagedrawers.block.*;
import com.jaquadro.minecraft.storagedrawers.block.tile.*;
import com.jaquadro.minecraft.storagedrawers.client.model.*;
import com.jaquadro.minecraft.storagedrawers.client.renderer.TileEntityDrawersRenderer;
import com.jaquadro.minecraft.storagedrawers.client.renderer.TileEntityFramingRenderer;
import com.jaquadro.minecraft.storagedrawers.config.SDConfig;
import com.jaquadro.minecraft.storagedrawers.item.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

public class ModBlocks {

    @ObjectHolder(StorageDrawers.MOD_ID + ":basicdrawers")
    public static BlockDrawers basicDrawers;
    @ObjectHolder(StorageDrawers.MOD_ID + ":compdrawers")
    public static BlockCompDrawers compDrawers;
    @ObjectHolder(StorageDrawers.MOD_ID + ":controller")
    public static BlockController controller;
    @ObjectHolder(StorageDrawers.MOD_ID + ":controllerslave")
    public static BlockSlave controllerSlave;
    @ObjectHolder(StorageDrawers.MOD_ID + ":trim")
    public static BlockTrim trim;
    @ObjectHolder(StorageDrawers.MOD_ID + ":framingtable")
    public static BlockFramingTable framingTable;
    @ObjectHolder(StorageDrawers.MOD_ID + ":customdrawers")
    public static BlockDrawersCustom customDrawers;
    @ObjectHolder(StorageDrawers.MOD_ID + ":customtrim")
    public static BlockTrimCustom customTrim;
    @ObjectHolder(StorageDrawers.MOD_ID + ":keybutton")
    public static BlockKeyButton keyButton;

    @Mod.EventBusSubscriber(modid = StorageDrawers.MOD_ID)
    public static class Registration {

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            IForgeRegistry<Block> registry = event.getRegistry();

            registry.registerAll(
                    new BlockVariantDrawers("basicdrawers", StorageDrawers.MOD_ID + ".basicDrawers"),
                    new BlockKeyButton("keybutton", StorageDrawers.MOD_ID + ".keyButton")
            );

            GameRegistry.registerTileEntity(TileEntityDrawersStandard.Legacy.class, StorageDrawers.MOD_ID + ":basicdrawers");
            GameRegistry.registerTileEntity(TileEntityDrawersStandard.Slot1.class, StorageDrawers.MOD_ID + ":basicdrawers.1");
            GameRegistry.registerTileEntity(TileEntityDrawersStandard.Slot2.class, StorageDrawers.MOD_ID + ":basicdrawers.2");
            GameRegistry.registerTileEntity(TileEntityDrawersStandard.Slot4.class, StorageDrawers.MOD_ID + ":basicdrawers.4");

            GameRegistry.registerTileEntity(TileEntityKeyButton.class, StorageDrawers.MOD_ID + ":keybutton");

            if (SDConfig.blocks.compdrawers.enabled) {
                registry.register(new BlockCompDrawers("compdrawers", StorageDrawers.MOD_ID + ".compDrawers"));
                GameRegistry.registerTileEntity(TileEntityDrawersComp.class, StorageDrawers.MOD_ID + ":compdrawers");
            }
            if (SDConfig.blocks.controller.enabled) {
                registry.register(new BlockController("controller", StorageDrawers.MOD_ID + ".controller"));
                GameRegistry.registerTileEntity(TileEntityController.class, StorageDrawers.MOD_ID + ":controller");
            }
            if (SDConfig.blocks.controllerslave.enabled) {
                registry.register(new BlockSlave("controllerslave", StorageDrawers.MOD_ID + ".controllerSlave"));
                GameRegistry.registerTileEntity(TileEntitySlave.class, StorageDrawers.MOD_ID + ":controllerslave");
            }
            if (SDConfig.blocks.trim.enabled) {
                registry.register(new BlockTrim("trim", StorageDrawers.MOD_ID + ".trim"));
            }

            if (SDConfig.blocks.framedblocks.enableFramedDrawers) {
                registry.register(new BlockDrawersCustom("customdrawers", StorageDrawers.MOD_ID + ".customDrawers"));
            }
            if (SDConfig.blocks.framedblocks.enableFramedTrims) {
                registry.register(new BlockTrimCustom("customtrim", StorageDrawers.MOD_ID + ".customTrim"));
                GameRegistry.registerTileEntity(TileEntityTrim.class, StorageDrawers.MOD_ID + ":trim");
            }
            if (SDConfig.blocks.framedblocks.enableFramingTable) {
                registry.register(new BlockFramingTable("framingtable", StorageDrawers.MOD_ID + ".framingTable"));
                GameRegistry.registerTileEntity(TileEntityFramingTable.class, StorageDrawers.MOD_ID + ":framingtable");
            }
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            IForgeRegistry<Item> registry = event.getRegistry();

            registry.registerAll(
                    new ItemBasicDrawers(basicDrawers).setRegistryName(basicDrawers.getRegistryName()),
                    new ItemKeyButton(keyButton).setRegistryName(keyButton.getRegistryName())
            );

            if (SDConfig.blocks.compdrawers.enabled)
                registry.register(new ItemCompDrawers(compDrawers).setRegistryName(compDrawers.getRegistryName()));
            if (SDConfig.blocks.controller.enabled)
                registry.register(new ItemController(controller).setRegistryName(controller.getRegistryName()));
            if (SDConfig.blocks.controllerslave.enabled)
                registry.register(new ItemBlock(controllerSlave).setRegistryName(controllerSlave.getRegistryName()));
            if (SDConfig.blocks.trim.enabled)
                registry.register(new ItemTrim(trim).setRegistryName(trim.getRegistryName()));

            if (SDConfig.blocks.framedblocks.enableFramedDrawers) {
                registry.register(new ItemCustomDrawers(customDrawers).setRegistryName(customDrawers.getRegistryName()));
            }
            if (SDConfig.blocks.framedblocks.enableFramedTrims) {
                registry.register(new ItemCustomTrim(customTrim).setRegistryName(customTrim.getRegistryName()));
            }
            if (SDConfig.blocks.framedblocks.enableFramingTable) {
                registry.register(new ItemFramingTable(framingTable).setRegistryName(framingTable.getRegistryName()));
            }

            for (String key : new String[]{"drawerBasic"})
                OreDictionary.registerOre(key, new ItemStack(basicDrawers, 1, OreDictionary.WILDCARD_VALUE));
            for (String key : new String[]{"drawerTrim"})
                OreDictionary.registerOre(key, new ItemStack(trim, 1, OreDictionary.WILDCARD_VALUE));
        }

        private static final ResourceLocation EMPTY_GROUP = new ResourceLocation("", "");

        @Nonnull
        public static ItemStack makeBasicDrawerItemStack(EnumBasicDrawer info, String material, int count) {
            ItemStack stack = new ItemStack(ModBlocks.basicDrawers, count, info.getMetadata());

            NBTTagCompound data = new NBTTagCompound();
            data.setString("material", material);
            stack.setTagCompound(data);

            return stack;
        }

        @SubscribeEvent
        public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
            IForgeRegistry<IRecipe> registry = event.getRegistry();

            for (BlockPlanks.EnumType material : BlockPlanks.EnumType.values()) {
                if (EnumBasicDrawer.FULL1.isEnabled()) {
                    ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.FULL1, material.getName(), EnumBasicDrawer.FULL1.getOutput());
                    registry.register(new ShapedOreRecipe(EMPTY_GROUP, result, "xxx", " y ", "xxx", 'x', new ItemStack(Blocks.PLANKS, 1, material.getMetadata()), 'y', "chestWood")
                            .setRegistryName(result.getItem().getRegistryName() + "_" + EnumBasicDrawer.FULL1.getUnlocalizedName() + "_" + material));
                }
                if (EnumBasicDrawer.FULL2.isEnabled()) {
                    ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.FULL2, material.getName(), EnumBasicDrawer.FULL2.getOutput());
                    registry.register(new ShapedOreRecipe(EMPTY_GROUP, result, "xyx", "xxx", "xyx", 'x', new ItemStack(Blocks.PLANKS, 1, material.getMetadata()), 'y', "chestWood")
                            .setRegistryName(result.getItem().getRegistryName() + "_" + EnumBasicDrawer.FULL2.getUnlocalizedName() + "_" + material));
                }
                if (EnumBasicDrawer.FULL4.isEnabled()) {
                    ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.FULL4, material.getName(), EnumBasicDrawer.FULL4.getOutput());
                    registry.register(new ShapedOreRecipe(EMPTY_GROUP, result, "yxy", "xxx", "yxy", 'x', new ItemStack(Blocks.PLANKS, 1, material.getMetadata()), 'y', "chestWood")
                            .setRegistryName(result.getItem().getRegistryName() + "_" + EnumBasicDrawer.FULL4.getUnlocalizedName() + "_" + material));
                }
                if (EnumBasicDrawer.HALF2.isEnabled()) {
                    ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.HALF2, material.getName(), EnumBasicDrawer.HALF2.getOutput());
                    registry.register(new ShapedOreRecipe(EMPTY_GROUP, result, "xyx", "xxx", "xyx", 'x', new ItemStack(Blocks.WOODEN_SLAB, 1, material.getMetadata()), 'y', "chestWood")
                            .setRegistryName(result.getItem().getRegistryName() + "_" + EnumBasicDrawer.HALF2.getUnlocalizedName() + "_" + material));
                }
                if (EnumBasicDrawer.HALF4.isEnabled()) {
                    ItemStack result = makeBasicDrawerItemStack(EnumBasicDrawer.HALF4, material.getName(), EnumBasicDrawer.HALF4.getOutput());
                    registry.register(new ShapedOreRecipe(EMPTY_GROUP, result, "yxy", "xxx", "yxy", 'x', new ItemStack(Blocks.WOODEN_SLAB, 1, material.getMetadata()), 'y', "chestWood")
                            .setRegistryName(result.getItem().getRegistryName() + "_" + EnumBasicDrawer.HALF4.getUnlocalizedName() + "_" + material));
                }
                if (SDConfig.blocks.trim.enabled) {
                    ItemStack result = new ItemStack(ModBlocks.trim, SDConfig.blocks.trim.recipeOutput, material.getMetadata());
                    registry.register(new ShapedOreRecipe(EMPTY_GROUP, result, "xyx", "yyy", "xyx", 'x', "stickWood", 'y', new ItemStack(Blocks.PLANKS, 1, material.getMetadata()))
                            .setRegistryName(result.getItem().getRegistryName() + "_" + material));
                }
            }
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void registerModels(ModelRegistryEvent event) {
            if (basicDrawers != null)
                basicDrawers.initDynamic();
            if (compDrawers != null)
                compDrawers.initDynamic();
            if (customDrawers != null)
                customDrawers.initDynamic();

            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDrawersStandard.class, new TileEntityDrawersRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDrawersComp.class, new TileEntityDrawersRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFramingTable.class, new TileEntityFramingRenderer());

            ModelRegistry modelRegistry = Chameleon.instance.modelRegistry;

            if (basicDrawers != null)
                modelRegistry.registerModel(new BasicDrawerModel.Register());
            if (compDrawers != null)
                modelRegistry.registerModel(new CompDrawerModel.Register());
            if (customDrawers != null) {
                modelRegistry.registerModel(new FramingTableModel.Register());
                modelRegistry.registerModel(new CustomDrawerModel.Register());
                modelRegistry.registerModel(new CustomTrimModel.Register());
            }

            modelRegistry.registerItemVariants(trim);
            modelRegistry.registerItemVariants(controller);
            modelRegistry.registerItemVariants(controllerSlave);
            modelRegistry.registerItemVariants(keyButton);
        }
    }
}
