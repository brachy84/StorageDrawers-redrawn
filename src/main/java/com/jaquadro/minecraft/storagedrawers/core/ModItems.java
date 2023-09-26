package com.jaquadro.minecraft.storagedrawers.core;

import com.jaquadro.minecraft.chameleon.Chameleon;
import com.jaquadro.minecraft.chameleon.resources.ModelRegistry;
import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.config.SDConfig;
import com.jaquadro.minecraft.storagedrawers.item.*;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems {

    @ObjectHolder(StorageDrawers.MOD_ID + ":upgrade_template")
    public static Item upgradeTemplate;
    @ObjectHolder(StorageDrawers.MOD_ID + ":upgrade_storage")
    public static ItemUpgradeStorage upgradeStorage;
    @ObjectHolder(StorageDrawers.MOD_ID + ":upgrade_one_stack")
    public static ItemUpgrade upgradeOneStack;
    @ObjectHolder(StorageDrawers.MOD_ID + ":upgrade_status")
    public static ItemUpgradeStatus upgradeStatus;
    @ObjectHolder(StorageDrawers.MOD_ID + ":drawer_key")
    public static ItemDrawerKey drawerKey;
    @ObjectHolder(StorageDrawers.MOD_ID + ":upgrade_void")
    public static ItemUpgrade upgradeVoid;
    @ObjectHolder(StorageDrawers.MOD_ID + ":upgrade_conversion")
    public static ItemUpgrade upgradeConversion;
    @ObjectHolder(StorageDrawers.MOD_ID + ":upgrade_creative")
    public static ItemUpgradeCreative upgradeCreative;
    @ObjectHolder(StorageDrawers.MOD_ID + ":upgrade_redstone")
    public static ItemUpgradeRedstone upgradeRedstone;
    @ObjectHolder(StorageDrawers.MOD_ID + ":shroud_key")
    public static ItemShroudKey shroudKey;
    @ObjectHolder(StorageDrawers.MOD_ID + ":personal_key")
    public static ItemPersonalKey personalKey;
    @ObjectHolder(StorageDrawers.MOD_ID + ":quantify_key")
    public static ItemQuantifyKey quantifyKey;
    @ObjectHolder(StorageDrawers.MOD_ID + ":tape")
    public static ItemTape tape;

    @Mod.EventBusSubscriber(modid = StorageDrawers.MOD_ID)
    public static class Registration {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            IForgeRegistry<Item> itemRegistry = event.getRegistry();

            itemRegistry.register(new Item().setTranslationKey(makeName("upgradeTemplate")).setRegistryName("upgrade_template").setCreativeTab(ModCreativeTabs.tabStorageDrawers));

            if (SDConfig.general.enableStorageUpgrades) {
                itemRegistry.register(new ItemUpgradeStorage("upgrade_storage", makeName("upgradeStorage")));
                itemRegistry.register(new ItemUpgrade("upgrade_one_stack", makeName("upgradeOneStack")));
            }

            if (SDConfig.general.enableIndicatorUpgrades)
                itemRegistry.register(new ItemUpgradeStatus("upgrade_status", makeName("upgradeStatus")));
            if (SDConfig.general.enableVoidUpgrades)
                itemRegistry.register(new ItemUpgrade("upgrade_void", makeName("upgradeVoid")));
            if (SDConfig.general.enableItemConversion)
                itemRegistry.register(new ItemUpgrade("upgrade_conversion", makeName("upgradeConversion")));
            if (SDConfig.general.enableCreativeUpgrades)
                itemRegistry.register(new ItemUpgradeCreative("upgrade_creative", makeName("upgradeCreative")));
            if (SDConfig.general.enableRedstoneUpgrades)
                itemRegistry.register(new ItemUpgradeRedstone("upgrade_redstone", makeName("upgradeRedstone")));
            if (SDConfig.general.enableLockUpgrades)
                itemRegistry.register(new ItemDrawerKey("drawer_key", makeName("drawerKey")));
            if (SDConfig.general.enableShroudUpgrades)
                itemRegistry.register(new ItemShroudKey("shroud_key", makeName("shroudKey")));
            if (SDConfig.general.enablePersonalUpgrades)
                itemRegistry.register(new ItemPersonalKey("personal_key", makeName("personalKey")));
            if (SDConfig.general.enableQuantifiableUpgrades)
                itemRegistry.register(new ItemQuantifyKey("quantify_key", makeName("quantifyKey")));
            if (SDConfig.general.enableTape)
                itemRegistry.register(new ItemTape("tape", makeName("tape")));
        }

        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            ModelRegistry modelRegistry = Chameleon.instance.modelRegistry;

            modelRegistry.registerItemVariants(upgradeTemplate);
            modelRegistry.registerItemVariants(upgradeVoid);
            modelRegistry.registerItemVariants(upgradeConversion);
            modelRegistry.registerItemVariants(tape);
            modelRegistry.registerItemVariants(drawerKey);
            modelRegistry.registerItemVariants(shroudKey);
            modelRegistry.registerItemVariants(personalKey);
            modelRegistry.registerItemVariants(quantifyKey);
            modelRegistry.registerItemVariants(upgradeStorage);
            modelRegistry.registerItemVariants(upgradeOneStack);
            modelRegistry.registerItemVariants(upgradeStatus);
            modelRegistry.registerItemVariants(upgradeCreative);
            modelRegistry.registerItemVariants(upgradeRedstone);
        }
    }

    public static String makeName(String name) {
        return StorageDrawers.MOD_ID.toLowerCase() + "." + name;
    }
}
