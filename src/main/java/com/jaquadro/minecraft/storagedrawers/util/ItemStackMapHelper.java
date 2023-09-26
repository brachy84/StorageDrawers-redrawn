package com.jaquadro.minecraft.storagedrawers.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemStackMapHelper {

    public static <V> Object2ObjectOpenCustomHashMap<ItemStack, V> createItemMetaMap() {
        return new Object2ObjectOpenCustomHashMap<>(ITEM_META_STRATEGY);
    }

    public static final Hash.Strategy<ItemStack> ITEM_META_STRATEGY = new Hash.Strategy<>() {

        @Override
        public int hashCode(ItemStack o) {
            return Objects.hash(o.getItem(), Items.DIAMOND.getDamage(o));
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            return (a.isEmpty() && b.isEmpty()) || ItemStack.areItemsEqual(a, b);
        }
    };
}
