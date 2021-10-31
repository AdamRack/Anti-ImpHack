package mod.imphack.setting.settings;

import java.awt.Color;

import mod.imphack.Main;
import mod.imphack.module.Module;
import mod.imphack.setting.Setting;
import mod.imphack.util.render.ColorUtil;

public class ColorSetting extends Setting {
	public int red;
	public int green;
	public int blue;
	private boolean rainbow;
	private ColorUtil value;

	public ColorSetting(String name, Module parent, int red, int green, int blue) {
		this.name = name;
		this.parent = parent;
		if (!Main.configLoaded) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
	}
	
	public ColorUtil getValue() {
		if (rainbow) {
			return getRainbow(0, this.getColor().getAlpha());
		}
		return this.value;
	}

	public ColorUtil getColor() {
		return this.value;
	}
	
	public static ColorUtil getRainbow(int incr, int alpha) {
		ColorUtil color =  ColorUtil.fromHSB(((System.currentTimeMillis() + incr * 200)%(360*20))/(360f * 20),0.5f,1f);
		return new ColorUtil(color.getRed(), color.getBlue(), color.getGreen(), alpha);
	}
}

