package mod.imphack.module.modules.render;

import me.zero.alpine.event.type.Cancellable;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import mod.imphack.event.ImpHackEventCancellable.Era;
import mod.imphack.event.events.ImpHackEventAddCollisionBox;
import mod.imphack.event.events.ImpHackEventApplyCollision;
import mod.imphack.event.events.ImpHackEventLiquidCollisionBB;
import mod.imphack.event.events.ImpHackEventMotionUpdate;
import mod.imphack.event.events.ImpHackEventMove;
import mod.imphack.event.events.ImpHackEventPacket;
import mod.imphack.event.events.ImpHackEventPush;
import mod.imphack.event.events.ImpHackEventPushOutOfBlocks;
import mod.imphack.event.events.ImpHackEventPushedByWater;
import mod.imphack.event.events.ImpHackEventRenderHelmet;
import mod.imphack.event.events.ImpHackEventRenderOverlay;
import mod.imphack.event.events.ImpHackEventSetOpaqueCube;
import mod.imphack.module.Category;
import mod.imphack.module.Module;
import mod.imphack.setting.settings.BooleanSetting;
import mod.imphack.setting.settings.FloatSetting;
import mod.imphack.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class Freecam extends Module {

	final FloatSetting speedSetting = new FloatSetting("Speed", this, 1.0f);
	final BooleanSetting view = new BooleanSetting("3D", this, false);
	final BooleanSetting packet = new BooleanSetting("Packet", this, true);
	final BooleanSetting allowDismount = new BooleanSetting("AllowDismount", this, true);


	public Freecam() {
		super("Freecam", "Allows Spectator Mode Outside The Body", Category.RENDER);

		addSetting(speedSetting);
		addSetting(view);
		addSetting(packet);
		addSetting(allowDismount);
		
	}


    private Entity riding;
    private EntityOtherPlayerMP entity;
    private Vec3d position;
    private float yaw;
    private float pitch;
    
    
    @Override
    public void onEnable() {
        super.onEnable();
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            this.entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            this.entity.copyLocationAndAnglesFrom(mc.player);
            if (mc.player.getRidingEntity() != null) {
                this.riding = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
                this.entity.startRiding(this.riding);
            } else {
                this.riding = null;
            }
            this.entity.rotationYaw = mc.player.rotationYaw;
            this.entity.rotationYawHead = mc.player.rotationYawHead;
            this.entity.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(69420, this.entity);
            this.position = mc.player.getPositionVector();
            this.yaw = mc.player.rotationYaw;
            this.pitch = mc.player.rotationPitch;
            mc.player.noClip = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            if (this.riding != null) {
                mc.player.startRiding(this.riding, true);
            }
            if (this.entity != null) {
                mc.world.removeEntity(this.entity);
            }
            if (this.position != null) {
                mc.player.setPosition(this.position.x, this.position.y, this.position.z);
            }
            mc.player.rotationYaw = this.yaw;
            mc.player.rotationPitch = this.pitch;
            mc.player.noClip = false;
            mc.player.motionX = 0;
            mc.player.motionY = 0;
            mc.player.motionZ = 0;
        }
    }

    @EventHandler
	private final Listener<ImpHackEventMove> moveListener = new Listener<>(event -> {
        Minecraft.getMinecraft().player.noClip = true;
    });


    public void onWalkingUpdate(ImpHackEventMotionUpdate event) {
        if (event.get_era() == Era.EVENT_PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            mc.player.setVelocity(0, 0, 0);
            mc.player.renderArmPitch = 5000;
            mc.player.jumpMovementFactor = this.speedSetting.getValue();

            final double[] dir = MathUtil.directionSpeed(this.speedSetting.getValue());

            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            } else {
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
            }

            mc.player.setSprinting(false);

            if (this.view.isEnabled()) {
                if (!mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY = (this.speedSetting.getValue() * (-MathUtil.degToRad(mc.player.rotationPitch))) * mc.player.movementInput.moveForward;
                }
            }

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motionY += this.speedSetting.getValue();
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motionY -= this.speedSetting.getValue();
            }
        }
    }

    @EventHandler
	private final Listener<ImpHackEventPacket.SendPacket> sendPacket = new Listener<>(event -> {
        if (event.get_era() == Era.EVENT_PRE) {
            if (Minecraft.getMinecraft().world != null) {
                if (!this.allowDismount.isEnabled()) {
                    if (event.get_packet() instanceof CPacketInput) {
                        event.setCanceled(true);
                    }
                    if (event.get_packet() instanceof CPacketEntityAction) {
                        CPacketEntityAction packetEntityAction = (CPacketEntityAction) event.get_packet();
                        if (packetEntityAction.getAction().equals(CPacketEntityAction.Action.START_SNEAKING)) {
                            event.setCanceled(true);
                        }
                    }
                }

                if (this.packet.isEnabled()) {
                    if (event.get_packet() instanceof CPacketPlayer) {
                        event.setCanceled(true);
                    }
                } else {
                    if (!(event.get_packet() instanceof CPacketUseEntity) && !(event.get_packet() instanceof CPacketPlayerTryUseItem) && !(event.get_packet() instanceof CPacketPlayerTryUseItemOnBlock) && !(event.get_packet() instanceof CPacketPlayer) && !(event.get_packet() instanceof CPacketVehicleMove) && !(event.get_packet() instanceof CPacketChatMessage) && !(event.get_packet() instanceof CPacketKeepAlive)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
	});

    @EventHandler
	private final Listener<ImpHackEventPacket.ReceivePacket> recievePacket = new Listener<>(event -> {
        if (event.get_era() == Era.EVENT_PRE) {
            if (event.get_packet() instanceof SPacketSetPassengers) {
                final SPacketSetPassengers packet = (SPacketSetPassengers) event.get_packet();
                final Entity riding = Minecraft.getMinecraft().world.getEntityByID(packet.getEntityId());

                if (riding != null && riding == this.riding) {
                    this.riding = null;
                }
            }
            if (event.get_packet() instanceof SPacketPlayerPosLook) {
                final SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.get_packet();
                if (this.packet.isEnabled()) {
                    if (this.entity != null) {
                        this.entity.setPositionAndRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
                    }
                    this.position = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                    Minecraft.getMinecraft().player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
                    event.setCanceled(true);
                } else {
                    event.setCanceled(true);
                }
            }
        }
	});
	

    
    public void collideWithBlock(ImpHackEventAddCollisionBox event) {
        
    }

    public void getLiquidCollisionBB(ImpHackEventLiquidCollisionBB event) {
        event.setBoundingBox(Block.NULL_AABB);
        event.setCanceled(true);
    }

    public void setOpaqueCube(ImpHackEventSetOpaqueCube event) {
        event.setCanceled(true);
    }

    public void renderOverlay(ImpHackEventRenderOverlay event) {
        event.setCanceled(true);
    }

    public void renderHelmet(ImpHackEventRenderHelmet event) {
        event.setCanceled(true);
    }
    public void pushOutOfBlocks(ImpHackEventPushOutOfBlocks event) {
        event.setCanceled(true);
    }

    public void pushedByWater(ImpHackEventPushedByWater event) {
        event.setCanceled(true);
    }

    public void applyCollision(ImpHackEventApplyCollision event) {
        event.setCanceled(true);

        //OLD
    
    }

}
