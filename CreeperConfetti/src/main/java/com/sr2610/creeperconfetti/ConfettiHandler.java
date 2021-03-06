package com.sr2610.creeperconfetti;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticle;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.Explosion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ConfettiHandler {
	private Method explode;

	@SubscribeEvent
	public void creeperExplodeEvent(LivingEvent.LivingUpdateEvent event) {

		CreeperEntity creeper = null;
		if (event.getEntityLiving() instanceof CreeperEntity)
			creeper = (CreeperEntity) event.getEntityLiving();
		else
			return;
		if (creeper != null && creeper.getCreeperState() == 1) {
			int ignitedTime = ObfuscationReflectionHelper.getPrivateValue(CreeperEntity.class, creeper,
					"field_70833_d");

			int fuseTime = ObfuscationReflectionHelper.getPrivateValue(CreeperEntity.class, creeper, "field_82225_f");
			if (ignitedTime >= fuseTime - 1) {
				if (willExplodeToConfetti()) {
					if (ConfigHandler.GENERAL.DamagePlayers.get())
						damagePlayers(creeper);
					Random rand = new Random();
					if (rand.nextInt(100) < 5)
						creeper.world.playSound(creeper.posX, creeper.posY, creeper.posZ, ModSounds.confetti, SoundCategory.HOSTILE,2F,1F, false);
					creeper.world.playSound(creeper.posX, creeper.posY, creeper.posZ,SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.HOSTILE, 1F,1F, false);		
					if(!creeper.world.isRemote)
					spawnParticles(creeper);
					creeper.remove(); // Removes the creeper from the world, as if it was dead
				} else {
					explode = ObfuscationReflectionHelper.findMethod(CreeperEntity.class, "func_146077_cc");
					try {
						explode.invoke(creeper, (Object[]) null);
					} catch (IllegalAccessException e) { // Should handle this better but oh well
					} catch (IllegalArgumentException e) {
					} catch (InvocationTargetException e) {
					}
				}
			}

		}
	}

	private boolean willExplodeToConfetti() {
		Random rand = new Random();
		if (!(rand.nextInt(100) < ConfigHandler.GENERAL.ConfettiChance.get())
				|| ConfigHandler.GENERAL.ConfettiChance.get() == 0)
			return false;
		else
			return true;
	}

	private void damagePlayers(CreeperEntity creeper) {
		float explosionStrength = creeper.getPowered() ? 2.0F : 1.0F;
        Explosion.Mode mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(creeper.getEntityWorld(), creeper) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
        Explosion explosion = new Explosion(creeper.getEntityWorld(), creeper, creeper.posX, creeper.posY, creeper.posZ, 3 * explosionStrength, false, mode);

		explosion.doExplosionA();
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnParticles(CreeperEntity creeper) {
		Minecraft.getInstance().particles
				.addEffect(new FireworkParticle.Starter(creeper.getEntityWorld(), creeper.posX, creeper.posY + 0.5F,
						creeper.posZ, 0, 0, 0, Minecraft.getInstance().particles, generateTag(creeper, false)));
		if (creeper.getPowered())
			Minecraft.getInstance().particles
					.addEffect(new FireworkParticle.Starter(creeper.getEntityWorld(), creeper.posX, creeper.posY + 2.5F,
							creeper.posZ, 0, 0, 0, Minecraft.getInstance().particles, generateTag(creeper, true)));
	}

	private CompoundNBT generateTag(CreeperEntity creeper, boolean powered) {
		CompoundNBT fireworkTag = new CompoundNBT();
		CompoundNBT fireworkItemTag = new CompoundNBT();
		ListNBT nbttaglist = new ListNBT();
		List<Integer> list = Lists.<Integer>newArrayList();
		Random rand = new Random();
		list.add(0xE67E22);
		list.add(0x00E0FF);
		list.add(0x0FFF00);
		for (int i = 0; i < rand.nextInt(3) + 3; i++)
			list.add(rand.nextInt(0xffffff + 1));
		int[] colours = new int[list.size()];
		for (int i = 0; i < colours.length; i++)
			colours[i] = list.get(i).intValue();
		fireworkTag.putIntArray("Colors", colours);
		fireworkTag.putBoolean("Flicker", true);
		fireworkTag.putByte("Type", (byte) (powered ? 3 : 4));
		nbttaglist.add((INBT) fireworkTag);
		fireworkItemTag.put("Explosions", nbttaglist);
		return fireworkItemTag;
	}
}
