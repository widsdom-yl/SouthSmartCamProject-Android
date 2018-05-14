package stcam.stcamproject.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.model.DevModel;
import com.model.SearchDevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import stcam.stcamproject.R;
import stcam.stcamproject.View.LoadingDialog;

public class AddDeviceAP2StaSetup extends AppCompatActivity implements View.OnClickListener {
    SearchDevModel model;
    DevModel devModel;
    EditText edittext_ssid_name;
    EditText edittext_ssid_pwd;
    Button button_next;
    LoadingDialog lod;
    final static  String tag =  "AddDeviceAP2StaSetup";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_ap2_sta_setup);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_add_ap_sta);
        }

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = bundle.getParcelable("model");
            devModel = new DevModel();
            devModel.UID = model.getUID();
            devModel.SN = model.getSN();
            devModel.WebPort = model.getHttpPort();
            devModel.DataPort = model.getDataPort();
            devModel.threadConnect(ipc,devModel,false);
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
        edittext_ssid_name = findViewById(R.id.edittext_ssid_name);
        edittext_ssid_pwd = findViewById(R.id.edittext_ssid_pwd);
        button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(this);
    }
    public final Handler ipc = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {


            super.handleMessage(msg);
            DevModel model;
            switch (msg.what)
            {
                case TMsg.Msg_NetConnSucceed:
                    model = (DevModel) msg.obj;
                    Log.e(tag,"NetConnSucceed:"+model.SN+"DevNode.NetHandle:"+model.NetHandle);
                    break;
                case TMsg.Msg_NetConnFail:
                    model = (DevModel) msg.obj;
                    Log.e(tag,"NetConnFail:"+model.SN+"DevNode.NetHandle:"+model.NetHandle);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        if (edittext_ssid_name.getText().length()>0&&edittext_ssid_pwd.getText().length()>0 ){
            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();
            SetSSIDTask task = new SetSSIDTask();
            task.execute(edittext_ssid_name.getText().toString(),edittext_ssid_pwd.getText().toString());
        }
    }

    class SetSSIDTask extends AsyncTask<String, Void, String> {
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
            String url = "http://0.0.0.0:0/cfg1.cgi?User="+devModel.usr+"&Psd="+devModel.pwd+"&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wifi_SSID_STA" +
                    "="+params[0]+"&wifi_Password_STA="+params[1];
            String ret = lib.thNetHttpGet(devModel.NetHandle,url);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            //Log.e(tag,"get playback list :"+result);
            lod.dismiss();
            super.onPostExecute(result);
        }
    }

}
