package com.jaquadro.minecraft.storagedrawers;

import com.jaquadro.minecraft.storagedrawers.capabilities.CapabilityDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.capabilities.CapabilityDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.capabilities.CapabilityItemRepository;
import com.jaquadro.minecraft.storagedrawers.config.*;
import com.jaquadro.minecraft.storagedrawers.core.Api;
import com.jaquadro.minecraft.storagedrawers.core.CommandDebug;
import com.jaquadro.minecraft.storagedrawers.core.CommonProxy;
import com.jaquadro.minecraft.storagedrawers.core.handlers.GuiHandler;
import com.jaquadro.minecraft.storagedrawers.integration.LocalIntegrationRegistry;
import com.jaquadro.minecraft.storagedrawers.security.SecurityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

// This will stop erroring once you run the build task once. Or, if you really hate the error message and don't want to build, run `./gradlew injectTags`.
@Mod(modid = StorageDrawers.MOD_ID, name = StorageDrawers.MOD_NAME, version = Version.VERSION,
        dependencies = "required-after:forge@[14.21.0.2362,);required-after:chameleon;",
        acceptedMinecraftVersions = "[1.12,1.13)")
public class StorageDrawers {

    public static final String MOD_ID = "storagedrawers";
    public static final String MOD_NAME = "Storage Drawers";
    public static final String SOURCE_PATH = "com.jaquadro.minecraft.storagedrawers.";

    public static final Api api = new Api();

    public static Logger log;
    public static CompTierRegistry compRegistry;
    public static OreDictRegistry oreDictRegistry;

    public static RenderRegistry renderRegistry;
    public static WailaRegistry wailaRegistry;
    public static SecurityRegistry securityRegistry;

    @Mod.Instance(MOD_ID)
    public static StorageDrawers instance;

    @SidedProxy(clientSide = SOURCE_PATH + "core.ClientProxy", serverSide = SOURCE_PATH + "core.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();

        CapabilityDrawerGroup.register();
        CapabilityItemRepository.register();
        CapabilityDrawerAttributes.register();

        compRegistry = new CompTierRegistry();
        oreDictRegistry = new OreDictRegistry();
        renderRegistry = new RenderRegistry();
        wailaRegistry = new WailaRegistry();
        securityRegistry = new SecurityRegistry();

        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(proxy);
        MinecraftForge.EVENT_BUS.register(instance);

        LocalIntegrationRegistry.instance().init();
        compRegistry.initialize();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LocalIntegrationRegistry.instance().postInit();
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandDebug());
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        boolean preShiftValue = SDConfig.general.invertShift;
        if (event.getModID().equals(MOD_ID)) {
            net.minecraftforge.common.config.ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
            for (String rule : SDConfig.registries.compRules)
                StorageDrawers.compRegistry.register(rule);
            if (StorageDrawers.oreDictRegistry != null) {
                oreDictRegistry.whiteListOrePrefixes(SDConfig.registries.orePrefixWhitelist);
                oreDictRegistry.blackListMaterials(SDConfig.registries.materialBlacklist);
            }
        }
        if (event.isWorldRunning() && preShiftValue != SDConfig.general.invertShift) {
            PlayerConfig.setConfig(Minecraft.getMinecraft().player, SDConfig.general.invertShift, SDConfig.general.invertClick);
        }
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PlayerConfig.onPlayerDisconnect((EntityPlayerMP) event.player);
        }
    }

    public static boolean arePosEqual(Vec3i a, Vec3i b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }
}
