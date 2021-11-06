package mod.imphack.module.modules.render;

import mod.imphack.event.events.ImpHackRenderItemEvent;
import mod.imphack.module.Category;
import mod.imphack.module.Module;
import mod.imphack.setting.settings.FloatSetting;

public class ViewModel extends Module {
	

	
	public ViewModel() {
		super("viewModel", "allows u to change how ur model look in 1st person." , Category.RENDER);
		addSetting(mainX, mainY, mainZ, offX, offY, offZ, mainAngel ,mainRx ,mainRy ,mainRz ,offAngel ,offRx , offRy,offRz
				,mainScaleX , mainScaleY, mainScaleZ,mainScaleZ,offScaleX ,offScaleY ,offScaleZ);
	}


    FloatSetting mainX = new FloatSetting("mainX" , this, 1.2f);
    FloatSetting mainY = new FloatSetting("mainY" , this, -0.95f);
    FloatSetting mainZ = new FloatSetting("mainZ" , this, -1.45f);
    FloatSetting offX = new FloatSetting("offX" , this, -1.2f);
    FloatSetting offY = new FloatSetting("offY" , this, -0.95f);
    FloatSetting offZ = new FloatSetting("offZ" , this, -1.45f);
    FloatSetting mainAngel = new FloatSetting("mainAngle" , this, 0.0f);
    FloatSetting mainRx = new FloatSetting("mainRotationPointX" , this, 0.0f);
    FloatSetting mainRy = new FloatSetting("mainRotationPointY" , this, 0.0f);
    FloatSetting mainRz = new FloatSetting("mainRotationPointZ" , this, 0.0f);
    FloatSetting offAngel = new FloatSetting("offAngle" , this, 0.0f);
    FloatSetting offRx = new FloatSetting("offRotationPointX" , this, 0.0f);
    FloatSetting offRy = new FloatSetting("offRotationPointY" , this, 0.0f);
    FloatSetting offRz = new FloatSetting("offRotationPointZ" , this, 0.0f);
    FloatSetting mainScaleX = new FloatSetting("mainScaleX" , this, 1.0f);
    FloatSetting mainScaleY = new FloatSetting("mainScaleY" , this, 1.0f);
    FloatSetting mainScaleZ = new FloatSetting("mainScaleZ", this, 1.0f);
    FloatSetting offScaleX = new FloatSetting("offScaleX", this, 1.0f);
    FloatSetting offScaleY = new FloatSetting("offScaleY", this, 1.0f);
    FloatSetting offScaleZ = new FloatSetting("offScaleZ", this, 1.0f);


    
    public void onItemRender(ImpHackRenderItemEvent event) {
        event.setMainX(mainX.getValue());
        event.setMainY(mainY.getValue());
        event.setMainZ(mainZ.getValue());

        event.setOffX(offX.getValue());
        event.setOffY(offY.getValue());
        event.setOffZ(offZ.getValue());

        event.setMainRAngel(mainAngel.getValue());
        event.setMainRx(mainRx.getValue());
        event.setMainRy(mainRy.getValue());
        event.setMainRz(mainRz.getValue());

        event.setOffRAngel(offAngel.getValue());
        event.setOffRx(offRx.getValue());
        event.setOffRy(offRy.getValue());
        event.setOffRz(offRz.getValue());

        event.setMainHandScaleX(mainScaleX.getValue());
        event.setMainHandScaleY(mainScaleY.getValue());
        event.setMainHandScaleZ(mainScaleZ.getValue());

        event.setOffHandScaleX(offScaleX.getValue());
        event.setOffHandScaleY(offScaleY.getValue());
        event.setOffHandScaleZ(offScaleZ.getValue());
    }
}