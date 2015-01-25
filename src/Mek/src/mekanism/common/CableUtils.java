package mekanism.common;

import buildcraft.api.power.IPowerReceptor;
import ic2.api.Direction;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import java.util.ArrayList;
import mekanism.api.ICableOutputter;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.IUniversalCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConnectionProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

public final class CableUtils
{
  public static TileEntity[] getConnectedEnergyAcceptors(TileEntity tileEntity)
  {
    TileEntity[] acceptors = { null, null, null, null, null, null };
    for (ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    {
    	 //Fixed: Accessing tiles out side the world bounds
    	if(orientation != ForgeDirection.UP || tileEntity.field_70330_m + 1 < 256)
    	{
	      TileEntity acceptor = VectorHelper.getTileEntityFromSide(tileEntity.field_70331_k, new Vector3(tileEntity.field_70329_l, tileEntity.field_70330_m, tileEntity.field_70327_n), orientation);
	      if (((acceptor instanceof IStrictEnergyAcceptor)) || ((acceptor instanceof IEnergySink)) || (((acceptor instanceof IPowerReceptor)) && (!(acceptor instanceof IUniversalCable)) && (Mekanism.hooks.BuildCraftLoaded))) {
	        acceptors[orientation.ordinal()] = acceptor;
      	}
      }
    }
    return acceptors;
  }
  
  public static TileEntity[] getConnectedCables(TileEntity tileEntity)
  {
    TileEntity[] cables = { null, null, null, null, null, null };
    for (ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    {
    	 //Fixed: Accessing tiles out side the world bounds
    	if(orientation != ForgeDirection.UP || tileEntity.field_70330_m + 1 < 256)
    	{
	      TileEntity cable = VectorHelper.getTileEntityFromSide(tileEntity.field_70331_k, new Vector3(tileEntity.field_70329_l, tileEntity.field_70330_m, tileEntity.field_70327_n), orientation);
	      if (((cable instanceof IUniversalCable)) && (((IUniversalCable)cable).canTransferEnergy(tileEntity))) {
	        cables[orientation.ordinal()] = cable;
	      }
    	}
    }
    return cables;
  }
  
  public static TileEntity[] getConnectedOutputters(TileEntity tileEntity)
  {
    TileEntity[] outputters = { null, null, null, null, null, null };
    for (ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    {
    	 //Fixed: Accessing tiles out side the world bounds
    	if(orientation != ForgeDirection.UP || tileEntity.field_70330_m + 1 < 256)
    	{
	      TileEntity outputter = VectorHelper.getTileEntityFromSide(tileEntity.field_70331_k, new Vector3(tileEntity.field_70329_l, tileEntity.field_70330_m, tileEntity.field_70327_n), orientation);
	      if ((((outputter instanceof ICableOutputter)) && (((ICableOutputter)outputter).canOutputTo(orientation.getOpposite()))) || (((outputter instanceof IEnergySource)) && (((IEnergySource)outputter).emitsEnergyTo(tileEntity, MekanismUtils.toIC2Direction(orientation.getOpposite()))))) {
	        outputters[orientation.ordinal()] = outputter;
	      }
    	}
    }
    return outputters;
  }
  
  public static boolean canConnectToAcceptor(ForgeDirection side, TileEntity tile)
  {
    TileEntity tileEntity = VectorHelper.getTileEntityFromSide(tile.field_70331_k, new Vector3(tile.field_70329_l, tile.field_70330_m, tile.field_70327_n), side);
    if (((tileEntity instanceof IStrictEnergyAcceptor)) && (((IStrictEnergyAcceptor)tileEntity).canReceiveEnergy(side.getOpposite()))) {
      return true;
    }
    if (((tileEntity instanceof IConnectionProvider)) && (((IConnectionProvider)tileEntity).canConnect(side.getOpposite()))) {
      return true;
    }
    if (((tileEntity instanceof IEnergyAcceptor)) && (((IEnergyAcceptor)tileEntity).acceptsEnergyFrom(tile, MekanismUtils.toIC2Direction(side).getInverse()))) {
      return true;
    }
    if (((tileEntity instanceof ICableOutputter)) && (((ICableOutputter)tileEntity).canOutputTo(side.getOpposite()))) {
      return true;
    }
    if (((tileEntity instanceof IPowerReceptor)) && (!(tileEntity instanceof IUniversalCable)) && (Mekanism.hooks.BuildCraftLoaded)) {
      if ((!(tileEntity instanceof IEnergyAcceptor)) || (((IEnergyAcceptor)tileEntity).acceptsEnergyFrom(null, MekanismUtils.toIC2Direction(side).getInverse()))) {
        if ((!(tileEntity instanceof IEnergySource)) || (((IEnergySource)tileEntity).emitsEnergyTo(null, MekanismUtils.toIC2Direction(side).getInverse()))) {
          return true;
        }
      }
    }
    return false;
  }
  
  public static double emitEnergyToNetwork(double amount, TileEntity sender, ForgeDirection facing)
  {
	  //Fixed: Accessing tiles out side the world bounds
	if(facing != ForgeDirection.UP || sender.field_70330_m + 1 < 256)
  	{
	    TileEntity pointer = VectorHelper.getTileEntityFromSide(sender.field_70331_k, new Vector3(sender.field_70329_l, sender.field_70330_m, sender.field_70327_n), facing);
	    if ((pointer instanceof IUniversalCable)) {
	      return new EnergyTransferProtocol(pointer, sender, amount, new ArrayList()).calculate();
	    }
  	}
    return amount;
  }
  
  public static double emitEnergyFromAllSides(double amount, TileEntity pointer)
  {
    if (pointer != null) {
      return new EnergyTransferProtocol(pointer, pointer, amount, new ArrayList()).calculate();
    }
    return amount;
  }
  
  public static double emitEnergyFromAllSidesIgnore(double amount, TileEntity pointer, ArrayList ignored)
  {
    if (pointer != null) {
      return new EnergyTransferProtocol(pointer, pointer, amount, ignored).calculate();
    }
    return amount;
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64.jar
 * Qualified Name:     mekanism.common.CableUtils
 * JD-Core Version:    0.7.0.1
 */