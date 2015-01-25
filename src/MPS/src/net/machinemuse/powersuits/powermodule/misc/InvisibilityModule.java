package net.machinemuse.powersuits.powermodule.misc;

import java.lang.reflect.Field;
import java.nio.file.FileSystemNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.machinemuse.api.moduletrigger.IPlayerTickModule;
import net.machinemuse.api.moduletrigger.IToggleableModule;
import net.machinemuse.powersuits.item.ItemComponent;
import net.machinemuse.powersuits.powermodule.PowerModuleBase;
import net.machinemuse.utils.ElectricItemUtils;
import net.machinemuse.utils.MuseItemUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class InvisibilityModule extends PowerModuleBase implements IPlayerTickModule, IToggleableModule
{
	public static final String MODULE_ACTIVE_CAMOUFLAGE = "Active Camouflage";
	public static final long tick_delay_for_attacking = 1000000000 * 2;
	public static HashMap<String, Long> last_time_cloaked = new HashMap();
	public static HashMap<String, Long> last_time_uncloaked = new HashMap();

	public InvisibilityModule(List validItems)
	{
		super(validItems);
		addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.laserHologram, 4));
		addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.fieldEmitter, 2));
		addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.controlCircuit, 2));
	}

	public String getCategory()
	{
		return "Special";
	}

	public String getName()
	{
		return "Active Camouflage";
	}

	public String getDescription()
	{
		return "Emit a hologram of your surroundings to make yourself almost imperceptible.";
	}

	public void onPlayerTickActive(EntityPlayer player, ItemStack item)
	{
		boolean has_attacked = false;
		boolean disable = false;
		double totalEnergy = ElectricItemUtils.getPlayerEnergy(player);
		PotionEffect invis = null;
		long time = System.nanoTime();
		
		//Get current inv potion effect
		Collection effects = player.func_70651_bq();
		for (Object e : effects)
		{
			PotionEffect effect = (PotionEffect) e;
			if ((effect.func_76458_c() == 81) && (effect.func_76456_a() == Potion.field_76441_p.field_76415_H))
			{
				invis = effect;
				break;
			}
		}	
		
		
		
		if(player.func_70680_aw() != null)
		{
			try
			{
				Field field = EntityLiving.class.getField("field_70718_bc");
				has_attacked = field.getInt(player.func_70680_aw()) > 0;
				if(has_attacked)
				{
					disable = true;
				}
			}
			catch (NoSuchFieldException e1)
			{
				e1.printStackTrace();
			}
			catch (SecurityException e1)
			{
			}
			catch (IllegalArgumentException e1)
			{
				e1.printStackTrace();
			}
			catch (IllegalAccessException e1)
			{
			}
		}
		
		if(disable)
		{
			//Set last time cloak was disabled
			if(invis != null)
			{
				last_time_uncloaked.put(player.func_70023_ak(), time);
			}
			player.func_82170_o(Potion.field_76441_p.field_76415_H);
		}
		else if (50.0D < totalEnergy)
		{
			//Set last time cloak was enabled
			if(invis == null)
			{
				last_time_cloaked.put(player.func_70023_ak(), time);
			}
			
			if ((invis == null) || (invis.func_76459_b() < 210))
			{
				player.func_70690_d(new PotionEffect(Potion.field_76441_p.field_76415_H, 500, 81));
				ElectricItemUtils.drainPlayerEnergy(player, 50.0D);
			}
		}		
	}

	public void onPlayerTickInactive(EntityPlayer player, ItemStack item)
	{		
		if(!player.field_70170_p.field_72995_K)
		{
			player.func_82170_o(Potion.field_76441_p.field_76415_H);
		}
	}

	public String getTextureFile()
	{
		return "bluedrone";
	}
}