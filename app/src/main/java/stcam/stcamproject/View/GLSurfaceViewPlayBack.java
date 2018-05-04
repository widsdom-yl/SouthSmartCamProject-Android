package stcam.stcamproject.View;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.model.DevModel;
import com.model.SDVideoModel;
import com.thSDK.lib;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import stcam.stcamproject.Util.TFun;



public class GLSurfaceViewPlayBack extends GLSurfaceView {
    //public actPlayLive Activity;
    DevModel mDevModel;
    SDVideoModel mSDVideoModel;

    public GLSurfaceViewPlayBack(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRenderer(new MyRenderer());
        requestFocus();
        setFocusableInTouchMode(true);
    }

    public void setModel(DevModel devModel, SDVideoModel sdVideoModel) {
        mDevModel = devModel;
        mSDVideoModel = sdVideoModel;
    }

    public void Play() {
       lib.thNetRemoteFilePlay(mDevModel.NetHandle,mSDVideoModel.sdVideo);
    }

    public void Stop() {
        lib.thNetRemoteFileStop(mDevModel.NetHandle);
    }

    class MyRenderer implements GLSurfaceView.Renderer {
        boolean IsGetNodeIcon = false;

        @Override
        public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            TFun.printf("====onSurfaceChanged");
            lib.thNetOpenGLUpdateArea(mDevModel.NetHandle, 0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl)//约60fps/s
        {


            if (lib.thNetOpenGLRender(mDevModel.NetHandle)) {

                if (IsGetNodeIcon == false) {

                }
            }
        }

    }


}
