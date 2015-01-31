package mekanism.common;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import ic2.api.Direction;
import ic2.api.energy.tile.IEnergySink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent.Load;

public class EnergyNetwork
{
  public Set cables = new HashSet();
  public Set possibleAcceptors = new HashSet();
  public Map acceptorDirections = new HashMap();
  private double joulesTransmitted = 0.0D;
  private double joulesLastTick = 0.0D;
  private double energyStored = 0.0D;
  private double maxEnergy = 1000.0D;
  
  public EnergyNetwork(IUniversalCable... varCables)
  {
    this.cables.addAll(Arrays.asList(varCables));
    EnergyNetworkRegistry.getInstance().registerNetwork(this);
  }
  
  public void update()
  {
    if ((this.cables.size() > 0) && (this.energyStored > 0.0D)) {
      this.energyStored = emit();
    }
  }
  
  public double getEnergyNeeded(ArrayList ignored)
  {
    double totalNeeded = 0.0D;
    for (TileEntity acceptor : getEnergyAcceptors()) {
      if (!ignored.contains(acceptor)) {
        if ((acceptor instanceof IStrictEnergyAcceptor)) {
          totalNeeded += ((IStrictEnergyAcceptor)acceptor).getMaxEnergy() - ((IStrictEnergyAcceptor)acceptor).getEnergy();
        } else if ((acceptor instanceof IEnergySink)) {
          totalNeeded += Math.min(((IEnergySink)acceptor).demandsEnergy() * Mekanism.FROM_IC2, ((IEnergySink)acceptor).getMaxSafeInput() * Mekanism.FROM_IC2);
        } else if (((acceptor instanceof IPowerReceptor)) && (Mekanism.hooks.BuildCraftLoaded)) {
          totalNeeded += ((IPowerReceptor)acceptor).powerRequest(((ForgeDirection)this.acceptorDirections.get(acceptor)).getOpposite()) * Mekanism.FROM_BC;
        }
      }
    }
    return totalNeeded;
  }
  
  public double emit(double energyToSend, ArrayList ignored)
  {
    if (energyToSend > 0.0D)
    {
      double newEnergy = this.energyStored + energyToSend;
      if (newEnergy > this.maxEnergy)
      {
        double rejected = newEnergy - this.maxEnergy;
        this.energyStored += energyToSend - rejected;
        return rejected;
      }
      this.energyStored += newEnergy;
    }
    return energyToSend;
  }
  
  private double emit()
  {
    double energyToSend = this.energyStored;
    double energyAvailable = energyToSend;
    
    List availableAcceptors = Arrays.asList(getEnergyAcceptors().toArray());
    
    Collections.shuffle(availableAcceptors);
    if (!availableAcceptors.isEmpty())
    {
      int divider = availableAcceptors.size();
      double remaining = energyToSend % divider;
      double sending = (energyToSend - remaining) / divider;
      for (Object obj : availableAcceptors) {
        if ((obj instanceof TileEntity))
        {
          TileEntity acceptor = (TileEntity)obj;
          double currentSending = sending + remaining;
          
          remaining = 0.0D;
          if ((acceptor instanceof IStrictEnergyAcceptor))
          {
            energyToSend -= currentSending - ((IStrictEnergyAcceptor)acceptor).transferEnergyToAcceptor(currentSending);
          }
          else if ((acceptor instanceof IEnergySink))
          {
            double toSend = Math.min(currentSending, ((IEnergySink)acceptor).getMaxSafeInput() * Mekanism.FROM_IC2);
            energyToSend -= toSend - ((IEnergySink)acceptor).injectEnergy(MekanismUtils.toIC2Direction(((ForgeDirection)this.acceptorDirections.get(acceptor)).getOpposite()), (int)(toSend * Mekanism.TO_IC2)) * Mekanism.FROM_IC2;
          }
          else if (((acceptor instanceof IPowerReceptor)) && (Mekanism.hooks.BuildCraftLoaded))
          {
            IPowerReceptor receptor = (IPowerReceptor)acceptor;
            double electricityNeeded = Math.min(receptor.powerRequest(((ForgeDirection)this.acceptorDirections.get(acceptor)).getOpposite()), receptor.getPowerProvider().getMaxEnergyStored() - receptor.getPowerProvider().getEnergyStored()) * Mekanism.FROM_BC;
            float transferEnergy = (float)Math.min(electricityNeeded, currentSending);
            receptor.getPowerProvider().receiveEnergy((float)(transferEnergy * Mekanism.TO_BC), ((ForgeDirection)this.acceptorDirections.get(acceptor)).getOpposite());
            energyToSend -= transferEnergy;
          }
        }
      }
      double sent = energyAvailable - energyToSend;
      this.joulesTransmitted += sent;
    }
    return energyToSend;
  }
  
