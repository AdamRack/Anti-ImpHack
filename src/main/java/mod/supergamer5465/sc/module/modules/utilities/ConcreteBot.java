package mod.supergamer5465.sc.module.modules.utilities;

import java.lang.reflect.Field;

import mod.supergamer5465.sc.Main;
import mod.supergamer5465.sc.event.ScEventBus;
import mod.supergamer5465.sc.module.Category;
import mod.supergamer5465.sc.module.Module;
import mod.supergamer5465.sc.setting.settings.IntSetting;
import mod.supergamer5465.sc.util.BlockInteractionUtil;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ConcreteBot extends Module {

	IntSetting x = new IntSetting("X", this, 0);
	IntSetting y = new IntSetting("Y", this, 0);
	IntSetting z = new IntSetting("Z", this, 0);

	public ConcreteBot() {
		super("ConcreteBot", "Turns Powder Into Concrete", Category.UTILITIES);

		addSetting(x);
		addSetting(y);
		addSetting(z);
	}

	private boolean breakBlock;

	@Override
	public void onUpdate() {

		int stage;

		if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe)) {
			for (int i = 0; i < 9; i++) {
				ItemStack stack = mc.player.inventory.getStackInSlot(i);
				if (stack.getItem() instanceof ItemPickaxe) {
					mc.player.inventory.currentItem = i;
					break;
				}
			}
		}

		if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe)) {
			this.toggle();
			return;
		}

		if (!(mc.player.getHeldItem(EnumHand.OFF_HAND)
				.getDisplayName() == new ItemStack(Item.getItemFromBlock(Blocks.CONCRETE_POWDER)).getDisplayName())) {
			for (int i = 0; i < 36; i++) {
				ItemStack stack = mc.player.inventory.getStackInSlot(i);
				Item block = Item.getItemFromBlock(Blocks.CONCRETE_POWDER);

				if (stack.getDisplayName() == new ItemStack(block).getDisplayName()) {
					inv.putInOffhand(stack);
					break;
				}
			}
		}

		if (!(mc.player.getHeldItem(EnumHand.OFF_HAND)
				.getDisplayName() == new ItemStack(Item.getItemFromBlock(Blocks.CONCRETE_POWDER)).getDisplayName())) {
			this.toggle();
			return;
		}

		if (mc.world.getBlockState(new BlockPos(x.value + 0.5, y.value + 0.5, z.value + 0.5)).getBlock()
				.equals(Blocks.CONCRETE)) {
			stage = 0;
		} else {
			stage = 1;
		}

		if (stage == 0) {
			float[] rotation = BlockInteractionUtil.getRotationsForPosition(x.value + 0.5, y.value + 0.5,
					z.value + 0.5);

			mc.player.rotationYaw = rotation[0];
			mc.player.rotationPitch = rotation[1];
			mc.player.rotationYawHead = rotation[0];

			breakBlock = true;

			return;
		}
		if (stage == 1) {
			float[] rotation = BlockInteractionUtil.getRotationsForPosition(x.value + 0.5, y.value + 0.5,
					z.value + 0.5);

			mc.player.rotationYaw = rotation[0];
			mc.player.rotationPitch = rotation[1];
			mc.player.rotationYawHead = rotation[0];

			mc.playerController.processRightClickBlock(mc.player, mc.world,
					new BlockPos(x.value + 0.5, y.value + 0.5, z.value + 0.5), EnumFacing.DOWN,
					mc.objectMouseOver.hitVec, EnumHand.OFF_HAND);

			return;
		}
	}

	@Override
	public void onEnable() {

		try {
			isHittingBlock = PlayerControllerMPClass.getDeclaredField("isHittingBlock");
			isHittingBlock.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(
					"Super Client error: missing field in class PlayerControllerMP " + e.getMessage());
		}

		MinecraftForge.EVENT_BUS.register(this);

		ScEventBus.EVENT_BUS.subscribe(this);

		Main.config.Save();
	}

	Field isHittingBlock;
	Class<PlayerControllerMP> PlayerControllerMPClass = PlayerControllerMP.class;

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (event.type != TickEvent.Type.CLIENT) {
			return;
		}
		if (mc.world == null || mc.player == null) {
			this.toggle();
			return;
		}

		if (breakBlock && mc.world.getBlockState(new BlockPos(x.value + 0.5, y.value + 0.5, z.value + 0.5)).getBlock()
				.equals(Blocks.CONCRETE)) {
			RayTraceResult trace = mc.objectMouseOver;
			boolean isBlockTrace = trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK;

			if (isBlockTrace) {
				if (mc.playerController.onPlayerDamageBlock(trace.getBlockPos(), trace.sideHit)) {
					mc.player.swingArm(EnumHand.MAIN_HAND);
				}
			}
		}
	}
}