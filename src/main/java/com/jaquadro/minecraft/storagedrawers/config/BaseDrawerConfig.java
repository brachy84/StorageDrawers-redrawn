package com.jaquadro.minecraft.storagedrawers.config;

import net.minecraftforge.common.config.Config;

public class BaseDrawerConfig {

    private static final String LANG_PREFIX = "storagedrawers.config.";

    @Config.LangKey(LANG_PREFIX + "prop.baseStorage")
    public int baseStorage;
    @Config.LangKey(LANG_PREFIX + "prop.enabled")
    public boolean enabled;
    @Config.LangKey(LANG_PREFIX + "prop.recipeOutput")
    public int recipeOutput;

    public BaseDrawerConfig(int baseStorage, int recipeOutput) {
        this.baseStorage = baseStorage;
        this.enabled = true;
        this.recipeOutput = recipeOutput;
    }

    public static class Controller {

        @Config.LangKey(LANG_PREFIX + "prop.enabled")
        public boolean enabled = true;
        @Config.LangKey(LANG_PREFIX + "prop.controllerRange")
        public int range = 12;
    }

    public static class Trim {

        @Config.LangKey(LANG_PREFIX + "prop.enabled")
        public boolean enabled = true;
        @Config.LangKey(LANG_PREFIX + "prop.recipeOutput")
        public int recipeOutput = 4;
    }

    public static class ControllerSlave {

        @Config.LangKey(LANG_PREFIX + "prop.enabled")
        public boolean enabled = true;
    }

    public static class FramedBlocks {

        @Config.LangKey(LANG_PREFIX + "framedBlocks.consumeDecorationItems")
        @Config.Comment("Changes whether items used for decoration in the Framing Table gets consumed. Leave true to consume items (default behaviour).")
        public boolean consumeDecorationItems = true;
        @Config.LangKey(LANG_PREFIX + "framedBlocks.enableFramedDrawers")
        public boolean enableFramedDrawers = true;
        @Config.LangKey(LANG_PREFIX + "framedBlocks.enableFramedTrims")
        public boolean enableFramedTrims = true;
        @Config.LangKey(LANG_PREFIX + "framedBlocks.enableFramingTable")
        public boolean enableFramingTable = true;
    }
}
