package com.darkenchanter.stairdoors.main;

import com.darkenchanter.stairdoors.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




@Mod(modid=Main.MODID, name="Stair Doors", version=Main.VERSION)
public class Main
{
	@SidedProxy(clientSide="com.darkenchanter.stairdoors.proxy.ClientProxy", serverSide="com.darkenchanter.stairdoors.proxy.ServerProxy")
	public static CommonProxy proxy;
	public static final String MODID = "stairdoors";
	public static final String VERSION = "1.0";
	public static final Logger logger = LogManager.getLogger("stairdoors");
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
	}
}