/*    */ package com.darkenchanter.stairdoors.main;
/*    */ 
/*    */ import com.darkenchanter.stairdoors.proxy.CommonProxy;
/*    */ import net.minecraftforge.fml.common.Mod;
/*    */ import net.minecraftforge.fml.common.Mod.EventHandler;
/*    */ import net.minecraftforge.fml.common.SidedProxy;
/*    */ import net.minecraftforge.fml.common.event.FMLInitializationEvent;
/*    */ import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
/*    */ import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
/*    */ import org.apache.logging.log4j.LogManager;
/*    */ import org.apache.logging.log4j.Logger;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @Mod(modid=Main.MODID, name="Stair Doors", version=Main.VERSION)
/*    */ public class Main
/*    */ {
/*    */   @SidedProxy(clientSide="com.darkenchanter.stairdoors.proxy.ClientProxy", serverSide="com.darkenchanter.stairdoors.proxy.ServerProxy")
/*    */   public static CommonProxy proxy;
/*    */   public static final String MODID = "stairdoors";
/*    */   public static final String VERSION = "1.0";
/* 23 */   public static final Logger logger = LogManager.getLogger("stairdoors");
/*    */   
/*    */   @Mod.EventHandler
/*    */   public void preInit(FMLPreInitializationEvent event)
/*    */   {
/* 28 */     proxy.preInit(event);
/*    */   }
/*    */   
/*    */   @Mod.EventHandler
/*    */   public void init(FMLInitializationEvent event)
/*    */   {
/* 34 */     proxy.init(event);
/*    */   }
/*    */   
/*    */   @Mod.EventHandler
/*    */   public void postInit(FMLPostInitializationEvent event)
/*    */   {
/* 40 */     proxy.postInit(event);
/*    */   }
/*    */ }


/* Location:              /home/greg/Documents/stairdoors.jar!/com/darkenchanter/stairdoors/main/Main.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */