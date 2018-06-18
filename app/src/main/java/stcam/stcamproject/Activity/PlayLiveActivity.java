package stcam.stcamproject.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import com.model.DevModel;
import com.thSDK.lib;

import stcam.stcamproject.R;
import stcam.stcamproject.Util.ConstraintUtil;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.PlayVoice;
import stcam.stcamproject.View.GLSurfaceViewLive;
import stcam.stcamproject.View.VoiceImageButton;

public class PlayLiveActivity extends AppCompatActivity implements View.OnClickListener , GestureDetector.OnGestureListener, View.OnTouchListener {

    GLSurfaceViewLive glView;
    DevModel devModel;
    TableLayout layout_control;
    RelativeLayout layout_land;
    VoiceImageButton button_snapshot,button_snapshot_o;
    ImageButton button_speech,button_speech_o;
    VoiceImageButton button_record,button_record_o;
    ImageButton button_slient;
    RelativeLayout ptz_layout;
    Button button_led,button_pix;
    boolean pix_low = true;
    ImageButton button_ptz_left,button_ptz_right,button_ptz_up,button_ptz_down;
    Button button_ptz;

    ImageButton imagebutton_to_lanscape,imagebutton_to_portrait;

    private GestureDetector mygesture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getParcelable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(devModel.DevName);
        }
        setContentView(R.layout.activity_play_live);
        initView();

        pix_low = true;
        glView.Play();

        mygesture = new GestureDetector(this);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (lib.thNetIsRec(devModel.NetHandle)) {
                    lib.thNetStopRec(devModel.NetHandle);
                    if (FileUtil.isFileEmpty(recordfileName)){
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Log.e(tag,"---------------------onKeyDown");

            if (lib.thNetIsRec(devModel.NetHandle)) {
                lib.thNetStopRec(devModel.NetHandle);
                if (FileUtil.isFileEmpty(recordfileName)){
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configurationChanged();

    }
    void configurationChanged(){
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            layout_land.setVisibility(View.INVISIBLE);
            layout_control.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().show();

            imagebutton_to_portrait.setVisibility(View.INVISIBLE);
            imagebutton_to_lanscape.setVisibility(View.VISIBLE);


            //设置当前窗体为全屏显示


        }else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            layout_land.setVisibility(View.VISIBLE);
            layout_control.setVisibility(View.INVISIBLE);

            imagebutton_to_portrait.setVisibility(View.VISIBLE);
            imagebutton_to_lanscape.setVisibility(View.INVISIBLE);
            getSupportActionBar().hide();
            int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            //设置当前窗体为全屏显示
            Window window = getWindow();
            window.setFlags(flag, flag);

        }
    }
    void initView(){
        glView = findViewById(R.id.glPlayLive);
        glView.setOnTouchListener(this);
        layout_control = findViewById(R.id.layout_control);
        glView.setModel(devModel);
        layout_land = findViewById(R.id.layout_land);


        button_snapshot = findViewById(R.id.button_snapshot);
        button_snapshot_o = findViewById(R.id.button_snapshot_o);
        button_snapshot.setEnumSoundWav(PlayVoice.EnumSoundWav.SNAP);
        button_snapshot_o.setEnumSoundWav(PlayVoice.EnumSoundWav.SNAP);
        button_speech = findViewById(R.id.button_speech);
        button_speech_o = findViewById(R.id.button_speech_o);
        button_record = findViewById(R.id.button_record);
        button_record_o = findViewById(R.id.button_record_o);
        button_record.setEnumSoundWav(PlayVoice.EnumSoundWav.CLICK);
        button_record_o.setEnumSoundWav(PlayVoice.EnumSoundWav.CLICK);
        button_ptz_left = findViewById(R.id.button_ptz_left);
        button_ptz_right = findViewById(R.id.button_ptz_right);
        button_ptz_up = findViewById(R.id.button_ptz_up);
        button_ptz_down = findViewById(R.id.button_ptz_down);
        button_led = findViewById(R.id.button_led);
        button_pix = findViewById(R.id.button_pix);
        button_slient = findViewById(R.id.button_slient);
        button_snapshot.setOnClickListener(this);
        button_snapshot_o.setOnClickListener(this);
        button_speech.setOnClickListener(this);
        button_speech_o.setOnClickListener(this);
        button_record.setOnClickListener(this);
        button_record_o.setOnClickListener(this);
        button_led.setOnClickListener(this);
        button_slient.setOnClickListener(this);
        button_pix.setOnClickListener(this);

        ptz_layout = findViewById(R.id.ptz_control_layout);
        button_ptz = findViewById(R.id.button_ptz);
        button_ptz.setOnClickListener(this);
        button_ptz_left.setOnClickListener(this);
        button_ptz_right.setOnClickListener(this);
        button_ptz_up.setOnClickListener(this);
        button_ptz_down.setOnClickListener(this);

        imagebutton_to_lanscape = findViewById(R.id.imagebutton_to_lanscape);
        imagebutton_to_portrait = findViewById(R.id.imagebutton_to_portrait);
        imagebutton_to_lanscape.setOnClickListener(this);
        imagebutton_to_portrait.setOnClickListener(this);

        configurationChanged();
    }
    ConstraintUtil constraintUtil;
    static final String tag = "PlayLiveActivity";

    String recordfileName;

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_snapshot:
            case R.id.button_snapshot_o:
                String fileName = FileUtil.generatePathSnapShotFileName(devModel.SN);
                if (fileName != null){
                    lib.thNetSaveToJpg(devModel.NetHandle,fileName);
                }
                break;
            case R.id.button_speech:
            case R.id.button_speech_o:
                if (button_speech.isSelected()){
                    button_speech.setSelected(false);
                    button_speech.setImageResource(R.drawable.microphonedefault);
                    lib.thNetTalkClose(devModel.NetHandle);
                }
                else{
                    button_speech.setSelected(true);
                    button_speech.setImageResource(R.drawable.microphoneselected);
                    lib.thNetTalkOpen(devModel.NetHandle);
                }
                break;
            case R.id.button_slient:
                if (button_slient.isSelected()){
                    button_slient.setSelected(false);
                    button_slient.setImageResource(R.drawable.talk_nor);
                    lib.thNetAudioPlayClose(devModel.NetHandle);
                }
                else{
                    button_slient.setSelected(true);
                    button_slient.setImageResource(R.drawable.talk_sel);
                    lib.thNetAudioPlayOpen(devModel.NetHandle);
                }
                break;
            case R.id.button_record:
            case R.id.button_record_o:

                if (lib.thNetIsRec(devModel.NetHandle)){
                    lib.thNetStopRec(devModel.NetHandle);
                    if (FileUtil.isFileEmpty(recordfileName)){
                        FileUtil.delFiles(recordfileName);
                    }
                    button_record.setImageResource(R.drawable.btnrecorddefault);
                    button_record_o.setImageResource(R.drawable.btnrecorddefault);
                }
                else{
                    recordfileName = FileUtil.generatePathRecordFileName(devModel.SN);
                   lib.thNetStartRec(devModel.NetHandle,recordfileName);
                    button_record.setImageResource(R.drawable.btnrecorddown);
                    button_record_o.setImageResource(R.drawable.btnrecorddown);
                }

                break;
            case R.id.button_led:

                Intent intent = new Intent(this,LedControlActivity.class);
                intent.putExtra("devModel",devModel);
                startActivity(intent);
               break;
            case R.id.button_ptz_left:
                Log.e(tag,"left");
                PtzControlTask task = new PtzControlTask();
                task.execute(5);
                break;
            case R.id.button_ptz_right:
                PtzControlTask task1 = new PtzControlTask();
                Log.e(tag,"right");
                task1.execute(7);
                break;
            case R.id.button_ptz_up:
                PtzControlTask task2 = new PtzControlTask();
                Log.e(tag,"up");
                task2.execute(1);
                break;
            case R.id.button_ptz_down:
                PtzControlTask task3 = new PtzControlTask();
                Log.e(tag,"down");
                task3.execute(3);
                break;
            case R.id.button_ptz:
                if (ptz_layout.getVisibility() ==View.VISIBLE){
                    ptz_layout.setVisibility(View.INVISIBLE);
                }
                else{
                    ptz_layout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.button_pix:
                pix_low = !pix_low;
                button_pix.setText(pix_low?R.string.action_pix_low:R.string.action_pix_high);
                devModel.Play(pix_low?1:0);
                break;
            case R.id.imagebutton_to_portrait:
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                break;
            case R.id.imagebutton_to_lanscape:
                if (this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            default:
                break;
        }



    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {
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
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return mygesture.onTouchEvent(motionEvent);
    }

    class PtzControlTask extends AsyncTask<Integer, Void, String> {
        // AsyncTask<Params, Progress, Result>
        //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Integer... params) {
            //第二个执行方法,onPreExecute()执行完后执行
            String url = "http://0.0.0.0:0/cfg1.cgi?User="+devModel.usr+"&Psd="+devModel.pwd+"&MsgID=13&cmd="+params[0]+"&sleep=500&s=23231";

            String ret = lib.thNetHttpGet(devModel.NetHandle,url);
            Log.e(tag,"PtzControlTask ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);


            super.onPostExecute(result);
        }
    }
}
