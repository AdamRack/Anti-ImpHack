package mod.imphack.event.events;

import mod.imphack.event.ImpHackEventCancellable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class ImpHackEventLiquidCollisionBB extends ImpHackEventCancellable {

    private AxisAlignedBB boundingBox;
    private BlockPos blockPos;

    public ImpHackEventLiquidCollisionBB() {

    }

    public ImpHackEventLiquidCollisionBB(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }
}