package com.jaquadro.minecraft.storagedrawers.block.tile;

import com.jaquadro.minecraft.chameleon.block.ChamTileEntity;
import com.jaquadro.minecraft.chameleon.block.tiledata.CustomNameData;
import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.security.ISecurityProvider;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributesModifiable;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.IProtectable;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.ISealable;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.ControllerData;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.MaterialData;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.UpgradeData;
import com.jaquadro.minecraft.storagedrawers.capabilities.BasicDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.core.ModItems;
import com.jaquadro.minecraft.storagedrawers.item.EnumUpgradeRedstone;
import com.jaquadro.minecraft.storagedrawers.item.EnumUpgradeStorage;
import com.jaquadro.minecraft.storagedrawers.network.CountUpdateMessage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

public abstract class TileEntityDrawers extends ChamTileEntity implements ISealable, IProtectable, IDrawerGroup, IWorldNameable {

    private final CustomNameData customNameData = new CustomNameData("storagedrawers.container.drawers");
    private final MaterialData materialData = new MaterialData();
    private final UpgradeData upgradeData = new DrawerUpgradeData();

    public final ControllerData controllerData = new ControllerData();

    private int direction;
    private String material;
    private int drawerCapacity = 1;
    private boolean taped = false;
    private UUID owner;
    private String securityKey;

    private final IDrawerAttributesModifiable drawerAttributes;

    private long lastClickTime;
    private UUID lastClickUUID;

    private class DrawerAttributes extends BasicDrawerAttributes {

        @Override
        protected void onAttributeChanged() {
            TileEntityDrawers.this.onAttributeChanged();
            if (getWorld() != null && !getWorld().isRemote) {
                markDirty();
                markBlockForUpdate();
            }
        }
    }

    private class DrawerUpgradeData extends UpgradeData {

        DrawerUpgradeData() {
            super(7);
        }

        @Override
        public boolean canAddUpgrade(@Nonnull ItemStack upgrade) {
            if (!super.canAddUpgrade(upgrade))
                return false;

            if (upgrade.getItem() == ModItems.upgradeOneStack) {
                int lostStackCapacity = upgradeData.getStorageMultiplier() * (getEffectiveDrawerCapacity() - 1);
                return stackCapacityCheck(lostStackCapacity);
            }

            return true;
        }

        @Override
        public boolean canRemoveUpgrade(int slot) {
            if (!super.canRemoveUpgrade(slot))
                return false;

            ItemStack upgrade = getUpgrade(slot);
            if (upgrade.getItem() == ModItems.upgradeStorage) {
                int storageLevel = EnumUpgradeStorage.byMetadata(upgrade.getMetadata()).getLevel();
                int storageMult = StorageDrawers.config.getStorageUpgradeMultiplier(storageLevel);
                int effectiveStorageMult = upgradeData.getStorageMultiplier();
                if (effectiveStorageMult == storageMult)
                    storageMult--;

                int addedStackCapacity = storageMult * getEffectiveDrawerCapacity();
                return stackCapacityCheck(addedStackCapacity);
            }

            return true;
        }

