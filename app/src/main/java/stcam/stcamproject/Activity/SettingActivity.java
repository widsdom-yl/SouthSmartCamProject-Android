package stcam.stcamproject.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.model.DevModel;
import com.model.PushSettingModel;
import com.model.RetModel;
import com.thSDK.lib;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.DeviceSettingAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.DataManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

import static stcam.stcamproject.Activity.MainDevListFragment.EnumMainEntry.EnumMainEntry_Visitor;

public class SettingActivity extends BaseAppCompatActivity implements View.OnClickListener, BaseAdapter.OnItemClickListener {




    ImageView imageview_thumb;
    TextView textview_uid;
    Button    button_ap_sta;
    Button button_delete_device;
    MainDevListFragment.EnumMainEntry entryType;

    RecyclerView mRecyclerView;
    DeviceSettingAdapter mAdapter;
    DevModel model;
    PushSettingModel mPushSettingModel;
    int MD_Sensitive = -1;

    List<String> items = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blank_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(R.string.action_dev_setting);

            setCustomTitle(getString(R.string.action_dev_setting),true);

        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = bundle.getParcelable("devModel");
            entryType = (MainDevListFragment.EnumMainEntry) bundle.getSerializable("entry");
        }
        initView();
        initValue();

        if (model.IsConnect()){
            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();

//            getConfigTask configTask = new getConfigTask();
//            configTask.execute();

            getPushConfigTask pushConfigTask = new getPushConfigTask();
            pushConfigTask.execute();
        }




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
        button_delete_device = findViewById(R.id.button_delete_device);
        button_ap_sta = findViewById(R.id.button_ap_sta);
        if (entryType == EnumMainEntry_Visitor){
            button_ap_sta.setText(R.string.action_AP_T_STA);
        }
        else{
            button_ap_sta.setText(R.string.action_STA_T_AP);
        }

        if (entryType == EnumMainEntry_Visitor){
            button_delete_device.setVisibility(View.INVISIBLE);
        }


        button_delete_device.setOnClickListener(this);
        button_ap_sta.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }
    void initValue(){
        items.add(getString(R.string.device_name));
        items.add(getString(R.string.action_device_pwd));
        items.add(getString(R.string.action_push));
        items.add(getString(R.string.action_manager_senior));

//        items.add(getString(R.string.action_manager_alarm_level));
        items.add(getString(R.string.action_version));

        mAdapter = new DeviceSettingAdapter(items);
        mAdapter.setDevModel(model);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

    }
    void refreshView(){
        List<String> files= FileUtil.getSNFilesFromPath(FileUtil.pathSnapShot(),model.SN);
        if (files.size()>0){

            Bitmap bitmap = BitmapFactory.decodeFile(files.get(0));
            imageview_thumb.setImageBitmap(bitmap);
        }
        textview_uid.setText(model.UID);
        mAdapter.notifyDataSetChanged();
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
                        ChangeDeviceNameTask task = new  ChangeDeviceNameTask();
                        task.execute(inputName);
                    }
                });
        builder.show();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.button_delete_device:
                new AlertDialog.Builder(this)
                        .setTitle(this.getString(R.string.action_delete_device_ask))
                        .setPositiveButton(this.getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (lod == null){
                                    lod = new LoadingDialog(SettingActivity.this);
                                }
                                lod.dialogShow();
                                ServerNetWork.getCommandApi()
                                        .app_user_del_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                                                model.SN, 0)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(observer_delete);
                            }
                        })
                        .setNegativeButton(this.getString(R.string.action_cancel), null)
                        .show();


                break;

