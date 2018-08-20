package stcam.stcamproject.View;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import com.model.DevModel;
import com.model.SDVideoModel;
import com.thSDK.TMsg;
import com.thSDK.lib;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import stcam.stcamproject.Util.TFun;

public class SurfaceViewPlayBack1 extends GLSurfaceView
{
  DevModel mDevModel;
  SDVideoModel mSDVideoModel;
  private Handler mHandler;
  boolean hasGotFirstFrame;

  public SurfaceViewPlayBack1(Context context, AttributeSet attrs)
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

  public void setModel(DevModel devModel, SDVideoModel sdVideoModel)
  {
    mDevModel = devModel;
    mSDVideoModel = sdVideoModel;
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

  class MyRenderer implements GLSurfaceView.Renderer
  {
    boolean IsGetNodeIcon = false;

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1)
    {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
      lib.thNetOpenGLUpdateArea(mDevModel.NetHandle, 0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl)//约60fps/s
    {
      if (lib.thNetOpenGLRender(mDevModel.NetHandle))
      {
        if (!hasGotFirstFrame)
        {
          hasGotFirstFrame = true;
          if (mHandler != null)
          {
            mHandler.sendMessage(Message.obtain(mHandler, TMsg.Msg_GotFirstFrame, null));
          }
        }

        if (IsGetNodeIcon == false)
        {
        }
      }
    }
  }
}
