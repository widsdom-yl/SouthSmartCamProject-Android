package stcam.stcamproject.Activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.model.DevModel;
import com.model.SDVideoModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.TFun;
import stcam.stcamproject.View.SurfaceViewPlayBack1;
import stcam.stcamproject.View.SurfaceViewPlayBack2;

public class PlayBackActivity extends BaseAppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener
{
  static final String tag = "PlayBackActivity";

  //SurfaceViewPlayBack1 glView;
  //int IActivityPlayBack = R.layout.activity_play_back1;
  SurfaceViewPlayBack2 glView;
  int IActivityPlayBack = R.layout.activity_play_back2;

  SDVideoModel model;
  DevModel devModel;
  ImageButton imagebutton_back;

  MainDevListFragment.EnumMainEntry entryType;
  ProgressBar load_progress;

  SeekBar seekBarTimer;
  TextView txtTimePosition, txtTimeDuration;
  ImageButton BtnPlay;
  boolean isPlay = true;//默认进入页面播放
  int iTimeDragPos = 0;
  boolean IsPauseUpdateseekBar = false;
  int IndexType = 0;
  int iPosition = 0;
  int iMax = 0;

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.blank_menu, menu);
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    getSupportActionBar().hide();
    int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
    //设置当前窗体为全屏显示
    Window window = getWindow();
    window.setFlags(flag, flag);

    setContentView(this.IActivityPlayBack);

    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null)
    {
      model = (SDVideoModel) bundle.getSerializable("model");
      devModel = (DevModel) bundle.getParcelable("devModel");
      entryType = (MainDevListFragment.EnumMainEntry) bundle.getSerializable("entry");
      Log.e(tag, "play sdVideo:" + model.sdVideo);
    }
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(model.sdVideo);

      setCustomTitle(model.sdVideo, true);

    }
    initView();
    glView.setmHandler(gotFrameHandler);
    glView.Play();
    isPlay = true;
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    handler_refresh.removeCallbacks(runnable_refresh);
    handler_enterbackground.postDelayed(runnable_enterbackground, 500);
  }

  Handler handler_enterbackground = new Handler();
  Runnable runnable_enterbackground = new Runnable()
  {
    @Override
    public void run()
    {
      if (STApplication.getInstance().getIsRunInBackground())
      {
        Log.e(tag, "---------------------Enterbackground");
        glView.Stop();
        PlayBackActivity.this.finish();
      }
    }
  };

  Handler handler_refresh = new Handler();
  Runnable runnable_refresh = new Runnable()
  {
    @Override
    public void run()
    {
      //获取播放的时常，并调节seekbar
      IndexType = lib.thNetRemoteFileGetIndexType(devModel.NetHandle);
      iPosition = lib.thNetRemoteFileGetPosition(devModel.NetHandle);
      iMax = lib.thNetRemoteFileGetDuration(devModel.NetHandle);

      if (iMax > 0 && IndexType > 0)
      {
        seekBarTimer.setMax(iMax);

        int iTimeNow = lib.GetTime();
        if (iTimeDragPos == 0)
        {
          seekBarTimer.setProgress(iPosition);
        }
        else
        {
          if (iTimeNow - iTimeDragPos > 3)
          {
            iTimeDragPos = 0;
          }
        }

        if (IndexType == 1)//按文件长度
        {
          String StrPosition = String.format("%d %", iPosition * 100 / iMax);
          String StrDuration = String.format("%d %", 100);
          txtTimePosition.setText(StrPosition);
          txtTimeDuration.setText(StrDuration);
        }
        else if (IndexType == 2)//按时间戳
        {
          int iHour, iMinute, iSecond;

          iPosition = iPosition / 1000;
          iHour = iPosition / 3600;
          iMinute = (iPosition - iHour * 60) / 60;
          iSecond = iPosition % 60;
          String StrPosition = String.format("%02d:%02d:%02d", iHour, iMinute, iSecond);
          txtTimePosition.setText(StrPosition);

          iMax = iMax / 1000;
          iHour = iMax / 3600;
          iMinute = (iMax - iHour * 60) / 60;
          iSecond = iMax % 60;
          String StrDuration = String.format("%02d:%02d:%02d", iHour, iMinute, iSecond);
          txtTimeDuration.setText(StrDuration);
        }
      }//if (iMax > 0 && IndexType > 0)
      handler_refresh.postDelayed(runnable_refresh, 1000);
    }
  };

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        glView.Stop();
        this.finish(); // back button

        return true;
    }
    return super.onOptionsItemSelected(item);
  }


  String recordfileName;

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    if (keyCode == KeyEvent.KEYCODE_BACK)
    {
      Log.e(tag, "---------------------onKeyDown");
      if (lib.thNetIsRec(devModel.NetHandle))
      {
        lib.thNetStopRec(devModel.NetHandle);
        if (FileUtil.isFileEmpty(recordfileName))
        {
          FileUtil.delFiles(recordfileName);
        }
      }
      glView.Stop();
      this.finish(); // back button
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);
//        glView.Stop();
//        glView.Play();
  }

  private Handler gotFrameHandler = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {


      super.handleMessage(msg);
      switch (msg.what)
      {
        case TMsg.Msg_GotFirstFrame:
          load_progress.setVisibility(View.GONE);
          glView.setBackgroundColor(Color.TRANSPARENT);
          handler_refresh.postDelayed(runnable_refresh, 1000);
          break;

        default:
          break;
      }
    }
  };

  void initView()
  {
    glView = findViewById(R.id.glPlayBack);
    glView.setModel(devModel, model);


    imagebutton_back = findViewById(R.id.imagebutton_back);
    load_progress = findViewById(R.id.load_progress);

    imagebutton_back.setOnClickListener(this);
//        if (entryType == MainDevListFragment.EnumMainEntry.EnumMainEntry_Visitor){
//            button_record.setVisibility(View.INVISIBLE);
//
//            button_snapshot.setVisibility(View.INVISIBLE);
//        }

    seekBarTimer = findViewById(R.id.seekBar_time);
    txtTimePosition = findViewById(R.id.txtTimePosition);
    txtTimeDuration = findViewById(R.id.txtTimeDuration);

    seekBarTimer.setOnSeekBarChangeListener(this);

    BtnPlay = findViewById(R.id.imagebutton_play);
    BtnPlay.setOnClickListener(this);
    BtnPlay.setImageResource(R.drawable.pause0);

    //BtnPlay.setVisibility(View.INVISIBLE);
    //seekBarTimer.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onClick(View view)
  {
    switch (view.getId())
    {
      case R.id.imagebutton_back:
        glView.Stop();
        this.finish(); // back button
        break;

      case R.id.imagebutton_play:
        isPlay = !isPlay;
        if (isPlay)
        {
          lib.thNetRemoteFilePlayControl(devModel.NetHandle, lib.PS_Play, 0, 0);
          BtnPlay.setImageResource(R.drawable.pause0);
        }
        else
        {
          lib.thNetRemoteFilePlayControl(devModel.NetHandle, lib.PS_Pause, 0, 0);
          BtnPlay.setImageResource(R.drawable.play0);
        }
        break;

      default:
        break;
    }
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int value, boolean b)
  {
/*
    if (IsPauseUpdateseekBar)
    {
      if (IndexType == 1)//按文件长度
      {
        String StrPosition = String.format("%d %", value * 100 / iMax);
        txtTimePosition.setText(StrPosition);
      }
      else if (IndexType == 2)//按时间戳
      {
        int iHour, iMinute, iSecond;

        value = value / 1000;
        iHour = value / 3600;
        iMinute = (value - iHour * 60) / 60;
        iSecond = value % 60;
        String StrPosition = String.format("%02d:%02d:%02d", iHour, iMinute, iSecond);
        txtTimePosition.setText(StrPosition);
      }
    }
    */
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar)
  {
    IsPauseUpdateseekBar = true;
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar)
  {
    int iPos = seekBar.getProgress();
    lib.thNetRemoteFileSetPosition(devModel.NetHandle, iPos);
    iTimeDragPos = lib.GetTime();
    IsPauseUpdateseekBar = false;
  }
}
