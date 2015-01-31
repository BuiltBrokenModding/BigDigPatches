package mekanism.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.Mod.ServerStopping;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.Recipes;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import mekanism.api.GasNetwork;
import mekanism.api.GasNetwork.GasTransferEvent;
import mekanism.api.GasTransmission;
import mekanism.api.InfuseObject;
import mekanism.api.InfuseRegistry;
import mekanism.api.InfuseType;
import mekanism.api.InfusionInput;
import mekanism.client.SoundHandler;
import mekanism.common.network.PacketConfiguratorState;
import mekanism.common.network.PacketControlPanel;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketDigitUpdate;
import mekanism.common.network.PacketElectricBowState;
import mekanism.common.network.PacketElectricChest;
import mekanism.common.network.PacketPortableTeleport;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.network.PacketRemoveUpgrade;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketStatusUpdate;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.network.PacketTime;
import mekanism.common.network.PacketTransmitterTransferUpdate;
import mekanism.common.network.PacketTransmitterTransferUpdate.TransmitterTransferType;
import mekanism.common.network.PacketWeather;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.EventBus;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rebelkeithy.mods.metallurgy.api.IMetalSet;
import rebelkeithy.mods.metallurgy.api.IOreInfo;
import rebelkeithy.mods.metallurgy.api.MetallurgyAPI;
import thermalexpansion.api.crafting.CraftingManagers;
import thermalexpansion.api.crafting.IPulverizerManager;

@Mod(modid="Mekanism", name="Mekanism", version="5.5.6")
@NetworkMod(channels={"MEK"}, clientSideRequired=true, serverSideRequired=false, packetHandler=PacketHandler.class)
public class Mekanism
{
  public static Logger logger = Logger.getLogger("Minecraft");
  @SidedProxy(clientSide="mekanism.client.ClientProxy", serverSide="mekanism.common.CommonProxy")
  public static CommonProxy proxy;
  public static boolean debug = false;
  @Mod.Instance("Mekanism")
  public static Mekanism instance;
  public static MekanismHooks hooks;
  public static Configuration configuration;
  public static Version versionNumber = new Version(5, 5, 6);
  public static Map teleporters = new HashMap();
  public static Map dynamicInventories = new HashMap();
  public static CreativeTabMekanism tabMekanism = new CreativeTabMekanism();
  public static List modulesLoaded = new ArrayList();
  public static String latestVersionNumber;
  public static String recentNews;
  @SideOnly(Side.CLIENT)
  public static SoundHandler audioHandler;
  public static List donators = new ArrayList();
  public static int basicBlockID = 3000;
  public static int machineBlockID = 3001;
  public static int oreBlockID = 3002;
  public static int obsidianTNTID = 3003;
  public static int energyCubeID = 3004;
  public static int boundingBlockID = 3005;
  public static int gasTankID = 3006;
  public static int transmitterID = 3007;
  public static ItemElectricBow ElectricBow;
  public static Item Stopwatch;
  public static Item WeatherOrb;
  public static Item EnrichedAlloy;
  public static ItemEnergized EnergyTablet;
  public static Item SpeedUpgrade;
  public static Item EnergyUpgrade;
  public static ItemRobit Robit;
  public static ItemAtomicDisassembler AtomicDisassembler;
  public static Item AtomicCore;
  public static ItemStorageTank StorageTank;
  public static Item ControlCircuit;
  public static Item EnrichedIron;
  public static Item CompressedCarbon;
  public static Item PortableTeleporter;
  public static Item TeleportationCore;
  public static Item Configurator;
  public static Item EnergyMeter;
  public static Block BasicBlock;
  public static Block MachineBlock;
  public static Block OreBlock;
  public static Block ObsidianTNT;
  public static Block EnergyCube;
  public static Block BoundingBlock;
  public static Block GasTank;
  public static Block Transmitter;
  public static Item Dust;
  public static Item Ingot;
  public static Item Clump;
  public static Item DirtyDust;
  public static boolean extrasEnabled = true;
  public static boolean osmiumGenerationEnabled = true;
  public static boolean disableBCBronzeCrafting = true;
  public static boolean disableBCSteelCrafting = true;
  public static boolean updateNotifications = true;
  public static boolean enableSounds = true;
  public static boolean fancyUniversalCableRender = true;
  public static boolean controlCircuitOreDict = true;
  public static boolean logPackets = false;
  public static boolean dynamicTankEasterEgg = false;
  public static int obsidianTNTBlastRadius = 12;
  public static int obsidianTNTDelay = 100;
  public static double TO_IC2;
  public static double TO_BC;
  public static double FROM_IC2;
  public static double FROM_BC;
  public static double ENERGY_PER_REDSTONE = 10000.0D;
  public static double enrichmentChamberUsage;
  public static double osmiumCompressorUsage;
  public static double combinerUsage;
  public static double crusherUsage;
  public static double theoreticalElementizerUsage;
  public static double factoryUsage;
  public static double metallurgicInfuserUsage;
  public static double purificationChamberUsage;
  public static double energizedSmelterUsage;
  public static int ticksPassed = 0;
  
