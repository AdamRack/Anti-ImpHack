package mod.supergamer5465.sc.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import mod.supergamer5465.sc.Main;
import mod.supergamer5465.sc.event.ScEventBus;
import mod.supergamer5465.sc.event.events.ScEventMotionUpdate;
import mod.supergamer5465.sc.module.Category;
import mod.supergamer5465.sc.module.Module;
import mod.supergamer5465.sc.setting.settings.BooleanSetting;
import mod.supergamer5465.sc.util.BlockUtil;
import mod.supergamer5465.sc.util.EntityUtil;
import mod.supergamer5465.sc.util.InventoryUtil;
import mod.supergamer5465.sc.util.MathUtil;
import mod.supergamer5465.sc.util.Timer;
import mod.supergamer5465.sc.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

public class Scaffold extends Module {

	BooleanSetting rotation = new BooleanSetting("Rotation", this, true);
	private final Timer timer = new Timer();

	public Scaffold() {
		super("Scaffold", "Places Blocks Below You", Category.PLAYER);

		addSetting(rotation);
	}

	@Override
	public void onEnable() {

		MinecraftForge.EVENT_BUS.register(this);

		ScEventBus.EVENT_BUS.subscribe(this);

		Main.config.Save();

		timer.reset();
	}

	@EventHandler
	private Listener<ScEventMotionUpdate> player_move = new Listener<>(event -> {

		BlockPos playerBlock;
		if (mc.world == null || event.stage == 0) {
			return;
		}
		if (!mc.gameSettings.keyBindJump.isKeyDown()) {
			this.timer.reset();
		}
		if (BlockUtil.isScaffoldPos((playerBlock = EntityUtil.getPlayerPosWithEntity()).add(0, -1, 0))) {
			if (BlockUtil.isValidBlock(playerBlock.add(0, -2, 0))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.UP);
			} else if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 0))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.EAST);
			} else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 0))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.WEST);
			} else if (BlockUtil.isValidBlock(playerBlock.add(0, -1, -1))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.SOUTH);
			} else if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.NORTH);
			} else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
				if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
					this.place(playerBlock.add(0, -1, 1), EnumFacing.NORTH);
				}
				this.place(playerBlock.add(1, -1, 1), EnumFacing.EAST);
			} else if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 1))) {
				if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 0))) {
					this.place(playerBlock.add(0, -1, 1), EnumFacing.WEST);
				}
				this.place(playerBlock.add(-1, -1, 1), EnumFacing.SOUTH);
			} else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
				if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
					this.place(playerBlock.add(0, -1, 1), EnumFacing.SOUTH);
				}
				this.place(playerBlock.add(1, -1, 1), EnumFacing.WEST);
			} else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
				if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
					this.place(playerBlock.add(0, -1, 1), EnumFacing.EAST);
				}
				this.place(playerBlock.add(1, -1, 1), EnumFacing.NORTH);
			}
		}
	});

	public void place(BlockPos posI, EnumFacing face) {
		BlockPos pos = posI;
		if (face == EnumFacing.UP) {
			pos = pos.add(0, -1, 0);
		} else if (face == EnumFacing.NORTH) {
			pos = pos.add(0, 0, 1);
		} else if (face == EnumFacing.SOUTH) {
			pos = pos.add(0, 0, -1);
		} else if (face == EnumFacing.EAST) {
			pos = pos.add(-1, 0, 0);
		} else if (face == EnumFacing.WEST) {
			pos = pos.add(1, 0, 0);
		}
		int oldSlot = mc.player.inventory.currentItem;
		int newSlot = -1;
		for (int i = 0; i < 9; ++i) {
			ItemStack stack = mc.player.inventory.getStackInSlot(i);
			if (InventoryUtil.isItemStackNull(stack) || !(stack.getItem() instanceof ItemBlock)
					|| !Block.getBlockFromItem(stack.getItem()).getDefaultState().isFullBlock())
				continue;
			newSlot = i;
			break;
		}
		if (newSlot == -1) {
			return;
		}
		boolean crouched = false;
		if (!mc.player.isSneaking()
				&& WorldUtil.RIGHTCLICKABLE_BLOCKS.contains(mc.world.getBlockState(pos).getBlock())) {
			mc.player.connection
					.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
			crouched = true;
		}
		if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
			mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
			mc.player.inventory.currentItem = newSlot;
			mc.playerController.updateController();
		}
		if (mc.gameSettings.keyBindJump.isKeyDown()) {
			mc.player.motionX *= 0.3;
			mc.player.motionZ *= 0.3;
			mc.player.jump();
			if (this.timer.getPassedMillis(1500L)) {
				mc.player.motionY = -0.28;
				this.timer.reset();
			}
		}
		if (this.rotation.enabled) {
			float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()),
					new Vec3d((float) pos.getX() + 0.5f, (float) pos.getY() - 0.5f, (float) pos.getZ() + 0.5f));
			mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0],
					(float) MathHelper.normalizeAngle((int) angle[1], 360), mc.player.onGround));
		}
		mc.playerController.processRightClickBlock(mc.player, mc.world, pos, face, new Vec3d(0.5, 0.5, 0.5),
				EnumHand.MAIN_HAND);
		mc.player.swingArm(EnumHand.MAIN_HAND);
		mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
		mc.player.inventory.currentItem = oldSlot;
		mc.playerController.updateController();
		if (crouched) {
			mc.player.connection
					.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
		}
	}
}