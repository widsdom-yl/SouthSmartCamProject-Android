package stcam.stcamproject.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by gyl
 */

public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public SurfaceHolder surfaceHolder;
    boolean isSurfaceExist = true;
    public VideoSurfaceView(Context context) {
        super(context);
        init();
    }



    public VideoSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        isSurfaceExist = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
       if (!isSurfaceExist){
           isSurfaceExist = true;
           //初始化surface
       }
       else{
           //调用jni surface change 接口
       }


    }
    private static String TAG = "VideoSurfaceView";
}