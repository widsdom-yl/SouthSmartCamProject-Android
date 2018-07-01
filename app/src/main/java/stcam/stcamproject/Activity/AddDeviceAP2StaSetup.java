package stcam.stcamproject.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.model.DevModel;
import com.model.RetModel;
import com.model.SSIDModel;
import com.model.SearchDevModel;
import com.thSDK.lib;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.Network;

public class AddDeviceAP2StaSetup extends AppCompatActivity implements View.OnClickListener {
    SearchDevModel model;
    DevModel devModel;
    Spinner spiner_ssid_name;
    private ArrayAdapter<String> arr_adapter;
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
            actionBar.setTitle(R.string.action_AP_T_STA);
        }

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = bundle.getParcelable("devModel");
        }
        initView();
        getSSIDList();

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
        spiner_ssid_name = findViewById(R.id.spiner_ssid_name);
        edittext_ssid_name = findViewById(R.id.edittext_ssid_name);
        edittext_ssid_pwd = findViewById(R.id.edittext_ssid_pwd);
        button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(this);
    }

    void getSSIDList(){
        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();
//            SetSSIDTask task = new SetSSIDTask();
//            task.execute(edittext_ssid_name.getText().toString(),edittext_ssid_pwd.getText().toString());

        Network.getCommandApi(devModel)
                .getSSIDList(devModel.usr,devModel.pwd,36,0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_SSIDList);
    }


    @Override
    public void onClick(View view) {
        if (edittext_ssid_pwd.getText().length()>0 ){
            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();
//            SetSSIDTask task = new SetSSIDTask();
//            task.execute(edittext_ssid_name.getText().toString(),edittext_ssid_pwd.getText().toString());

            Network.getCommandApi(devModel)
                    .AP2STA_changeValue(devModel.usr,devModel.pwd,38,1,0,spiner_ssid_name.getSelectedItem().toString(),
                            edittext_ssid_pwd.getText().toString(),0)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer_ret);

        }
    }


    Observer<List<SSIDModel>> observer_SSIDList = new Observer<List<SSIDModel>>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"observer_SSIDList---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"observer_SSIDList ---------------------1:"+e.getLocalizedMessage());

        }

        @Override
        public void onNext(List<SSIDModel> ssidModels) {
            lod.dismiss();
            Log.e(tag,"observer_SSIDList ---------------------1:"+ssidModels.size());
            if(ssidModels.size() == 0){
                edittext_ssid_name.setVisibility(View.VISIBLE);
                spiner_ssid_name.setVisibility(View.INVISIBLE);
                return;
            }
            edittext_ssid_name.setVisibility(View.INVISIBLE);
            spiner_ssid_name.setVisibility(View.VISIBLE);

            //数据
            List data_list = new ArrayList<String>();
            for (SSIDModel model : ssidModels){
                data_list.add(model.SSID);
            }

            //适配器
            arr_adapter= new ArrayAdapter<String>(AddDeviceAP2StaSetup.this, android.R.layout.simple_spinner_item, data_list);
            //设置样式
            arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //加载适配器
            spiner_ssid_name.setAdapter(arr_adapter);

        }
    };


    Observer<RetModel> observer_ret = new Observer<RetModel>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
            SouthUtil.showDialog(AddDeviceAP2StaSetup.this,getString(R.string.device_reboot));
            for (DevModel existModel : MainDevListActivity.mDevices){
                if (devModel.SN.equals(existModel.SN)){
                    existModel.Disconn();
                    existModel.NetHandle = 0;
                }
            }

        }

        @Override
        public void onNext(RetModel m) {
            lod.dismiss();
            Log.e(tag,"---------------------0:"+m.ret);
            if (1 == m.ret){
                SouthUtil.showDialog(AddDeviceAP2StaSetup.this,getString(R.string.action_AP_T_STA_Success));
                for (DevModel existModel : MainDevListActivity.mDevices){
                    if (devModel.SN.equals(existModel.SN)){
                        existModel.Disconn();
                        existModel.NetHandle = 0;
                    }
                }
            }
            else if(2 == m.ret){
                for (DevModel existModel : MainDevListActivity.mDevices){
                    if (devModel.SN.equals(existModel.SN)){
                        existModel.Disconn();
                        existModel.NetHandle = 0;
                    }
                }
            }
            else{
                SouthUtil.showDialog(AddDeviceAP2StaSetup.this,getString(R.string.action_AP_T_STA_Failed));
            }

        }
    };

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
            String url = devModel.getHttpCfg1UsrPwd() +
            "&MsgID=38&wifi_Active=1&wifi_IsAPMode=0&wifi_SSID_STA" +
                    "="+params[0]+"&wifi_Password_STA="+params[1];
            Log.e(tag,url+",NetHandle is "+devModel.NetHandle);
            String ret = lib.thNetHttpGet(devModel.NetHandle,url);
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
                    SouthUtil.showDialog(AddDeviceAP2StaSetup.this,getString(R.string.action_AP_T_STA_Success));

                }
                else if(retModel.ret == 2){
                    SouthUtil.showDialog(AddDeviceAP2StaSetup.this,getString(R.string.action_AP_T_STA_Success));

                }
                else {
                    SouthUtil.showDialog(AddDeviceAP2StaSetup.this,getString(R.string.action_AP_T_STA_Failed));
                }
            }
            super.onPostExecute(result);
        }
    }

}
