package stcam.stcamproject.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.model.DevModel;
import com.model.LedStatusModel;
import com.thSDK.lib;

import info.hoang8f.android.segmented.SegmentedGroup;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.View.LoadingDialog;

public class LedControlActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    DevModel devModel;
    static final String tag = "LedControlActivity";

    RadioButton btn_mode_1;
    RadioButton btn_mode_2;
    RadioButton btn_mode_3;
    RadioButton btn_mode_4;

    SegmentedGroup modeGroup ;
    RelativeLayout relativelayout_time_span;
    RelativeLayout layout_sensitive;
    RelativeLayout layout_time;
    RelativeLayout layout_brintness;

    Button light_time_span;
    SeekBar seekBar;//亮灯时间
    SegmentedGroup segmented3;//光感灵敏度
    RadioButton btn_sensitive_1;
    RadioButton btn_sensitive_2;
    RadioButton btn_sensitive_3;


    LedStatusModel statusModel;
    LoadingDialog lod;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_led_control);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getParcelable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
        initView();

        initValue();

    }
    void initValue(){
        if (lod == null){
           lod = new LoadingDialog(this);
        }
        lod.dialogShow();
        GetLedStatusTask task = new GetLedStatusTask();
        task.execute(0);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initView(){
        btn_mode_1 = findViewById(R.id.btn_mode_1);
        btn_mode_2 = findViewById(R.id.btn_mode_2);
        btn_mode_3 = findViewById(R.id.btn_mode_3);
        btn_mode_4 = findViewById(R.id.btn_mode_4);
        modeGroup = findViewById(R.id.segmented2);

        relativelayout_time_span = findViewById(R.id.relativelayout_time_span);
        layout_sensitive = findViewById(R.id.layout_sensitive);
        layout_time = findViewById(R.id.layout_time);
        layout_brintness = findViewById(R.id.layout_brintness);


        modeGroup.setOnCheckedChangeListener(this);
        modeGroup.check(R.id.btn_mode_1);

        seekBar = findViewById(R.id.seekBar);
        light_time_span = findViewById(R.id.light_time_span);

         segmented3= findViewById(R.id.segmented3);
         btn_sensitive_1= findViewById(R.id.btn_sensitive_1);
         btn_sensitive_2= findViewById(R.id.btn_sensitive_2);
         btn_sensitive_3= findViewById(R.id.btn_sensitive_3);
    }

    void hideAllLayout(){
        relativelayout_time_span.setVisibility(View.GONE);
        layout_sensitive.setVisibility(View.GONE);
        layout_time.setVisibility(View.GONE);
        layout_brintness.setVisibility(View.GONE);
    }
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == modeGroup){
            hideAllLayout();
            switch (checkedId){
                case R.id.btn_mode_1:
                    relativelayout_time_span.setVisibility(View.VISIBLE);
                    layout_sensitive.setVisibility(View.VISIBLE);

                    break;
                case R.id.btn_mode_2:
                    layout_brintness.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_mode_3:
                    layout_time.setVisibility(View.VISIBLE);
                    layout_brintness.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_mode_4:
                    layout_brintness.setVisibility(View.VISIBLE);
                    layout_sensitive.setVisibility(View.VISIBLE);
                    break;
                default:
                     break;
            }
        }
    }
    void reloadView(){
        if (statusModel != null){
            switch (statusModel.getMode()){
                case 1:
                    modeGroup.check(R.id.btn_mode_1);
                    seekBar.setProgress(statusModel.getAuto().getDelay());
                    light_time_span.setText(""+statusModel.getAuto().getDelay());
                    if (statusModel.getAuto().getLux() == 0 ){
                        segmented3.check(R.id.btn_sensitive_1);
                    }
                    else if (statusModel.getAuto().getLux() == 1 ){
                        segmented3.check(R.id.btn_sensitive_2);
                    }
                    else if (statusModel.getAuto().getLux() == 2 ){
                        segmented3.check(R.id.btn_sensitive_3);
                    }
                    break;
                case 2:
                    modeGroup.check(R.id.btn_mode_2);
                    break;
                case 3:
                    modeGroup.check(R.id.btn_mode_3);
                    break;
                case 4:
                    modeGroup.check(R.id.btn_mode_4);
                    break;
                default:
                    break;
            }
            //if (statusModel.getMode() == 1)
        }
    }
    class GetLedStatusTask extends AsyncTask<Integer, Void, String> {
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
            String url = "http://0.0.0.0:0/cfg1.cgi?User="+ devModel.usr+"&Psd="+ devModel.pwd+"&MsgID=96";
            Log.e(tag,"url "+url);
            String ret = lib.thNetHttpGet(devModel.NetHandle,url);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {

            statusModel = GsonUtil.parseJsonWithGson(result,LedStatusModel.class);
            Log.e(tag,"get status model is "+result);
            // get status model is {"Mode":1,"Auto":{"Delay":90,"Lux":2},"Manual":{"Brightness":0},
            // "Timer":{"Brightness":0,"StartH":0,"StartM":0,"StopH":0,"StopM":0},"D2D":{"Brightness":0,"Lux":0}}
            reloadView();
            lod.dismiss();
            super.onPostExecute(result);
        }
    }
}
