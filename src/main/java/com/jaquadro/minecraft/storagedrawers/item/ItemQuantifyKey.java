package com.jaquadro.minecraft.storagedrawers.item;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributesModifiable;
import com.jaquadro.minecraft.storagedrawers.core.ModCreativeTabs;

public class ItemQuantifyKey extends ItemKey {

    public ItemQuantifyKey(String registryName, String unlocalizedName) {
        setRegistryName(registryName);
        setTranslationKey(unlocalizedName);
        setCreativeTab(ModCreativeTabs.tabStorageDrawers);
        setMaxDamage(0);
    }

    @Override
    protected void handleDrawerAttributes(IDrawerAttributesModifiable attrs) {
        attrs.setIsShowingQuantity(!attrs.isShowingQuantity());
    }
}
