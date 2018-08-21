package stcam.stcamproject.View;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.print.PrintHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.model.DevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import stcam.stcamproject.Util.FileUtil;

/**
 * Created by gyl
 */

public class SurfaceViewLive2 extends SurfaceView implements SurfaceHolder.Callback
{
  public SurfaceHolder surfaceHolder;
  DevModel model;
  boolean hasCapture;
  boolean hasGotFirstFrame;
  private Handler mHandler;

  public SurfaceViewLive2(Context context)
  {
    super(context);
    init();
  }

  public void setmHandler(Handler mHandler)
  {
    this.mHandler = mHandler;
  }

  public void setModel(DevModel m)
  {
    model = m;
  }


  public SurfaceViewLive2(Context context, AttributeSet attributeSet)
  {
    super(context, attributeSet);
    init();
  }

  private void init()
  {
    surfaceHolder = getHolder();
    surfaceHolder.addCallback(this);
    hasGotFirstFrame = false;
    hasCapture = false;
    //  this.setBackgroundColor(Color.TRANSPARENT);
  }

  public void Play()
  {
    DevModel.threadStartPlay(null, model);
  }

  public void Stop()
  {
    DevModel.threadStopPlay(null, model);
  }


  public void surfaceCreated(SurfaceHolder holder)
  {
    lib.thOpenGLCreateEGL(model.NetHandle, surfaceHolder.getSurface());
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (true)
        {
          hasGotFirstFrame = lib.thNetIsVideoDecodeSuccess(model.NetHandle);
          if (hasGotFirstFrame)
          {
            if (mHandler != null)
            {
              mHandler.sendMessage(Message.obtain(mHandler, TMsg.Msg_GotFirstFrame, null));
            }

            if (!hasCapture)
            {
              hasCapture = true;
              String fileName = FileUtil.generateThumbFileName(model.SN);
              if (fileName != null)
              {
                lib.thNetSaveToJpg(model.NetHandle, fileName);
              }
            }
            return;
          }
        }
      }
    }).start();
  }

  public void surfaceDestroyed(SurfaceHolder holder)
  {
    lib.thOpenGLFreeEGL(model.NetHandle);
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
  {
    lib.thOpenGLSurfaceChanged(model.NetHandle, w, h);
  }

  private static String TAG = "VideoSurfaceView";
}