package mod.imphack.module.modules.combat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


import mod.imphack.module.Category;
import mod.imphack.module.Module;
import mod.imphack.setting.settings.BooleanSetting;
import mod.imphack.setting.settings.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public class KillAura extends Module {

	public IntSetting range = new IntSetting("range", this, 6);
	public BooleanSetting switchA = new BooleanSetting("switch", this, false);
	public BooleanSetting swordOnly = new BooleanSetting("swordOnly", this, false);
	public BooleanSetting players = new BooleanSetting("players", this, true);
	public BooleanSetting passives = new BooleanSetting("passives", this, false);
	public BooleanSetting hostiles = new BooleanSetting("hostiles", this, false);
	
	public KillAura() {
		super ("killAura", "automatically hits anything near u.", Category.COMBAT);
		
		this.addSetting(range);
		this.addSetting(switchA);
		this.addSetting(swordOnly);
		this.addSetting(players);
		this.addSetting(passives);
		this.addSetting(hostiles);

	}

	@Override
	public void onUpdate() {
		if (mc.player == null || mc.player.isDead) return;
		List<Entity> targets = mc.world.loadedEntityList.stream()
				.filter(entity -> entity != mc.player)
				.filter(entity -> mc.player.getDistance(entity) <= range.getValue())
				.filter(entity -> !entity.isDead)
				.filter(entity -> attackCheck(entity))
				.sorted(Comparator.comparing(s -> mc.player.getDistance(s)))
				.collect(Collectors.toList());

		targets.forEach(target -> {
			attack(target);
		});
	}

	public void attack(Entity e) {
		if (mc.player.getCooledAttackStrength(0) >= 1){
			mc.playerController.attackEntity(mc.player, e);
			mc.player.swingArm(EnumHand.MAIN_HAND);
		}
	}
	
	private boolean attackCheck(Entity entity) {
		if (players.isEnabled() && entity instanceof EntityPlayer) {
				if (((EntityPlayer) entity).getHealth() > 0) { 
					return true;
				}
			}
		

		if (passives.isEnabled() && entity instanceof EntityAnimal) {
			if (entity instanceof EntityTameable) {
				return false;
			}else {
				return true;
			}
		}
		if (hostiles.isEnabled() && entity instanceof EntityMob) {
			return true;
		}
		return false;
	}
}