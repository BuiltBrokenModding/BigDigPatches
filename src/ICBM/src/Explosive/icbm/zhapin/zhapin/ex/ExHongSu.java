package icbm.zhapin.zhapin.ex;

import icbm.api.explosion.ExplosionEvent;
import icbm.api.explosion.IExplosiveIgnore;
import icbm.core.ZhuYaoBase;
import icbm.zhapin.CommonProxy;
import icbm.zhapin.EFeiBlock;
import icbm.zhapin.ZhuYaoZhaPin;
import icbm.zhapin.zhapin.EZhaDan;
import icbm.zhapin.zhapin.EZhaPin;
import icbm.zhapin.zhapin.ZhaPin;
import java.util.List;
import java.util.Random;
import mffs.api.IForceFieldBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.liquids.ILiquid;
import net.minecraftforge.oredict.ShapedOreRecipe;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.RecipeHelper;

public class ExHongSu extends ZhaPin
{
	private int MAX_TAKE_BLOCKS = 5;
	private int max_range = 35;
	private int timer = 600;

	public ExHongSu(String name, int ID, int tier)
	{
		super(name, ID, tier);
		// Changed: Added Max range, Added disable movement
		ZhuYaoBase.CONFIGURATION.load();
		this.isMobile = ZhuYaoBase.CONFIGURATION.get("general", "Redmatter_movement_allowed", true).getBoolean(true);
		this.max_range = ZhuYaoBase.CONFIGURATION.get("general", "Redmatter_range", this.max_range).getInt(this.max_range);
		this.MAX_TAKE_BLOCKS = ZhuYaoBase.CONFIGURATION.get("general", "Redmatter_max_edits_per_tick", this.MAX_TAKE_BLOCKS).getInt(this.MAX_TAKE_BLOCKS);		
		this.timer = ZhuYaoBase.CONFIGURATION.get("general", "Redmatter_timer_seconds", this.MAX_TAKE_BLOCKS).getInt(this.timer);	
		ZhuYaoBase.CONFIGURATION.save();
	}

	public void baoZhaQian(World worldObj, Vector3 position, Entity explosionSource)
	{
		if (!worldObj.field_72995_K)
		{
			worldObj.func_72876_a(explosionSource, position.x, position.y, position.z, 5.0F, true);
		}
	}

	public boolean doBaoZha(World worldObj, Vector3 position, Entity explosionSource, int explosionMetadata, int callCount)
	{		
		if (!worldObj.field_72995_K)
		{
			if(callCount >= (timer * 20))
			{
				return false;
			}
			int takenBlocks = 0;
			boolean exit = false;
			for (int r = 1; r < getRadius(); r++)
			{
				for (int x = -r; x < r; x++)
				{
					for (int y = -r; y < r; y++)
					{
						for (int z = -r; z < r; z++)
						{
							double dist = MathHelper.func_76133_a(x * x + y * y + z * z);
							if ((dist <= r) && (dist >= r - 2))
							{
								Vector3 currentPos = new Vector3(position.x + x, position.y + y, position.z + z);
								int blockID = worldObj.func_72798_a(currentPos.intX(), currentPos.intY(), currentPos.intZ());
								Block block = Block.field_71973_m[blockID];
								if (block != null)
								{
									if ((block instanceof IForceFieldBlock))
									{
										((IForceFieldBlock) block).weakenForceField(worldObj, currentPos.intX(), currentPos.intY(), currentPos.intZ(), 50);
									}
									else if (block.func_71934_m(worldObj, currentPos.intX(), currentPos.intY(), currentPos.intZ()) > -1.0F)
									{
										int metadata = worldObj.func_72805_g(currentPos.intX(), currentPos.intY(), currentPos.intZ());

										int notify = 2;
										if ((block instanceof BlockFluid))
										{
											notify = 0;
										}
										worldObj.func_72832_d(currentPos.intX(), currentPos.intY(), currentPos.intZ(), 0, 0, notify);
										if ((!(block instanceof BlockFluid)) && (!(block instanceof ILiquid)))
										{
											currentPos.add(0.5D);
											if (worldObj.field_73012_v.nextFloat() > 0.8D)
											{
												EFeiBlock entity = new EFeiBlock(worldObj, currentPos, blockID, metadata);
												worldObj.func_72838_d(entity);
												entity.yawChange = (50.0F * worldObj.field_73012_v.nextFloat());
												entity.pitchChange = (50.0F * worldObj.field_73012_v.nextFloat());
											}
											takenBlocks++;
											//Added break
											if (takenBlocks >= MAX_TAKE_BLOCKS)
											{
												exit = true;
												break;
											}
										}
									}
								}
							}
						}
						//Added break
						if(exit) break;
					}
					//Added break
					if(exit) break;
				}
			}
		}
		
		float radius = getRadius() + getRadius() / 2.0F;
		AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(position.x - radius, position.y - radius, position.z - radius, position.x + radius, position.y + radius, position.z + radius);
		List allEntities = worldObj.func_72872_a(Entity.class, bounds);
		boolean explosionCreated = false;
		for (Object o : allEntities)
		{
			Entity entity = (Entity) o;
			if ((entity != explosionSource) &&

			((!(entity instanceof IExplosiveIgnore)) ||

			(!((IExplosiveIgnore) entity).canIgnore(new ExplosionEvent(worldObj, position.x, position.y, position.z, this)))) && (

			(!(entity instanceof EntityPlayer)) ||

			(!((EntityPlayer) entity).field_71075_bZ.field_75098_d)))
			{
				double xDifference = entity.field_70165_t - position.x;
				double yDifference = entity.field_70163_u - position.y;
				double zDifference = entity.field_70161_v - position.z;

				float r = radius;
				if (xDifference < 0.0D)
				{
					r = (int) -radius;
				}
				entity.field_70159_w -= (r - xDifference) * 0.002D;

				r = radius;
				if (yDifference < 0.0D)
				{
					r = (int) -radius;
				}
				entity.field_70181_x -= (r - yDifference) * 0.005D;

				r = radius;
				if (zDifference < 0.0D)
				{
					r = -radius;
				}
				entity.field_70179_y -= (r - zDifference) * 0.002D;
				if ((entity instanceof EFeiBlock))
				{
					if (worldObj.field_72995_K)
					{
						if (ZhuYaoZhaPin.proxy.getParticleSetting() == 0)
						{
							if (worldObj.field_73012_v.nextInt(5) == 0)
							{
								ZhuYaoZhaPin.proxy.spawnParticle("digging", worldObj, new Vector3(entity), -xDifference, -yDifference + 10.0D, -zDifference, ((EFeiBlock) entity).blockID, 0.0F, ((EFeiBlock) entity).metadata, 2.0F, 1.0D);
							}
						}
					}
				}
				if (Vector3.distance(new Vector3(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v), position) < 4.0D)
				{
					if ((!explosionCreated) && (callCount % 5 == 0))
					{
						worldObj.func_72876_a(explosionSource, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, 3.0F, true);
						explosionCreated = true;
					}
					if ((entity instanceof EntityLiving))
					{
						entity.field_70143_R = 0.0F;
					}
					else if ((entity instanceof EZhaPin))
					{
						if (((EZhaPin) entity).haoMa == ZhaPin.fanWuSu.getID())
						{
							worldObj.func_72908_a(position.x, position.y, position.z, "icbm.explosion", 7.0F, (1.0F + (worldObj.field_73012_v.nextFloat() - worldObj.field_73012_v.nextFloat()) * 0.2F) * 0.7F);
							if ((worldObj.field_73012_v.nextFloat() > 0.85D) && (!worldObj.field_72995_K))
							{
								entity.func_70106_y();
								return false;
							}
						}
					}
					else if ((entity instanceof EZhaDan))
					{
						((EZhaDan) entity).explode();
					}
					else
					{
						entity.func_70106_y();
					}
				}
			}
		}
		if (worldObj.field_73012_v.nextInt(10) == 0)
		{
			worldObj.func_72908_a(position.x + (Math.random() - 0.5D) * radius, position.y + (Math.random() - 0.5D) * radius, position.z + (Math.random() - 0.5D) * radius, "icbm.collapse", 6.0F - worldObj.field_73012_v.nextFloat(), 1.0F - worldObj.field_73012_v.nextFloat() * 0.4F);
		}
		worldObj.func_72908_a(position.x, position.y, position.z, "icbm.redmatter", 3.0F, (1.0F + (worldObj.field_73012_v.nextFloat() - worldObj.field_73012_v.nextFloat()) * 0.2F) * 1.0F);

		return true;
	}

	public int proceduralInterval()
	{
		return 1;
	}

	public void init()
	{
		RecipeHelper.addRecipe(new ShapedOreRecipe(getItemStack(), new Object[] { "AAA", "AEA", "AAA", Character.valueOf('E'), fanWuSu.getItemStack(), Character.valueOf('A'), "strangeMatter" }), getUnlocalizedName(), ZhuYaoBase.CONFIGURATION, true);
	}

	public float getRadius()
	{
		return (float) max_range;
	}

	public double getEnergy()
	{
		return 4000.0D;
	}
}

/*
 * Location: C:\Users\robert\Dropbox\Public\work\zCarl\ICBM_Explosion_v1.2.0.108.jar Qualified Name:
 * icbm.zhapin.zhapin.ex.ExHongSu JD-Core Version: 0.7.0.1
 */