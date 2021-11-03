package mod.imphack.event.events;

import mod.imphack.event.ImpHackEventCancellable;

public class ImpHackEventRenderOverlay extends ImpHackEventCancellable {

    private OverlayType type;

    public ImpHackEventRenderOverlay(OverlayType type) {
        this.type = type;
    }

    public OverlayType getType() {
        return type;
    }

    public void setType(OverlayType type) {
        this.type = type;
    }

    public enum OverlayType {
        BLOCK,
        LIQUID,
        FIRE
    }

}