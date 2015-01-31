package mekanism.common;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import mekanism.api.IUniversalCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

public class TileEntityUniversalCable
  extends TileEntity
  implements IUniversalCable, IPowerReceptor
{
  public CablePowerProvider powerProvider;
  public float energyScale;
  
  public TileEntityUniversalCable()
  {
    if (PowerFramework.currentFramework != null)
    {
      this.powerProvider = new CablePowerProvider(this);
      this.powerProvider.configure(0, 0, 100, 0, 100);
    }
  }
  
  public void func_70316_g()
  {
    if (this.field_70331_k.field_72995_K) {
      if (this.energyScale > 0.0F) {
        this.energyScale = ((float)(this.energyScale - 0.01D));
      }
    }
  }
  
  public boolean canTransferEnergy(TileEntity fromTile)
  {
    return this.field_70331_k.func_94577_B(this.field_70329_l, this.field_70330_m, this.field_70327_n) == 0;
  }
  
  public void onTransfer()
  {
    this.energyScale = Math.min(1.0F, this.energyScale + 0.02F);
  }
  
  public boolean canUpdate()
  {
    return true;
  }
  
  public void setPowerProvider(IPowerProvider provider) {}
  
  public IPowerProvider getPowerProvider()
  {
    return this.powerProvider;
  }
  
  public void doWork() {}
  
  public int powerRequest(ForgeDirection from)
  {
    ArrayList ignored = new ArrayList();
    ignored.add(VectorHelper.getTileEntityFromSide(this.field_70331_k, new Vector3(this.field_70329_l, this.field_70330_m, this.field_70327_n), from));
    return canTransferEnergy(VectorHelper.getTileEntityFromSide(this.field_70331_k, new Vector3(this.field_70329_l, this.field_70330_m, this.field_70327_n), from)) ? (int)Math.min(100.0D, new EnergyTransferProtocol(this, this, ignored).neededEnergy()) : 0;
  }
  
  @SideOnly(Side.CLIENT)
  public AxisAlignedBB getRenderBoundingBox()
  {
    return INFINITE_EXTENT_AABB;
  }
}


/* Location:           C:\Users\robert\Dropbox\Public\work\zCarl\Mekanism-v5.5.6.64_modified.jar
 * Qualified Name:     mekanism.common.TileEntityUniversalCable
 * JD-Core Version:    0.7.0.1
 */