package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.model.DevModel;
import com.model.RetModel;
import com.model.SearchDevModel;
import com.model.ShareModel;
import com.thSDK.lib;

import java.io.Serializable;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.DataManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class ChangeDevicePwdActivity extends AppCompatActivity implements View.OnClickListener {

    final String tag = "ChangeDevicePwdActivity";

    public enum EnumChangeDevicePwd implements Serializable{
        SHARE,
        WLAN,
        STA,//ap to sta
        AP,//ap 游客模式
        DEVICE_SETTING
    }

    EnumChangeDevicePwd enumType;
    SearchDevModel model;
    ShareModel shareModel;
    DevModel setModel;
    DevModel dbModel;
    EditText editText_confirm_pwd;
    EditText editText_new_pwd;
    EditText editText_old_pwd;
    TextView text_uid_detail;
    Button confirmButton;
    String SN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_device_pwd);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_device_password_manager);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            enumType = (EnumChangeDevicePwd) bundle.getSerializable("type");
            switch (enumType){
                case SHARE:
                    //ShareModel model
                    shareModel = bundle.getParcelable("model");
                    dbModel = DataManager.getInstance().getSNDev(shareModel.SN);

                    SN = shareModel.SN;
                    break;
                case WLAN:
                case STA:
                case AP:
                    model = bundle.getParcelable("model");
                    dbModel = DataManager.getInstance().getSNDev(model.getSN());
                    SN = model.getSN();
                    break;
                case DEVICE_SETTING:
                    setModel = bundle.getParcelable("model");
                    dbModel = DataManager.getInstance().getSNDev(setModel.SN);
                    SN = setModel.SN;
                default:
                        break;
            }


        }

        initView();

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

        editText_confirm_pwd = findViewById(R.id.editText_confirm_pwd);
        editText_new_pwd = findViewById(R.id.editText_new_pwd);
        editText_old_pwd = findViewById(R.id.editText_old_pwd);
        confirmButton = findViewById(R.id.button_next);
        text_uid_detail = findViewById(R.id.text_uid_detail);

        switch (enumType) {
            case SHARE:
                //ShareModel model
                text_uid_detail.setText(shareModel.UID);
                break;
            case WLAN:
            case STA:
            case AP:
                text_uid_detail.setText( model.getUID());
                break;
            case DEVICE_SETTING:
                text_uid_detail.setText( setModel.UID);
            default:
                break;
        }



        confirmButton.setOnClickListener(this);

        if (dbModel != null){
            editText_old_pwd.setText(dbModel.pwd);
        }
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_next){
            Log.e(tag,"----0,change pwd ,new pwd is  "+editText_new_pwd.getText().toString());
            if (editText_new_pwd.getText().toString().length() >= 20 ){
                SouthUtil.showDialog(ChangeDevicePwdActivity.this,getString(R.string.action_dev_pwd_limit_lessthan_19));
                return;
            }
            if (editText_new_pwd.getText().toString().equals(editText_confirm_pwd.getText().toString()) && editText_new_pwd.getText().toString().length() >= 4){
                Log.e(tag,"----1,change pwd ,new pwd is  "+editText_new_pwd.getText().toString());
                if (dbModel == null){
                    Log.e(tag,"----2,change pwd ,new pwd is  "+editText_new_pwd.getText().toString());
                    DevModel devModel = new DevModel();
                    devModel.SN = SN;
                    devModel.usr = "admin";//默认填写admin
                    devModel.pwd = editText_new_pwd.getText().toString();
                    boolean ret  = DataManager.getInstance().addDev(devModel);
                    Log.e(tag,"addDev ,ret is "+ret);
                }
                else{
                    Log.e(tag,"----3,change pwd ,new pwd is  "+editText_new_pwd.getText().toString());
                    dbModel.pwd = editText_new_pwd.getText().toString();
                    boolean ret = DataManager.getInstance().updateDev(dbModel);
                    Log.e(tag,"updateDev ,ret is "+ret);
                }

                //修改密码


                if (enumType == EnumChangeDevicePwd.WLAN){
                    if (lod == null){
                        lod = new LoadingDialog(this);
                    }
                    lod.dialogShow();
                    ServerNetWork.getCommandApi().app_user_add_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                            JPushManager.getJPushRegisterID(),
                            1,0,0,model.getSN(),0)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(observer_add_dev);
                }
                else if(enumType == EnumChangeDevicePwd.STA){
                   // AddDeviceAP2StaSetup
                    Intent intent = new Intent(this,AddDeviceAP2StaSetup.class);
                    intent.putExtra("model" ,model);
                    startActivity(intent);
                }
                else if(enumType == EnumChangeDevicePwd.SHARE){
                    if (lod == null){
                        lod = new LoadingDialog(this);
                    }
                    lod.dialogShow();
                    ServerNetWork.getCommandApi().app_share_add_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                            shareModel.From,JPushManager.getJPushRegisterID(),1,0,0,shareModel.SN,shareModel.Video,shareModel.History,shareModel.Push,
                            shareModel.Setup,shareModel.Control).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(observer_add_dev);
                }
                else if(enumType == EnumChangeDevicePwd.DEVICE_SETTING){
                    if (lod == null){
                        lod = new LoadingDialog(this);
                    }
                    lod.dialogShow();
                    ChangeDevicePwdTask task = new ChangeDevicePwdTask();
                    task.execute();
                   // finish();
                }
            }
        }

    }

    class ChangeDevicePwdTask extends AsyncTask<String, Void, String> {
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


            String url = "http://"+setModel.IPUID+":"+setModel.WebPort+"/cfg1.cgi?User=admin&Psd="+editText_old_pwd.getText().toString()+"&MsgID=33&UserName0=admin&Password0="+editText_new_pwd.getText().toString();
            Log.e(tag,url+",NetHandle is "+setModel.NetHandle);
            String ret = lib.thNetHttpGet(setModel.NetHandle,url);
            Log.e(tag,"ret :"+ret);

            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);


            RetModel retModel = GsonUtil.parseJsonWithGson(result,RetModel.class);
            if (retModel != null){
                if (retModel.ret == 1){
                    lod.dismiss();
                    SouthUtil.showDialog(ChangeDevicePwdActivity.this,getString(R.string.action_Success));
                }
                else if(retModel.ret == 2){
                    RebootTask task =  new RebootTask();
                    task.execute();
                }
                else
                {
                    lod.dismiss();
                    SouthUtil.showDialog(ChangeDevicePwdActivity.this,getString(R.string.action_Failed));
                }
            }
            else{
                lod.dismiss();
            }
            super.onPostExecute(result);
        }
    }
    class RebootTask extends AsyncTask<String, Void, String> {
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


            String url = "http://"+setModel.IPUID+":"+setModel.WebPort+"/cfg1.cgi?User=admin&Psd="+editText_old_pwd.getText().toString()+"&MsgID=18";
            Log.e(tag,url+",NetHandle is "+setModel.NetHandle);
            String ret = lib.thNetHttpGet(setModel.NetHandle,url);
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
                    SouthUtil.showDialog(ChangeDevicePwdActivity.this,getString(R.string.action_Success));
                }
                else
                {
                    SouthUtil.showDialog(ChangeDevicePwdActivity.this,getString(R.string.action_Failed));
                }
            }
            super.onPostExecute(result);
        }
    }

    Observer<RetModel> observer_add_dev = new Observer<RetModel>() {
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
                SouthUtil.showToast(ChangeDevicePwdActivity.this,"add success");
            }
            else{
                SouthUtil.showToast(ChangeDevicePwdActivity.this,"add failed");
            }

        }
    };
    LoadingDialog lod;

}
