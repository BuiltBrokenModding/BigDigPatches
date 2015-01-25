package mekanism.common;

import com.google.common.io.ByteArrayDataInput;
import ic2.api.item.IElectricItem;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.IItemElectric;

public class TileEntityElectricChest
  extends TileEntityElectricBlock
{
  public String password = "";
  public boolean authenticated = false;
  public boolean locked = false;
  public float lidAngle;
  public float prevLidAngle;
  
  public TileEntityElectricChest()
  {
    super("Electric Chest", 12000.0D);
    this.inventory = new ItemStack[55];
  }
  
  public void onUpdate()
  {
    super.onUpdate();
    
    this.prevLidAngle = this.lidAngle;
    float increment = 0.1F;
    if ((this.playersUsing.size() > 0) && (this.lidAngle == 0.0F)) {
      this.field_70331_k.func_72908_a(this.field_70329_l + 0.5F, this.field_70330_m + 0.5D, this.field_70327_n + 0.5F, "random.chestopen", 0.5F, this.field_70331_k.field_73012_v.nextFloat() * 0.1F + 0.9F);
    }
    if (((this.playersUsing.size() == 0) && (this.lidAngle > 0.0F)) || ((this.playersUsing.size() > 0) && (this.lidAngle < 1.0F)))
    {
      float angle = this.lidAngle;
      if (this.playersUsing.size() > 0) {
        this.lidAngle += increment;
      } else {
        this.lidAngle -= increment;
      }
      if (this.lidAngle > 1.0F) {
        this.lidAngle = 1.0F;
      }
      float split = 0.5F;
      if ((this.lidAngle < split) && (angle >= split)) {
        this.field_70331_k.func_72908_a(this.field_70329_l + 0.5D, this.field_70330_m + 0.5D, this.field_70327_n + 0.5D, "random.chestclosed", 0.5F, this.field_70331_k.field_73012_v.nextFloat() * 0.1F + 0.9F);
      }
      if (this.lidAngle < 0.0F) {
        this.lidAngle = 0.0F;
      }
    }
    ChargeUtils.discharge(54, this);
  }
  
  public void func_70307_a(NBTTagCompound nbtTags)
  {
    super.func_70307_a(nbtTags);
    
    this.authenticated = nbtTags.func_74767_n("authenticated");
    this.locked = nbtTags.func_74767_n("locked");
    this.password = nbtTags.func_74779_i("password");
  }
  
  public void func_70310_b(NBTTagCompound nbtTags)
  {
    super.func_70310_b(nbtTags);
    
    nbtTags.func_74757_a("authenticated", this.authenticated);
    nbtTags.func_74757_a("locked", this.locked);
    if(password != null)
    	nbtTags.func_74778_a("password", this.password);
  }
  
  public void handlePacketData(ByteArrayDataInput dataStream)
  {
    super.handlePacketData(dataStream);
    this.authenticated = dataStream.readBoolean();
    this.locked = dataStream.readBoolean();
    this.password = dataStream.readUTF();
  }
  
  public ArrayList getNetworkedData(ArrayList data)
  {
    super.getNetworkedData(data);
    data.add(Boolean.valueOf(this.authenticated));
    data.add(Boolean.valueOf(this.locked));
    data.add(this.password);
    return data;
  }
  
  public boolean canAccess()
  {
    return (this.authenticated) && ((getEnergy() == 0.0D) || (!this.locked));
  }
  
  public boolean func_94041_b(int slotID, ItemStack itemstack)
  {
    if (slotID == 54) {
      return (((itemstack.func_77973_b() instanceof IElectricItem)) && (((IElectricItem)itemstack.func_77973_b()).canProvideEnergy(itemstack))) || (((itemstack.func_77973_b() instanceof IItemElectric)) && (((IItemElectric)itemstack.func_77973_b()).getProvideRequest(itemstack).amperes != 0.0D)) || (itemstack.field_77993_c == Item.field_77767_aC.field_77779_bT);
    }
    return true;
  }
  
  public int getScaledEnergyLevel(int i)
  {
    return (int)(this.electricityStored * i / this.MAX_ELECTRICITY);
  }
  
  //Fixed: Index out of bounds exception
  /*
   * java.lang.ArrayIndexOutOfBoundsException: 54
	 at mekanism.common.TileEntityElectricChest.func_94128_d(TileEntityElectricChest.java:155)
   */
  static int[] ret;
  public int[] func_94128_d(int side)
  {
    if (side == 0) {
      return new int[] { 1 };
    }
    if(ret == null) //Added if statement and cached slots as a static value, should increase performance
    {
    	ret = new int[55];    
	    for (int i = 0; i < 55 /* original, i <= 54 */; i++) {
	      ret[i] = i;
	    }
    }
    return ret;
  }
  
  public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
  {
    if (slotID == 54) {
      return (((itemstack.func_77973_b() instanceof IItemElectric)) && (((IItemElectric)itemstack.func_77973_b()).getProvideRequest(itemstack).getWatts() == 0.0D)) || (((itemstack.func_77973_b() instanceof IElectricItem)) && (((IElectricItem)itemstack.func_77973_b()).canProvideEnergy(itemstack)) && ((!(itemstack.func_77973_b() instanceof IItemElectric)) || (((IItemElectric)itemstack.func_77973_b()).getProvideRequest(itemstack).getWatts() == 0.0D)));
    }
    return true;
  }
  
  public int getStartInventorySide(ForgeDirection side)
  {
    if (side == ForgeDirection.DOWN) {
      return 54;
    }
    return 0;
  }
  
  public int getSizeInventorySide(ForgeDirection side)
  {
    if (side == ForgeDirection.DOWN) {
      return 1;
    }
    return 54;
  }
  
  public boolean wrenchCanRemove(EntityPlayer entityPlayer)
  {
    return false;
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64.jar
 * Qualified Name:     mekanism.common.TileEntityElectricChest
 * JD-Core Version:    0.7.0.1
 */