package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.model.DevModel;
import com.model.RetModel;
import com.model.SearchDevModel;
import com.model.ShareModel;
import com.thSDK.TMsg;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.DataManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.DeviceParseUtil;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class AddDeviceActivity extends BaseAppCompatActivity implements View.OnClickListener {

    static final String tag = "AddDeviceActivity";
    final int REQUEST_CODE = 10001;




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

            setCustomTitle(getString(R.string.action_add_device),true);


        }

        setContentView(R.layout.activity_add_device);
        TextView btn_search = findViewById(R.id.btn_add_device_search);
        TextView btn_add_device_share = findViewById(R.id.btn_add_device_share);
        btn_search.setOnClickListener(this);
        btn_add_device_share.setOnClickListener(this);
        findViewById(R.id.btn_add_device_one_key).setOnClickListener(this);
        findViewById(R.id.btn_add_device_ap_sta).setOnClickListener(this);
        //findViewById(R.id.btn_add_device_ap).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(tag, "onActivityResult"+"requestCode"+requestCode+"\n resultCode="+resultCode);
        if(requestCode==REQUEST_CODE) {
            if (requestCode == REQUEST_CODE) {
                //处理扫描结果（在界面上显示）
                if (null != data) {
                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        return;
                    }
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                       // Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                        addDevice_share(result);

                    } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                        Toast.makeText(AddDeviceActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add_device_search:
//                if (lod == null){
//                    lod = new LoadingDialog(this);
//                }
//                lod.dialogShow();
//
//                SouthUtil.showToast(this,"search");
//                new Thread()
//                {
//                    @Override
//                    public void run()
//                    {
//                        SearchMsg = lib.thNetSearchDevice(3000, 1);
//                        ipc.sendMessage(Message.obtain(ipc, TMsg.Msg_SearchOver, 0, 0, null));
//                        IsSearching = false;
//                    }
//                }.start();
                Intent intent4 = new Intent(STApplication.getInstance(), AddDeviceWlanActivity.class);
                startActivity(intent4);
                  break;
            case R.id.btn_add_device_share:
                Intent intent = new Intent(AddDeviceActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.btn_add_device_one_key:
                Intent intent1 = new Intent(STApplication.getInstance(), AddDeviceOneStepActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_add_device_ap_sta:
                Intent intent2 = new Intent(STApplication.getInstance(), AddDeviceAP2StaActivity.class);
                intent2.putExtra("type",1);
                startActivity(intent2);
                break;
//            case R.id.btn_add_device_ap:
//                Intent intent3 = new Intent(STApplication.getInstance(), AddDeviceAP2StaActivity.class);
//                intent3.putExtra("type",2);
//                startActivity(intent3);
//                break;
            default:
                break;
        }

    }

    void back2TopActivity(){
        Intent intent= new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    public final Handler ipc = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {


            super.handleMessage(msg);
            switch (msg.what)
            {
                case TMsg.Msg_SearchOver:
                    if (SearchMsg.equals(""))
                    {
                        return;
                    }
                    Log.e(tag,SearchMsg);
                    //[{"SN":"80005556","DevModal":"401H","DevName":"IPCAM_80005556","DevMAC":"00:C1:A1:62:55:56",
                    // "DevIP":"192.168.0.199","SubMask":"255.255.255.0","Gateway":"192.168.0.1","DNS1":"192.168.0.1",
                    // "SoftVersion":"V7.113.1759.00","DataPort":7556,"HttpPort":8556,"rtspPort":554,
                    // "DDNSServer":"211.149.199.247","DDNSHost":"80005556.southtech.xyz","UID":"NULL"}]
                    List<SearchDevModel> lists = DeviceParseUtil.parseSearchMsg(SearchMsg);
                    if (lists.size()>0){
                        addDevice(lists);
                    }
                    else{
                        lod.dismiss();
                    }


                    break;

                default:
                    break;
            }
        }
    };


    void addDevice_share(String json){
        ShareModel model = GsonUtil.parseJsonWithGson(json, ShareModel.class);
        if (model != null){
            Log.e(tag,"model.uid:"+model.UID);
//            if (lod == null){
//                lod = new LoadingDialog(this);
//            }
//            lod.dialogShow();
//            ServerNetWork.getCommandApi().app_share_add_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
//                    model.From,JPushManager.getJPushRegisterID(),1,0,0,model.SN,model.Video,model.History,model.Push,
//                    model.Setup,model.Control).subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(observer_add_dev);

            //ChangeDevicePwdActivity
//            Intent intent = new Intent(this,ChangeDevicePwdActivity.class);
//            intent.putExtra("type",ChangeDevicePwdActivity.EnumChangeDevicePwd.SHARE);
//            intent.putExtra("model",model);
//            startActivity(intent);

            DevModel devModel = new DevModel();
            devModel.SN = model.SN;
            devModel.usr = "admin";//默认填写admin
            devModel.pwd = model.Pwd;
            boolean ret  = DataManager.getInstance().addDev(devModel);

            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();
            ServerNetWork.getCommandApi().app_share_add_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                    model.From,JPushManager.getJPushRegisterID(),1,0,0,model.SN,model.Video,model.History,model.Push,
                    model.Setup,model.Control).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer_add_dev);


        }
        else{
            Log.e(tag,"parseJsonWithGson failed");
        }
    }

    void addDevice( List<SearchDevModel> devices){
        List<Observable<RetModel>> observables = new ArrayList<>();
        for (SearchDevModel device : devices){
            observables.add(ServerNetWork.getCommandApi().app_user_add_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                                                                            JPushManager.getJPushRegisterID(),
                    1,0,0,device.getSN(),0));
        }
        Observable.combineLatest(observables,new FuncN<RetModel>(){

            @Override
            public RetModel call(Object... args) {
                RetModel model = new RetModel();
                model.ret = 1;
                for (Object i : args){
                    RetModel retModel = (RetModel) i;
                    Log.e(tag,"---------------------app_user_add_dev ret :"+retModel.ret);

                    model.ret &= retModel.ret;

                }
                return model;
            }
        }).subscribeOn(Schedulers.io())
           .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer_add_dev);
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
                back2TopActivity();
            }
            else{

            }

        }
    };
    LoadingDialog lod;
    String SearchMsg;
    boolean IsSearching;
}
