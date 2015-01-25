package mekanism.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryElectricChest
  extends InventoryBasic
{
  public EntityPlayer entityPlayer;
  public boolean reading;
  
  public InventoryElectricChest(EntityPlayer player)
  {
    super("Electric Chest", false, 55);
    this.entityPlayer = player;
    
    read();
  }
  
  public void func_70296_d()
  {
    super.func_70296_d();
    if (!this.reading) {
      write();
    }
  }
  
  public void func_70295_k_()
  {
    read();
    ((IElectricChest)getItemStack().func_77973_b()).setOpen(getItemStack(), true);
  }
  
  public void func_70305_f()
  {
    write();
    ((IElectricChest)getItemStack().func_77973_b()).setOpen(getItemStack(), false);
  }
  
  public void write()
  {
	  //Fixed: Class cast exception, original code had no if statement
	  if(getItemStack() != null && getItemStack().func_77973_b() instanceof ISustainedInventory)
	  {
	    NBTTagList tagList = new NBTTagList();
	    for (int slotCount = 0; slotCount < func_70302_i_(); slotCount++) 
	    {
	      if (func_70301_a(slotCount) != null)
	      {
	        NBTTagCompound tagCompound = new NBTTagCompound();
	        tagCompound.func_74774_a("Slot", (byte)slotCount);
	        func_70301_a(slotCount).func_77955_b(tagCompound);
	        tagList.func_74742_a(tagCompound);
	      }
	    }
	    ((ISustainedInventory)getItemStack().func_77973_b()).setInventory(tagList, new Object[] { getItemStack() });
	  }
    }
  
  public void read()
  {
    this.reading = true;
    
    if(getItemStack() != null && getItemStack().func_77973_b() instanceof ISustainedInventory)
    {
	    NBTTagList tagList = ((ISustainedInventory)getItemStack().func_77973_b()).getInventory(new Object[] { getItemStack() });
	    for (int tagCount = 0; tagCount < tagList.func_74745_c(); tagCount++)
	    {
	      NBTTagCompound tagCompound = (NBTTagCompound)tagList.func_74743_b(tagCount);
	      byte slotID = tagCompound.func_74771_c("Slot");
	      if ((slotID >= 0) && (slotID < func_70302_i_())) {
	        func_70299_a(slotID, ItemStack.func_77949_a(tagCompound));
	      }
	    }
    }
    this.reading = false;
  }
  
  public ItemStack getItemStack()
  {
    return this.entityPlayer.func_71045_bC();
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64.jar
 * Qualified Name:     mekanism.common.InventoryElectricChest
 * JD-Core Version:    0.7.0.1
 */