//                break;
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
                        SouthUtil.showDialog(SettingActivity.this,"当前无有效连接，无法转AP模式");
                    }


                }
                break;
                default:
                    break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {


        /*
         items.add(getString(R.string.device_name));
        items.add(getString(R.string.action_device_pwd));
        items.add(getString(R.string.action_push));
        items.add(getString(R.string.action_manager_senior));
        items.add(getString(R.string.action_manager_volume));
//        items.add(getString(R.string.action_manager_alarm_level));
        items.add(getString(R.string.action_version));
        * */
        if (entryType == EnumMainEntry_Visitor){
            SouthUtil.showDialog(this,getString(R.string.string_mode_visitor));
            return;
        }



        if (!model.IsConnect()){
            SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_net_not_connect));
            return;
        }

        if (0 == position){

            if (model.IsShare == 0){
                SouthUtil.showDialog(this,getString(R.string.string_device_is_share));
                return;
            }

           changeDeviceNameDialog();

        }
        else if(1 == position){

            if (model.IsShare == 0){
                SouthUtil.showDialog(this,getString(R.string.string_device_is_share));
                return;
            }

                Intent intent = new Intent(this,ChangeDevicePwdActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("model",model);
                bundle.putSerializable("type",ChangeDevicePwdActivity.EnumChangeDevicePwd.DEVICE_SETTING);

                intent.putExtras(bundle);
                startActivity(intent);
        }
        else if(2 == position){

            if (model.IsPush == 0){
                SouthUtil.showDialog(this,getString(R.string.string_no_push_permisson));
                return;
            }

            dialogChoice1();
        }
        else if(3 == position){
            if (model.IsShare == 0){
                SouthUtil.showDialog(this,getString(R.string.string_device_is_share));
                return;
            }

            Intent intent = new Intent(this,PushSettingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("devModel",model);
            intent.putExtras(bundle);
            startActivity(intent);
        }

//        else if(4 == position){
//            dialogChoice();
//        }
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
                setPushConfigTask task = new setPushConfigTask();
                task.execute();
            }
        });
        builder.create().show();
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
                    lod = new LoadingDialog(SettingActivity.this);
                }
                lod.dialogShow();
                SetMdSensitiveConfigTask task = new SetMdSensitiveConfigTask();
                task.execute(chooseLevel);
            }
        });
        builder.create().show();
    }



    @Override
    public void onLongClick(View view, int position) {

    }

    class ChangeDeviceNameTask extends AsyncTask<String, Void, String> {
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



//                Encoding targetEncoding = Xml.Encoding.GetEncoding(1252);
//                byte[] utf8Bytes = targetEncoding.GetBytes(text);
//                byte[] ansiBytes = Encoding.Convert(Encoding.UTF8,
//                        targetEncoding,
//                        utf8Bytes);


                //String devName = new String(params[0].getBytes("UTF-8"), "GB2312");
                String url = model.getHttpCfg1UsrPwd() +"&MsgID=31&DevName="+params[0];
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
            String url = model.getHttpCfg1UsrPwd() +"&MsgID=38&wifi_Active=1&wifi_IsAPMode=1&s=0";
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
                    model.NetHandle = 0;
                    for (DevModel existModel : MainDevListFragment.mDevices){
                        if (model.SN.equals(existModel.SN)){
                            existModel.Disconn();
                            existModel.NetHandle = 0;
                        }
                    }
                }
                else if(retModel.ret == 2){
                   // SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_STA_T_AP_Success));
                    RebootTask task = new RebootTask();
                    task.execute();
                }
                else {
                    SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_STA_T_AP_Failed));
                }
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


            String url = model.getHttpCfg1UsrPwd() +"&MsgID==18";

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
                    model.NetHandle = 0;
                    for (DevModel existModel : MainDevListFragment.mDevices){
                        if (model.SN.equals(existModel.SN)){
                            existModel.Disconn();
                            existModel.NetHandle = 0;
                        }
                    }
                }
                else
                {
                    SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_Failed));
                }
            }
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
                    SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_Success));
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


    Observer<RetModel> observer_delete = new Observer<RetModel>() {
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
                DataManager.getInstance().deleteDev(model);
                for (DevModel existModel : MainDevListFragment.mDevices){
                    if (model.SN.equals(existModel.SN)){
                        MainDevListFragment.mDevices.remove(existModel);
                        break;
                    }
                }

                back2TopActivity();
            }
            else{
                SouthUtil.showToast(SettingActivity.this,"delete failed");
            }

        }
    };

    void back2TopActivity(){
        Intent intent= new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

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
                mAdapter.setmPushSettingModel(mPushSettingModel);
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



    LoadingDialog lod;
    final String tag = "SettingActivity";
}
