package mod.imphack.module.modules.client;

import mod.imphack.module.Category;
import mod.imphack.module.Module;
import mod.imphack.module.ModuleManager;
import mod.imphack.setting.settings.ModeSetting;
import mod.imphack.util.ImpHackDiscordRichPresence;

public class DiscordRPC extends Module {

	public DiscordRPC() {
		super("DiscordRPC", "Rich Presence For Discord", Category.CLIENT);
		
		addSetting(mode);
	}
	
	public ModeSetting mode = new ModeSetting("Mode", this, "Imp", new String[] { "Vanilla", "Imp" });

	@Override
	public void onEnable() {
		ImpHackDiscordRichPresence.start(mode.getMode());
	}

	@Override
	public void onDisable() {
		ImpHackDiscordRichPresence.stop();
	}
}
