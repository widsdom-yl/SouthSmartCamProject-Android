package stcam.stcamproject.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.model.DevModel;
import com.model.PushSettingModel;
import com.model.RecConfigModel;
import com.model.RetModel;
import com.thSDK.lib;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.PushSettingAdapter;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;

public class PushSettingActivity extends BaseAppCompatActivity implements BaseAdapter.OnItemClickListener, View.OnClickListener {
    List<String> items = new ArrayList<>();
    RecyclerView mRecyclerView;
    PushSettingAdapter mAdapter;
    DevModel model;
    PushSettingModel mPushSettingModel = new PushSettingModel();
    Button button_reset;
    int MD_Sensitive = -1;
    RecConfigModel mRecConfigModel = new  RecConfigModel();
    Handler handler=new Handler();
    boolean AUDIO_IsPlayPromptSound;
    Runnable runnable=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            setPushConfigTask task = new setPushConfigTask();
            task.execute();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blank_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(R.string.action_manager_senior);

            setCustomTitle(getString(R.string.action_manager_senior),true);


        }
        setContentView(R.layout.activity_push_setting);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = bundle.getParcelable("devModel");

        }

        initView();
        initValue();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.back(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){


//            SetLedStatusTask task1 = new SetLedStatusTask();
//            task1.execute(0);
            this.back(); // back button

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    void back(){

        handler.postDelayed(runnable,100);
        this.finish(); // back button
    }

    void initView(){


        mRecyclerView = findViewById(R.id.push_setting_list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        button_reset = findViewById(R.id.button_reset);
        button_reset.setOnClickListener(this);

    }
    void initValue(){

        items.add(getString(R.string.action_push_interval));

        items.add(getString(R.string.action_manager_alarm_level));

        items.add(getString(R.string.action_pir_sensitivity));
        items.add(getString(R.string.action_dev_sound));
        items.add(getString(R.string.action_alarm_time_span));
        items.add(getString(R.string.action_manager_volume));
        mAdapter = new PushSettingAdapter(items);

        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);


        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();

        getPushConfigTask configTask = new getPushConfigTask();
        configTask.execute();

        getConfigTask mdtask = new getConfigTask();
        mdtask.execute();

        getRecConfigTask recConfigTask = new getRecConfigTask();
        recConfigTask.execute();

        getAudioPlayPromptSoundTask audioConfigTask = new getAudioPlayPromptSoundTask();
        audioConfigTask.execute();
    }

    @Override
    public void onItemClick(View view, int position) {

        if(0 == position){
            dialogChoice2();
        }
        else if (1 == position){
            dialogChoice();
        }
        else if(2 == position){
            dialogChoice3();
        }
        else if(3 == position){
            dialogChoice1();
        }
        else if(4 == position){
            dialogChoice_alarm_time();
        }
        else if(5 == position){
            if(model.ExistSD == 0){
                SouthUtil.showDialog(PushSettingActivity.this,getString(R.string.action_not_exist_sd));
                return;
            }
            Intent intent = new Intent(this,SDVolumeManagerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("devModel",model);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    /*报警灵明度*/
    int chooseLevel = -1;
    private void dialogChoice() {
        chooseLevel = -1;
        final String items[] = {getString(R.string.action_level_low), getString(R.string.action_level_middle), getString(R.string.action_level_high)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle(getString(R.string.action_manager_alarm_level));
        builder.setIcon(R.mipmap.ic_launcher);
        if (MD_Sensitive <= 100){
            chooseLevel = 0;
        }
        else if(MD_Sensitive <= 150){
            chooseLevel = 1;
        }
        else if(MD_Sensitive <= 200){
            chooseLevel = 2;
        }

        builder.setSingleChoiceItems(items, chooseLevel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(tag,"choose :"+which);
                        chooseLevel = which;
                    }
                });
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Log.e(tag,"final choose :"+which);
                if (lod == null){
                    lod = new LoadingDialog(PushSettingActivity.this);
                }
                lod.dialogShow();
                SetMdSensitiveConfigTask task = new SetMdSensitiveConfigTask();
                task.execute(chooseLevel);
            }
        });
        builder.create().show();
    }

    /*声音开关*/
    private void dialogChoice1() {

        final String items[] = {getString(R.string.action_close), getString(R.string.action_open)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle(getString(R.string.action_dev_sound));
        builder.setIcon(R.mipmap.ic_launcher);


        builder.setSingleChoiceItems(items,AUDIO_IsPlayPromptSound?1:0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(tag,"choose :"+which);
                        AUDIO_IsPlayPromptSound = (which == 0 ? false : true);
                        mAdapter.setAUDIO_IsPlayPromptSound(AUDIO_IsPlayPromptSound);
                        mAdapter.notifyDataSetChanged();
                        SetAudioPlayPromptSoundTask task = new SetAudioPlayPromptSoundTask();
                        if (AUDIO_IsPlayPromptSound){

                            task.execute(1);
                        }
                        else{
                            task.execute(0);
                        }
                    }
                });
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }
    /*时间间隔*/
    private void dialogChoice2() {

        final String items[] = {10+getString(R.string.string_second), 30+getString(R.string.string_second),1+getString(R.string.string_miniute)
        ,5+getString(R.string.string_miniute),10+getString(R.string.string_miniute)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle(getString(R.string.action_push_interval));
        builder.setIcon(R.mipmap.ic_launcher);


        builder.setSingleChoiceItems(items, mPushSettingModel.getPushIntervalLevel(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(tag,"choose :"+which);
                        mPushSettingModel.setPushIntervalLevel(which);
                        mAdapter.notifyDataSetChanged();
                    }
                });
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }
    /*灵敏度*/
    private void dialogChoice3() {

        final String items[] = {getString(R.string.action_level_low),getString(R.string.action_level_middle),getString(R.string.action_level_high)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle(getString(R.string.action_pir_sensitivity));
        builder.setIcon(R.mipmap.ic_launcher);


        builder.setSingleChoiceItems(items, mPushSettingModel.getPIRSensitive(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(tag,"choose :"+which);
                        mPushSettingModel.setPIRSensitive(which);
                        mAdapter.notifyDataSetChanged();
                    }
                });
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }



    /*报警时间间隔*/
    private void dialogChoice_alarm_time() {

        final String items[] = {5+getString(R.string.string_second), 10+getString(R.string.string_second),20+getString(R.string.string_second)
                ,30+getString(R.string.string_second),60+getString(R.string.string_second)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle(getString(R.string.action_alarm_time_span));
        builder.setIcon(R.mipmap.ic_launcher);


        builder.setSingleChoiceItems(items, mRecConfigModel.getRec_AlmTimeLenChoice(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(tag,"choose :"+which);
                        mRecConfigModel.setRec_AlmTimeLenChoice(which);
                        mAdapter.setmRecConfigModel(mRecConfigModel);
                        mAdapter.notifyDataSetChanged();
                        setRecConfigTask task = new setRecConfigTask();
                        task.execute();
                    }
                });
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }



    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_reset){
            new AlertDialog.Builder(this)
                    .setTitle(this.getString(R.string.action_reset_origin_ask))
                    .setPositiveButton(this.getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (lod == null){
                                lod = new LoadingDialog(PushSettingActivity.this);
                            }
                            lod.dialogShow();

                            resetConfigTask task = new resetConfigTask();
                            task.execute();
                        }
                    })
                    .setNegativeButton(this.getString(R.string.action_cancel), null)
                    .show();
        }
    }

    class getPushConfigTask extends AsyncTask<String, Void, String> {
        // AsyncTask<Params, Progress, Result>
        //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            //第二个执行方法,onPreExecute()执行完后执行
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx
            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=98";
            Log.e(tag,url+"" +
                    ""+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);
            lod.dismiss();

            PushSettingModel pushSettingModel = GsonUtil.parseJsonWithGson(result,PushSettingModel.class);
            if (pushSettingModel != null){
                mPushSettingModel = pushSettingModel;
                mAdapter.setPushSettingModel(mPushSettingModel);
                mAdapter.notifyDataSetChanged();
            }

            super.onPostExecute(result);
        }
    }

    class setPushConfigTask extends AsyncTask<String, Void, String> {
        // AsyncTask<Params, Progress, Result>
        //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            //第二个执行方法,onPreExecute()执行完后执行
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx
            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=99&PushActive="+
                   mPushSettingModel.getPushActive()+"&PushInterval="+mPushSettingModel.getPushInterval()+"&PIRSensitive="+mPushSettingModel.getPIRSensitive();
            Log.e(tag,url+"," +
                    ""+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"MsgID=99：ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {


            super.onPostExecute(result);
        }
    }

    class getConfigTask extends AsyncTask<String, Void, String> {
        // AsyncTask<Params, Progress, Result>
        //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            //第二个执行方法,onPreExecute()执行完后执行
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx
            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=45";
            Log.e(tag,url+"" +
                    ""+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);
            lod.dismiss();
            if (result == null){
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                MD_Sensitive = jsonObject.getInt("MD_Sensitive");
                mAdapter.setMD_Sensitive(MD_Sensitive);
                mAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }


            super.onPostExecute(result);
        }
    }

    class getRecConfigTask extends AsyncTask<String, Void, String> {
        // AsyncTask<Params, Progress, Result>
        //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            //第二个执行方法,onPreExecute()执行完后执行
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx
            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=55";
            Log.e(tag,url+"" +
                    ""+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);
            lod.dismiss();
            if (result != null && result.length() > 0){
                RecConfigModel recConfigModel = GsonUtil.parseJsonWithGson(result,RecConfigModel.class);
                if (recConfigModel != null){
                    mRecConfigModel = recConfigModel;
                    mAdapter.setmRecConfigModel(mRecConfigModel);
                    mAdapter.notifyDataSetChanged();
                }
            }



            super.onPostExecute(result);
        }
    }

    class setRecConfigTask extends AsyncTask<String, Void, String> {
        // AsyncTask<Params, Progress, Result>
        //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            //第二个执行方法,onPreExecute()执行完后执行
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx
            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=56&Rec_AlmTimeLen="+mRecConfigModel.getRec_AlmTimeLen();
            Log.e(tag,url+"" +
                    ""+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
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


    class SetMdSensitiveConfigTask extends AsyncTask<Integer, Void, String> {
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
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx

            if (params[0] == 0){
                MD_Sensitive = 100;
            }
            else if (params[0] == 1){
                MD_Sensitive = 150;
            }
            else if (params[0] == 2){
                MD_Sensitive = 200;
            }

            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=46&MD_Sensitive="+MD_Sensitive+"&MD_Active=1";
            Log.e(tag,url+",MD_Sensitive,NetHandle is "+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);
            lod.dismiss();



            RetModel retModel = GsonUtil.parseJsonWithGson(result,RetModel.class);
            if (retModel != null){
                if (retModel.ret == 1){
                    mAdapter.setMD_Sensitive(MD_Sensitive);
                    mAdapter.notifyDataSetChanged();
                    SouthUtil.showDialog(PushSettingActivity.this,getString(R.string.action_Success));
                }
                else {
                    SouthUtil.showDialog(PushSettingActivity.this,getString(R.string.action_STA_T_AP_Failed));
                }
            }
            super.onPostExecute(result);
        }
    }

    class resetConfigTask extends AsyncTask<Integer, Void, String> {
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
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx



            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=19";
            Log.e(tag,url+",MD_Sensitive,NetHandle is "+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);
            lod.dismiss();



            RetModel retModel = GsonUtil.parseJsonWithGson(result,RetModel.class);
            if (retModel != null){
                if (retModel.ret == 0){

                    SouthUtil.showDialog(PushSettingActivity.this,getString(R.string.action_Failed));
                }
                else {
                    SouthUtil.showToast(PushSettingActivity.this,getString(R.string.action_Success));
                    back2TopActivity();
                    for (DevModel existModel : MainDevListFragment.mDevices){
                        if (model.SN.equals(existModel.SN)){
                            existModel.Disconn();
                            existModel.NetHandle = 0;
                        }
                    }
                }



                //需要断开连接，需要查看
            }
            else{
                SouthUtil.showToast(PushSettingActivity.this,getString(R.string.action_Success));
                back2TopActivity();
            }
            super.onPostExecute(result);
        }
    }


    class getAudioPlayPromptSoundTask extends AsyncTask<String, Void, String> {
        // AsyncTask<Params, Progress, Result>
        //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            //第二个执行方法,onPreExecute()执行完后执行
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx
            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=41";
            Log.e(tag,url+"" +
                    ""+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);
            lod.dismiss();
            if (result == null){
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                AUDIO_IsPlayPromptSound = jsonObject.getInt("AUDIO_IsPlayPromptSound") == 1 ? true : false;
                mAdapter.setAUDIO_IsPlayPromptSound(AUDIO_IsPlayPromptSound);
                mAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }


            super.onPostExecute(result);
        }
    }

    class SetAudioPlayPromptSoundTask extends AsyncTask<Integer, Void, String> {
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
            // http://IP:Port/cfg1.cgi?User=admin&Psd=admin&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wif
            //i_SSID_STA=xxxxxxxx&wifi_Password_STA=xxxxxxxx



            String url = "http://"+model.IPUID+":"+model.WebPort+"/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=42&AUDIO_IsPlayPromptSound="+params[0];
            Log.e(tag,url+",MD_Sensitive,NetHandle is "+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);
            lod.dismiss();



            RetModel retModel = GsonUtil.parseJsonWithGson(result,RetModel.class);
            if (retModel != null){
                if (retModel.ret == 1){

                   // SouthUtil.showDialog(PushSettingActivity.this,getString(R.string.action_Success));
                }
                else {
                   // SouthUtil.showDialog(PushSettingActivity.this,getString(R.string.action_Failed));
                }
            }
            super.onPostExecute(result);
        }
    }

    void back2TopActivity(){
        Intent intent= new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    LoadingDialog lod;
    final String tag = "PushSettingActivity";
}
