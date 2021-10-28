package mod.imphack.module.modules.render;

import java.awt.Font;

import org.lwjgl.opengl.GL11;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import mod.imphack.event.events.ImpHackEventRender;
import mod.imphack.event.events.ImpHackEventRenderEntityName;
import mod.imphack.module.Category;
import mod.imphack.module.Module;
import mod.imphack.setting.settings.BooleanSetting;
import mod.imphack.setting.settings.FloatSetting;
import mod.imphack.setting.settings.IntSetting;
import mod.imphack.util.font.FontUtils;
import mod.imphack.util.render.ColorUtil;
import mod.imphack.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

public class Nametags extends Module {
	FloatSetting scaleSetting = new FloatSetting("Scale", this, 10.0f);
	BooleanSetting renderSelf = new BooleanSetting("self", this, true);
	IntSetting range = new IntSetting("Range", this, 150);
	BooleanSetting items = new BooleanSetting("items", this, true);
	BooleanSetting durability = new BooleanSetting("durability", this, true);
	BooleanSetting protType = new BooleanSetting("protType", this, true);
	BooleanSetting health = new BooleanSetting("health", this, true);
	BooleanSetting ping = new BooleanSetting("ping", this, true);

	public Nametags() {
		super("Nametags", "Adds More Features To Nametags", Category.RENDER);

		addSetting(renderSelf);
		addSetting(scaleSetting);
		addSetting(range);
		addSetting(items);
		addSetting(durability);
		addSetting(protType);
		addSetting(health);
		addSetting(ping);

	}

	public static FontUtils font1 = new FontUtils("Confortaa", Font.PLAIN, 15);

	@Override
	public void render(ImpHackEventRender event) {
		if (mc.player == null || mc.world == null)
			return;

		mc.world.playerEntities.stream().filter(this::shouldRender).forEach(entityPlayer -> {
			Vec3d vec3d = findEntityVec3d(entityPlayer);
			renderNameTags(entityPlayer, vec3d.x, vec3d.y, vec3d.z);
		});
	}

	private void renderNameTags(EntityPlayer entityPlayer, double posX, double posY, double posZ) {
		double adjustedY = posY + (entityPlayer.isSneaking() ? 1.9 : 2.1);

		String[] name = new String[1];
		name[0] = buildEntityNameString(entityPlayer);

		RenderUtil.drawNametag(posX, adjustedY, posZ, name, new ColorUtil(255, 255, 255, 255), 2);
		renderItemsPos(entityPlayer, 0, 0);
		GlStateManager.popMatrix();
	}

	// utils
	private boolean shouldRender(EntityPlayer entityPlayer) {
		if (entityPlayer == mc.player && !renderSelf.isEnabled())
			return false;

		if (entityPlayer.isDead || entityPlayer.getHealth() <= 0)
			return false;

		return !(entityPlayer.getDistance(mc.player) > range.getValue());
	}

	private Vec3d findEntityVec3d(EntityPlayer entityPlayer) {
		double posX = balancePosition(entityPlayer.posX, entityPlayer.lastTickPosX);
		double posY = balancePosition(entityPlayer.posY, entityPlayer.lastTickPosY);
		double posZ = balancePosition(entityPlayer.posZ, entityPlayer.lastTickPosZ);

		return new Vec3d(posX, posY, posZ);
	}

	private double balancePosition(double newPosition, double oldPosition) {
		return oldPosition + (newPosition - oldPosition) * mc.getRenderPartialTicks();
	}

	private TextFormatting healthColor(int health) {
		if (health <= 0) {
			return TextFormatting.DARK_RED;
		} else if (health <= 5) {
			return TextFormatting.RED;
		} else if (health <= 10) {
			return TextFormatting.GOLD;
		} else if (health <= 15) {
			return TextFormatting.YELLOW;
		} else if (health <= 20) {
			return TextFormatting.DARK_GREEN;
		}
		return TextFormatting.GREEN;
	}

	// render text
	private String buildEntityNameString(EntityPlayer entityPlayer) {
		String name = entityPlayer.getName();
		if (ping.isEnabled()) {
			int value = 0;

			if (mc.getConnection() != null && mc.getConnection().getPlayerInfo(entityPlayer.getUniqueID()) != null) {
				value = mc.getConnection().getPlayerInfo(entityPlayer.getUniqueID()).getResponseTime();
			}
			name = name + " " + value + "ms";
		}
		if (health.isEnabled()) {
			int health = (int) (entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount());
			TextFormatting textFormatting = healthColor(health);

			name = name + " " + textFormatting + health;
		}

		return name;
	}

