package stcam.stcamproject.View;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.model.DevModel;
import com.thSDK.lib;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import stcam.stcamproject.Util.TFun;

public class GLSurfaceViewLive extends GLSurfaceView {
    //public actPlayLive Activity;
    DevModel model;

    public GLSurfaceViewLive(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRenderer(new MyRenderer());
        requestFocus();
        setFocusableInTouchMode(true);
    }

    public void setModel(DevModel m) {
        model = m;
    }

    public void Play() {
        DevModel.threadStartPlay(null, model);
    }

    public void Stop() {
        DevModel.threadStopPlay(null, model);
    }

    class MyRenderer implements GLSurfaceView.Renderer {
        boolean IsGetNodeIcon = false;

        @Override
        public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            TFun.printf("====onSurfaceChanged");
            lib.thNetOpenGLUpdateArea(model.NetHandle, 0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl)//约60fps/s
        {


            if (lib.thNetOpenGLRender(model.NetHandle)) {

                if (IsGetNodeIcon == false) {

                }
            }
        }

    }


}