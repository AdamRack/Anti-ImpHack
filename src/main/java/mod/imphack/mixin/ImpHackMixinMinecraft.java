package mod.imphack.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.imphack.ui.ImpHackSplashScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;

@Mixin(value = { Minecraft.class })
public abstract class ImpHackMixinMinecraft {

	@Shadow
	public abstract void displayGuiScreen(@Nullable GuiScreen var1);

	@Inject(method = { "displayGuiScreen" }, at = { @At(value = "HEAD") })
	private void displayGuiScreen(GuiScreen screen, CallbackInfo ci) {
		if (screen instanceof GuiMainMenu) {
			this.displayGuiScreen(new ImpHackSplashScreen());
		}
	}

	@Inject(method = { "runTick()V" }, at = { @At(value = "RETURN") })
	private void runTick(CallbackInfo callbackInfo) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu) {
			Minecraft.getMinecraft().displayGuiScreen(new ImpHackSplashScreen());
		}
	}
}
