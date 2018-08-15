package stcam.stcamproject.View;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.model.DevModel;

import static com.thSDK.lib.requestEGLChange;
import static com.thSDK.lib.requestEGLDestory;
import static com.thSDK.lib.requestInitEGL;

/**
 * Created by gyl
 */

public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public SurfaceHolder surfaceHolder;
    boolean isSurfaceExist = false;
    DevModel model;
    boolean hasCapture;
    boolean hasGotFirstFrame;
    private Handler mHandler;
    public VideoSurfaceView(Context context) {
        super(context);
        init();
    }

    public void  setmHandler(Handler mHandler){
        this.mHandler = mHandler;
    }

    public void setModel(DevModel m) {
        model = m;
    }



    public VideoSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        hasGotFirstFrame = false;
    }

    public void Play() {
        DevModel.threadStartPlay(null, model);
    }

    public void Stop() {
        DevModel.threadStopPlay(null, model);
    }


    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        isSurfaceExist = false;
        requestEGLDestory();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
       if (!isSurfaceExist){
           isSurfaceExist = true;
           //初始化surface
           requestInitEGL(holder.getSurface());
       }
       else{
           //调用jni surface change 接口
           requestEGLChange(holder.getSurface());
       }


    }
    private static String TAG = "VideoSurfaceView";
}