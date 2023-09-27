package com.jaquadro.minecraft.storagedrawers.config;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OreDictRegistry {

    private final Set<String> whiteListPrefixes = new ObjectOpenHashSet<>();
    private final Set<String> blackListMaterials = new ObjectOpenHashSet<>();
    private final Object2BooleanOpenHashMap<String> cache = new Object2BooleanOpenHashMap<>();

    public OreDictRegistry() {
        blackListMaterials(""); // effectively blacklists sticks
        whiteListOrePrefixes(SDConfig.registries.orePrefixWhitelist);
        blackListMaterials(SDConfig.registries.materialBlacklist);
    }

    public void whiteListOrePrefixes(String... prefixes) {
        Collections.addAll(this.whiteListPrefixes, prefixes);
    }

    public void blackListMaterials(String... materials) {
        Collections.addAll(this.blackListMaterials, materials);
    }

    public boolean isEntryValid(String entry) {
        if (this.cache.containsKey(entry)) {
            return this.cache.getBoolean(entry);
        }
        boolean result = false;
        for (String prefix : this.whiteListPrefixes) {
            if (entry.startsWith(prefix)) {
                result = true;
                String material = entry.substring(prefix.length() - 1);
                if (this.blackListMaterials.contains(material)) {
                    result = false;
                }
                break;
            }
        }
        if (result && !isValidForEquiv(entry)) {
            result = false;
        }
        this.cache.put(entry, result);
        return result;
    }

    private boolean isValidForEquiv(String oreName) {
        List<ItemStack> oreList = OreDictionary.getOres(oreName);
        if (oreList.isEmpty())
            return false;

        // Fail entries that have any wildcard items registered to them.

        Set<String> modIds = new ObjectOpenHashSet<>();
        for (ItemStack anOreList : oreList) {
            if (anOreList.getItemDamage() == OreDictionary.WILDCARD_VALUE)
                return false;

            modIds.add(anOreList.getItem().getRegistryName().getNamespace());
        }

        // Fail entries that have multiple instances of an item registered, differing by metadata or other
        // criteria.

        if (modIds.size() < oreList.size())
            return false;

        // Fail entries where the keys in at least one stack are not the super-set of all other stacks.
        // Can be determined by merging all keys and testing cardinality.

        IntSet mergedIds = new IntOpenHashSet();
        int maxKeyCount = 0;

        for (ItemStack anOreList : oreList) {
            int[] ids = OreDictionary.getOreIDs(anOreList);
            maxKeyCount = Math.max(maxKeyCount, ids.length);

            for (int id : ids)
                mergedIds.add(id);
        }

        return maxKeyCount >= mergedIds.size();
    }
}
