package mod.imphack.event.events;

import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import mod.imphack.Main;
import mod.imphack.event.ImpHackEventCancellable;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

public class ImpHackEventEntity extends ImpHackEventCancellable {
	
	
	@Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
	public void velocity(Entity entity, double x, double y, double z) {
		if (!Main.moduleManager.getModule("noPush").isEnabled()) {
			entity.motionX += x;
			entity.motionY += y;
			entity.motionZ += z;
			entity.isAirBorne = true;
		}
	}
	private Entity entity;

	public ImpHackEventEntity(Entity entity) {
		this.entity = entity;
	}

	public Entity get_entity() {
		return this.entity;
	}
	 public void set_Entity(Entity entity) {
	        this.entity = entity;
	    }
	
	public ImpHackEventEntity(Entity entityIn, ICamera camera, double camX, double camY, double camZ) {
        entity = entityIn;
    }

	public static class ScEventCollision extends ImpHackEventEntity {
		private double x, y, z;

		public ScEventCollision(Entity entity, double x, double y, double z) {
			super(entity);

			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void set_x(double x) {
			this.x = x;
		}

		public void set_y(double y) {
			this.y = y;
		}

		public void set_z(double z) {
			this.z = z;
		}

		public double get_x() {
			return this.x;
		}

		public double get_y() {
			return this.y;
		}

		public double get_z() {
			return this.z;
		}
	}
}