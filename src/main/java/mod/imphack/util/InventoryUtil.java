package mod.imphack.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;

public class InventoryUtil {
	
	  private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static boolean isItemStackNull(final ItemStack stack) {
		return stack == null || stack.getItem() instanceof ItemAir;
	}
	
	
	  public static int getBlockInHotbar(Block block) {
	        for (int i = 0; i < 9; i++) {
	            Item item = mc.player.inventory.getStackInSlot(i).getItem();
	            if (item instanceof ItemBlock && ((ItemBlock) item).getBlock().equals(block))
	                return i;
	        }

	        return -1;
	    }
	  
	   public static int getAnyBlockInHotbar() {
	        for (int i = 0; i < 9; i++) {
	            Item item = mc.player.inventory.getStackInSlot(i).getItem();
	            if (item instanceof ItemBlock)
	                return i;
	        }

	        return -1;
	    }
	   
	   public static void switchToSlot(int slot) {
	        if (slot != -1 && mc.player.inventory.currentItem != slot)
	            mc.player.inventory.currentItem = slot;
	    }

	    public static void switchToSlot(Block block) {
	        if (getBlockInHotbar(block) != -1 && mc.player.inventory.currentItem != getBlockInHotbar(block))
	            mc.player.inventory.currentItem = getBlockInHotbar(block);
	    }

	    public static void switchToSlot(Item item) {
	        if (getHotbarItemSlot(item) != -1 && mc.player.inventory.currentItem != getHotbarItemSlot(item))
	            mc.player.inventory.currentItem = getHotbarItemSlot(item);
	    }

	    public static void switchToSlotGhost(int slot) {
	        if (slot != -1 && mc.player.inventory.currentItem != slot)
	            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
	    }

	    public static void switchToSlotGhost(Block block) {
	        if (getBlockInHotbar(block) != -1 && mc.player.inventory.currentItem != getBlockInHotbar(block))
	            mc.player.connection.sendPacket(new CPacketHeldItemChange(getBlockInHotbar(block)));
	    }

	    public static void switchToSlotGhost(Item item) {
	        if (getHotbarItemSlot(item) != -1 && mc.player.inventory.currentItem != getHotbarItemSlot(item))
	            switchToSlotGhost(getHotbarItemSlot(item));
	    }
	    
	    public static int getHotbarItemSlot(Item item) {
	        for (int i = 0; i < 9; i++) {
	            if (mc.player.inventory.getStackInSlot(i).getItem() == item)
	                return i;
	        }

	        return -1;
	    }
	    
	    public static boolean getHeldItem(Item item) {
	        return mc.player.getHeldItemMainhand().getItem().equals(item) || mc.player.getHeldItemOffhand().getItem().equals(item);
	    }

}