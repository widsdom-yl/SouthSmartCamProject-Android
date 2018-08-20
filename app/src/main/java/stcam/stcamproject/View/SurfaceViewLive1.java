package stcam.stcamproject.View;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import com.model.DevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.TFun;

public class SurfaceViewLive1 extends GLSurfaceView
{
  //public actPlayLive Activity;
  DevModel model;
  boolean hasCapture;
  boolean hasGotFirstFrame;
  private Handler mHandler;

  public SurfaceViewLive1(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setRenderer(new MyRenderer());
    requestFocus();
    setFocusableInTouchMode(true);
    hasGotFirstFrame = false;
  }

  public void setmHandler(Handler mHandler)
  {
    this.mHandler = mHandler;
  }

  public void setModel(DevModel m)
  {
    model = m;
    hasCapture = false;
  }

  public void Play()
  {
    DevModel.threadStartPlay(null, model);
  }

  public void Stop()
  {
    DevModel.threadStopPlay(null, model);
  }

  class MyRenderer implements GLSurfaceView.Renderer
  {


    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1)
    {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
      TFun.printf("====onSurfaceChanged");
      lib.thNetOpenGLUpdateArea(model.NetHandle, 0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl)//约60fps/s
    {


      if (lib.thNetOpenGLRender(model.NetHandle))
      {

        if (!hasGotFirstFrame)
        {
          hasGotFirstFrame = true;
          if (mHandler != null)
          {
            mHandler.sendMessage(Message.obtain(mHandler, TMsg.Msg_GotFirstFrame, null));
          }
        }
        if (hasCapture == false)
        {
          hasCapture = true;
          String fileName = FileUtil.generateThumbFileName(model.SN);
          if (fileName != null)
          {
            lib.thNetSaveToJpg(model.NetHandle, fileName);
          }
        }
      }
    }

  }


}