        @Override
        public boolean canSwapUpgrade(int slot, @NotNull ItemStack add) {
            if (!canRemoveUpgrade(slot) || !canAddUpgrade(add))
                return false;

            // Check if slot upgrade was a downgrade (everything can be put instead of downgrade)
            ItemStack upgrade = getUpgrade(slot);
            if (upgrade.getItem() == ModItems.upgradeOneStack)
                return true;

            // Slot Upgrade is a normal upgrade, as it failed the previous check. Checking if new is a normal upgrade...
            // If both upgrades, then because of the RemoveUpgrade check, it is fine, so return true
            if (add.getItem() == ModItems.upgradeStorage) {
                return true;
            }

            // New item is a downgrade
            int currentUpgradeMult = upgradeData.getStorageMultiplier();
            int storageLevel = EnumUpgradeStorage.byMetadata(upgrade.getMetadata()).getLevel();
            int storageMult = StorageDrawers.config.getStorageUpgradeMultiplier(storageLevel);

            // The below first calculates the amount of stacks to remove if the multiplier stayed the same, then adds the removed multiplier,
            // which results in the amount of stacks (storage) to remove. The addition would be multiplied by
            // the stacks to scale to, but in this case, that is 1.

            // We need the below removed stacks calculation to be less than or equal to
            // currentUpgradeMult * getEffectiveDrawerCapacity - 1, as otherwise, the calculated stacks to remove will be equal
            // to the current max stack size of the drawer, which will result in a calculation of 0 stacks.

            int removedStacks = Math.min(currentUpgradeMult * getEffectiveDrawerCapacity() - 1,
                    currentUpgradeMult * (getEffectiveDrawerCapacity() - 1) + storageMult);
            return stackCapacityCheck(removedStacks);
        }

        @Override
        protected void onUpgradeChanged(ItemStack oldUpgrade, ItemStack newUpgrade) {

            if (getWorld() != null && !getWorld().isRemote) {
                markDirty();
                markBlockForUpdate();
            }
        }

        private boolean stackCapacityCheck(int stackCapacity) {
            for (int i = 0; i < getDrawerCount(); i++) {
                IDrawer drawer = getDrawer(i);
                if (!drawer.isEnabled() || drawer.isEmpty())
                    continue;

                int addedItemCapacity = stackCapacity * drawer.getStoredItemStackSize();
                if (drawer.getMaxCapacity() - addedItemCapacity < drawer.getStoredItemCount())
                    return false;
            }

            return true;
        }
    }

    protected TileEntityDrawers() {
        drawerAttributes = new DrawerAttributes();

        upgradeData.setDrawerAttributes(drawerAttributes);

        injectPortableData(customNameData);
        injectPortableData(upgradeData);
        injectPortableData(materialData);
        injectData(controllerData);
    }

    public abstract IDrawerGroup getGroup();

    public IDrawerAttributes getDrawerAttributes() {
        return drawerAttributes;
    }

    public UpgradeData upgrades() {
        return upgradeData;
    }

    public MaterialData material() {
        return materialData;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction % 6;
    }

    public String getMaterial() {
        return material;
    }