	// render items
	private void renderItem(ItemStack itemStack, int posX, int posY, int posY2) {
		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
		GlStateManager.enableDepth();
		GlStateManager.disableAlpha();

		final int posY3 = (posY2 > 4) ? ((posY2 - 4) * 8 / 2) : 0;

		mc.getRenderItem().zLevel = -150.0f;
		RenderHelper.enableStandardItemLighting();
		mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, posX, posY + posY3);
		mc.getRenderItem().renderItemOverlays(mc.fontRenderer, itemStack, posX, posY + posY3);
		RenderHelper.disableStandardItemLighting();
		mc.getRenderItem().zLevel = 0.0f;
		RenderUtil.prepare();
		GlStateManager.pushMatrix();
		GlStateManager.scale(.5, .5, .5);
		renderEnchants(itemStack, posX, posY - 24);
		GlStateManager.popMatrix();
	}

	private void renderItemDurability(ItemStack itemStack, int posX, int posY) {
		float damagePercent = (itemStack.getMaxDamage() - itemStack.getItemDamage()) / (float) itemStack.getMaxDamage();

		float green = damagePercent;
		if (green > 1)
			green = 1;
		else if (green < 0)
			green = 0;

		GlStateManager.enableTexture2D();
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.5, 0.5, 0.5);
		mc.fontRenderer.drawStringWithShadow((int) (damagePercent * 100) + "%", posX * 2, posY, 0xff00ff00);
		GlStateManager.popMatrix();
		GlStateManager.disableTexture2D();
	}

	// render item positions
	private void renderItemsPos(EntityPlayer entityPlayer, int posX, int posY) {
		ItemStack mainHandItem = entityPlayer.getHeldItemMainhand();
		ItemStack offHandItem = entityPlayer.getHeldItemOffhand();

		int armorCount = 3;
		for (int i = 0; i <= 3; i++) {
			ItemStack itemStack = entityPlayer.inventory.armorInventory.get(armorCount);

			if (!itemStack.isEmpty()) {
				posX -= 8;

				int size = EnchantmentHelper.getEnchantments(itemStack).size();

				if (items.isEnabled() && size > posY) {
					posY = size;
				}
			}
			armorCount--;
		}

		if (!mainHandItem.isEmpty()
				&& (items.isEnabled() || durability.isEnabled() && offHandItem.isItemStackDamageable())) {
			posX -= 8;

			int enchantSize = EnchantmentHelper.getEnchantments(offHandItem).size();
			if (items.isEnabled() && enchantSize > posY) {
				posY = enchantSize;
			}
		}

		if (!mainHandItem.isEmpty()) {
			int enchantSize = EnchantmentHelper.getEnchantments(mainHandItem).size();
			if (items.isEnabled() && enchantSize > posY) {
				posY = enchantSize;
			}
			int armorY = findArmorY(posY);
			if (items.isEnabled() || (durability.isEnabled() && mainHandItem.isItemStackDamageable())) {
				posX -= 8;
			}
			if (items.isEnabled()) {
				renderItem(mainHandItem, posX, armorY, posY);
				armorY -= 32;
			}
			if (durability.isEnabled() && mainHandItem.isItemStackDamageable()) {
				renderItemDurability(mainHandItem, posX, armorY);
			}
			armorY -= (mc.fontRenderer.FONT_HEIGHT);
			if (items.isEnabled() || (durability.isEnabled() && mainHandItem.isItemStackDamageable())) {
				posX += 16;
			}
		}

		int armorCount2 = 3;
		for (int i = 0; i <= 3; i++) {
			ItemStack itemStack = entityPlayer.inventory.armorInventory.get(armorCount2);

			if (!itemStack.isEmpty()) {
				int armorY = findArmorY(posY);
				if (items.isEnabled()) {
					renderItem(itemStack, posX, armorY, posY);
					armorY -= 32;
				}
				if (durability.isEnabled() && itemStack.isItemStackDamageable()) {
					renderItemDurability(itemStack, posX, armorY);
				}
				posX += 16;
			}
			armorCount2--;
		}

		if (!offHandItem.isEmpty()) {
			int armorY = findArmorY(posY);
			if (items.isEnabled()) {
				renderItem(offHandItem, posX, armorY, posY);
				armorY -= 32;
			}
			if (durability.isEnabled() && offHandItem.isItemStackDamageable()) {
				renderItemDurability(offHandItem, posX, armorY);
			}
		}
	}

	private int findArmorY(int posY) {
		int posY2 = durability.isEnabled() ? -26 : -27;
		if (posY > 4) {
			posY2 -= (posY - 4) * 8;
		}

		return posY2;
	}

	// enchantment

	private void renderEnchants(ItemStack itemStack, int posX, int posY) {
		GlStateManager.enableTexture2D();

		for (Enchantment enchantment : EnchantmentHelper.getEnchantments(itemStack).keySet()) {
			int level = EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack);

			if (enchantment == null) {
				continue;
			}

			if (protType.isEnabled()) {
				if (enchantment.equals(Enchantments.BLAST_PROTECTION))
					mc.fontRenderer.drawStringWithShadow(
							ChatFormatting.WHITE + findStringForEnchants(enchantment, level), posX * 2 + 13, posY + 0,
							0xffffffff);
			}
			if (enchantment.equals(Enchantments.PROTECTION))

				font1.drawString(findStringForEnchants(enchantment, level), posX * 2 + 13, posY + 0, 0xffffffff);

			if (enchantment.equals(Enchantments.MENDING))
				mc.fontRenderer.drawString(ChatFormatting.WHITE + findStringForEnchants(enchantment, level),
						posX * 2 + 13, posY + 5, 0xffffffff);
		}

		GlStateManager.disableTexture2D();
	}

	private String findStringForEnchants(Enchantment enchantment, int level) {
		ResourceLocation resourceLocation = Enchantment.REGISTRY.getNameForObject(enchantment);

		String string = resourceLocation == null ? enchantment.getName() : resourceLocation.toString();

		int charCount = (level > 1) ? 12 : 13;

		if (string.length() > charCount) {
			string = string.substring(10, charCount);
		}

		return string.substring(0, 1).toUpperCase() + string.substring(1) + ((level > 1) ? level : "");
	}

	@EventHandler
	private Listener<ImpHackEventRenderEntityName> player_nametag = new Listener<>(event -> {
		event.cancel();
	});

}