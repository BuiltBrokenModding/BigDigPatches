package mekanism.common;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityUniversalCable
  extends TileEntity
  implements IUniversalCable, IPowerReceptor
{
  public CablePowerProvider powerProvider;
  public EnergyNetwork energyNetwork;
  
  public TileEntityUniversalCable()
  {
    this.powerProvider = new CablePowerProvider(this);
    this.powerProvider.configure(0, 0, 100, 0, 100);
  }
  
  public boolean canUpdate()
  {
    return false;
  }
  
  public EnergyNetwork getNetwork()
  {
    if (this.energyNetwork == null) {
      this.energyNetwork = new EnergyNetwork(new IUniversalCable[] { this });
    }
    return this.energyNetwork;
  }
  
  public void func_70313_j()
  {
    if (!this.field_70331_k.field_72995_K) {
      getNetwork().split(this);
    }
    super.func_70313_j();
  }
  
  public void setNetwork(EnergyNetwork network)
  {
    this.energyNetwork = network;
  }
  
  public void refreshNetwork()
  {
    if (!this.field_70331_k.field_72995_K)
    {
      for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
      {
        TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(this.field_70331_k);
        if ((tileEntity instanceof IUniversalCable)) {
          getNetwork().merge(((IUniversalCable)tileEntity).getNetwork());
        }
      }
      getNetwork().refresh();
    }
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
    ignored.add(Object3D.get(this).getFromSide(from).getTileEntity(this.field_70331_k));
    return (int)Math.min(100.0D, getNetwork().getEnergyNeeded(ignored) * Mekanism.TO_BC);
  }
  
  @SideOnly(Side.CLIENT)
  public AxisAlignedBB getRenderBoundingBox()
  {
    return INFINITE_EXTENT_AABB;
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64_wire_fix.jar
 * Qualified Name:     mekanism.common.TileEntityUniversalCable
 * JD-Core Version:    0.7.0.1
 */