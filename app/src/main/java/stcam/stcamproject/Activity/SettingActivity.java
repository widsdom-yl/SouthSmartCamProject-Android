package stcam.stcamproject.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.model.DevModel;
import com.model.RetModel;
import com.thSDK.lib;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

import static stcam.stcamproject.Activity.MainDevListActivity.EnumMainEntry.EnumMainEntry_Visitor;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imageview_thumb;
    TextView textview_uid;
    TextView textview_name;
    TextView textview_pwd;
    TextView textview_version;
    Button button_change_pwd,button_ap_sta;
    Button button_delete_device;
    Button button_device_name;
    MainDevListActivity.EnumMainEntry entryType;
    DevModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_setting);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = bundle.getParcelable("devModel");
            entryType = (MainDevListActivity.EnumMainEntry) bundle.getSerializable("entry");
        }
        initView();
    }
    @Override
    protected void onResume() {
        super.onResume();
        model.updateUserAndPwd();
        refreshView();
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
        imageview_thumb = findViewById(R.id.imageview_thumb);
        textview_uid = findViewById(R.id.textview_uid);
        textview_name = findViewById(R.id.textview_name);
        textview_pwd = findViewById(R.id.textview_pwd);
        textview_version = findViewById(R.id.textview_version);
        button_change_pwd = findViewById(R.id.button_change_pwd);
        button_delete_device = findViewById(R.id.button_delete_device);
        button_ap_sta = findViewById(R.id.button_ap_sta);
        button_device_name = findViewById(R.id.button_device_name);
        if (entryType == EnumMainEntry_Visitor){
            button_ap_sta.setText(R.string.action_AP_T_STA);
        }
        else{
            button_ap_sta.setText(R.string.action_STA_T_AP);
        }

        if (entryType == EnumMainEntry_Visitor){
            button_delete_device.setVisibility(View.INVISIBLE);
        }


        button_change_pwd.setOnClickListener(this);
        button_delete_device.setOnClickListener(this);
        button_ap_sta.setOnClickListener(this);
        button_device_name.setOnClickListener(this);

    }
    void refreshView(){
        List<String> files= FileUtil.getImagePathFromPath(FileUtil.pathSnapShot(),model.SN);
        if (files.size()>0){

            Bitmap bitmap = BitmapFactory.decodeFile(files.get(0));
            imageview_thumb.setImageBitmap(bitmap);
        }
        textview_uid.setText(model.UID);
        button_device_name.setText(model.DevName);
        textview_pwd.setText(getString(R.string.device_pwd)+":"+model.pwd);
        textview_version.setText(getString(R.string.device_version)+":"+model.SoftVersion);
    }

    private void changeDeviceNameDialog() {

        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.action_change_device_name)).setView(inputServer)
                .setNegativeButton(
                getString(R.string.cancel), null);
        builder.setPositiveButton(getString(R.string.OK),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (lod == null){
                            lod = new LoadingDialog(SettingActivity.this);
                        }
                        lod.dialogShow();
                        String inputName = inputServer.getText().toString();
                        ChangeDeviceTask task = new  ChangeDeviceTask();
                        task.execute(inputName);
                    }
                });
        builder.show();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_change_pwd:
                Intent intent = new Intent(this,ChangeDevicePwdActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("model",model);
                bundle.putSerializable("type",ChangeDevicePwdActivity.EnumChangeDevicePwd.DEVICE_SETTING);

                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.button_delete_device:
                if (lod == null){
                    lod = new LoadingDialog(this);
                }
                lod.dialogShow();
                ServerNetWork.getCommandApi()
                        .app_user_del_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                                model.SN, 0)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(observer);
                break;
            case R.id.button_device_name:
                if (model.IsConnect()){
                    changeDeviceNameDialog();
                }
                break;
            case R.id.button_ap_sta:
                if (entryType == EnumMainEntry_Visitor){
                    //设置ap转sta
                    Intent intent1 = new Intent(STApplication.getInstance(), AddDeviceAP2StaSetup.class);

                    Bundle bundle1 = new Bundle();
                    bundle1.putParcelable("devModel",model);

                    intent1.putExtras(bundle1);
                    Log.e(tag,"to DeviceShareActivity NetHandle:"+model.NetHandle);

                    startActivity(intent1);
                }
                else{
                    //设置sta转ap

                    if (model.getConnectType() == DevModel.CONNECT_TYPE.IS_CONN_LAN || model.getConnectType() == DevModel.CONNECT_TYPE.IS_CONN_DDNS || model.getConnectType() == DevModel.CONNECT_TYPE.IS_CONN_P2P)
                    {
                        if (lod == null){
                            lod = new LoadingDialog(this);
                        }
                        lod.dialogShow();
//                        Network.getCommandApi(model)
//                                .STA2AP_keepValue(model.usr,model.pwd,38,1,1,0)
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(observer_sta_ap);
                        Sta2ApTask task = new Sta2ApTask();
                        task.execute();
                    }
                    else{
                        SouthUtil.showDialog(SettingActivity.this,"当前五有效连接，无法转AP模式");
                    }


                }
                break;
                default:
                    break;
        }
    }

    class ChangeDeviceTask extends AsyncTask<String, Void, String> {
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
            String url = "http://0.0.0.0:0/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=31&DevName="+params[0];
            Log.e(tag,url+",NetHandle is "+model.NetHandle);
            String ret = lib.thNetHttpGet(model.NetHandle,url);
            Log.e(tag,"ret :"+ret);
            RetModel retModel = GsonUtil.parseJsonWithGson(ret,RetModel.class);
            if (retModel != null){
                if (retModel.ret == 1){
                    model.DevName = params[0];
                }
            }
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
                    SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_Success));
                    refreshView();
                }
                else {
                    SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_Failed));
                }
            }
            super.onPostExecute(result);
        }
    }

    class Sta2ApTask extends AsyncTask<String, Void, String> {
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
            String url = "http://0.0.0.0:0/cfg1.cgi?User="+model.usr+"&Psd="+model.pwd+"&MsgID=38&wifi_Active=1&wifi_IsAPMode=1&s=0";
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
                    SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_STA_T_AP_Success));
                }
                else {
                    SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_STA_T_AP_Failed));
                }
            }
            super.onPostExecute(result);
        }
    }

    Observer<RetModel> observer_sta_ap = new Observer<RetModel>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
        }

        @Override
        public void onNext(RetModel m) {
            lod.dismiss();
            Log.e(tag,"---------------------0:"+m.ret);
            if (1 == m.ret){
                SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_STA_T_AP_Success));
            }
            else{
                SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_STA_T_AP_Failed));
            }

        }
    };


    Observer<RetModel> observer = new Observer<RetModel>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
            SouthUtil.showToast(getApplicationContext(),"delete failed");
        }

        @Override
        public void onNext(RetModel m) {
            lod.dismiss();
            Log.e(tag,"---------------------0:"+m.ret);
            if (1 == m.ret){
                SouthUtil.showToast(SettingActivity.this,"delete ok");
            }
            else{
                SouthUtil.showToast(SettingActivity.this,"delete failed");
            }

        }
    };


    LoadingDialog lod;
    final String tag = "SettingActivity";
}
