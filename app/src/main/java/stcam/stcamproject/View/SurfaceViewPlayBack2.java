package stcam.stcamproject.View;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.model.DevModel;
import com.model.SDVideoModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

public class SurfaceViewPlayBack2 extends SurfaceView implements SurfaceHolder.Callback
{
  public SurfaceHolder surfaceHolder;
  boolean isSurfaceExist = false;

  DevModel mDevModel;
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
    //lib.thNetRemoteFilePlay(mDevModel.NetHandle, mSDVideoModel.sdVideo);
    //lib.thNetAudioPlayOpen(mDevModel.NetHandle);
    new Thread()
    {
      @Override
      public void run()
      {
        lib.thNetRemoteFilePlay(mDevModel.NetHandle, mSDVideoModel.sdVideo);
        lib.thNetAudioPlayOpen(mDevModel.NetHandle);
      }
    }.start();
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
    lib.thOpenGLCreateEGL(mDevModel.NetHandle, surfaceHolder.getSurface());

    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (true)
        {
          hasGotFirstFrame = lib.thOpenGLIsRenderSuccess(mDevModel.NetHandle);
          if (hasGotFirstFrame)
          {
            if (mHandler != null)
            {
              mHandler.sendMessage(Message.obtain(mHandler, TMsg.Msg_GotFirstFrame, null));
            }
            return;
          }
        }
      }
    }).start();
  }

  public void surfaceDestroyed(SurfaceHolder holder)
  {
    lib.thOpenGLFreeEGL(mDevModel.NetHandle);
    isSurfaceExist = false;
    //requestEGLDestory();
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
  {
    lib.thOpenGLSurfaceChanged(mDevModel.NetHandle, w, h);
  }

  private static String TAG = "VideoSurfaceView";
}