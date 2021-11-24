package formats.bdhcam.animation;

import editor.handler.MapEditorHandler;
import formats.bdhcam.BdhcamHandler;
import formats.bdhcam.camplate.CamParameter;
import formats.bdhcam.camplate.CamParameterPosIndep;
import formats.bdhcam.camplate.CameraSettings;
import formats.bdhcam.camplate.CamplatePosIndep;

public class CamAnimPosIndep extends CamAnimator{

    private CamplatePosIndep plate;
    private int frame = 0;
    private int finalFrame = 0;

    public CamAnimPosIndep(MapEditorHandler handler, BdhcamHandler bdhcamHandler, CamplatePosIndep plate) {
        super(handler, bdhcamHandler);
        this.plate = plate;
        for(CamParameter param : plate.parameters){
            CamParameterPosIndep paramPosIndep = (CamParameterPosIndep) param;
            if(paramPosIndep.duration > finalFrame){
                finalFrame = paramPosIndep.duration;
            }
        }
    }

    @Override
    protected void updateLogic() {
        bdhcamHandler.getDialog().getBdhcamDisplay().setCamera(new CameraSettings(plate, frame));
        frame++;
        if(frame >= finalFrame){
            running = false;
        }
    }

    @Override
    protected void repaint() {
        bdhcamHandler.getDialog().getBdhcamDisplay().repaint();
    }
}
