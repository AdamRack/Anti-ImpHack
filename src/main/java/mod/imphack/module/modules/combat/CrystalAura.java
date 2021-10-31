package mod.imphack.module.modules.combat;

import mod.imphack.event.events.ImpHackEventPacket;
import mod.imphack.event.events.ImpHackEventRender;
import mod.imphack.module.Category;
import mod.imphack.module.Module;
import mod.imphack.setting.settings.BooleanSetting;
import mod.imphack.setting.settings.ColorSetting;
import mod.imphack.setting.settings.FloatSetting;
import mod.imphack.setting.settings.ModeSetting;
import mod.imphack.util.Timer;
import mod.imphack.util.render.ColorUtil;
import mod.imphack.util.render.RenderUtil;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class CrystalAura extends Module {
	
	
	
	public CrystalAura() {
		super("CrystalAura", "AutoPlaces Crystals", Category.COMBAT);

		addSetting(attack);
		addSetting(attackRapid);
		addSetting(attackDelay);
		addSetting(attackRadius);
		addSetting(attackMaxDistance);
		addSetting(place);
		addSetting(placeRapid);
		addSetting(placeSpread);
		addSetting(placeSpreadDistance);
		addSetting(placeDelay);
		addSetting(facePlaceValue);
		addSetting(highPing);
		addSetting(antiGhost);
		addSetting(rotate);
		addSetting(minDmg);
		addSetting(multiplace);
		addSetting(multiplaceValue);
		addSetting(multiplacePlus);
		addSetting(antiSuicide);
		addSetting(maxSelfDmg);
		addSetting(antiSelfPop);
		addSetting(enemyRange);
		addSetting(wallsRange);
		addSetting(mode113);
		addSetting(outline);
		addSetting(showDamage);
		addSetting(color);
	}
	 public final BooleanSetting attack = new BooleanSetting("Attack", this, true);
	    public final BooleanSetting attackRapid = new BooleanSetting("AttackRapid", this, true);
	    public final FloatSetting attackDelay = new FloatSetting("AttackDelay", this, 50.0f);
	    public final FloatSetting attackRadius = new FloatSetting("AttackRadius", this, 4.0f);
	    public final FloatSetting attackMaxDistance = new FloatSetting("AttackMaxDistance", this, 8.0f);
	    public final BooleanSetting place = new BooleanSetting("Place", this, true);
	    public final BooleanSetting placeRapid = new BooleanSetting("PlaceRapid", this, true);
	    public final BooleanSetting placeSpread = new BooleanSetting("PlaceSpread", this, false);
	    public final FloatSetting placeSpreadDistance = new FloatSetting("PlaceSpreadDistance", this, 1.0f);
	    public final FloatSetting placeDelay = new FloatSetting("PlaceDelay", this, 15.0f);
	    public final FloatSetting placeRadius = new FloatSetting("PlaceRadius", this, 5.5f);
	    public final FloatSetting placeMaxDistance = new FloatSetting("PlaceMaxDistance",this, 1.5f);
	    public final FloatSetting placeLocalDistance = new FloatSetting("PlaceLocalDistance", this, 8.0f);
	    public final FloatSetting minDamage = new FloatSetting("MinDamage", this, 1.5f);
	    public final BooleanSetting offHand = new BooleanSetting("Offhand", this, false);
	    public final BooleanSetting predict = new BooleanSetting("Predict", this, true);
	    public final BooleanSetting rotate = new BooleanSetting("Rotate", this, true);
	    public final BooleanSetting swing = new BooleanSetting("Swing", this, true);
	    public final BooleanSetting ignore = new BooleanSetting("Ignore", this, false);
	    public final BooleanSetting render = new BooleanSetting("Render", this, true);
	    public final BooleanSetting renderDamage = new BooleanSetting("RenderDamage", this, true);
	    public final BooleanSetting fixDesync = new BooleanSetting("FixDesync", this, true);
	    public final FloatSetting fixDesyncRadius = new FloatSetting("FixDesyncRadius", this, 10.0f);

	    private final Timer attackTimer = new Timer();
	    private final Timer placeTimer = new Timer();

	    private final Map<Integer, EntityEnderCrystal> predictedCrystals = Maps.newConcurrentMap();
	    private final List<PlaceLocation> placeLocations = Lists.newArrayList();

	    private final RotationTask placeRotationTask = new RotationTask("CrystalAuraPlaceTask", 6);
	    private final RotationTask attackRotationTask = new RotationTask("CrystalAuraAttackTask", 7);

	    private BlockPos currentPlacePosition = null;
	    private BlockPos lastPlacePosition = null;
	    private Entity lastAttackEntity = null;
	    private Entity currentAttackEntity = null;
	    private Entity currentAttackPlayer = null;

	    public CrystalAuraModule() {
	        super("CrystalAura", new String[]{"AutoCrystal", "Crystal"}, "Automatically places crystals near enemies and detonates them", "NONE", -1, ModuleType.COMBAT);
	    }

	    @Override
	    public void onDisable() {
	        super.onDisable();
	        Seppuku.INSTANCE.getRotationManager().finishTask(this.placeRotationTask);
	        Seppuku.INSTANCE.getRotationManager().finishTask(this.attackRotationTask);
	        this.currentPlacePosition = null;
	        this.lastPlacePosition = null;
	        this.currentAttackEntity = null;
	        this.lastAttackEntity = null;
	        this.currentAttackPlayer = null;
	        this.predictedCrystals.clear();
	        this.placeLocations.clear();
	    }

	    @Listener
	    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
	        final Minecraft mc = Minecraft.getMinecraft();
	        if (mc.player == null || mc.world == null)
	            return;

	        switch (event.getStage()) {
	            case PRE:
	                this.currentPlacePosition = null;
	                this.currentAttackEntity = null;

	                if (this.predict.getValue()) {
	                    this.predictedCrystals.forEach((i, entityEnderCrystal) -> {
	                        if (!entityEnderCrystal.isEntityAlive() || mc.player.getDistance(entityEnderCrystal) > this.attackRadius.getValue()) {
	                            this.predictedCrystals.remove(i);
	                        }
	                    });
	                }

	                if (mc.player.getHeldItem(this.offHand.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND).getItem() == Items.END_CRYSTAL) {
	                    if (this.place.getValue()) {
	                        final float radius = this.placeRadius.getValue();
	                        float damage = 0;
	                        double maxDistanceToLocal = this.placeLocalDistance.getValue();
	                        EntityLivingBase targetPlayer = null;
	                        if (this.placeRapid.getValue()) {
	                            this.doPlaceLogic(mc, radius, damage, maxDistanceToLocal, targetPlayer);
	                        } else {
	                            if (this.placeTimer.passed(this.placeDelay.getValue())) {
	                                this.doPlaceLogic(mc, radius, damage, maxDistanceToLocal, targetPlayer);
	                                this.placeTimer.reset();
	                            }
	                        }
	                    }

	                    if (this.attack.getValue()) {
	                        if (this.predict.getValue()) {
	                            this.predictedCrystals.forEach((i, entityEnderCrystal) -> {
	                                if (mc.player.getDistance(entityEnderCrystal) <= this.attackRadius.getValue()) {
	                                    for (Entity ent : mc.world.loadedEntityList) {
	                                        if (ent != null && ent != mc.player && (ent.getDistance(entityEnderCrystal) <= this.attackMaxDistance.getValue()) && ent instanceof EntityPlayer) {
	                                            final EntityPlayer player = (EntityPlayer) ent;
	                                            float currentDamage = calculateExplosionDamage(player, 6.0f, (float) entityEnderCrystal.posX, (float) entityEnderCrystal.posY, (float) entityEnderCrystal.posZ) / 2.0f;
	                                            float localDamage = calculateExplosionDamage(mc.player, 6.0f, (float) entityEnderCrystal.posX, (float) entityEnderCrystal.posY, (float) entityEnderCrystal.posZ) / 2.0f;

	                                            if (this.isLocalImmune()) {
	                                                localDamage = -1;
	                                            }

	                                            if (localDamage <= currentDamage && currentDamage >= this.minDamage.getValue()) {
	                                                final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entityEnderCrystal.getPositionVector());

	                                                Seppuku.INSTANCE.getRotationManager().startTask(this.attackRotationTask);
	                                                if (this.attackRotationTask.isOnline() || this.attackRapid.getValue()) {
	                                                    Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
	                                                    this.currentAttackEntity = entityEnderCrystal;
	                                                }
	                                            }
	                                        }
	                                    }
	                                }
	                            });
	                        }
	                        for (Entity entity : mc.world.loadedEntityList) {
	                            if (entity instanceof EntityEnderCrystal) {
	                                if (mc.player.getDistance(entity) <= this.attackRadius.getValue()) {
	                                    for (Entity ent : mc.world.loadedEntityList) {
	                                        if (ent != null && ent != mc.player && (ent.getDistance(entity) <= this.attackMaxDistance.getValue()) && ent != entity && ent instanceof EntityPlayer) {
	                                            final EntityPlayer player = (EntityPlayer) ent;
	                                            float currentDamage = calculateExplosionDamage(player, 6.0f, (float) entity.posX, (float) entity.posY, (float) entity.posZ) / 2.0f;
	                                            float localDamage = calculateExplosionDamage(mc.player, 6.0f, (float) entity.posX, (float) entity.posY, (float) entity.posZ) / 2.0f;

	                                            if (this.isLocalImmune()) {
	                                                localDamage = -1;
	                                            }

	                                            if (localDamage <= currentDamage && currentDamage >= this.minDamage.getValue()) {
	                                                final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector());

	                                                Seppuku.INSTANCE.getRotationManager().startTask(this.attackRotationTask);
	                                                if (this.attackRotationTask.isOnline() || this.attackRapid.getValue()) {
	                                                    if (this.rotate.getValue()) {
	                                                        Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
	                                                    }
	                                                    this.currentAttackEntity = entity;
	                                                }
	                                            }
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	                break;
	            case POST:
	                if (this.currentPlacePosition != null) {
	                    if (this.placeRotationTask.isOnline() || this.placeRapid.getValue()) {
	                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.currentPlacePosition, EnumFacing.UP, this.offHand.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
	                        this.placeLocations.add(new PlaceLocation(this.currentPlacePosition.getX(), this.currentPlacePosition.getY(), this.currentPlacePosition.getZ()));
	                        this.lastPlacePosition = this.currentPlacePosition;
	                    }
	                } else {
	                    Seppuku.INSTANCE.getRotationManager().finishTask(this.placeRotationTask);
	                }

	                if (this.currentAttackEntity != null) {
	                    if (this.attackRotationTask.isOnline() || this.attackRapid.getValue()) {
	                        if (this.attackRapid.getValue()) {
	                            if (this.swing.getValue()) {
	                                mc.player.swingArm(this.offHand.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
	                            }
	                            mc.playerController.attackEntity(mc.player, this.currentAttackEntity);
	                        } else {
	                            if (this.attackTimer.passed(this.attackDelay.getValue())) {
	                                if (this.swing.getValue()) {
	                                    mc.player.swingArm(this.offHand.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
	                                }
	                                mc.playerController.attackEntity(mc.player, this.currentAttackEntity);
	                                this.attackTimer.reset();
	                            }
	                        }
	                    }

	                    this.lastAttackEntity = this.currentAttackEntity;
	                } else {
	                    Seppuku.INSTANCE.getRotationManager().finishTask(this.attackRotationTask);
	                }
	                break;
	        }
	    }

	    @Listener
	    public void onEntityAdd(EventAddEntity eventAddEntity) {
	        if (eventAddEntity.getEntity() != null) {
	            if (eventAddEntity.getEntity() instanceof EntityEnderCrystal) {
	                final EntityEnderCrystal entityEnderCrystal = (EntityEnderCrystal) eventAddEntity.getEntity();
	                this.predictedCrystals.put(eventAddEntity.getEntity().getEntityId(), entityEnderCrystal);
	            }
	        }
	    }

	    @Listener
	    public void onReceivePacket(EventReceivePacket event) {
	        if (event.getStage() == EventStageable.EventStage.POST) {
	            if (event.getPacket() instanceof SPacketSpawnObject) {
	                final SPacketSpawnObject packetSpawnObject = (SPacketSpawnObject) event.getPacket();
	                if (packetSpawnObject.getType() == 51) {
	                    for (int i = this.placeLocations.size() - 1; i >= 0; i--) {
	                        final PlaceLocation placeLocation = this.placeLocations.get(i);
	                        if (placeLocation.getDistance((int) packetSpawnObject.getX(), (int) packetSpawnObject.getY() - 1, (int) packetSpawnObject.getZ()) <= 1) {
	                            placeLocation.placed = true;
	                        }
	                    }
	                }
	            }

	            if (this.fixDesync.getValue()) {
	                if (event.getPacket() instanceof SPacketSoundEffect) {
	                    final SPacketSoundEffect packetSoundEffect = (SPacketSoundEffect) event.getPacket();
	                    if (packetSoundEffect.getCategory() == SoundCategory.BLOCKS && packetSoundEffect.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
	                        final Minecraft mc = Minecraft.getMinecraft();
	                        if (mc.world != null) {
	                            for (int i = mc.world.loadedEntityList.size() - 1; i > 0; i--) {
	                                Entity entity = mc.world.loadedEntityList.get(i);
	                                if (entity != null) {
	                                    if (entity.isEntityAlive() && entity instanceof EntityEnderCrystal) {
	                                        if (entity.getDistance(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ()) <= this.fixDesyncRadius.getValue()) {
	                                            entity.setDead();
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }

	    @Listener
	    public void onRender(EventRender3D event) {
	        if (!this.render.getValue())
	            return;

	        final Minecraft mc = Minecraft.getMinecraft();

	        RenderUtil.begin3D();
	        for (int i = this.placeLocations.size() - 1; i >= 0; i--) {
	            final PlaceLocation placeLocation = this.placeLocations.get(i);
	            if (placeLocation.alpha <= 0) {
	                this.placeLocations.remove(placeLocation);
	                continue;
	            }

	            placeLocation.update();

	            if (placeLocation.placed) {
	                final AxisAlignedBB bb = new AxisAlignedBB(
	                        placeLocation.getX() - mc.getRenderManager().viewerPosX,
	                        placeLocation.getY() - mc.getRenderManager().viewerPosY,
	                        placeLocation.getZ() - mc.getRenderManager().viewerPosZ,
	                        placeLocation.getX() + 1 - mc.getRenderManager().viewerPosX,
	                        placeLocation.getY() + 1 - mc.getRenderManager().viewerPosY,
	                        placeLocation.getZ() + 1 - mc.getRenderManager().viewerPosZ);


	                float crystalAlpha = placeLocation.alpha / 2.0f;
	                int crystalColorRounded = Math.round(255.0f - (crystalAlpha * 255.0f / (255.0f / 2)));
	                int crystalColorHex = 255 - crystalColorRounded << 8 | crystalColorRounded << 16;

	                RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(crystalColorHex, placeLocation.alpha / 2));
	                RenderUtil.drawBoundingBox(bb, 1, ColorUtil.changeAlpha(crystalColorHex, placeLocation.alpha));

//	                if (this.renderDamage.getValue()) {
//	                    GlStateManager.pushMatrix();
//	                    RenderUtil.glBillboardDistanceScaled((float) placeLocation.getX() + 0.5f, (float) placeLocation.getY() + 0.5f, (float) placeLocation.getZ() + 0.5f, mc.player, 1);
//	                    final float damage = placeLocation.damage;
//	                    if (damage != -1) {
//	                        final String damageText = (Math.floor(damage) == damage ? (int) damage : String.format("%.1f", damage)) + "";
//	                        //GlStateManager.disableDepth();
//	                        GlStateManager.translate(-(mc.fontRenderer.getStringWidth(damageText) / 2.0d), 0, 0);
//	                        mc.fontRenderer.drawStringWithShadow(damageText, 0, 0, 0xFFAAAAAA);
//	                    }
//	                    GlStateManager.popMatrix();
//	                }
	            }
	        }
	        RenderUtil.end3D();
	    }

	    private void doPlaceLogic(final Minecraft mc, final float radius, float damage, double maxDistanceToLocal, EntityLivingBase targetPlayer) {
	        for (float x = radius; x >= -radius; x--) {
	            for (float y = radius; y >= -radius; y--) {
	                for (float z = radius; z >= -radius; z--) {
	                    final BlockPos blockPos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);

	                    if (this.canPlaceCrystal(blockPos)) {
	                        for (Entity entity : mc.world.loadedEntityList) {
	                            if (entity instanceof EntityPlayer) {
	                                final EntityPlayer player = (EntityPlayer) entity;
	                                if (player != mc.player && !player.getName().equals(mc.player.getName()) && player.getHealth() > 0 && Seppuku.INSTANCE.getFriendManager().isFriend(player) == null) {
	                                    final double distToBlock = entity.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	                                    final double distToLocal = entity.getDistance(mc.player.posX, mc.player.posY, mc.player.posZ);
	                                    if (distToBlock < this.placeMaxDistance.getValue() && distToLocal <= maxDistanceToLocal) {
	                                        targetPlayer = player;
	                                        maxDistanceToLocal = distToLocal;
	                                    }
	                                }
	                            }
	                        }

	                        if (targetPlayer != null) {
	                            this.currentAttackPlayer = targetPlayer;

	                            if (this.currentAttackPlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > this.placeMaxDistance.getValue())
	                                continue;

	                            final float currentDamage = calculateExplosionDamage(targetPlayer, 6.0f, blockPos.getX() + 0.5f, blockPos.getY() + 1.0f, blockPos.getZ() + 0.5f) / 2.0f;

	                            float localDamage = calculateExplosionDamage(mc.player, 6.0f, blockPos.getX() + 0.5f, blockPos.getY() + 1.0f, blockPos.getZ() + 0.5f) / 2.0f;
	                            if (this.isLocalImmune()) {
	                                localDamage = -1;
	                            }

	                            if (currentDamage > damage && currentDamage >= this.minDamage.getValue() && localDamage <= currentDamage) {
	                                damage = currentDamage;
	                                this.currentPlacePosition = blockPos;
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        if (this.currentPlacePosition != null && damage > 0) {
	            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(this.currentPlacePosition.getX() + 0.5f, this.currentPlacePosition.getY() + 0.5f, this.currentPlacePosition.getZ() + 0.5f));

	            Seppuku.INSTANCE.getRotationManager().startTask(this.placeRotationTask);
	            if (this.placeRotationTask.isOnline() || this.placeRapid.getValue()) {
	                Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
	            }
	        }
	    }

	    private boolean isLocalImmune() {
	        final Minecraft mc = Minecraft.getMinecraft();

	        if (mc.player.capabilities.isCreativeMode) {
	            return true;
	        }

	        final GodModeModule mod = (GodModeModule) Seppuku.INSTANCE.getModuleManager().find(GodModeModule.class);
	        if (mod != null && mod.isEnabled()) {
	            return true;
	        }

	        if (this.ignore.getValue()) {
	            return true;
	        }

	        return false;
	    }

	    private boolean canPlaceCrystal(BlockPos pos) {
	        final Minecraft mc = Minecraft.getMinecraft();
	        final Block block = mc.world.getBlockState(pos).getBlock();

	        if (this.placeSpread.getValue()) {
	            if (this.lastPlacePosition != null)
	                if (pos.getDistance(this.lastPlacePosition.getX(), this.lastPlacePosition.getY(), this.lastPlacePosition.getZ()) <= this.placeSpreadDistance.getValue())
	                    return false;
	        }

	        if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
	            final Block floor = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
	            final Block ceil = mc.world.getBlockState(pos.add(0, 2, 0)).getBlock();

	            if (floor == Blocks.AIR && ceil == Blocks.AIR) {
	                if (mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.add(0, 1, 0))).isEmpty()) {
	                    return mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) <= this.placeRadius.getValue();
	                }
	            }
	        }

	        return false;
	    }

	    private float calculateExplosionDamage(EntityLivingBase entity, float size, float x, float y, float z) {
	        final Minecraft mc = Minecraft.getMinecraft();
	        final float scale = size * 2.0F;
	        final Vec3d pos = MathUtil.interpolateEntity(entity, mc.getRenderPartialTicks());
	        final double dist = MathUtil.getDistance(pos, x, y, z) / (double) scale;
	        //final double dist = entity.getDistance(x, y, z) / (double) scale;
	        final Vec3d vec3d = new Vec3d(x, y, z);
	        final double density = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
	        final double densityScale = (1.0D - dist) * density;

	        float unscaledDamage = (float) ((int) ((densityScale * densityScale + densityScale) / 2.0d * 7.0d * (double) scale + 1.0d));

	        unscaledDamage *= 0.5f * mc.world.getDifficulty().getId();

	        return scaleExplosionDamage(entity, new Explosion(mc.world, null, x, y, z, size, false, true), unscaledDamage);
	    }

	    private float scaleExplosionDamage(EntityLivingBase entity, Explosion explosion, float damage) {
	        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
	        damage *= (1.0F - MathHelper.clamp(EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), DamageSource.causeExplosionDamage(explosion)), 0.0F, 20.0F) / 25.0F);
	        return damage;
	    }

	    private static final class PlaceLocation extends Vec3i {

	        private int alpha = 0xAA;
	        private boolean placed = false;
	        private float damage = -1;

	        private PlaceLocation(int xIn, int yIn, int zIn, float damage) {
	            super(xIn, yIn, zIn);
	            this.damage = damage;
	        }

	        private PlaceLocation(int xIn, int yIn, int zIn) {
	            super(xIn, yIn, zIn);
	        }

	        private void update() {
	            if (this.alpha > 0)
	                this.alpha -= 2;
	        }
	    }

	    public Timer getAttackTimer() {
	        return attackTimer;
	    }

	    public Timer getPlaceTimer() {
	        return placeTimer;
	    }

	    public List<PlaceLocation> getPlaceLocations() {
	        return placeLocations;
	    }

	    public RotationTask getPlaceRotationTask() {
	        return placeRotationTask;
	    }

	    public RotationTask getAttackRotationTask() {
	        return attackRotationTask;
	    }

	    public BlockPos getCurrentPlacePosition() {
	        return currentPlacePosition;
	    }

	    public void setCurrentPlacePosition(BlockPos currentPlacePosition) {
	        this.currentPlacePosition = currentPlacePosition;
	    }

	    public BlockPos getLastPlacePosition() {
	        return lastPlacePosition;
	    }

	    public void setLastPlacePosition(BlockPos lastPlacePosition) {
	        this.lastPlacePosition = lastPlacePosition;
	    }

	    public Entity getCurrentAttackEntity() {
	        return currentAttackEntity;
	    }

	    public Entity getCurrentAttackPlayer() {
	        return currentAttackPlayer;
	    }

	    public void setCurrentAttackEntity(Entity currentAttackEntity) {
	        this.currentAttackEntity = currentAttackEntity;
	    }
	}



	 

