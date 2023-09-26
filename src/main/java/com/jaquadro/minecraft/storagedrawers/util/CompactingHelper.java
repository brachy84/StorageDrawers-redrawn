package com.jaquadro.minecraft.storagedrawers.util;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.config.CompTierRegistry;
import com.jaquadro.minecraft.storagedrawers.config.SDConfig;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO WTF
public class CompactingHelper {

    private static final InventoryLookup lookup1 = new InventoryLookup(1, 1);
    private static final InventoryLookup lookup2 = new InventoryLookup(2, 2);
    private static final InventoryLookup lookup3 = new InventoryLookup(3, 3);

    private final World world;

    public class Result {

        @Nonnull
        private final ItemStack stack;
        private final int size;

        public Result(ItemStack stack, int size) {
            this.stack = stack;
            this.size = size;
        }

        @Nonnull
        public ItemStack getStack() {
            return stack;
        }

        public int getSize() {
            return size;
        }
    }

    public CompactingHelper(World world) {
        this.world = world;
    }

    @Nonnull
    public Result findHigherTier(@Nonnull ItemStack stack) {
        if (!world.isRemote && SDConfig.general.enableDebugLogging)
            StorageDrawers.log.info("Finding ascending candidates for " + stack);

        CompTierRegistry.Record record = StorageDrawers.compRegistry.findHigherTier(stack);
        if (record != null) {
            if (!world.isRemote && SDConfig.general.enableDebugLogging)
                StorageDrawers.log.info("Found " + record.upper + " in registry with conv=" + record.convRate);

            return new Result(record.upper, record.convRate);
        }

        List<ItemStack> candidates = new ArrayList<>();

        int lookupSize = setupLookup(lookup3, stack);
        List<ItemStack> fwdCandidates = findAllMatchingRecipes(lookup3);

        if (fwdCandidates.size() == 0) {
            lookupSize = setupLookup(lookup2, stack);
            fwdCandidates = findAllMatchingRecipes(lookup2);
        }

        if (fwdCandidates.size() > 0) {
            for (ItemStack match : fwdCandidates) {
                setupLookup(lookup1, match);
                List<ItemStack> backCandidates = findAllMatchingRecipes(lookup1);

                for (ItemStack comp : backCandidates) {
                    if (comp.getCount() != lookupSize)
                        continue;

                    if (!ItemStackOreMatcher.areItemsEqual(comp, stack, false))
                        continue;

                    candidates.add(match);
                    if (!world.isRemote && SDConfig.general.enableDebugLogging)
                        StorageDrawers.log.info("Found ascending candidate for " + stack + ": " + match + " size=" + lookupSize + ", inverse=" + comp);

                    break;
                }
            }
        }

        ItemStack modMatch = findMatchingModCandidate(stack, candidates);
        if (!modMatch.isEmpty())
            return new Result(modMatch, lookupSize);

        if (candidates.size() > 0)
            return new Result(candidates.get(0), lookupSize);

        if (!world.isRemote && SDConfig.general.enableDebugLogging)
            StorageDrawers.log.info("No candidates found");

        return new Result(ItemStack.EMPTY, 0);
    }

