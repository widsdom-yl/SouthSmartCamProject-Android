package stcam.stcamproject.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.model.DevModel;
import com.model.RetModel;
import com.model.SDInfoModel;
import com.thSDK.lib;

import java.util.ArrayList;
import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.SDVolumeSettingAdapter;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;

public class SDVolumeManagerActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, SDVolumeSettingAdapter.OnRecordClickListener {

    List<String> items = new ArrayList<>();
    SDVolumeSettingAdapter mAdapter;
    DevModel model;
    RecyclerView mRecyclerView;
    Button button_format;
    int isRecord;
    SDInfoModel sdInfoModel;

    RadioGroup record_group ;

//    TextView textView_left;
//    TextView textView_used;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdvolume_manager);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_manager_volume);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = bundle.getParcelable("devModel");
        }


        initView();
        initValue();


        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();


        GetSDInfoTask task1 = new GetSDInfoTask();
        task1.execute();

        GetRecordStateTask task = new GetRecordStateTask();
        task.execute();

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
    void initView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        button_format = findViewById(R.id.button_format);
        button_format.setOnClickListener(this);

//        record_group = findViewById(R.id.record_group);

//        textView_left = findViewById(R.id.textView_left);
//        textView_used = findViewById(R.id.textView_used);


    }
    void initValue(){
        items.add(getString(R.string.action_volume_total));
//        items.add(getString(R.string.action_volume_record));
        items.add(getString(R.string.action_volume_left));
        items.add(getString(R.string.action_record_recycle));



        mAdapter = new SDVolumeSettingAdapter(items);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnRecordClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void onClick(View view) {
        if (view == button_format){
            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();
            SDFormatTask task = new SDFormatTask();
            task.execute();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
//        switch (checkedId) {
//            case R.id.record_stop_radio:
//                ControlRecordStateTask task = new ControlRecordStateTask();
//                task.execute(22);
//                break;
//            case R.id.record_recycle_radio:
//                ControlRecordStateTask task1 = new ControlRecordStateTask();
//                task1.execute(21);
//                break;
//            default:
//                break;
//        }

    }

    @Override
    public void onRecordClickListener(boolean record) {
        if (!record){
            ControlRecordStateTask task = new ControlRecordStateTask();
            task.execute(22);
        }
        else{
            ControlRecordStateTask task1 = new ControlRecordStateTask();
            task1.execute(21);
        }


    }


    class SDFormatTask extends AsyncTask<String, Void, String> {
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
            String url = model.getHttpCfg1UsrPwd() +"&MsgID=92";
            Log.e(tag,url+",NetHandle is "+model.NetHandle);
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
                    SouthUtil.showDialog(SDVolumeManagerActivity.this,getString(R.string.action_Success));
                }
                else if (retModel.ret == 2){
                    SouthUtil.showDialog(SDVolumeManagerActivity.this,getString(R.string.action_Success_Reboot));
                }
                else {
                    SouthUtil.showDialog(SDVolumeManagerActivity.this,getString(R.string.action_Failed));
                }
            }
            super.onPostExecute(result);
        }
    }

    class GetRecordStateTask extends AsyncTask<String, Void, String> {
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
            String url = model.getHttpCfg1UsrPwd() +"&MsgID=85";
            Log.e(tag,url+",NetHandle is "+model.NetHandle);
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
//                if (retModel.ret == 1){
//                    isRecord = 1;
//                    //record_group.check(R.id.record_recycle_radio);
//
//
//                }
//                else{
//                    //record_group.check(R.id.record_stop_radio);
//                    isRecord = 0;
//
//                }
                mAdapter.setIsRecord(retModel.ret == 1? true:false);
                mAdapter.notifyDataSetChanged();
            }
            //record_group.setOnCheckedChangeListener(SDVolumeManagerActivity.this);

            super.onPostExecute(result);
        }
    }

    class GetSDInfoTask extends AsyncTask<String, Void, String> {
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
            String url = model.getHttpCfg1UsrPwd() +"&MsgID=53";
            Log.e(tag,url+",NetHandle is "+model.NetHandle);
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

            sdInfoModel = GsonUtil.parseJsonWithGson(result,SDInfoModel.class);
            if (sdInfoModel != null){
                mAdapter.setSdInfoModel(sdInfoModel);
                mAdapter.notifyDataSetChanged();
//                textView_left.setText(SDVolumeManagerActivity.this.getString(R.string.action_volume_left_percent)+sdInfoModel.getFreePercent());
//                textView_used.setText(SDVolumeManagerActivity.this.getString(R.string.action_volume_left_percent)+sdInfoModel.getUsedPercent());
            }

            super.onPostExecute(result);
        }
    }

    class ControlRecordStateTask extends AsyncTask<Integer, Void, String> {
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
            String url = model.getHttpCfg1UsrPwd() +"&MsgID="+params[0];
            Log.e(tag,url+",NetHandle is "+model.NetHandle);
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
                    SouthUtil.showDialog(SDVolumeManagerActivity.this,getString(R.string.action_Success));
                }
                else{
                    SouthUtil.showDialog(SDVolumeManagerActivity.this,getString(R.string.action_Failed));
                }
            }

            super.onPostExecute(result);
        }
    }


    LoadingDialog lod;
    final String tag = "SDVolumeManagerActivity";
}
