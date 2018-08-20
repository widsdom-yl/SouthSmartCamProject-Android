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

public class SurfaceViewPlayBack2 extends SurfaceView implements SurfaceHolder.Callback
{
  public SurfaceHolder surfaceHolder;
  boolean isSurfaceExist = false;

  DevModel mDevModel;
  boolean hasCapture;
  boolean hasGotFirstFrame;
  private Handler mHandler;
  SDVideoModel mSDVideoModel;
  public SurfaceViewPlayBack2(Context context)
  {
    super(context);
    init();
  }

  public void setmHandler(Handler mHandler)
  {
    this.mHandler = mHandler;
  }


  public void setModel(DevModel devModel, SDVideoModel sdVideoModel)
  {
    mDevModel = devModel;
    mSDVideoModel = sdVideoModel;
  }

  public SurfaceViewPlayBack2(Context context, AttributeSet attributeSet)
  {
    super(context, attributeSet);
    init();
  }

  private void init()
  {
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


  public void surfaceCreated(SurfaceHolder holder)
  {
    lib.thNetEGLCreate(mDevModel.NetHandle, surfaceHolder.getSurface());
  }

  public void surfaceDestroyed(SurfaceHolder holder)
  {
    lib.thNetEGLFree(mDevModel.NetHandle);
    isSurfaceExist = false;
    //requestEGLDestory();
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
  {
//       if (!isSurfaceExist){
//           isSurfaceExist = true;
//           //初始化surface
//           requestEGLInit(surfaceHolder.getSurface(),model.NetHandle);
//       }
//       else{
//           //调用jni surface change 接口
//           requestEGLChange(surfaceHolder.getSurface());
//       }
    lib.thNetOpenGLUpdateArea(mDevModel.NetHandle, 0, 0, w, h);
  }

  private static String TAG = "VideoSurfaceView";
}