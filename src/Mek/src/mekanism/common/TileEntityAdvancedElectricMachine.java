package mekanism.common;

import com.google.common.io.ByteArrayDataInput;
import dan200.computer.api.IComputerAccess;
import ic2.api.item.IElectricItem;
import java.io.PrintStream;
import java.util.ArrayList;
import mekanism.api.EnumColor;
import mekanism.api.EnumGas;
import mekanism.api.IStorageTank;
import mekanism.api.SideData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.IItemElectric;

public abstract class TileEntityAdvancedElectricMachine
  extends TileEntityBasicMachine
{
  public int SECONDARY_ENERGY_PER_TICK;
  public int MAX_SECONDARY_ENERGY;
  public int secondaryEnergyStored = 0;
  
  public TileEntityAdvancedElectricMachine(String soundPath, String name, String path, int perTick, int secondaryPerTick, int ticksRequired, double maxEnergy, int maxSecondaryEnergy)
  {
    super(soundPath, name, path, perTick, ticksRequired, maxEnergy);
    
    this.sideOutputs.add(new SideData(EnumColor.GREY, 0, 0, new int[0]));
    this.sideOutputs.add(new SideData(EnumColor.DARK_RED, 0, 1, new int[] { 0 }));
    this.sideOutputs.add(new SideData(EnumColor.PURPLE, 1, 1, new int[] { 1 }));
    this.sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 2, 1, new int[] { 2 }));
    this.sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 3, 1, new int[] { 3 }));
    this.sideOutputs.add(new SideData(EnumColor.ORANGE, 4, 1, new int[] { 4 }));
    
    this.sideConfig = new byte[] { 2, 1, 0, 4, 5, 3 };
    
    this.inventory = new ItemStack[5];
    this.SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
    this.MAX_SECONDARY_ENERGY = maxSecondaryEnergy;
  }
  
  public abstract int getFuelTicks(ItemStack paramItemStack);
  
  public void onUpdate()
  {
    super.onUpdate();
    if (!this.field_70331_k.field_72995_K)
    {
      ChargeUtils.discharge(3, this);
      if (this.inventory[4] != null)
      {
        if ((this.inventory[4].func_77969_a(new ItemStack(Mekanism.EnergyUpgrade))) && (this.energyMultiplier < 8))
        {
          if (this.upgradeTicks < this.UPGRADE_TICKS_REQUIRED)
          {
            this.upgradeTicks += 1;
          }
          else if (this.upgradeTicks == this.UPGRADE_TICKS_REQUIRED)
          {
            this.upgradeTicks = 0;
            this.energyMultiplier += 1;
            
            this.inventory[4].field_77994_a -= 1;
            if (this.inventory[4].field_77994_a == 0) {
              this.inventory[4] = null;
            }
          }
        }
        else if ((this.inventory[4].func_77969_a(new ItemStack(Mekanism.SpeedUpgrade))) && (this.speedMultiplier < 8))
        {
          if (this.upgradeTicks < this.UPGRADE_TICKS_REQUIRED)
          {
            this.upgradeTicks += 1;
          }
          else if (this.upgradeTicks == this.UPGRADE_TICKS_REQUIRED)
          {
            this.upgradeTicks = 0;
            this.speedMultiplier += 1;
            
            this.inventory[4].field_77994_a -= 1;
            if (this.inventory[4].field_77994_a == 0) {
              this.inventory[4] = null;
            }
          }
        }
        else {
          this.upgradeTicks = 0;
        }
      }
      else {
        this.upgradeTicks = 0;
      }
      handleSecondaryFuel();
      if ((this.electricityStored >= this.ENERGY_PER_TICK) && (this.secondaryEnergyStored >= this.SECONDARY_ENERGY_PER_TICK)) {
        if ((canOperate()) && (this.operatingTicks + 1 < MekanismUtils.getTicks(this.speedMultiplier, this.TICKS_REQUIRED)) && (this.secondaryEnergyStored >= this.SECONDARY_ENERGY_PER_TICK))
        {
          this.operatingTicks += 1;
          this.secondaryEnergyStored -= this.SECONDARY_ENERGY_PER_TICK;
          this.electricityStored -= this.ENERGY_PER_TICK;
        }
        else if (this.operatingTicks + 1 >= MekanismUtils.getTicks(this.speedMultiplier, this.TICKS_REQUIRED))
        {
          operate();
          
          this.operatingTicks = 0;
          this.secondaryEnergyStored -= this.SECONDARY_ENERGY_PER_TICK;
          this.electricityStored -= this.ENERGY_PER_TICK;
        }
      }
      if (!canOperate()) {
        this.operatingTicks = 0;
      }
      if ((canOperate()) && (this.electricityStored >= this.ENERGY_PER_TICK) && (this.secondaryEnergyStored >= this.SECONDARY_ENERGY_PER_TICK)) {
        setActive(true);
      } else {
        setActive(false);
      }
    }
  }
  
  public void handleSecondaryFuel()
  {
    if (this.inventory[1] != null)
    {
      int fuelTicks = getFuelTicks(this.inventory[1]);
      int energyNeeded = this.MAX_SECONDARY_ENERGY - this.secondaryEnergyStored;
      if ((fuelTicks > 0) && (fuelTicks <= energyNeeded))
      {
        if (fuelTicks <= energyNeeded) {
          setSecondaryEnergy(this.secondaryEnergyStored + fuelTicks);
        } else if (fuelTicks > energyNeeded) {
          setSecondaryEnergy(this.secondaryEnergyStored + energyNeeded);
        }
        this.inventory[1].field_77994_a -= 1;
        if (this.inventory[1].field_77994_a == 0) {
          this.inventory[1] = null;
        }
      }
    }
  }
  
  public boolean func_94041_b(int slotID, ItemStack itemstack)
  {
    if (slotID == 2) {
      return false;
    }
    if (slotID == 4) {
      return (itemstack.field_77993_c == Mekanism.SpeedUpgrade.field_77779_bT) || (itemstack.field_77993_c == Mekanism.EnergyUpgrade.field_77779_bT);
    }
    if (slotID == 0) {
      return RecipeHandler.getOutput(itemstack, false, getRecipes()) != null;
    }
    if (slotID == 3) {
      return (((itemstack.func_77973_b() instanceof IElectricItem)) && (((IElectricItem)itemstack.func_77973_b()).canProvideEnergy(itemstack))) || (((itemstack.func_77973_b() instanceof IItemElectric)) && (((IItemElectric)itemstack.func_77973_b()).getProvideRequest(itemstack).amperes != 0.0D)) || (itemstack.field_77993_c == Item.field_77767_aC.field_77779_bT);
    }
    if (slotID == 1) {
      return (getFuelTicks(itemstack) > 0) || (((this instanceof TileEntityPurificationChamber)) && ((itemstack.func_77973_b() instanceof IStorageTank)) && (((IStorageTank)itemstack.func_77973_b()).getGasType(itemstack) == EnumGas.OXYGEN));
    }
    return true;
  }
  
  public void operate()
  {
    ItemStack itemstack = RecipeHandler.getOutput(this.inventory[0], true, getRecipes());
    if (this.inventory[0] == null || this.inventory[0].field_77994_a <= 0) {
      this.inventory[0] = null;
    }
    if (this.inventory[2] == null) {
      this.inventory[2] = itemstack;
    } else {
      this.inventory[2].field_77994_a += itemstack.field_77994_a;
    }
  }
  
  public boolean canOperate()
  {
    if (this.inventory[0] == null) {
      return false;
    }
    ItemStack itemstack = RecipeHandler.getOutput(this.inventory[0], false, getRecipes());
    if (itemstack == null) {
      return false;
    }
    if (this.inventory[2] == null) {
      return true;
    }
    if (!this.inventory[2].func_77969_a(itemstack)) {
      return false;
    }
    return this.inventory[2].field_77994_a + itemstack.field_77994_a <= this.inventory[2].func_77976_d();
  }
  
  public void handlePacketData(ByteArrayDataInput dataStream)
  {
    super.handlePacketData(dataStream);
    this.secondaryEnergyStored = dataStream.readInt();
  }
  
  public ArrayList getNetworkedData(ArrayList data)
  {
    super.getNetworkedData(data);
    data.add(Integer.valueOf(this.secondaryEnergyStored));
    return data;
  }
  
  public void func_70307_a(NBTTagCompound nbtTags)
  {
    super.func_70307_a(nbtTags);
    
    this.secondaryEnergyStored = nbtTags.func_74762_e("secondaryEnergyStored");
  }
  
  public void func_70310_b(NBTTagCompound nbtTags)
  {
    super.func_70310_b(nbtTags);
    
    nbtTags.func_74768_a("secondaryEnergyStored", this.secondaryEnergyStored);
  }
  
  public void setSecondaryEnergy(int energy)
  {
    this.secondaryEnergyStored = Math.max(Math.min(energy, this.MAX_SECONDARY_ENERGY), 0);
  }
  
  public int getScaledSecondaryEnergyLevel(int i)
  {
    return this.secondaryEnergyStored * i / this.MAX_SECONDARY_ENERGY;
  }
  
  public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
  {
    if (slotID == 3) {
      return (((itemstack.func_77973_b() instanceof IItemElectric)) && (((IItemElectric)itemstack.func_77973_b()).getProvideRequest(itemstack).getWatts() == 0.0D)) || (((itemstack.func_77973_b() instanceof IElectricItem)) && (((IElectricItem)itemstack.func_77973_b()).canProvideEnergy(itemstack)) && ((!(itemstack.func_77973_b() instanceof IItemElectric)) || (((IItemElectric)itemstack.func_77973_b()).getProvideRequest(itemstack).getWatts() == 0.0D)));
    }
    if (slotID == 2) {
      return true;
    }
    return false;
  }
  
  public String[] getMethodNames()
  {
    return new String[] { "getStored", "getSecondaryStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded" };
  }
  
  public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments)
    throws Exception
  {
    switch (method)
    {
    case 0: 
      return new Object[] { Double.valueOf(this.electricityStored) };
    case 1: 
      return new Object[] { Integer.valueOf(this.secondaryEnergyStored) };
    case 2: 
      return new Object[] { Integer.valueOf(this.operatingTicks) };
    case 3: 
      return new Object[] { Boolean.valueOf(this.isActive) };
    case 4: 
      return new Object[] { Integer.valueOf(this.facing) };
    case 5: 
      return new Object[] { Boolean.valueOf(canOperate()) };
    case 6: 
      return new Object[] { Double.valueOf(MekanismUtils.getEnergy(this.energyMultiplier, this.MAX_ELECTRICITY)) };
    case 7: 
      return new Object[] { Double.valueOf(MekanismUtils.getEnergy(this.energyMultiplier, this.MAX_ELECTRICITY) - this.electricityStored) };
    }
    System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
    return new Object[] { "Unknown command." };
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64.jar
 * Qualified Name:     mekanism.common.TileEntityAdvancedElectricMachine
 * JD-Core Version:    0.7.0.1
 */