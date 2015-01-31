package mekanism.common;

import buildcraft.api.power.IPowerReceptor;
import ic2.api.Direction;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.api.ICableOutputter;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConnectionProvider;

public final class CableUtils
{
  public static TileEntity[] getConnectedEnergyAcceptors(TileEntity tileEntity)
  {
    TileEntity[] acceptors = { null, null, null, null, null, null };
    for (ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    {
      TileEntity acceptor = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.field_70331_k);
      if (((acceptor instanceof IStrictEnergyAcceptor)) || ((acceptor instanceof IEnergySink)) || (((acceptor instanceof IPowerReceptor)) && (!(acceptor instanceof IUniversalCable)) && (Mekanism.hooks.BuildCraftLoaded))) {
        acceptors[orientation.ordinal()] = acceptor;
      }
    }
    return acceptors;
  }
  
  public static TileEntity[] getConnectedCables(TileEntity tileEntity)
  {
    TileEntity[] cables = { null, null, null, null, null, null };
    for (ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    {
      TileEntity cable = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.field_70331_k);
      if ((cable instanceof IUniversalCable)) {
        cables[orientation.ordinal()] = cable;
      }
    }
    return cables;
  }
  
  public static boolean[] getConnections(TileEntity tileEntity)
  {
    boolean[] connectable = { false, false, false, false, false, false };
    
    TileEntity[] connectedAcceptors = getConnectedEnergyAcceptors(tileEntity);
    TileEntity[] connectedCables = getConnectedCables(tileEntity);
    TileEntity[] connectedOutputters = getConnectedOutputters(tileEntity);
    for (TileEntity tile : connectedAcceptors)
    {
      int side = Arrays.asList(connectedAcceptors).indexOf(tile);
      if (canConnectToAcceptor(ForgeDirection.getOrientation(side), tileEntity)) {
        connectable[side] = true;
      }
    }
    for (TileEntity tile : connectedOutputters) {
      if (tile != null)
      {
        int side = Arrays.asList(connectedOutputters).indexOf(tile);
        
        connectable[side] = true;
      }
    }
    for (TileEntity tile : connectedCables) {
      if (tile != null)
      {
        int side = Arrays.asList(connectedCables).indexOf(tile);
        
        connectable[side] = true;
      }
    }
    return connectable;
  }
  
  public static TileEntity[] getConnectedOutputters(TileEntity tileEntity)
  {
    TileEntity[] outputters = { null, null, null, null, null, null };
    for (ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    {
      TileEntity outputter = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.field_70331_k);
      if ((((outputter instanceof ICableOutputter)) && (((ICableOutputter)outputter).canOutputTo(orientation.getOpposite()))) || (((outputter instanceof IEnergySource)) && (((IEnergySource)outputter).emitsEnergyTo(tileEntity, MekanismUtils.toIC2Direction(orientation.getOpposite()))))) {
        outputters[orientation.ordinal()] = outputter;
      }
    }
    return outputters;
  }
  
  public static boolean canConnectToAcceptor(ForgeDirection side, TileEntity tile)
  {
    TileEntity tileEntity = Object3D.get(tile).getFromSide(side).getTileEntity(tile.field_70331_k);
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
    TileEntity pointer = Object3D.get(sender).getFromSide(facing).getTileEntity(sender.field_70331_k);
    if ((pointer instanceof IUniversalCable))
    {
      IUniversalCable cable = (IUniversalCable)pointer;
      
      ArrayList ignored = new ArrayList();
      ignored.add(sender);
      
      return cable.getNetwork().emit(amount, ignored);
    }
    return amount;
  }
  
  public static double emitEnergyFromAllSides(double amount, TileEntity pointer, ArrayList ignored)
  {
    if (pointer != null)
    {
      Set networks = new HashSet();
      double totalRemaining = 0.0D;
      
      ignored.add(pointer);
      for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
      {
        TileEntity sideTile = Object3D.get(pointer).getFromSide(side).getTileEntity(pointer.field_70331_k);
        if (((sideTile instanceof IUniversalCable)) && (!ignored.contains(sideTile))) {
          networks.add(((IUniversalCable)sideTile).getNetwork());
        }
      }
      if (networks.size() == 0) {
        return amount;
      }
      double remaining = amount % networks.size();
      double splitEnergy = (amount - remaining) / networks.size();
      for (EnergyNetwork network : networks)
      {
        totalRemaining += network.emit(splitEnergy + remaining, ignored);
        remaining = 0.0D;
      }
      return totalRemaining;
    }
    return amount;
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64_wire_fix.jar
 * Qualified Name:     mekanism.common.CableUtils
 * JD-Core Version:    0.7.0.1
 */