    public String getMaterialOrDefault() {
        String mat = getMaterial();
        return (mat != null) ? mat : "oak";
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getDrawerCapacity() {
        return drawerCapacity;
    }

    public int getEffectiveDrawerCapacity() {
        if (upgradeData.hasOneStackUpgrade())
            return 1;

        return getDrawerCapacity();
    }

    @Override
    public UUID getOwner() {
        if (!StorageDrawers.config.cache.enablePersonalUpgrades)
            return null;

        return owner;
    }

    @Override
    public boolean setOwner(UUID owner) {
        if (!StorageDrawers.config.cache.enablePersonalUpgrades)
            return false;

        if ((this.owner != null && !this.owner.equals(owner)) || (owner != null && !owner.equals(this.owner))) {
            this.owner = owner;

            if (getWorld() != null && !getWorld().isRemote) {
                markDirty();
                markBlockForUpdate();
            }
        }

        return true;
    }

    @Override
    public ISecurityProvider getSecurityProvider() {
        return StorageDrawers.securityRegistry.getProvider(securityKey);
    }

    @Override
    public boolean setSecurityProvider(ISecurityProvider provider) {
        if (!StorageDrawers.config.cache.enablePersonalUpgrades)
            return false;

        String newKey = (provider == null) ? null : provider.getProviderID();
        if ((newKey != null && !newKey.equals(securityKey)) || (securityKey != null && !securityKey.equals(newKey))) {
            securityKey = newKey;

            if (getWorld() != null && !getWorld().isRemote) {
                markDirty();
                markBlockForUpdate();
            }
        }

        return true;
    }

    protected void onAttributeChanged() {
    }

    public boolean isSealed() {
        if (!StorageDrawers.config.cache.enableTape)
            return false;

        return taped;
    }

    public boolean setIsSealed(boolean sealed) {
        if (!StorageDrawers.config.cache.enableTape)
            return false;

        if (this.taped != sealed) {
            this.taped = sealed;

            if (getWorld() != null && !getWorld().isRemote) {
                markDirty();
                markBlockForUpdate();
            }
        }

        return true;
    }

    public boolean isRedstone() {
        if (!StorageDrawers.config.cache.enableRedstoneUpgrades)
            return false;

        return upgradeData.getRedstoneType() != null;
    }

    public int getRedstoneLevel() {
        EnumUpgradeRedstone type = upgradeData.getRedstoneType();
        if (type == null)
            return 0;

        return switch (type) {
            case COMBINED -> getCombinedRedstoneLevel();
            case MAX -> getMaxRedstoneLevel();
            case MIN -> getMinRedstoneLevel();
            default -> 0;
        };
    }

    protected int getCombinedRedstoneLevel() {
        int active = 0;
        float fillRatio = 0;

        for (int i = 0; i < getDrawerCount(); i++) {
            IDrawer drawer = getDrawer(i);
            if (!drawer.isEnabled())
                continue;

            if (drawer.getMaxCapacity() > 0)
                fillRatio += ((float) drawer.getStoredItemCount() / drawer.getMaxCapacity());

            active++;
        }

        if (active == 0)
            return 0;

        if (fillRatio == active)
            return 15;

        return (int) Math.ceil((fillRatio / active) * 14);
    }

    protected int getMinRedstoneLevel() {
        float minRatio = 2;

        for (int i = 0; i < getDrawerCount(); i++) {
            IDrawer drawer = getDrawer(i);
            if (!drawer.isEnabled())
                continue;

            if (drawer.getMaxCapacity() > 0)
                minRatio = Math.min(minRatio, (float) drawer.getStoredItemCount() / drawer.getMaxCapacity());
            else
                minRatio = 0;
        }

        if (minRatio > 1)
            return 0;
        if (minRatio == 1)
            return 15;

        return (int) Math.ceil(minRatio * 14);
    }

    protected int getMaxRedstoneLevel() {
        float maxRatio = 0;

        for (int i = 0; i < getDrawerCount(); i++) {
            IDrawer drawer = getDrawer(i);
            if (!drawer.isEnabled())
                continue;

            if (drawer.getMaxCapacity() > 0)
                maxRatio = Math.max(maxRatio, (float) drawer.getStoredItemCount() / drawer.getMaxCapacity());
        }

        if (maxRatio == 1)
            return 15;

        return (int) Math.ceil(maxRatio * 14);
    }

    @Nonnull
    public ItemStack takeItemsFromSlot(int slot, int count) {
        IDrawer drawer = getGroup().getDrawer(slot);
        if (!drawer.isEnabled() || drawer.isEmpty())
            return ItemStack.EMPTY;

        ItemStack stack = drawer.getStoredItemPrototype().copy();
        stack.setCount(Math.min(count, drawer.getStoredItemCount()));

        drawer.setStoredItemCount(drawer.getStoredItemCount() - stack.getCount());

        if (isRedstone() && getWorld() != null) {
            getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), false);
            getWorld().notifyNeighborsOfStateChange(getPos().down(), getBlockType(), false);
        }

        // TODO: Reset empty drawer in subclasses

        return stack;
    }

    public int putItemsIntoSlot(int slot, @Nonnull ItemStack stack, int count) {
        IDrawer drawer = getGroup().getDrawer(slot);
        if (!drawer.isEnabled())
            return 0;

        if (drawer.isEmpty())
            drawer = drawer.setStoredItem(stack);

        if (!drawer.canItemBeStored(stack))
            return 0;

        int countAdded = Math.min(count, stack.getCount());
        if (!drawerAttributes.isVoid())
            countAdded = Math.min(countAdded, drawer.getRemainingCapacity());

        drawer.setStoredItemCount(drawer.getStoredItemCount() + countAdded);
        stack.shrink(countAdded);

        return countAdded;
    }

    public int interactPutCurrentItemIntoSlot(int slot, EntityPlayer player) {
        IDrawer drawer = getDrawer(slot);
        if (!drawer.isEnabled())
            return 0;

        int count = 0;
        ItemStack playerStack = player.inventory.getCurrentItem();
        if (!playerStack.isEmpty())
            count = putItemsIntoSlot(slot, playerStack, playerStack.getCount());

        return count;
    }

    public int interactPutCurrentInventoryIntoSlot(int slot, EntityPlayer player) {
        IDrawer drawer = getGroup().getDrawer(slot);
        if (!drawer.isEnabled())
            return 0;

        int count = 0;
        if (!drawer.isEmpty()) {
            for (int i = 0, n = player.inventory.getSizeInventory(); i < n; i++) {
                ItemStack subStack = player.inventory.getStackInSlot(i);
                if (!subStack.isEmpty()) {
                    int subCount = putItemsIntoSlot(slot, subStack, subStack.getCount());
                    if (subCount > 0 && subStack.getCount() == 0)
                        player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);

                    count += subCount;
                }
            }
        }

        if (count > 0)
            StorageDrawers.proxy.updatePlayerInventory(player);

        return count;
    }

    public int interactPutItemsIntoSlot(int slot, EntityPlayer player) {
        int count;
        if (getWorld().getTotalWorldTime() - lastClickTime < 10 && player.getPersistentID().equals(lastClickUUID))
            count = interactPutCurrentInventoryIntoSlot(slot, player);
        else
            count = interactPutCurrentItemIntoSlot(slot, player);

        lastClickTime = getWorld().getTotalWorldTime();
        lastClickUUID = player.getPersistentID();

        return count;
    }

    @Override
    protected void readFromFixedNBT(NBTTagCompound tag) {
        super.readFromFixedNBT(tag);

        setDirection(tag.getByte("Dir"));

        taped = false;
        if (tag.hasKey("Tape"))
            taped = tag.getBoolean("Tape");
    }

    @Override
    protected NBTTagCompound writeToFixedNBT(NBTTagCompound tag) {
        tag = super.writeToFixedNBT(tag);

        tag.setByte("Dir", (byte) direction);

        if (taped)
            tag.setBoolean("Tape", true);

        return tag;
    }

    @Override
    public void readFromPortableNBT(NBTTagCompound tag) {
        super.readFromPortableNBT(tag);

        material = null;
        if (tag.hasKey("Mat"))
            material = tag.getString("Mat");

        drawerCapacity = tag.getInteger("Cap");

        drawerAttributes.setItemLocked(LockAttribute.LOCK_EMPTY, false);
        drawerAttributes.setItemLocked(LockAttribute.LOCK_POPULATED, false);
        if (tag.hasKey("Lock")) {
            EnumSet<LockAttribute> attrs = LockAttribute.getEnumSet(tag.getByte("Lock"));
            if (attrs != null) {
                drawerAttributes.setItemLocked(LockAttribute.LOCK_EMPTY, attrs.contains(LockAttribute.LOCK_EMPTY));
                drawerAttributes.setItemLocked(LockAttribute.LOCK_POPULATED, attrs.contains(LockAttribute.LOCK_POPULATED));
            }
        }

        drawerAttributes.setIsConcealed(false);
        if (tag.hasKey("Shr"))
            drawerAttributes.setIsConcealed(tag.getBoolean("Shr"));

        drawerAttributes.setIsShowingQuantity(false);
        if (tag.hasKey("Qua")) {
            drawerAttributes.setIsShowingQuantity(tag.getBoolean("Qua"));
        }

        owner = null;
        if (tag.hasKey("Own"))
            owner = UUID.fromString(tag.getString("Own"));

        securityKey = null;
        if (tag.hasKey("Sec"))
            securityKey = tag.getString("Sec");
    }

    @Override
    public NBTTagCompound writeToPortableNBT(NBTTagCompound tag) {
        tag = super.writeToPortableNBT(tag);

        tag.setInteger("Cap", getDrawerCapacity());

        if (material != null)
            tag.setString("Mat", material);

        EnumSet<LockAttribute> attrs = EnumSet.noneOf(LockAttribute.class);
        if (drawerAttributes.isItemLocked(LockAttribute.LOCK_EMPTY))
            attrs.add(LockAttribute.LOCK_EMPTY);
        if (drawerAttributes.isItemLocked(LockAttribute.LOCK_POPULATED))
            attrs.add(LockAttribute.LOCK_POPULATED);

        if (!attrs.isEmpty()) {
            tag.setByte("Lock", (byte) LockAttribute.getBitfield(attrs));
        }

        if (drawerAttributes.isConcealed())
            tag.setBoolean("Shr", true);

        if (drawerAttributes.isShowingQuantity())
            tag.setBoolean("Qua", true);

        if (owner != null)
            tag.setString("Own", owner.toString());

        if (securityKey != null)
            tag.setString("Sec", securityKey);

        return tag;
    }

    @Override
    public void markDirty() {
        if (isRedstone() && getWorld() != null) {
            getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), false);
            getWorld().notifyNeighborsOfStateChange(getPos().down(), getBlockType(), false);
        }

        super.markDirty();
    }

    protected void syncClientCount(int slot, int count) {
        if (getWorld() != null && getWorld().isRemote)
            return;

        TargetPoint point = new TargetPoint(getWorld().provider.getDimension(),
                getPos().getX(), getPos().getY(), getPos().getZ(), 500);
        StorageDrawers.network.sendToAllAround(new CountUpdateMessage(getPos(), slot, count), point);
    }

    @SideOnly(Side.CLIENT)
    public void clientUpdateCount(final int slot, final int count) {
        if (!getWorld().isRemote)
            return;

        Minecraft.getMinecraft().addScheduledTask(() -> TileEntityDrawers.this.clientUpdateCountAsync(slot, count));
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdateCountAsync(int slot, int count) {
        IDrawer drawer = getDrawer(slot);
        if (drawer.isEnabled() && drawer.getStoredItemCount() != count)
            drawer.setStoredItemCount(count);

    }

    @Override
    public boolean dataPacketRequiresRenderUpdate() {
        return true;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    @Deprecated
    public int getDrawerCount() {
        return getGroup().getDrawerCount();
    }

    @Nonnull
    @Override
    @Deprecated
    public IDrawer getDrawer(int slot) {
        return getGroup().getDrawer(slot);
    }

    @Nonnull
    @Override
    @Deprecated
    public int[] getAccessibleDrawerSlots() {
        return getGroup().getAccessibleDrawerSlots();
    }

    @Override
    public String getName() {
        return customNameData.getName();
    }

    @Override
    public boolean hasCustomName() {
        return customNameData.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return customNameData.getDisplayName();
    }

    public void setInventoryName(String name) {
        customNameData.setName(name);
    }

    @CapabilityInject(IDrawerGroup.class)
    public static Capability<IDrawerGroup> DRAWER_GROUP_CAPABILITY = null;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == DRAWER_GROUP_CAPABILITY)
            return (T) getGroup();

        if (getGroup().hasCapability(capability, facing))
            return getGroup().getCapability(capability, facing);

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == DRAWER_GROUP_CAPABILITY)
            return true;

        if (getGroup().hasCapability(capability, facing))
            return true;

        return super.hasCapability(capability, facing);
    }
}