    @Nonnull
    public Result findLowerTier(@Nonnull ItemStack stack) {
        if (!world.isRemote && SDConfig.general.enableDebugLogging)
            StorageDrawers.log.info("Finding descending candidates for " + stack);

        CompTierRegistry.Record record = StorageDrawers.compRegistry.findLowerTier(stack);
        if (record != null) {
            if (!world.isRemote && SDConfig.general.enableDebugLogging)
                StorageDrawers.log.info("Found " + record.lower + " in registry with conv=" + record.convRate);

            return new Result(record.lower, record.convRate);
        }

        List<ItemStack> candidates = new ArrayList<>();
        Object2IntOpenHashMap<ItemStack> candidatesRate = new Object2IntOpenHashMap<>();

        for (IRecipe recipe : CraftingManager.REGISTRY) {
            ItemStack output = recipe.getRecipeOutput();
            if (!ItemStackOreMatcher.areItemsEqual(stack, output, true))
                continue;

            @Nonnull ItemStack match = tryMatch(stack, recipe.getIngredients());
            if (!match.isEmpty()) {
                int lookupSize = setupLookup(lookup1, output);
                List<ItemStack> compMatches = findAllMatchingRecipes(lookup1);
                for (ItemStack comp : compMatches) {
                    int recipeSize = recipe.getIngredients().size();
                    if (ItemStackOreMatcher.areItemsEqual(match, comp, true) && comp.getCount() == recipeSize) {
                        candidates.add(match);
                        candidatesRate.put(match, recipeSize);

                        if (!world.isRemote && SDConfig.general.enableDebugLogging)
                            StorageDrawers.log.info("Found descending candidate for " + stack + ": " + match + " size=" + recipeSize + ", inverse=" + comp);
                    } else if (!world.isRemote && SDConfig.general.enableDebugLogging)
                        StorageDrawers.log.info("Back-check failed for " + match + " size=" + lookupSize + ", inverse=" + comp);
                }
            }
        }

        ItemStack modMatch = findMatchingModCandidate(stack, candidates);
        if (!modMatch.isEmpty())
            return new Result(modMatch, candidatesRate.getInt(modMatch));

        if (!candidates.isEmpty()) {
            ItemStack match = candidates.get(0);
            return new Result(match, candidatesRate.getInt(match));
        }

        if (!world.isRemote && SDConfig.general.enableDebugLogging)
            StorageDrawers.log.info("No candidates found");

        return new Result(ItemStack.EMPTY, 0);
    }

    private List<ItemStack> findAllMatchingRecipes(InventoryCrafting crafting) {
        List<ItemStack> candidates = new ArrayList<>();

        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe.matches(crafting, world)) {
                ItemStack result = recipe.getCraftingResult(crafting);
                if (!result.isEmpty())
                    candidates.add(result);
            }
        }

        return candidates;
    }

    @Nonnull
    private ItemStack findMatchingModCandidate(@Nonnull ItemStack reference, List<ItemStack> candidates) {
        ResourceLocation referenceName = reference.getItem().getRegistryName();
        if (referenceName != null) {
            for (ItemStack candidate : candidates) {
                ResourceLocation matchName = candidate.getItem().getRegistryName();
                if (matchName != null) {
                    if (referenceName.getNamespace().equals(matchName.getNamespace()))
                        return candidate;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Nonnull
    private ItemStack tryMatch(@Nonnull ItemStack stack, NonNullList<Ingredient> ingredients) {
        if (ingredients.size() != 9 && ingredients.size() != 4)
            return ItemStack.EMPTY;

        Ingredient refIngredient = ingredients.get(0);
        ItemStack[] refMatchingStacks = refIngredient.getMatchingStacks();
        if (refMatchingStacks.length == 0)
            return ItemStack.EMPTY;

        for (int i = 1, n = ingredients.size(); i < n; i++) {
            Ingredient ingredient = ingredients.get(i);
            @Nonnull ItemStack match = ItemStack.EMPTY;

            for (ItemStack ingItemMatch : refMatchingStacks) {
                if (ingredient.apply(ingItemMatch)) {
                    match = ingItemMatch;
                    break;
                }
            }

            if (match.isEmpty())
                return ItemStack.EMPTY;
        }

        ItemStack match = findMatchingModCandidate(stack, Arrays.asList(refMatchingStacks));
        if (match.isEmpty())
            match = refMatchingStacks[0];

        return match;
    }

    private int setupLookup(InventoryLookup inv, @Nonnull ItemStack stack) {
        for (int i = 0, n = inv.getSizeInventory(); i < n; i++)
            inv.setInventorySlotContents(i, stack);

        return inv.getSizeInventory();
    }

    private static class InventoryLookup extends InventoryCrafting {

        private final ItemStack[] stackList;

        public InventoryLookup(int width, int height) {
            super(null, width, height);

            stackList = new ItemStack[width * height];
            Arrays.fill(stackList, ItemStack.EMPTY);
        }

        @Override
        public int getSizeInventory() {
            return this.stackList.length;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return slot >= this.getSizeInventory() ? ItemStack.EMPTY : this.stackList[slot];
        }

        @Override
        @Nonnull
        public ItemStack removeStackFromSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack decrStackSize(int slot, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
            stackList[slot] = stack;
        }
    }
}