  public Set getEnergyAcceptors()
  {
    Set toReturn = new HashSet();
    for (TileEntity acceptor : this.possibleAcceptors) {
      if ((acceptor instanceof IStrictEnergyAcceptor))
      {
        if (((IStrictEnergyAcceptor)acceptor).canReceiveEnergy(((ForgeDirection)this.acceptorDirections.get(acceptor)).getOpposite())) {
          if (((IStrictEnergyAcceptor)acceptor).getMaxEnergy() - ((IStrictEnergyAcceptor)acceptor).getEnergy() > 0.0D) {
            toReturn.add(acceptor);
          }
        }
      }
      else if ((acceptor instanceof IEnergySink))
      {
        if (((IEnergySink)acceptor).acceptsEnergyFrom(null, MekanismUtils.toIC2Direction((ForgeDirection)this.acceptorDirections.get(acceptor)).getInverse())) {
          if (Math.min(((IEnergySink)acceptor).demandsEnergy() * Mekanism.FROM_IC2, ((IEnergySink)acceptor).getMaxSafeInput() * Mekanism.FROM_IC2) > 0.0D) {
            toReturn.add(acceptor);
          }
        }
      }
      else if (((acceptor instanceof IPowerReceptor)) && (Mekanism.hooks.BuildCraftLoaded)) {
        if (((IPowerReceptor)acceptor).getPowerProvider() != null) {
          if (((IPowerReceptor)acceptor).powerRequest(((ForgeDirection)this.acceptorDirections.get(acceptor)).getOpposite()) * Mekanism.FROM_BC > 0.0D) {
            toReturn.add(acceptor);
          }
        }
      }
    }
    return toReturn;
  }
  
  public void refresh()
  {
    Iterator it = this.cables.iterator();
    
    this.possibleAcceptors.clear();
    this.acceptorDirections.clear();
    while (it.hasNext())
    {
      IUniversalCable conductor = (IUniversalCable)it.next();
      if (conductor == null) {
        it.remove();
      } else if (((TileEntity)conductor).func_70320_p()) {
        it.remove();
      } else {
        conductor.setNetwork(this);
      }
    }
    for (IUniversalCable cable : this.cables)
    {
      TileEntity[] acceptors = CableUtils.getConnectedEnergyAcceptors((TileEntity)cable);
      for (TileEntity acceptor : acceptors) {
        if ((acceptor != null) && (!(acceptor instanceof IUniversalCable)))
        {
          this.possibleAcceptors.add(acceptor);
          this.acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
        }
      }
    }
  }
  
  public void merge(EnergyNetwork network)
  {
    EnergyNetworkRegistry registry = EnergyNetworkRegistry.getInstance();
    if ((network != null) && (network != this))
    {
      EnergyNetwork newNetwork = new EnergyNetwork(new IUniversalCable[0]);
      newNetwork.cables.addAll(this.cables);
      registry.removeNetwork(this);
      newNetwork.cables.addAll(network.cables);
      registry.removeNetwork(network);
      newNetwork.refresh();
    }
  }
  
