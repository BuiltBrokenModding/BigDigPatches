package mekanism.common;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class EnergyNetworkRegistry
  implements ITickHandler
{
  private static EnergyNetworkRegistry INSTANCE = new EnergyNetworkRegistry();
  private Set networks = new HashSet();
  
  public EnergyNetworkRegistry()
  {
    TickRegistry.registerTickHandler(this, Side.SERVER);
  }
  
  public static EnergyNetworkRegistry getInstance()
  {
    return INSTANCE;
  }
  
  public void registerNetwork(EnergyNetwork network)
  {
    this.networks.add(network);
  }
  
  public void removeNetwork(EnergyNetwork network)
  {
    if (this.networks.contains(network)) {
      this.networks.remove(network);
    }
  }
  
  public void tickStart(EnumSet type, Object... tickData) {}
  
  public void tickEnd(EnumSet type, Object... tickData)
  {
    for (EnergyNetwork net : this.networks)
    {
      net.update();
      net.clearJoulesTransmitted();
    }
  }
  
  public EnumSet ticks()
  {
    return EnumSet.of(TickType.SERVER);
  }
  
  public String getLabel()
  {
    return "Mekanism Energy Networks";
  }
  
  public String toString()
  {
    return this.networks.toString();
  }
}


/* Location:           C:\Users\robert\Desktop\Mekanism-v5.5.6.64_wire_fix.jar
 * Qualified Name:     mekanism.common.EnergyNetworkRegistry
 * JD-Core Version:    0.7.0.1
 */