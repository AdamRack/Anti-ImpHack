package mod.imphack.event.events;

import mod.imphack.event.ImpHackEventCancellable;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class ImpHackEventAddCollisionBox extends ImpHackEventCancellable {

    private BlockPos pos;
    private Entity entity;

    public ImpHackEventAddCollisionBox(BlockPos pos, Entity entity) {
        this.pos = pos;
        this.entity = entity;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}