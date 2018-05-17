package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.model.RetModel;
import com.model.SearchDevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.AddDeviceAdapter;
import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.DeviceParseUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class AddDeviceWlanActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener {

    AddDeviceAdapter adapter;
    RecyclerView rv;
    final static  String tag =  "AddDeviceWlanActivity";
    List<SearchDevModel> lists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_wlan);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_add_device_search);
        }
        initView();
        searchDevices();
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
        rv = findViewById(R.id.device_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this));
    }
    void searchDevices(){
        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();
        SouthUtil.showToast(this,"search");
        new Thread()
        {
            @Override
            public void run()
            {
                SearchMsg = lib.thNetSearchDevice(3000, 1);
                ipc.sendMessage(Message.obtain(ipc, TMsg.Msg_SearchOver, 0, 0, null));
                IsSearching = false;
            }
        }.start();
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
                    lod.dismiss();
                    if (SearchMsg.equals(""))
                    {
                        return;
                    }
                    Log.e(tag,SearchMsg);
                    //[{"SN":"80005556","DevModal":"401H","DevName":"IPCAM_80005556","DevMAC":"00:C1:A1:62:55:56",
                    // "DevIP":"192.168.0.199","SubMask":"255.255.255.0","Gateway":"192.168.0.1","DNS1":"192.168.0.1",
                    // "SoftVersion":"V7.113.1759.00","DataPort":7556,"HttpPort":8556,"rtspPort":554,
                    // "DDNSServer":"211.149.199.247","DDNSHost":"80005556.southtech.xyz","UID":"NULL"}]
                    lists = DeviceParseUtil.parseSearchMsg(SearchMsg);
                    if (lists.size()>0){
                        adapter = new AddDeviceAdapter(lists);
                        rv.setAdapter(adapter);
                        adapter.setOnItemClickListener(AddDeviceWlanActivity.this);
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

    LoadingDialog lod;
    String SearchMsg;
    boolean IsSearching;


    @Override
    public void onItemClick(View view, int position) {
//        Intent intent = new Intent(this,ChangeDevicePwdActivity.class);
//        intent.putExtra("type",ChangeDevicePwdActivity.EnumChangeDevicePwd.WLAN);
//        SearchDevModel model = lists.get(position);
//        intent.putExtra("model",model);
//        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

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

            }
            else{

            }

        }
    };

}