  public void split(IUniversalCable splitPoint)
  {
    if ((splitPoint instanceof TileEntity))
    {
      this.cables.remove(splitPoint);
      
      TileEntity[] connectedBlocks = new TileEntity[6];
      boolean[] dealtWith = { false, false, false, false, false, false };
      for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
      {
        TileEntity sideTile = Object3D.get((TileEntity)splitPoint).getFromSide(direction).getTileEntity(((TileEntity)splitPoint).field_70331_k);
        if (sideTile != null) {
          connectedBlocks[Arrays.asList(ForgeDirection.values()).indexOf(direction)] = sideTile;
        }
      }
      for (int countOne = 0; countOne < connectedBlocks.length; countOne++)
      {
        TileEntity connectedBlockA = connectedBlocks[countOne];
        if (((connectedBlockA instanceof IUniversalCable)) && (dealtWith[countOne] == 0))
        {
          NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).field_70331_k, Object3D.get(connectedBlockA), new Object3D[] { Object3D.get((TileEntity)splitPoint) });
          List partNetwork = finder.findNetwork();
          for (int countTwo = countOne + 1; countTwo < connectedBlocks.length; countTwo++)
          {
            TileEntity connectedBlockB = connectedBlocks[countTwo];
            if (((connectedBlockB instanceof IUniversalCable)) && (dealtWith[countTwo] == 0)) {
              if (partNetwork.contains(Object3D.get(connectedBlockB))) {
                dealtWith[countTwo] = true;
              }
            }
          }
          EnergyNetwork newNetwork = new EnergyNetwork(new IUniversalCable[0]);
          for (Object3D node : finder.iterated)
          {
            TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).field_70331_k);
            if ((nodeTile instanceof IUniversalCable)) {
              if (nodeTile != splitPoint) {
                newNetwork.cables.add((IUniversalCable)nodeTile);
              }
            }
          }
          newNetwork.refresh();
        }
      }
      EnergyNetworkRegistry.getInstance().removeNetwork(this);
    }
  }
  
  public static class NetworkFinder
  {
    public World worldObj;
    public Object3D start;
    public List iterated = new ArrayList();
    public List toIgnore = new ArrayList();
    
    public NetworkFinder(World world, Object3D location, Object3D... ignore)
    {
      this.worldObj = world;
      this.start = location;
      if (ignore != null) {
        this.toIgnore = Arrays.asList(ignore);
      }
    }
    
    public void loopAll(Object3D location)
    {
      if ((location.getTileEntity(this.worldObj) instanceof IUniversalCable)) {
        this.iterated.add(location);
      }
      for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
      {
        Object3D obj = location.getFromSide(direction);
        if ((!this.iterated.contains(obj)) && (!this.toIgnore.contains(obj)))
        {
          TileEntity tileEntity = obj.getTileEntity(this.worldObj);
          if ((tileEntity instanceof IUniversalCable)) {
            loopAll(obj);
          }
        }
      }
    }
    
    public List findNetwork()
    {
      loopAll(this.start);
      
      return this.iterated;
    }
  }
  
  public static class NetworkLoader
  {
    @ForgeSubscribe
    public void onChunkLoad(ChunkEvent.Load event)
    {
      if (event.getChunk() != null) {
        for (Object obj : event.getChunk().field_76648_i.values()) {
          if ((obj instanceof TileEntity))
          {
            TileEntity tileEntity = (TileEntity)obj;
            if ((tileEntity instanceof IUniversalCable)) {
              ((IUniversalCable)tileEntity).refreshNetwork();
            }
          }
        }
      }
    }
  }
  
  public String toString()
  {
    return "[EnergyNetwork] " + this.cables.size() + " cables, " + this.possibleAcceptors.size() + " acceptors.";
  }
  
  public void clearJoulesTransmitted()
  {
    this.joulesLastTick = this.joulesTransmitted;
    this.joulesTransmitted = 0.0D;
  }
  
  public double getPower()
  {
    return this.joulesTransmitted * 20.0D;
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64_wire_fix.jar
 * Qualified Name:     mekanism.common.EnergyNetwork
 * JD-Core Version:    0.7.0.1
 */