package com.tencao;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = PHCore.MODID, name = PHCore.NAME, version = PHCore.VERSION)
@SideOnly(Side.CLIENT)
public class PHCore {
    public static final String MODID = "ProxyHelper";
    public static final String NAME = "Proxy Helper";
    public static final String VERSION = "0.3";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
    }
}
