package stcam.stcamproject.View;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.model.DevModel;
import com.model.SDVideoModel;
import com.thSDK.lib;

import stcam.stcamproject.Util.FileUtil;

import static com.thSDK.lib.videoPlay;

/**
 * Created by gyl
 */

public class PlayBackVideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public SurfaceHolder surfaceHolder;
    boolean isSurfaceExist = false;

    DevModel mDevModel;
    boolean hasCapture;
    boolean hasGotFirstFrame;
    private Handler mHandler;
    SDVideoModel mSDVideoModel;
    String videoPath = FileUtil.getSDRootPath() + "/video/111.mp4";

    public PlayBackVideoSurfaceView(Context context) {
        super(context);
        init();
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }



    public void setModel(DevModel devModel, SDVideoModel sdVideoModel)
    {
        mDevModel = devModel;
        mSDVideoModel = sdVideoModel;
    }

    public PlayBackVideoSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        hasGotFirstFrame = false;
        //  this.setBackgroundColor(Color.TRANSPARENT);
    }

    public void Play()
    {
        lib.thNetRemoteFilePlay(mDevModel.NetHandle, mSDVideoModel.sdVideo);
        lib.thNetAudioPlayOpen(mDevModel.NetHandle);
    }

    public void Stop()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                lib.thNetRemoteFileStop(mDevModel.NetHandle);
                lib.thNetAudioPlayClose(mDevModel.NetHandle);
            }
        }.start();
    }


    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        isSurfaceExist = false;
        //requestEGLDestory();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//       if (!isSurfaceExist){
//           isSurfaceExist = true;
//           //初始化surface
//           requestInitEGL(surfaceHolder.getSurface(),model.NetHandle);
//       }
//       else{
//           //调用jni surface change 接口
//           requestEGLChange(surfaceHolder.getSurface());
//       }

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                videoPlay(videoPath, surfaceHolder.getSurface());
            }
        };
        thread.start();


    }

    private static String TAG = "VideoSurfaceView";
}