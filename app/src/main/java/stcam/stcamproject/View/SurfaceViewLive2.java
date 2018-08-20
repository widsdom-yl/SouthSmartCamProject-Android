package stcam.stcamproject.View;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.model.DevModel;
import com.thSDK.lib;

import stcam.stcamproject.Util.FileUtil;

/**
 * Created by gyl
 */

public class SurfaceViewLive2 extends SurfaceView implements SurfaceHolder.Callback
{
  public SurfaceHolder surfaceHolder;
  boolean isSurfaceExist = false;
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
  }

  public void surfaceDestroyed(SurfaceHolder holder)
  {
    lib.thOpenGLFreeEGL(model.NetHandle);
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
    lib.thOpenGLSurfaceChanged(model.NetHandle, w, h);
  }

  private static String TAG = "VideoSurfaceView";
}