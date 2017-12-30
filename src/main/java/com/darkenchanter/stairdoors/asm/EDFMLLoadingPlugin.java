/*    */ package com.darkenchanter.stairdoors.asm;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import java.util.Map;
/*    */ import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
/*    */ 
/*    */ public class EDFMLLoadingPlugin implements IFMLLoadingPlugin
/*    */ {
/*    */   public String[] getASMTransformerClass()
/*    */   {
/*    */     
/* 13 */     return new String[] { EDClassTransformer.class.getName() };
/*    */   }
/*    */   
/*    */ 
/*    */   public String getModContainerClass()
/*    */   {
/* 19 */     return null;
/*    */   }
/*    */   
/*    */ 
/*    */   public String getSetupClass()
/*    */   {
/* 25 */     return null;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */   public void injectData(Map<String, Object> data) {}
/*    */   
/*    */ 
/*    */ 
/*    */   public String getAccessTransformerClass()
/*    */   {
/* 37 */     return null;
/*    */   }
/*    */ }


/* Location:              /home/greg/Documents/stairdoors.jar!/com/darkenchanter/stairdoors/asm/EDFMLLoadingPlugin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */