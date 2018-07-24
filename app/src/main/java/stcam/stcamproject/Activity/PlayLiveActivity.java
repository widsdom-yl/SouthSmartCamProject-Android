package stcam.stcamproject.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.model.DevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.ConstraintUtil;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.PlayVoice;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.GLSurfaceViewLive;
import stcam.stcamproject.View.VoiceImageButton;

public class PlayLiveActivity extends BaseAppCompatActivity implements View.OnClickListener, GestureDetector.OnGestureListener, View
  .OnTouchListener
{

  GLSurfaceViewLive glView;
  DevModel devModel;
  VoiceImageButton button_snapshot;
  ImageButton button_speech, button_setting;
  VoiceImageButton button_record;
  ImageButton button_slient;
  RelativeLayout ptz_layout;
  ImageButton button_led, button_pix;
  boolean pix_low = true;
  ImageButton button_ptz_left, button_ptz_right, button_ptz_up, button_ptz_down;
  ImageButton button_back;
  ProgressBar load_progress;
  boolean hasGotFirstFrame = false;//是否收到了第一帧
//    ImageButton button_ptz;

  boolean isPlayAudio;

  TextView tx_record;
  int recordTotalTime = 0;
  boolean isRecording;

  ImageButton imagebutton_to_lanscape;

  private GestureDetector mygesture;
  MainDevListFragment.EnumMainEntry entryType;

  //定时刷新列表
  public int TIME = 1000;

  boolean isTalk = false;

  Handler handler_refresh = new Handler();
  Runnable runnable_fresh = new Runnable()
  {
    @Override
    public void run()
    {
      //

      if (isRecording)
      {
        recordTotalTime++;
        tx_record.setVisibility(View.VISIBLE);
        tx_record.setText("REC   " + SouthUtil.getTimeSSMM(recordTotalTime));
      }
      else
      {
        tx_record.setText("REC   ");
      }

      handler_refresh.postDelayed(runnable_fresh, TIME);


    }
  };


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
    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null)
    {
      devModel = (DevModel) bundle.getParcelable("devModel");
      entryType = (MainDevListFragment.EnumMainEntry) bundle.getSerializable("entry");
      Log.e(tag, "NetHandle:" + devModel.NetHandle + ",SN:" + devModel.SN);
    }
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      actionBar.setHomeButtonEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);

      //actionBar.setTitle(devModel.DevName);

      setCustomTitle(devModel.DevName, true);

    }


    pix_low = true;