  public void addRecipes()
  {
    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 3), new Object[] { "***", "***", "***", Character.valueOf('*'), Item.field_77705_m }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Item.field_77705_m, 9), new Object[] { "*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 3) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 2), new Object[] { "***", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Ingot, 9, 0), new Object[] { "*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 2) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 4), new Object[] { "***", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Ingot, 9, 3), new Object[] { "*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 4) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 0), new Object[] { "XXX", "XXX", "XXX", Character.valueOf('X'), "ingotOsmium" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Ingot, 9, 1), new Object[] { "*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 0) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 1), new Object[] { "***", "***", "***", Character.valueOf('*'), "ingotBronze" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Ingot, 9, 2), new Object[] { "*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 1) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 5), new Object[] { "***", "***", "***", Character.valueOf('*'), "ingotSteel" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Ingot, 9, 4), new Object[] { "*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 5) }));
    



    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(ObsidianTNT, 1), new Object[] { "***", "XXX", "***", Character.valueOf('*'), Block.field_72089_ap, Character.valueOf('X'), Block.field_72091_am }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(ElectricBow.getUnchargedItem(), new Object[] { " AB", "E B", " AB", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('B'), Item.field_77683_K, Character.valueOf('E'), EnergyTablet.getUnchargedItem() }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(EnergyTablet.getUnchargedItem(), new Object[] { "RCR", "ECE", "RCR", Character.valueOf('C'), Item.field_77717_p, Character.valueOf('R'), Item.field_77767_aC, Character.valueOf('E'), EnrichedAlloy }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 0), new Object[] { "ARA", "CIC", "ARA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('R'), Item.field_77767_aC, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('C'), ControlCircuit }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 1), new Object[] { "RCR", "GIG", "RCR", Character.valueOf('R'), Item.field_77767_aC, Character.valueOf('C'), "basicCircuit", Character.valueOf('G'), Block.field_71946_M, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 2), new Object[] { "SCS", "RIR", "SCS", Character.valueOf('S'), Block.field_71978_w, Character.valueOf('C'), "basicCircuit", Character.valueOf('R'), Item.field_77767_aC, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 3), new Object[] { "RLR", "CIC", "RLR", Character.valueOf('R'), Item.field_77767_aC, Character.valueOf('L'), Item.field_77775_ay, Character.valueOf('C'), "basicCircuit", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(SpeedUpgrade), new Object[] { " G ", "APA", " G ", Character.valueOf('P'), "dustOsmium", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), Block.field_71946_M }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(EnergyUpgrade), new Object[] { " G ", "ADA", " G ", Character.valueOf('G'), Block.field_71946_M, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), "dustGold" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(AtomicCore), new Object[] { "AOA", "PDP", "AOA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('O'), "dustObsidian", Character.valueOf('P'), "dustOsmium", Character.valueOf('D'), Item.field_77702_n }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(AtomicDisassembler.getUnchargedItem(), new Object[] { "AEA", "ACA", " O ", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotRefinedObsidian" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(StorageTank.getEmptyItem(), new Object[] { "III", "IDI", "III", Character.valueOf('I'), Item.field_77703_o, Character.valueOf('D'), "dustIron" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(GasTank), new Object[] { "PPP", "PDP", "PPP", Character.valueOf('P'), "ingotOsmium", Character.valueOf('D'), "dustIron" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.BASIC), new Object[] { "RLR", "TIT", "RLR", Character.valueOf('R'), Item.field_77767_aC, Character.valueOf('L'), new ItemStack(Item.field_77756_aW, 1, 4), Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.ADVANCED), new Object[] { "EGE", "TBT", "EGE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('G'), Item.field_77717_p, Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('B'), MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.BASIC) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.ELITE), new Object[] { "CDC", "TAT", "CDC", Character.valueOf('C'), "basicCircuit", Character.valueOf('D'), Item.field_77702_n, Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.ADVANCED) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.ULTIMATE), new Object[] { "COC", "TAT", "COC", Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.ELITE) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(ControlCircuit), new Object[] { " P ", "PEP", " P ", Character.valueOf('P'), "ingotOsmium", Character.valueOf('E'), EnrichedAlloy }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new ShapelessOreRecipe(new ItemStack(EnrichedIron, 2), new Object[] { Item.field_77767_aC, Item.field_77703_o }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(EnrichedIron, 4), new Object[] { "C", "I", "C", Character.valueOf('C'), "dustCopper", Character.valueOf('I'), Item.field_77703_o }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(EnrichedIron, 4), new Object[] { "T", "I", "T", Character.valueOf('T'), "dustTin", Character.valueOf('I'), Item.field_77703_o }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 8), new Object[] { "IFI", "CEC", "IFI", Character.valueOf('I'), Item.field_77703_o, Character.valueOf('F'), Block.field_72051_aB, Character.valueOf('C'), "basicCircuit", Character.valueOf('E'), EnrichedAlloy }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(TeleportationCore), new Object[] { "LAL", "GDG", "LAL", Character.valueOf('L'), new ItemStack(Item.field_77756_aW, 1, 4), Character.valueOf('A'), AtomicCore, Character.valueOf('G'), Item.field_77717_p, Character.valueOf('D'), Item.field_77702_n }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(PortableTeleporter), new Object[] { " E ", "CTC", " E ", Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), "basicCircuit", Character.valueOf('T'), TeleportationCore }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 11), new Object[] { "COC", "OTO", "COC", Character.valueOf('C'), "basicCircuit", Character.valueOf('O'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('T'), TeleportationCore }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 9), new Object[] { "CAC", "ERE", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), AtomicCore, Character.valueOf('E'), EnrichedAlloy, Character.valueOf('R'), new ItemStack(BasicBlock, 1, 8) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Configurator), new Object[] { " L ", "AEA", " S ", Character.valueOf('L'), new ItemStack(Item.field_77756_aW, 1, 4), Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('S'), Item.field_77669_D }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 9, 7), new Object[] { "OOO", "OGO", "OOO", Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('G'), "ingotRefinedGlowstone" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Transmitter, 8, 0), new Object[] { "SGS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), Block.field_71946_M }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 8), new Object[] { " S ", "SPS", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('P'), "ingotOsmium" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 10), new Object[] { "SCS", "GIG", "SCS", Character.valueOf('S'), Block.field_71978_w, Character.valueOf('C'), ControlCircuit, Character.valueOf('G'), Block.field_71946_M, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Transmitter, 8, 1), new Object[] { "SRS", Character.valueOf('S'), "ingotSteel", Character.valueOf('R'), Item.field_77767_aC }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 12), new Object[] { " B ", "ECE", "OOO", Character.valueOf('B'), Item.field_77788_aw, Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('O'), "ingotOsmium" }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 13), new Object[] { "SGS", "CcC", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), Block.field_71946_M, Character.valueOf('C'), Block.field_72077_au, Character.valueOf('c'), ControlCircuit }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(Transmitter, 8, 2), new Object[] { "SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Item.field_77788_aw }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 8, 9), new Object[] { " I ", "ISI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('S'), Block.field_71978_w }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 8, 10), new Object[] { " I ", "IGI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('G'), Block.field_71946_M }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(BasicBlock, 2, 11), new Object[] { " I ", "ICI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('C'), ControlCircuit }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 14), new Object[] { "PPP", "SES", Character.valueOf('P'), Block.field_72044_aK, Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), EnergyTablet.getUnchargedItem() }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(Robit.getUnchargedItem(), new Object[] { " S ", "ECE", "OIO", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('I'), new ItemStack(MachineBlock, 1, 13) }));
    



    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getFactory(Tier.FactoryTier.BASIC, IFactory.RecipeType.SMELTING), new Object[] { "CAC", "GOG", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "dustGold", Character.valueOf('O'), new ItemStack(MachineBlock, 1, 10) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getFactory(Tier.FactoryTier.BASIC, IFactory.RecipeType.ENRICHING), new Object[] { "CAC", "GOG", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "dustGold", Character.valueOf('O'), new ItemStack(MachineBlock, 1, 0) }));
    

    CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getFactory(Tier.FactoryTier.BASIC, IFactory.RecipeType.CRUSHING), new Object[] { "CAC", "GOG", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "dustGold", Character.valueOf('O'), new ItemStack(MachineBlock, 1, 3) }));
    for (IFactory.RecipeType type : IFactory.RecipeType.values())
    {
      CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getFactory(Tier.FactoryTier.ADVANCED, type), new Object[] { "CAC", "DOD", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), "dustDiamond", Character.valueOf('O'), MekanismUtils.getFactory(Tier.FactoryTier.BASIC, type) }));
      

      CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(MekanismUtils.getFactory(Tier.FactoryTier.ELITE, type), new Object[] { "CAC", "cOc", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('c'), AtomicCore, Character.valueOf('O'), MekanismUtils.getFactory(Tier.FactoryTier.ADVANCED, type) }));
    }
    if (extrasEnabled) {
      CraftingManager.func_77594_a().func_77592_b().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 4), new Object[] { "SGS", "GDG", "SGS", Character.valueOf('S'), EnrichedAlloy, Character.valueOf('G'), Block.field_71946_M, Character.valueOf('D'), Block.field_72071_ax }));
    }
    FurnaceRecipes.func_77602_a().addSmelting(oreBlockID, 0, new ItemStack(Ingot, 1, 1), 1.0F);
    FurnaceRecipes.func_77602_a().addSmelting(Dust.field_77779_bT, 2, new ItemStack(Ingot, 1, 1), 1.0F);
    FurnaceRecipes.func_77602_a().addSmelting(Dust.field_77779_bT, 0, new ItemStack(Item.field_77703_o), 1.0F);
    FurnaceRecipes.func_77602_a().addSmelting(Dust.field_77779_bT, 1, new ItemStack(Item.field_77717_p), 1.0F);
    FurnaceRecipes.func_77602_a().addSmelting(Dust.field_77779_bT, 5, new ItemStack(Ingot, 1, 4), 1.0F);
    FurnaceRecipes.func_77602_a().addSmelting(EnrichedIron.field_77779_bT, 0, new ItemStack(EnrichedAlloy), 1.0F);
    

    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_72047_aN), new ItemStack(Item.field_77767_aC, 12));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_72089_ap), new ItemStack(DirtyDust, 1, 6));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Item.field_77705_m, 1, 0), new ItemStack(CompressedCarbon));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Item.field_77705_m, 1, 1), new ItemStack(CompressedCarbon));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_71947_N), new ItemStack(Item.field_77756_aW, 12, 4));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_71950_I), new ItemStack(Item.field_77705_m, 2));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_72073_aw), new ItemStack(Item.field_77702_n, 2));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_72087_ao), new ItemStack(Block.field_71978_w));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_71981_t), new ItemStack(Block.field_72007_bm, 1, 2));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_72007_bm, 1, 2), new ItemStack(Block.field_72007_bm, 1, 0));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_72007_bm, 1, 0), new ItemStack(Block.field_72007_bm, 1, 3));
    RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_72007_bm, 1, 1), new ItemStack(Block.field_72007_bm, 1, 0));
    

    RecipeHandler.addCombinerRecipe(new ItemStack(Item.field_77767_aC, 16), new ItemStack(Block.field_72047_aN));
    RecipeHandler.addCombinerRecipe(new ItemStack(Item.field_77756_aW, 16, 4), new ItemStack(Block.field_71947_N));
    

    RecipeHandler.addOsmiumCompressorRecipe(new ItemStack(Item.field_77751_aT), new ItemStack(Ingot, 1, 3));
    

    RecipeHandler.addCrusherRecipe(new ItemStack(Item.field_77702_n), new ItemStack(Dust, 1, 4));
    RecipeHandler.addCrusherRecipe(new ItemStack(Item.field_77703_o), new ItemStack(Dust, 1, 0));
    RecipeHandler.addCrusherRecipe(new ItemStack(Item.field_77717_p), new ItemStack(Dust, 1, 1));
    RecipeHandler.addCrusherRecipe(new ItemStack(Block.field_71940_F), new ItemStack(Item.field_77804_ap));
    RecipeHandler.addCrusherRecipe(new ItemStack(Block.field_71981_t), new ItemStack(Block.field_71978_w));
    RecipeHandler.addCrusherRecipe(new ItemStack(Block.field_71978_w), new ItemStack(Block.field_71939_E));
    RecipeHandler.addCrusherRecipe(new ItemStack(Block.field_72007_bm, 1, 2), new ItemStack(Block.field_71981_t));
    RecipeHandler.addCrusherRecipe(new ItemStack(Block.field_72007_bm, 1, 0), new ItemStack(Block.field_72007_bm, 1, 2));
    RecipeHandler.addCrusherRecipe(new ItemStack(Block.field_72007_bm, 1, 3), new ItemStack(Block.field_72007_bm, 1, 0));
    

    RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("CARBON"), 10, new ItemStack(EnrichedIron)), new ItemStack(Dust, 1, 5));
    if (InfuseRegistry.contains("BIO"))
    {
      RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("BIO"), 10, new ItemStack(Block.field_71978_w)), new ItemStack(Block.field_72087_ao));
      RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("BIO"), 10, new ItemStack(Block.field_72007_bm, 1, 0)), new ItemStack(Block.field_72007_bm, 1, 1));
    }
    InfuseRegistry.registerInfuseObject(new ItemStack(Item.field_77705_m, 1, 0), new InfuseObject(InfuseRegistry.get("CARBON"), 10));
    InfuseRegistry.registerInfuseObject(new ItemStack(Item.field_77705_m, 1, 1), new InfuseObject(InfuseRegistry.get("CARBON"), 20));
    InfuseRegistry.registerInfuseObject(new ItemStack(CompressedCarbon), new InfuseObject(InfuseRegistry.get("CARBON"), 100));
  }
  
  public void addNames()
  {
    LanguageRegistry.addName(ElectricBow, "Electric Bow");
    LanguageRegistry.addName(ObsidianTNT, "Obsidian TNT");
    if (extrasEnabled == true)
    {
      LanguageRegistry.addName(Stopwatch, "Steve's Stopwatch");
      LanguageRegistry.addName(WeatherOrb, "Weather Orb");
    }
    LanguageRegistry.addName(EnrichedAlloy, "Enriched Alloy");
    LanguageRegistry.addName(EnergyTablet, "Energy Tablet");
    LanguageRegistry.addName(SpeedUpgrade, "Speed Upgrade");
    LanguageRegistry.addName(EnergyUpgrade, "Energy Upgrade");
    LanguageRegistry.addName(Robit, "Robit");
    LanguageRegistry.addName(AtomicDisassembler, "Atomic Disassembler");
    LanguageRegistry.addName(AtomicCore, "Atomic Core");
    LanguageRegistry.addName(ElectricBow, "Electric Bow");
    LanguageRegistry.addName(StorageTank, "Hydrogen Tank");
    LanguageRegistry.addName(BoundingBlock, "Bounding Block");
    LanguageRegistry.addName(GasTank, "Gas Tank");
    LanguageRegistry.addName(StorageTank, "Storage Tank");
    LanguageRegistry.addName(ControlCircuit, "Control Circuit");
    LanguageRegistry.addName(EnrichedIron, "Enriched Iron");
    LanguageRegistry.addName(CompressedCarbon, "Compressed Carbon");
    LanguageRegistry.addName(PortableTeleporter, "Portable Teleporter");
    LanguageRegistry.addName(TeleportationCore, "Teleportation Core");
    LanguageRegistry.addName(Configurator, "Configurator");
    LanguageRegistry.addName(EnergyMeter, "EnergyMeter");
    

    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.OsmiumBlock.name", "Osmium Block");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.BronzeBlock.name", "Bronze Block");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RefinedObsidian.name", "Refined Obsidian");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.CoalBlock.name", "Coal Block");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RefinedGlowstone.name", "Refined Glowstone");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.SteelBlock.name", "Steel Block");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.ControlPanel.name", "Control Panel");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.TeleporterFrame.name", "Teleporter Frame");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.SteelCasing.name", "Steel Casing");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.DynamicTank.name", "Dynamic Tank");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.DynamicGlass.name", "Dynamic Glass");
    LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.DynamicValve.name", "Dynamic Valve");
    

    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EnrichmentChamber.name", "Enrichment Chamber");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.OsmiumCompressor.name", "Osmium Compressor");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Combiner.name", "Combiner");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Crusher.name", "Crusher");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.TheoreticalElementizer.name", "Theoretical Elementizer");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.BasicFactory.name", "Basic Factory");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.AdvancedFactory.name", "Advanced Factory");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EliteFactory.name", "Elite Factory");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.MetallurgicInfuser.name", "Metallurgic Infuser");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.PurificationChamber.name", "Purification Chamber");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EnergizedSmelter.name", "Energized Smelter");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Teleporter.name", "Teleporter");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.ElectricPump.name", "Electric Pump");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.ElectricChest.name", "Electric Chest");
    LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Chargepad.name", "Chargepad");
    

    LanguageRegistry.instance().addStringLocalization("tile.OreBlock.OsmiumOre.name", "Osmium Ore");
    

    LanguageRegistry.instance().addStringLocalization("tile.Transmitter.PressurizedTube.name", "Pressurized Tube");
    LanguageRegistry.instance().addStringLocalization("tile.Transmitter.UniversalCable.name", "Universal Cable");
    LanguageRegistry.instance().addStringLocalization("tile.Transmitter.MechanicalPipe.name", "Mechanical Pipe");
    

    LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Basic.name", "Basic Energy Cube");
    LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Advanced.name", "Advanced Energy Cube");
    LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Elite.name", "Elite Energy Cube");
    LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Ultimate.name", "Ultimate Energy Cube");
    

    LanguageRegistry.instance().addStringLocalization("item.ironDust.name", "Iron Dust");
    LanguageRegistry.instance().addStringLocalization("item.goldDust.name", "Gold Dust");
    LanguageRegistry.instance().addStringLocalization("item.osmiumDust.name", "Osmium Dust");
    LanguageRegistry.instance().addStringLocalization("item.obsidianDust.name", "Refined Obsidian Dust");
    LanguageRegistry.instance().addStringLocalization("item.diamondDust.name", "Diamond Dust");
    LanguageRegistry.instance().addStringLocalization("item.steelDust.name", "Steel Dust");
    LanguageRegistry.instance().addStringLocalization("item.copperDust.name", "Copper Dust");
    LanguageRegistry.instance().addStringLocalization("item.tinDust.name", "Tin Dust");
    LanguageRegistry.instance().addStringLocalization("item.silverDust.name", "Silver Dust");
    

    LanguageRegistry.instance().addStringLocalization("item.ironClump.name", "Iron Clump");
    LanguageRegistry.instance().addStringLocalization("item.goldClump.name", "Gold Clump");
    LanguageRegistry.instance().addStringLocalization("item.osmiumClump.name", "Osmium Clump");
    LanguageRegistry.instance().addStringLocalization("item.copperClump.name", "Copper Clump");
    LanguageRegistry.instance().addStringLocalization("item.tinClump.name", "Tin Clump");
    LanguageRegistry.instance().addStringLocalization("item.silverClump.name", "Silver Clump");
    

    LanguageRegistry.instance().addStringLocalization("item.dirtyIronDust.name", "Dirty Iron Dust");
    LanguageRegistry.instance().addStringLocalization("item.dirtyGoldDust.name", "Dirty Gold Dust");
    LanguageRegistry.instance().addStringLocalization("item.dirtyOsmiumDust.name", "Dirty Osmium Dust");
    LanguageRegistry.instance().addStringLocalization("item.dirtyCopperDust.name", "Dirty Copper Dust");
    LanguageRegistry.instance().addStringLocalization("item.dirtyTinDust.name", "Dirty Tin Dust");
    LanguageRegistry.instance().addStringLocalization("item.dirtySilverDust.name", "Dirty Silver Dust");
    LanguageRegistry.instance().addStringLocalization("item.dirtyObsidianDust.name", "Dirty Obsidian Dust");
    

    LanguageRegistry.instance().addStringLocalization("item.obsidianIngot.name", "Obsidian Ingot");
    LanguageRegistry.instance().addStringLocalization("item.osmiumIngot.name", "Osmium Ingot");
    LanguageRegistry.instance().addStringLocalization("item.bronzeIngot.name", "Bronze Ingot");
    LanguageRegistry.instance().addStringLocalization("item.glowstoneIngot.name", "Glowstone Ingot");
    LanguageRegistry.instance().addStringLocalization("item.steelIngot.name", "Steel Ingot");
    

    LanguageRegistry.instance().addStringLocalization("itemGroup.tabMekanism", "Mekanism");
  }
  
  public void addItems()
  {
    configuration.load();
    ElectricBow = (ItemElectricBow)new ItemElectricBow(configuration.getItem("ElectricBow", 11200).getInt()).func_77655_b("ElectricBow");
    if (extrasEnabled == true)
    {
      Stopwatch = new ItemStopwatch(configuration.getItem("Stopwatch", 11202).getInt()).func_77655_b("Stopwatch");
      WeatherOrb = new ItemWeatherOrb(configuration.getItem("WeatherOrb", 11203).getInt()).func_77655_b("WeatherOrb");
    }
    Dust = new ItemDust(configuration.getItem("Dust", 11204).getInt() - 256);
    Ingot = new ItemIngot(configuration.getItem("Ingot", 11205).getInt() - 256);
    EnergyTablet = (ItemEnergized)new ItemEnergized(configuration.getItem("EnergyTablet", 11206).getInt(), 1000000.0D, 120.0D).func_77655_b("EnergyTablet");
    SpeedUpgrade = new ItemMachineUpgrade(configuration.getItem("SpeedUpgrade", 11207).getInt(), 0, 150).func_77655_b("SpeedUpgrade");
    EnergyUpgrade = new ItemMachineUpgrade(configuration.getItem("EnergyUpgrade", 11208).getInt(), 1000, 0).func_77655_b("EnergyUpgrade");
    Robit = (ItemRobit)new ItemRobit(configuration.getItem("Robit", 11209).getInt()).func_77655_b("Robit");
    AtomicDisassembler = (ItemAtomicDisassembler)new ItemAtomicDisassembler(configuration.getItem("AtomicDisassembler", 11210).getInt()).func_77655_b("AtomicDisassembler");
    AtomicCore = new ItemMekanism(configuration.getItem("AtomicCore", 11211).getInt()).func_77655_b("AtomicCore");
    EnrichedAlloy = new ItemMekanism(configuration.getItem("EnrichedAlloy", 11212).getInt()).func_77655_b("EnrichedAlloy");
    StorageTank = (ItemStorageTank)new ItemStorageTank(configuration.getItem("StorageTank", 11213).getInt(), 1600, 16).func_77655_b("StorageTank");
    ControlCircuit = new ItemMekanism(configuration.getItem("ControlCircuit", 11214).getInt()).func_77655_b("ControlCircuit");
    EnrichedIron = new ItemMekanism(configuration.getItem("EnrichedIron", 11215).getInt()).func_77655_b("EnrichedIron");
    CompressedCarbon = new ItemMekanism(configuration.getItem("CompressedCarbon", 11216).getInt()).func_77655_b("CompressedCarbon");
    PortableTeleporter = new ItemPortableTeleporter(configuration.getItem("PortableTeleporter", 11217).getInt()).func_77655_b("PortableTeleporter");
    TeleportationCore = new ItemMekanism(configuration.getItem("TeleportationCore", 11218).getInt()).func_77655_b("TeleportationCore");
    Clump = new ItemClump(configuration.getItem("Clump", 11219).getInt() - 256);
    DirtyDust = new ItemDirtyDust(configuration.getItem("DirtyDust", 11220).getInt() - 256);
    Configurator = new ItemConfigurator(configuration.getItem("Configurator", 11221).getInt()).func_77655_b("Configurator");
    EnergyMeter = new ItemEnergyMeter(configuration.getItem("EnergyMeter", 11222).getInt()).func_77655_b("EnergyMeter");
    configuration.save();
    

    GameRegistry.registerItem(ElectricBow, "ElectricBow");
    if (extrasEnabled)
    {
      GameRegistry.registerItem(Stopwatch, "Stopwatch");
      GameRegistry.registerItem(WeatherOrb, "WeatherOrb");
    }
    GameRegistry.registerItem(Dust, "Dust");
    GameRegistry.registerItem(Ingot, "Ingot");
    GameRegistry.registerItem(EnergyTablet, "EnergyTablet");
    GameRegistry.registerItem(SpeedUpgrade, "SpeedUpgrade");
    GameRegistry.registerItem(EnergyUpgrade, "EnergyUpgrade");
    GameRegistry.registerItem(Robit, "Robit");
    GameRegistry.registerItem(AtomicDisassembler, "AtomicDisassembler");
    GameRegistry.registerItem(AtomicCore, "AtomicCore");
    GameRegistry.registerItem(EnrichedAlloy, "EnrichedAlloy");
    GameRegistry.registerItem(StorageTank, "StorageTank");
    GameRegistry.registerItem(ControlCircuit, "ControlCircuit");
    GameRegistry.registerItem(EnrichedIron, "EnrichedIron");
    GameRegistry.registerItem(CompressedCarbon, "CompressedCarbon");
    GameRegistry.registerItem(PortableTeleporter, "PortableTeleporter");
    GameRegistry.registerItem(TeleportationCore, "TeleportationCore");
    GameRegistry.registerItem(Clump, "Clump");
    GameRegistry.registerItem(DirtyDust, "DirtyDust");
    GameRegistry.registerItem(Configurator, "Configurator");
    GameRegistry.registerItem(EnergyMeter, "EnergyMeter");
  }
  
  public void addBlocks()
  {
    BasicBlock = new BlockBasic(basicBlockID).func_71864_b("BasicBlock");
    MachineBlock = new BlockMachine(machineBlockID).func_71864_b("MachineBlock");
    OreBlock = new BlockOre(oreBlockID).func_71864_b("OreBlock");
    EnergyCube = new BlockEnergyCube(energyCubeID).func_71864_b("EnergyCube");
    ObsidianTNT = new BlockObsidianTNT(obsidianTNTID).func_71864_b("ObsidianTNT").func_71849_a(tabMekanism);
    BoundingBlock = (BlockBounding)new BlockBounding(boundingBlockID).func_71864_b("BoundingBlock");
    GasTank = new BlockGasTank(gasTankID).func_71864_b("GasTank");
    Transmitter = new BlockTransmitter(transmitterID).func_71864_b("Transmitter");
    

    GameRegistry.registerBlock(ObsidianTNT, "ObsidianTNT");
    GameRegistry.registerBlock(BoundingBlock, "BoundingBlock");
    GameRegistry.registerBlock(GasTank, "GasTank");
    

    Item.field_77698_e[basicBlockID] = new ItemBlockBasic(basicBlockID - 256, BasicBlock).func_77655_b("BasicBlock");
    Item.field_77698_e[machineBlockID] = new ItemBlockMachine(machineBlockID - 256, MachineBlock).func_77655_b("MachineBlock");
    Item.field_77698_e[oreBlockID] = new ItemBlockOre(oreBlockID - 256, OreBlock).func_77655_b("OreBlock");
    Item.field_77698_e[energyCubeID] = new ItemBlockEnergyCube(energyCubeID - 256, EnergyCube).func_77655_b("EnergyCube");
    Item.field_77698_e[transmitterID] = new ItemBlockTransmitter(transmitterID - 256, Transmitter).func_77655_b("Transmitter");
  }
  
  public void addIntegratedItems()
  {
    OreDictionary.registerOre("dustIron", new ItemStack(Dust, 1, 0));
    OreDictionary.registerOre("dustGold", new ItemStack(Dust, 1, 1));
    OreDictionary.registerOre("dustOsmium", new ItemStack(Dust, 1, 2));
    OreDictionary.registerOre("dustRefinedObsidian", new ItemStack(Dust, 1, 3));
    OreDictionary.registerOre("dustDiamond", new ItemStack(Dust, 1, 4));
    OreDictionary.registerOre("dustSteel", new ItemStack(Dust, 1, 5));
    OreDictionary.registerOre("dustCopper", new ItemStack(Dust, 1, 6));
    OreDictionary.registerOre("dustTin", new ItemStack(Dust, 1, 7));
    OreDictionary.registerOre("dustSilver", new ItemStack(Dust, 1, 8));
    
    OreDictionary.registerOre("ingotRefinedObsidian", new ItemStack(Ingot, 1, 0));
    OreDictionary.registerOre("ingotOsmium", new ItemStack(Ingot, 1, 1));
    OreDictionary.registerOre("ingotBronze", new ItemStack(Ingot, 1, 2));
    OreDictionary.registerOre("ingotRefinedGlowstone", new ItemStack(Ingot, 1, 3));
    OreDictionary.registerOre("ingotSteel", new ItemStack(Ingot, 1, 4));
    
    OreDictionary.registerOre("blockOsmium", new ItemStack(BasicBlock, 1, 0));
    OreDictionary.registerOre("blockBronze", new ItemStack(BasicBlock, 1, 1));
    OreDictionary.registerOre("blockRefinedObsidian", new ItemStack(BasicBlock, 1, 2));
    OreDictionary.registerOre("blockCoal", new ItemStack(BasicBlock, 1, 3));
    OreDictionary.registerOre("blockRefinedGlowstone", new ItemStack(BasicBlock, 1, 4));
    OreDictionary.registerOre("blockSteel", new ItemStack(BasicBlock, 1, 5));
    
    OreDictionary.registerOre("dustDirtyIron", new ItemStack(DirtyDust, 1, 0));
    OreDictionary.registerOre("dustDirtyGold", new ItemStack(DirtyDust, 1, 1));
    OreDictionary.registerOre("dustDirtyOsmium", new ItemStack(DirtyDust, 1, 2));
    OreDictionary.registerOre("dustDirtyCopper", new ItemStack(DirtyDust, 1, 3));
    OreDictionary.registerOre("dustDirtyTin", new ItemStack(DirtyDust, 1, 4));
    OreDictionary.registerOre("dustDirtySilver", new ItemStack(DirtyDust, 1, 5));
    OreDictionary.registerOre("dustDirtyObsidian", new ItemStack(DirtyDust, 1, 6));
    

    OreDictionary.registerOre("dustObsidian", new ItemStack(DirtyDust, 1, 6));
    
    OreDictionary.registerOre("clumpIron", new ItemStack(Clump, 1, 0));
    OreDictionary.registerOre("clumpGold", new ItemStack(Clump, 1, 1));
    OreDictionary.registerOre("clumpOsmium", new ItemStack(Clump, 1, 2));
    OreDictionary.registerOre("clumpCopper", new ItemStack(Clump, 1, 3));
    OreDictionary.registerOre("clumpTin", new ItemStack(Clump, 1, 4));
    OreDictionary.registerOre("clumpSilver", new ItemStack(Clump, 1, 5));
    
    OreDictionary.registerOre("oreOsmium", new ItemStack(OreBlock, 1, 0));
    try
    {
      CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(OreBlock, 1, 0), new ItemStack(Dust, 2, 2), false);
      
      CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Ingot, 1, 1), new ItemStack(Dust, 1, 2), false);
      CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Ingot, 1, 0), new ItemStack(Dust, 1, 3), false);
      CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Ingot, 1, 3), new ItemStack(Item.field_77751_aT), false);
      CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Ingot, 1, 4), new ItemStack(Dust, 1, 5), false);
      
      CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 0), new ItemStack(DirtyDust, 1, 0), false);
      CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 1), new ItemStack(DirtyDust, 1, 1), false);
      CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 2), new ItemStack(DirtyDust, 1, 2), false);
      CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 3), new ItemStack(DirtyDust, 1, 3), false);
      CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 4), new ItemStack(DirtyDust, 1, 4), false);
      CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 5), new ItemStack(DirtyDust, 1, 5), false);
      System.out.println("[Mekanism] Hooked into Thermal Expansion successfully.");
    }
    catch (Exception e) {}
    if (controlCircuitOreDict) {
      OreDictionary.registerOre("basicCircuit", new ItemStack(ControlCircuit));
    }
    OreDictionary.registerOre("itemCompressedCarbon", new ItemStack(CompressedCarbon));
    OreDictionary.registerOre("itemEnrichedAlloy", new ItemStack(EnrichedAlloy));
    if (hooks.IC2Loaded) {
      if (!hooks.RailcraftLoaded) {
        Recipes.macerator.addRecipe(new ItemStack(Block.field_72089_ap), new ItemStack(DirtyDust, 1, 6));
      }
    }
    for (ItemStack ore : OreDictionary.getOres("dustRefinedObsidian"))
    {
      RecipeHandler.addOsmiumCompressorRecipe(MekanismUtils.size(ore, 1), new ItemStack(Ingot, 1, 0));
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(DirtyDust, 1, 6));
    }
    for (ItemStack ore : OreDictionary.getOres("clumpIron")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(DirtyDust, 1, 0));
    }
    for (ItemStack ore : OreDictionary.getOres("clumpGold")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(DirtyDust, 1, 1));
    }
    for (ItemStack ore : OreDictionary.getOres("clumpOsmium")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(DirtyDust, 1, 2));
    }
    for (ItemStack ore : OreDictionary.getOres("clumpCopper")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(DirtyDust, 1, 3));
    }
    for (ItemStack ore : OreDictionary.getOres("clumpTin")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(DirtyDust, 1, 4));
    }
    for (ItemStack ore : OreDictionary.getOres("clumpSilver")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(DirtyDust, 1, 5));
    }
    for (ItemStack ore : OreDictionary.getOres("dustDirtyIron")) {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 0));
    }
    for (ItemStack ore : OreDictionary.getOres("dustDirtyGold")) {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 1));
    }
    for (ItemStack ore : OreDictionary.getOres("dustDirtyOsmium")) {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 2));
    }
    for (ItemStack ore : OreDictionary.getOres("dustDirtyCopper")) {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 6));
    }
    for (ItemStack ore : OreDictionary.getOres("dustDirtyTin")) {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 7));
    }
    for (ItemStack ore : OreDictionary.getOres("dustDirtySilver")) {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 8));
    }
    for (ItemStack ore : OreDictionary.getOres("oreCopper"))
    {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 2, 6));
      RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Clump, 3, 3));
    }
    for (ItemStack ore : OreDictionary.getOres("oreTin"))
    {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 2, 7));
      RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Clump, 3, 4));
    }
    for (ItemStack ore : OreDictionary.getOres("oreOsmium"))
    {
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 2, 2));
      RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Clump, 3, 2));
    }
    for (ItemStack ore : OreDictionary.getOres("oreIron"))
    {
      RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_71949_H), new ItemStack(Dust, 2, 0));
      RecipeHandler.addPurificationChamberRecipe(new ItemStack(Block.field_71949_H), new ItemStack(Clump, 3, 0));
    }
    for (ItemStack ore : OreDictionary.getOres("oreGold"))
    {
      RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.field_71941_G), new ItemStack(Dust, 2, 1));
      RecipeHandler.addPurificationChamberRecipe(new ItemStack(Block.field_71941_G), new ItemStack(Clump, 3, 1));
    }
    try
    {
      for (ItemStack ore : OreDictionary.getOres("oreLead")) {
        RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), MekanismUtils.size((ItemStack)OreDictionary.getOres("dustLead").get(0), 2));
      }
      for (ItemStack ore : OreDictionary.getOres("ingotLead")) {
        RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), MekanismUtils.size((ItemStack)OreDictionary.getOres("dustLead").get(0), 1));
      }
    }
    catch (Exception e) {}
    try
    {
      for (ItemStack ore : OreDictionary.getOres("oreSilver"))
      {
        RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 2, 8));
        RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Clump, 3, 5));
      }
      for (ItemStack ore : OreDictionary.getOres("ingotSilver")) {
        RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 8));
      }
    }
    catch (Exception e) {}
    for (ItemStack ore : OreDictionary.getOres("ingotRefinedObsidian")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 3));
    }
    for (ItemStack ore : OreDictionary.getOres("ingotOsmium")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 2));
    }
    for (ItemStack ore : OreDictionary.getOres("ingotRedstone")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Item.field_77767_aC));
    }
    for (ItemStack ore : OreDictionary.getOres("ingotRefinedGlowstone")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Item.field_77751_aT));
    }
    try
    {
      RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 2), MekanismUtils.size((ItemStack)OreDictionary.getOres("dustBronze").get(0), 1));
      if (hooks.IC2Loaded) {
        Recipes.macerator.addRecipe(new ItemStack(Ingot, 1, 2), MekanismUtils.size((ItemStack)OreDictionary.getOres("dustBronze").get(0), 1));
      }
      if (hooks.TELoaded) {
        CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Ingot, 1, 2), MekanismUtils.size((ItemStack)OreDictionary.getOres("dustBronze").get(0), 1), false);
      }
    }
    catch (Exception e) {}
    try
    {
      FurnaceRecipes.func_77602_a().addSmelting(Dust.field_77779_bT, 6, MekanismUtils.size((ItemStack)OreDictionary.getOres("ingotCopper").get(0), 1), 1.0F);
    }
    catch (Exception e) {}
    try
    {
      FurnaceRecipes.func_77602_a().addSmelting(Dust.field_77779_bT, 7, MekanismUtils.size((ItemStack)OreDictionary.getOres("ingotTin").get(0), 1), 1.0F);
    }
    catch (Exception e) {}
    try
    {
      FurnaceRecipes.func_77602_a().addSmelting(Dust.field_77779_bT, 8, MekanismUtils.size((ItemStack)OreDictionary.getOres("ingotSilver").get(0), 1), 1.0F);
    }
    catch (Exception e) {}
    try
    {
      RecipeHandler.addCrusherRecipe(new ItemStack(Item.field_77705_m), MekanismUtils.size((ItemStack)OreDictionary.getOres("dustCoal").get(0), 1));
    }
    catch (Exception e) {}
    try
    {
      RecipeHandler.addCrusherRecipe(new ItemStack(Item.field_77705_m, 1, 1), MekanismUtils.size((ItemStack)OreDictionary.getOres("dustCharcoal").get(0), 1));
    }
    catch (Exception e) {}
    try
    {
      for (ItemStack ore : OreDictionary.getOres("ingotCopper")) {
        RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 6));
      }
    }
    catch (Exception e) {}
    try
    {
      for (ItemStack ore : OreDictionary.getOres("ingotTin")) {
        RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 7));
      }
    }
    catch (Exception e) {}
    try
    {
      for (ItemStack ore : OreDictionary.getOres("ingotSilver")) {
        RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 8));
      }
    }
    catch (Exception e) {}
    for (ItemStack ore : OreDictionary.getOres("dustIron")) {
      RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Block.field_71949_H));
    }
    for (ItemStack ore : OreDictionary.getOres("ingotSteel")) {
      RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Dust, 1, 5));
    }
    for (ItemStack ore : OreDictionary.getOres("dustGold")) {
      RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Block.field_71941_G));
    }
    for (ItemStack ore : OreDictionary.getOres("dustObsidian"))
    {
      RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 1), new ItemStack(Block.field_72089_ap));
      RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("DIAMOND"), 10, MekanismUtils.size(ore, 1)), new ItemStack(Dust, 1, 3));
    }
    for (ItemStack ore : OreDictionary.getOres("dustOsmium")) {
      RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(OreBlock, 1, 0));
    }
    for (ItemStack ore : OreDictionary.getOres("dustDiamond"))
    {
      InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("DIAMOND"), 80));
      RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Item.field_77702_n));
    }
    try
    {
      for (ItemStack ore : OreDictionary.getOres("dustCopper")) {
        RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), MekanismUtils.size((ItemStack)OreDictionary.getOres("oreCopper").get(0), 1));
      }
    }
    catch (Exception e) {}
    try
    {
      for (ItemStack ore : OreDictionary.getOres("ingotCopper")) {
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("TIN"), 10, MekanismUtils.size(ore, 1)), new ItemStack(Ingot, 1, 2));
      }
    }
    catch (Exception e) {}
    try
    {
      for (ItemStack ore : OreDictionary.getOres("dustTin"))
      {
        RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), MekanismUtils.size((ItemStack)OreDictionary.getOres("oreTin").get(0), 1));
        InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("TIN"), 50));
      }
    }
    catch (Exception e) {}
    try
    {
      for (ItemStack ore : OreDictionary.getOres("dustLead")) {
        RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), MekanismUtils.size((ItemStack)OreDictionary.getOres("oreLead").get(0), 1));
      }
    }
    catch (Exception e) {}
    try
    {
      for (ItemStack ore : OreDictionary.getOres("dustSilver")) {
        RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), MekanismUtils.size((ItemStack)OreDictionary.getOres("oreSilver").get(0), 1));
      }
    }
    catch (Exception e) {}
    if (hooks.MetallurgyCoreLoaded) {
      try
      {
        String[] setNames = { "base", "precious", "nether", "fantasy", "ender", "utility" };
        for (String setName : setNames) {
          for (IOreInfo oreInfo : MetallurgyAPI.getMetalSet(setName).getOreList().values()) {
            switch (1.$SwitchMap$rebelkeithy$mods$metallurgy$api$OreType[oreInfo.getType().ordinal()])
            {
            case 1: 
              if ((oreInfo.getIngot() != null) && (oreInfo.getDust() != null)) {
                RecipeHandler.addCrusherRecipe(MekanismUtils.size(oreInfo.getIngot(), 1), MekanismUtils.size(oreInfo.getDust(), 1));
              }
              break;
            case 2: 
              ItemStack ore = oreInfo.getOre();
              ItemStack drop = oreInfo.getDrop();
              if ((drop != null) && (ore != null)) {
                RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), MekanismUtils.size(drop, 12));
              }
              break;
            default: 
              ItemStack ore = oreInfo.getOre();
              ItemStack dust = oreInfo.getDust();
              ItemStack ingot = oreInfo.getIngot();
              if ((ore != null) && (dust != null))
              {
                RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), MekanismUtils.size(dust, 2));
                RecipeHandler.addCombinerRecipe(MekanismUtils.size(dust, 8), MekanismUtils.size(ore, 1));
              }
              if ((ingot != null) && (dust != null)) {
                RecipeHandler.addCrusherRecipe(MekanismUtils.size(ingot, 1), MekanismUtils.size(dust, 1));
              }
              break;
            }
          }
        }
      }
      catch (Exception e) {}
    }
  }
  
  public void addEntities()
  {
    EntityRegistry.registerGlobalEntityID(EntityObsidianTNT.class, "ObsidianTNT", EntityRegistry.findGlobalUniqueEntityId());
    EntityRegistry.registerGlobalEntityID(EntityRobit.class, "Robit", EntityRegistry.findGlobalUniqueEntityId());
    

    EntityRegistry.registerModEntity(EntityObsidianTNT.class, "ObsidianTNT", 0, this, 40, 5, true);
    EntityRegistry.registerModEntity(EntityRobit.class, "Robit", 1, this, 40, 2, true);
    

    GameRegistry.registerTileEntity(TileEntityEnergyCube.class, "EnergyCube");
    GameRegistry.registerTileEntity(TileEntityBoundingBlock.class, "BoundingBlock");
    GameRegistry.registerTileEntity(TileEntityControlPanel.class, "ControlPanel");
    GameRegistry.registerTileEntity(TileEntityGasTank.class, "GasTank");
    GameRegistry.registerTileEntity(TileEntityTeleporter.class, "MekanismTeleporter");
    

    proxy.registerSpecialTileEntities();
  }
  
  @Mod.ServerStarting
  public void serverStarting(FMLServerStartingEvent event)
  {
    event.registerServerCommand(new CommandMekanism());
  }
  
  @Mod.ServerStopping
  public void serverStopping(FMLServerStoppingEvent event)
  {
    teleporters.clear();
    dynamicInventories.clear();
  }
  
  @Mod.PreInit
  public void preInit(FMLPreInitializationEvent event)
  {
    File config = event.getSuggestedConfigurationFile();
    

    configuration = new Configuration(config);
    if (config.getAbsolutePath().contains("voltz")) {
      System.out.println("[Mekanism] Detected Voltz in root directory - hello, fellow user!");
    } else if (config.getAbsolutePath().contains("tekkit")) {
      System.out.println("[Mekanism] Detected Tekkit in root directory - hello, fellow user!");
    }
    InfuseRegistry.registerInfuseType(new InfuseType("CARBON", "/mods/mekanism/infuse/Infusions.png", 0, 0));
    InfuseRegistry.registerInfuseType(new InfuseType("TIN", "/mods/mekanism/infuse/Infusions.png", 4, 0));
    InfuseRegistry.registerInfuseType(new InfuseType("DIAMOND", "/mods/mekanism/infuse/Infusions.png", 8, 0));
  }
  
  @Mod.PostInit
  public void postInit(FMLPostInitializationEvent event)
  {
    hooks = new MekanismHooks();
    hooks.hook();
    
    addIntegratedItems();
    
    System.out.println("[Mekanism] Hooking complete.");
    
    proxy.loadSoundHandler();
  }
  
  @Mod.Init
  public void init(FMLInitializationEvent event)
  {
    GameRegistry.registerWorldGenerator(new OreHandler());
    

    NetworkRegistry.instance().registerGuiHandler(this, new CoreGuiHandler());
    

    System.out.println("[Mekanism] Version " + versionNumber + " initializing...");
    

    new ThreadGetData();
    

    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(new IC2EnergyHandler());
    MinecraftForge.EVENT_BUS.register(new EnergyNetwork.NetworkLoader());
    MinecraftForge.EVENT_BUS.register(new LiquidNetwork.NetworkLoader());
    

    GasTransmission.register();
    

    proxy.loadConfiguration();
    

    addItems();
    addBlocks();
    addNames();
    addRecipes();
    addEntities();
    

    PacketHandler.registerPacket(PacketRobit.class);
    PacketHandler.registerPacket(PacketTransmitterTransferUpdate.class);
    PacketHandler.registerPacket(PacketTime.class);
    PacketHandler.registerPacket(PacketWeather.class);
    PacketHandler.registerPacket(PacketElectricChest.class);
    PacketHandler.registerPacket(PacketElectricBowState.class);
    PacketHandler.registerPacket(PacketConfiguratorState.class);
    PacketHandler.registerPacket(PacketControlPanel.class);
    PacketHandler.registerPacket(PacketTileEntity.class);
    PacketHandler.registerPacket(PacketPortalFX.class);
    PacketHandler.registerPacket(PacketDataRequest.class);
    PacketHandler.registerPacket(PacketStatusUpdate.class);
    PacketHandler.registerPacket(PacketDigitUpdate.class);
    PacketHandler.registerPacket(PacketPortableTeleport.class);
    PacketHandler.registerPacket(PacketRemoveUpgrade.class);
    

    donators.add("mrgreaper");
    

    proxy.registerRenderInformation();
    proxy.loadUtilities();
    

    System.out.println("[Mekanism] Loading complete.");
    

    logger.info("[Mekanism] Mod loaded.");
  }
  
  @ForgeSubscribe
  public void onGasTransferred(GasNetwork.GasTransferEvent event)
  {
    try
    {
      PacketHandler.sendPacket(PacketHandler.Transmission.ALL_CLIENTS, new PacketTransmitterTransferUpdate().setParams(new Object[] { PacketTransmitterTransferUpdate.TransmitterTransferType.GAS, event.gasNetwork.tubes.iterator().next(), event.transferType }), new Object[0]);
    }
    catch (Exception e) {}
  }
  
  @ForgeSubscribe
  public void onLiquidTransferred(LiquidNetwork.LiquidTransferEvent event)
  {
    try
    {
      PacketHandler.sendPacket(PacketHandler.Transmission.ALL_CLIENTS, new PacketTransmitterTransferUpdate().setParams(new Object[] { PacketTransmitterTransferUpdate.TransmitterTransferType.LIQUID, event.liquidNetwork.pipes.iterator().next(), event.liquidSent }), new Object[0]);
    }
    catch (Exception e) {}
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64_wire_fix.jar
 * Qualified Name:     mekanism.common.Mekanism
 * JD-Core Version:    0.7.0.1
 */