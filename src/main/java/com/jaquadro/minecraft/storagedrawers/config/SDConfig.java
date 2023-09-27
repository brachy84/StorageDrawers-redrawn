package com.jaquadro.minecraft.storagedrawers.config;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import net.minecraftforge.common.config.Config;

@Config(modid = StorageDrawers.MOD_ID, category = "")
public class SDConfig {

    private static final String LANG_PREFIX = "storagedrawers.config.";

    @Config.LangKey(LANG_PREFIX + "blocks")
    public static final Blocks blocks = new Blocks();
    @Config.LangKey(LANG_PREFIX + "general")
    public static final General general = new General();
    @Config.LangKey(LANG_PREFIX + "integration")
    public static final Integration integration = new Integration();
    @Config.LangKey(LANG_PREFIX + "registries")
    public static final Registries registries = new Registries();
    @Config.LangKey(LANG_PREFIX + "upgrades")
    public static final Upgrades upgrades = new Upgrades();

    public static int getStorageUpgradeMultiplier(int level) {
        return switch (level) {
            case 2 -> upgrades.level2Mult;
            case 3 -> upgrades.level3Mult;
            case 4 -> upgrades.level4Mult;
            case 5 -> upgrades.level5Mult;
            case 6 -> upgrades.level6Mult;
            default -> 1;
        };
    }

    public static class Blocks {

        private static final String LP = LANG_PREFIX + "blocks.";

        @Config.LangKey(LP + "fullDrawers1")
        public final BaseDrawerConfig fulldrawers1 = new BaseDrawerConfig(32, 1);
        @Config.LangKey(LP + "fullDrawers2")
        public final BaseDrawerConfig fulldrawers2 = new BaseDrawerConfig(16, 2);
        @Config.LangKey(LP + "fullDrawers4")
        public final BaseDrawerConfig fulldrawers4 = new BaseDrawerConfig(8, 4);
        @Config.LangKey(LP + "halfDrawers2")
        public final BaseDrawerConfig halfdrawers2 = new BaseDrawerConfig(8, 2);
        @Config.LangKey(LP + "halfDrawers4")
        public final BaseDrawerConfig halfdrawers4 = new BaseDrawerConfig(4, 4);
        @Config.LangKey(LP + "compSrawers")
        public final BaseDrawerConfig compdrawers = new BaseDrawerConfig(16, 1);
        @Config.LangKey(LP + "controller")
        public final BaseDrawerConfig.Controller controller = new BaseDrawerConfig.Controller();
        @Config.LangKey(LP + "trim")
        public final BaseDrawerConfig.Trim trim = new BaseDrawerConfig.Trim();
        @Config.LangKey(LP + "controllerSlave")
        public final BaseDrawerConfig.ControllerSlave controllerslave = new BaseDrawerConfig.ControllerSlave();
        @Config.LangKey(LP + "framedBlocks")
        public final BaseDrawerConfig.FramedBlocks framedblocks = new BaseDrawerConfig.FramedBlocks();
    }

    public static class General {

        private static final String LP = LANG_PREFIX + "prop.";

        @Config.LangKey(LP + "creativeTabVanillaWoods")
        public boolean creativeTabVanillaWoods = true;
        @Config.LangKey(LP + "defaultQuantify")
        public boolean defaultQuantify = true;
        @Config.LangKey(LP + "enableCreativeUpgrades")
        public boolean enableCreativeUpgrades = true;
        @Config.Comment("Writes additional log messages while using the mod.  Mainly for debug purposes.  Should be kept disabled unless instructed otherwise.")
        @Config.LangKey(LP + "enableDebugLogging")
        public boolean enableDebugLogging = false;
        @Config.LangKey(LP + "enableDrawerUI")
        public boolean enableDrawerUI = true;
        @Config.LangKey(LP + "enableFallbackRecipes")
        public boolean enableFallbackRecipes = true;
        @Config.LangKey(LP + "enableIndicatorUpgrades")
        public boolean enableIndicatorUpgrades = true;
        @Config.LangKey(LP + "enableItemConversion")
        public boolean enableItemConversion = true;
        @Config.LangKey(LP + "enableLockUpgrades")
        public boolean enableLockUpgrades = true;
        @Config.LangKey(LP + "enablePersonalUpgrades")
        public boolean enablePersonalUpgrades = true;
        @Config.LangKey(LP + "enableQuantifiableUpgrades")
        public boolean enableQuantifiableUpgrades = true;
        @Config.LangKey(LP + "enableRedstoneUpgrades")
        public boolean enableRedstoneUpgrades = true;
        @Config.LangKey(LP + "enableShroudUpgrades")
        public boolean enableShroudUpgrades = true;
        @Config.LangKey(LP + "enableSidedInput")
        public boolean enableSidedInput = true;
        @Config.LangKey(LP + "enableSidedOutput")
        public boolean enableSidedOutput = true;
        @Config.LangKey(LP + "enableStorageUpgrades")
        public boolean enableStorageUpgrades = true;
        @Config.LangKey(LP + "enableTape")
        public boolean enableTape = true;
        @Config.LangKey(LP + "enableVoidUpgrades")
        public boolean enableVoidUpgrades = true;
        @Config.LangKey(LP + "invertClick")
        @Config.Comment("Inverts left and right click action on drawers.  If this is true, left click will insert items and right click will extract items.  Leave false for default behavior.")
        public boolean invertClick = false;
        @Config.LangKey(LP + "invertShift")
        @Config.Comment("Inverts how shift works with drawers. If this is true, shifting will only give one item, where regular clicks will give a full stack. Leave false for default behavior.")
        public boolean invertShift = false;
        @Config.LangKey(LP + "keepContentsOnBreak")
        public boolean keepContentsOnBreak = true;
        @Config.LangKey(LP + "renderRange")
        @Config.Comment("Range at which drawers should render their items. Default = 20 blocks")
        public int renderRange = 20;
        @Config.LangKey(LP + "wailaStackRemainder")
        @Config.Comment("Not sure what this is. Allowed values: 'exact' and basically anything else. Default = 'stack + remainder'")
        public String wailaStackRemainder = "stack + remainder";

        public boolean isStackRemainder() {
            return !wailaStackRemainder.equals("exact");
        }

    }

    public static class Integration {

        private static final String LP = LANG_PREFIX + "integration.";

        @Config.LangKey(LP + "enableMineTweaker")
        @Config.Comment("Why is this here???")
        public boolean enableMineTweaker = true;
        @Config.LangKey(LP + "enableTOP")
        @Config.Comment("Whether to enable The One Probe integration, which overrides the displayed block for Storage Drawers related blocks. Warning: Turning this off will make TOP display some Storage Drawers blocks incorrectly.")
        public boolean enableTOP = true;
        @Config.LangKey(LP + "enableThaumcraft")
        @Config.Comment("Whether to enable Thaumcraft integration, which adding icons on drawers if the item stored has an Aspect.")
        public boolean enableThaumcraft = true;
        @Config.LangKey(LP + "enableWaila")
        @Config.Comment("Whether to enable What Am I Looking At integration, which overrides the displayed block for Storage Drawers related blocks, and adds several Storage Drawers related options to the config. Warning: Turning this off will make Waila display some Storage Drawers blocks incorrectly.")
        public boolean enableWaila = true;
    }

    public static class Registries {

        private static final String LP = LANG_PREFIX + "registries.";

        @Config.LangKey(LP + "compRules")
        @Config.Comment("Items should be in form domain:item or domain:item:meta. [default: [minecraft:clay, minecraft:clay_ball, 4]]")
        public String[] compRules = new String[]{"minecraft:clay, minecraft:clay_ball, 4"};
        @Config.LangKey(LP + "oreWhitelist")
        @Config.Comment("List of ore dictionary names to blacklist for substitution. [default: ]")
        public String[] oreWhitelist = new String[0];
        @Config.LangKey(LP + "oreBlacklist")
        @Config.Comment("List of ore dictionary names to whitelist for substitution. [default: ]")
        public String[] oreBlacklist = new String[0];
        @Config.LangKey(LP + "registerExtraCompactingRules")
        public boolean registerExtraCompactingRules = true;
    }

    public static class Upgrades {

        private static final String LP = LANG_PREFIX + "upgrades.";

        @Config.LangKey(LP + "level2Mult")
        public int level2Mult = 2;
        @Config.LangKey(LP + "level3Mult")
        public int level3Mult = 4;
        @Config.LangKey(LP + "level4Mult")
        public int level4Mult = 8;
        @Config.LangKey(LP + "level5Mult")
        public int level5Mult = 16;
        @Config.LangKey(LP + "level6Mult")
        public int level6Mult = 32;
    }
}