//
    configurationChanged();
    glView.Play();
    mygesture = new GestureDetector(this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
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
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    handler_refresh.postDelayed(runnable_fresh, TIME);
    if (isPlayAudio)
    {
      lib.thNetAudioPlayOpen(devModel.NetHandle);
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();

    handler_refresh.removeCallbacks(runnable_fresh);
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    if (isPlayAudio)
    {
      lib.thNetAudioPlayClose(devModel.NetHandle);
    }
    handler_enterbackground.postDelayed(runnable_enterbackground, 500);


  }

  Handler handler_enterbackground = new Handler();
  Runnable runnable_enterbackground = new Runnable()
  {
    @Override
    public void run()
    {
      //
      if (STApplication.getInstance().getIsRunInBackground())
      {
        Log.e(tag, "---------------------Enterbackground");
        glView.Stop();
        PlayLiveActivity.this.finish();

      }


    }
  };


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
    configurationChanged();

  }

  void configurationChanged()
  {
    if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
    {

      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      getSupportActionBar().show();


      setContentView(R.layout.activity_play_live);
      initView(true);


      //设置当前窗体为全屏显示


    }
    else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
    {


//            imagebutton_to_portrait.setVisibility(View.VISIBLE);
//            imagebutton_to_lanscape.setVisibility(View.INVISIBLE);
      getSupportActionBar().hide();
      int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
      //设置当前窗体为全屏显示
      Window window = getWindow();
      window.setFlags(flag, flag);

      setContentView(R.layout.activity_play_live);
      initView(false);

    }
  }

  void initView(boolean isPortrait)
  {
    glView = findViewById(R.id.glPlayLive);

    if (devModel.IsControl == 1)
    {
      glView.setOnTouchListener(this);
    }


    glView.setModel(devModel);
    glView.setmHandler(gotFrameHandler);

    tx_record = findViewById(R.id.tx_record);
    button_snapshot = findViewById(R.id.button_snapshot);
    button_snapshot.setEnumSoundWav(PlayVoice.EnumSoundWav.SNAP);
    button_speech = findViewById(R.id.button_speech);
    button_record = findViewById(R.id.button_record);
    button_setting = findViewById(R.id.button_setting);
    button_record.setEnumSoundWav(PlayVoice.EnumSoundWav.CLICK);
    button_ptz_left = findViewById(R.id.button_ptz_left);
    button_ptz_right = findViewById(R.id.button_ptz_right);
    button_ptz_up = findViewById(R.id.button_ptz_up);
    button_ptz_down = findViewById(R.id.button_ptz_down);
    button_led = findViewById(R.id.button_led);
    button_pix = findViewById(R.id.button_pix);
    button_slient = findViewById(R.id.button_slient);
    button_snapshot.setOnClickListener(this);
    //  button_speech.setOnClickListener(this);
    button_record.setOnClickListener(this);
    button_led.setOnClickListener(this);
    button_slient.setOnClickListener(this);
    button_pix.setOnClickListener(this);
    button_setting.setOnClickListener(this);
    load_progress = findViewById(R.id.load_progress);
    if (hasGotFirstFrame)
    {
      load_progress.setVisibility(View.GONE);
    }
    else
    {
      load_progress.setVisibility(View.VISIBLE);
    }
    ptz_layout = findViewById(R.id.ptz_control_layout);
//        button_ptz = findViewById(R.id.button_ptz);
//        button_ptz.setOnClickListener(this);
    button_ptz_left.setOnClickListener(this);
    button_ptz_right.setOnClickListener(this);
    button_ptz_up.setOnClickListener(this);
    button_ptz_down.setOnClickListener(this);

    //ptz_layout.setOnClickListener(this);
    //glView.setOnClickListener(this);
    if (isPortrait)
    {
      imagebutton_to_lanscape = findViewById(R.id.button_fullscreen);
//        imagebutton_to_portrait = findViewById(R.id.imagebutton_to_portrait);
      imagebutton_to_lanscape.setOnClickListener(this);
//        imagebutton_to_portrait.setOnClickListener(this);
    }
    else
    {
      button_back = findViewById(R.id.button_back);
      button_back.setOnClickListener(this);
    }

    //configurationChanged();

    if (devModel.IsVideo == 0 && entryType != MainDevListFragment.EnumMainEntry.EnumMainEntry_Visitor)
    {
      //音频
      button_speech.setVisibility(View.INVISIBLE);
      button_slient.setVisibility(View.INVISIBLE);
    }

    if (!isRecording)
    {
      button_record.setImageResource(R.drawable.liverecord_nor);
    }
    else
    {
      button_record.setImageResource(R.drawable.liverecord_sel);
    }

    button_pix.setImageResource(pix_low ? R.drawable.livehd_nor : R.drawable.livehd_sel);


    if (!isPlayAudio)
    {
      button_slient.setSelected(false);
      button_slient.setImageResource(R.drawable.livetalkoff_nor);
    }
    else
    {
      button_slient.setSelected(true);
      button_slient.setImageResource(R.drawable.livetalkon_sel);
    }

//        if (!isTalk){
//            button_speech.setSelected(false);
//            button_speech.setImageResource(R.drawable.livespeech_nor);}
//        else{
//            button_speech.setSelected(true);
//            button_speech.setImageResource(R.drawable.livespeech_sel);
//        }

    button_speech.setOnTouchListener(new View.OnTouchListener()
    {
      @Override
      public boolean onTouch(View v, MotionEvent event)
      {
        switch (event.getAction())
        {
          case MotionEvent.ACTION_UP://松开事件发生后执行代码的区域
            Log.e(tag, "talk off");

            button_speech.setImageResource(R.drawable.livespeech_nor);
            lib.thNetTalkClose(devModel.NetHandle);
            if (isPlayAudio)
            {
              lib.thNetAudioPlayOpen(devModel.NetHandle);
            }
            break;
          case MotionEvent.ACTION_DOWN://按住事件发生后执行代码的区域
            Log.e(tag, "talk on ");
            if (isPlayAudio)
            {
              lib.thNetAudioPlayClose(devModel.NetHandle);
            }
            button_speech.setImageResource(R.drawable.livespeech_sel);
            lib.thNetTalkOpen(devModel.NetHandle);


            break;
          default:
            break;
        }
        return true;
      }
    });


//        if (entryType == MainDevListFragment.EnumMainEntry.EnumMainEntry_Visitor){
//            button_record.setVisibility(View.INVISIBLE);
//            button_record_o.setVisibility(View.INVISIBLE);
//            button_snapshot.setVisibility(View.INVISIBLE);
//            button_snapshot_o.setVisibility(View.INVISIBLE);
//        }
  }

  ConstraintUtil constraintUtil;
  static final String tag = "PlayLiveActivity";

  String recordfileName;

  private Handler gotFrameHandler = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {


      super.handleMessage(msg);
      switch (msg.what)
      {
        case TMsg.Msg_GotFirstFrame:
          hasGotFirstFrame = true;
          load_progress.setVisibility(View.GONE);
          break;

        default:
          break;
      }
    }
  };
  Handler handler = new Handler();
  Runnable runnable = new Runnable()
  {
    @Override
    public void run()
    {
      // TODO Auto-generated method stub

      button_speech.setEnabled(true);
      button_slient.setEnabled(true);
      button_record.setEnabled(true);
      button_pix.setEnabled(true);
    }
  };

  void enableBtnAfterSeconds()
  {
    button_speech.setEnabled(false);
    button_slient.setEnabled(false);
    button_record.setEnabled(false);
    button_pix.setEnabled(false);
    handler.postDelayed(runnable, 1500);
  }

  @Override
  public void onClick(View view)
  {
    switch (view.getId())
    {
      case R.id.button_snapshot:
        String fileName = FileUtil.generatePathSnapShotFileName(devModel.SN);
        if (fileName != null)
        {
          lib.thNetSaveToJpg(devModel.NetHandle, fileName);
        }
        break;
      case R.id.button_speech:
        enableBtnAfterSeconds();
        if (isTalk)
        {
          button_speech.setSelected(false);
          button_speech.setImageResource(R.drawable.livespeech_nor);
//                    if (isPlayAudio){
//                        lib.thNetAudioPlayOpen(devModel.NetHandle);
//                    }
          isTalk = false;
          lib.thNetTalkClose(devModel.NetHandle);
        }
        else
        {
          button_speech.setSelected(true);

          button_speech.setImageResource(R.drawable.livespeech_sel);
//                    if (isPlayAudio){
//                        lib.thNetAudioPlayClose(devModel.NetHandle);
//                    }
          isTalk = true;
          lib.thNetTalkOpen(devModel.NetHandle);
        }
        break;
      case R.id.button_slient:
        enableBtnAfterSeconds();
        if (button_slient.isSelected())
        {
          isPlayAudio = false;
          button_slient.setSelected(false);
          button_slient.setImageResource(R.drawable.livetalkoff_nor);
          lib.thNetAudioPlayClose(devModel.NetHandle);
        }
        else
        {
          button_slient.setSelected(true);
          isPlayAudio = true;
          button_slient.setImageResource(R.drawable.livetalkon_sel);
          lib.thNetAudioPlayOpen(devModel.NetHandle);
        }
        break;
      case R.id.button_record:
        enableBtnAfterSeconds();
        if (lib.thNetIsRec(devModel.NetHandle))
        {

          isRecording = false;
          recordTotalTime = 0;
          tx_record.setVisibility(View.INVISIBLE);
          lib.thNetStopRec(devModel.NetHandle);
          if (FileUtil.isFileEmpty(recordfileName))
          {
            FileUtil.delFiles(recordfileName);
          }
          button_record.setImageResource(R.drawable.liverecord_nor);
        }
        else
        {
          isRecording = true;
          recordTotalTime = 0;
          tx_record.setVisibility(View.VISIBLE);
          recordfileName = FileUtil.generatePathRecordFileName(devModel.SN);
          lib.thNetStartRec(devModel.NetHandle, recordfileName);
          button_record.setImageResource(R.drawable.liverecord_sel);
        }

        break;
      case R.id.button_led:

        Intent intent = new Intent(this, LedControlActivity.class);
        intent.putExtra("devModel", devModel);
        startActivity(intent);
        break;
      case R.id.button_ptz_left:
        Log.e(tag, "left");
        PtzControlTask task = new PtzControlTask();
        task.execute(5);
        break;
      case R.id.button_ptz_right:
        PtzControlTask task1 = new PtzControlTask();
        Log.e(tag, "right");
        task1.execute(7);
        break;
      case R.id.button_ptz_up:
        PtzControlTask task2 = new PtzControlTask();
        Log.e(tag, "up");
        task2.execute(1);
        break;
      case R.id.button_ptz_down:
        PtzControlTask task3 = new PtzControlTask();
        Log.e(tag, "down");
        task3.execute(3);
        break;
      case R.id.ptz_control_layout:
      case R.id.glPlayLive:
        if (ptz_layout.getVisibility() == View.VISIBLE)
        {
          ptz_layout.setVisibility(View.INVISIBLE);
        }
        else
        {
          ptz_layout.setVisibility(View.VISIBLE);
        }
        break;
      case R.id.button_pix:
        enableBtnAfterSeconds();
        pix_low = !pix_low;
        // button_pix.setText(pix_low?R.string.action_pix_low:R.string.action_pix_high);
        button_pix.setImageResource(pix_low ? R.drawable.livehd_nor : R.drawable.livehd_sel);
        //线程操作
        new Thread()
        {
          @Override
          public void run()
          {
            devModel.Stop();
            devModel.Play(pix_low ? 1 : 0);
          }
        }.start();


        break;
      case R.id.button_back:
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        break;
      case R.id.button_fullscreen:
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        break;
      case R.id.button_setting:
      {
        Intent intentSetting = new Intent(STApplication.getInstance(), SettingActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable("devModel", devModel);
        bundle.putSerializable("entry", entryType);
        intentSetting.putExtras(bundle);
        Log.e(tag, "to SettingActivity NetHandle:" + devModel.NetHandle);

        startActivity(intentSetting);
      }

      default:
        break;
    }


  }

  @Override
  public boolean onDown(MotionEvent motionEvent)
  {
    return true;
  }

  @Override
  public void onShowPress(MotionEvent motionEvent)
  {

  }

  @Override
  public boolean onSingleTapUp(MotionEvent motionEvent)
  {
    return false;
  }

  @Override
  public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1)
  {
    return false;
  }

  @Override
  public void onLongPress(MotionEvent motionEvent)
  {

  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1)
  {
    if (e1.getX() - e2.getX() > 120)
    {
      Log.e(tag, "left");
      PtzControlTask task2 = new PtzControlTask();
      task2.execute(5);
    }
    else if (e1.getX() - e2.getX() < -120)
    {
      Log.e(tag, "right");
      PtzControlTask task2 = new PtzControlTask();
      task2.execute(7);
    }
    else if (e1.getY() - e2.getY() < 120)
    {
      Log.e(tag, "down");
      PtzControlTask task2 = new PtzControlTask();
      task2.execute(3);
    }
    else if (e1.getY() - e2.getY() > 120)
    {
      Log.e(tag, "up");
      PtzControlTask task2 = new PtzControlTask();
      task2.execute(1);
    }
    Log.e(tag, "onFling");
    return false;
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent)
  {
    return mygesture.onTouchEvent(motionEvent);
  }

  class PtzControlTask extends AsyncTask<Integer, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(Integer... params)
    {
      //第二个执行方法,onPreExecute()执行完后执行
      String url = devModel.getHttpCfg1UsrPwd() + "&MsgID=13&cmd=" + params[0] + "&sleep=500&s=23231";

      String ret = lib.thNetHttpGet(devModel.NetHandle, url);
      Log.e(tag, "PtzControlTask ret :" + ret);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
      //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
      //Log.e(tag,"get playback list :"+result);


      super.onPostExecute(result);
    }
  }
}
