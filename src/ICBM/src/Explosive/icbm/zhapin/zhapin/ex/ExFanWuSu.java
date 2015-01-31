package icbm.zhapin.zhapin.ex;

import icbm.core.ZhuYaoBase;
import icbm.zhapin.zhapin.EZhaPin;
import icbm.zhapin.zhapin.ZhaPin;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import net.minecraftforge.oredict.ShapedOreRecipe;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.RecipeHelper;

public class ExFanWuSu
  extends ZhaPin
{
  public boolean destroyBedrock = true;
  public int max_range = 40;
  
  public ExFanWuSu(String name, int ID, int tier)
  {
    super(name, ID, tier);
    setYinXin(300);
    ZhuYaoBase.CONFIGURATION.load();
    this.destroyBedrock = ZhuYaoBase.CONFIGURATION.get("general", "Antimatter Destroy Bedrock", this.destroyBedrock).getBoolean(this.destroyBedrock);
    //Changed: Added Max range
    this.max_range = ZhuYaoBase.CONFIGURATION.get("general", "Antimatter_range", this.max_range).getInt(this.max_range);
    ZhuYaoBase.CONFIGURATION.save();
  }
  
  public void baoZhaQian(World worldObj, Vector3 position, Entity explosionSource)
  {
    super.baoZhaQian(worldObj, position, explosionSource);
    worldObj.func_72908_a(position.x, position.y, position.z, "icbm.antimatter", 7.0F, (float)(worldObj.field_73012_v.nextFloat() * 0.1D + 0.8999999761581421D));
    explosionSource.field_70163_u += 5.0D;
    doDamageEntities(worldObj, position, getRadius() * 2.0F, 2.147484E+009F);
  }
  
  public boolean doBaoZha(World worldObj, Vector3 position, Entity explosionSource, int callCount)
  {
    if (!worldObj.field_72995_K) {
      if (callCount == 1)
      {
        for (int x = (int)-getRadius(); x < getRadius(); x++) {
          for (int y = (int)-getRadius(); y < getRadius(); y++) {
            for (int z = (int)-getRadius(); z < getRadius(); z++)
            {
              Vector3 targetPosition = Vector3.add(position, new Vector3(x, y, z));
              double dist = position.distanceTo(targetPosition);
              if (dist < getRadius())
              {
                int blockID = targetPosition.getBlockID(worldObj);
                if (blockID > 0) {
                  if ((blockID != Block.field_71986_z.field_71990_ca) || (this.destroyBedrock)) {
                    if ((dist < getRadius() - 1.0F) || (worldObj.field_73012_v.nextFloat() > 0.7D)) {
                      targetPosition.setBlock(worldObj, 0);
                    }
                  }
                }
              }
            }
          }
        }
        return false;
      }
    }
    if (callCount > getRadius()) {
      return false;
    }
    return true;
  }
  
  public void baoZhaHou(World worldObj, Vector3 position, Entity explosionSource)
  {
    super.baoZhaHou(worldObj, position, explosionSource);
    
    doDamageEntities(worldObj, position, getRadius() * 2.0F, 2.147484E+009F);
  }
  
  protected boolean onDamageEntity(Entity entity)
  {
    if ((entity instanceof EZhaPin)) {
      if (((EZhaPin)entity).haoMa == ZhaPin.hongSu.getID())
      {
        entity.func_70106_y();
        return true;
      }
    }
    return false;
  }
  
  public void onYinZha(World worldObj, Vector3 position, int fuseTicks)
  {
    super.onYinZha(worldObj, position, fuseTicks);
    if (fuseTicks % 25 == 0) {
      worldObj.func_72908_a(position.x, position.y, position.z, "icbm.alarm", 4.0F, 1.0F);
    }
  }
  
  public void init()
  {
    RecipeHelper.addRecipe(new ShapedOreRecipe(getItemStack(), new Object[] { "AAA", "AEA", "AAA", Character.valueOf('E'), ZhaPin.yuanZi.getItemStack(), Character.valueOf('A'), "antimatterGram" }), getUnlocalizedName(), ZhuYaoBase.CONFIGURATION, true);
  }
  
  protected int proceduralInterval()
  {
    return 1;
  }
  
  public float getRadius()
  {
    return max_range;
  }
  
  public double getEnergy()
  {
    return 1000000.0D;
  }
}


/* Location:           C:\Users\robert\Dropbox\Public\work\zCarl\ICBM_Explosion_v1.2.0.108.jar
 * Qualified Name:     icbm.zhapin.zhapin.ex.ExFanWuSu
 * JD-Core Version:    0.7.0.1
 */