package stcam.stcamproject.Activity;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.model.DevModel;
import com.model.PushSettingModel;
import com.thSDK.lib;

import java.util.ArrayList;
import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.PushSettingAdapter;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.View.LoadingDialog;

public class PushSettingActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener {
    List<String> items = new ArrayList<>();
    RecyclerView mRecyclerView;
    PushSettingAdapter mAdapter;
    DevModel model;
    PushSettingModel mPushSettingModel = new PushSettingModel();

    Handler handler=new Handler();
    Runnable runnable=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            setPushConfigTask task = new setPushConfigTask();
            task.execute();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_manager_push);
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

    }
    void initValue(){
        items.add(getString(R.string.action_push));
        items.add(getString(R.string.action_push_interval));
        items.add(getString(R.string.action_pir_sensitivity));
        mAdapter = new PushSettingAdapter(items);

        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);


        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();

        getPushConfigTask configTask = new getPushConfigTask();
        configTask.execute();

    }

    @Override
    public void onItemClick(View view, int position) {
        if (0 == position){
            dialogChoice1();
        }
        else if(1 == position){
            dialogChoice2();
        }
        else if(2 == position){
            dialogChoice3();
        }
    }

    /*开关*/
    private void dialogChoice1() {

        final String items[] = {getString(R.string.action_close), getString(R.string.action_open)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle(getString(R.string.action_push));
        builder.setIcon(R.mipmap.ic_launcher);


        builder.setSingleChoiceItems(items, mPushSettingModel.getPushActive(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(tag,"choose :"+which);
                        mPushSettingModel.setPushActive(which);
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
        builder.setTitle(getString(R.string.action_push_interval));
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



    @Override
    public void onLongClick(View view, int position) {

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
            Log.e(tag,url+"" +
                    ""+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {


            super.onPostExecute(result);
        }
    }

    LoadingDialog lod;
    final String tag = "PushSettingActivity";
}
