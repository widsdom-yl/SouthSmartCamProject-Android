package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.nuptboyzhb.lib.SuperSwipeRefreshLayout;
import com.model.DevModel;
import com.model.SearchDevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.DeviceListAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.DeviceParseUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

import static stcam.stcamproject.Activity.MainDevListActivity.EnumMainEntry.EnumMainEntry_Login;

public class MainDevListActivity extends AppCompatActivity implements DeviceListAdapter.OnItemClickListener {

    RecyclerView mRecyclerView;
    DeviceListAdapter mAdapter;
    public static  List<DevModel>mDevices = new ArrayList<>();//这个list中的model，判断了连接状态
    List<DevModel>mAccountDevices = new ArrayList<>();//没有连接状态
    SuperSwipeRefreshLayout refreshLayout;

    EnumMainEntry entryType;
    public enum EnumMainEntry implements Serializable {
        EnumMainEntry_Login,
        EnumMainEntry_Visitor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dev_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.title_main_dev_list);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            entryType = (EnumMainEntry) bundle.getSerializable("entry");
        }


        initView();
       // mAdapter = new DeviceListAdapter(this,null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDevices != null) {
            for (DevModel existModel : mDevices){
                 existModel.updateUserAndPwd();
            }
        }
        if (entryType == EnumMainEntry.EnumMainEntry_Login){
            loadDevList(false);
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (entryType == EnumMainEntry.EnumMainEntry_Login){
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        else{
            getMenuInflater().inflate(R.menu.menu_search, menu);
        }

        return true;
    }
    String SearchMsg;
    boolean IsSearching;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(this, AddDeviceActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_alarm) {
            Intent intent = new Intent(this, AlarmListActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_media) {
            Intent intent = new Intent(this, MediaActivity.class);
            Bundle bundle = new Bundle();
            //bundle.putSerializable("devModel",model);
            ArrayList<DevModel> devices = (ArrayList<DevModel>) mDevices;
            bundle.putParcelableArrayList("devices",devices);
            intent.putExtras(bundle);

            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_search){
            searchDevices();
        }
        if (item.getItemId() == R.id.action_setting){
            Intent intent = new Intent(this, SystemSettingActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == android.R.id.home){
            Log.e(tag,"---------------------back to home");
            mDevices.clear();
            this.finish(); // back button
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Log.e(tag,"---------------------onKeyDown");
            mDevices.clear();
            this.finish(); // back button
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void initView(){
        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = findViewById(R.id.recyler_device);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

         mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (entryType == EnumMainEntry.EnumMainEntry_Login){
            refreshLayout
                    .setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {

                        @Override
                        public void onRefresh() {
                            //TODO 开始刷新
                            loadDevList(true);
                        }

                        @Override
                        public void onPullDistance(int distance) {
                            //TODO 下拉距离
                        }

                        @Override
                        public void onPullEnable(boolean enable) {
                            //TODO 下拉过程中，下拉的距离是否足够出发刷新
                        }
                    });
            refreshLayout
                    .setOnPushLoadMoreListener(new SuperSwipeRefreshLayout.OnPushLoadMoreListener() {

                        @Override
                        public void onLoadMore() {
                            loadDevList(true);
                        }

                        @Override
                        public void onPushEnable(boolean enable) {
                            //TODO 上拉过程中，上拉的距离是否足够出发刷新
                        }

                        @Override
                        public void onPushDistance(int distance) {
                            // TODO 上拉距离

                        }

                    });
            refreshLayout.setFooterView(createFooterView());
        }
        else{
            refreshLayout.setEnabled(false);
        }

    }

    View createFooterView(){
        View view = LayoutInflater.from(this).inflate(R.layout.load_more, null);

        return view;
    }
    void loadDevList(boolean refresh){
        if (lod == null){
            lod = new LoadingDialog(this);
        }
        if (!refresh)
            lod.dialogShow();
        subscription = ServerNetWork.getCommandApi()
//                .app_user_get_devlst("4719373@qq.com","admin111")
                .app_user_get_devlst(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance().getDefaultPwd())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_get_devlst);
    }

    Observer<List<DevModel>> observer_get_devlst = new Observer<List<DevModel>>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            refreshLayout.setRefreshing(false);
            refreshLayout.setLoadMore(false);

           // refreshLayout.setLoading(false);

            Log.e(tag,"---------------------2");
            subscription.unsubscribe();
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            refreshLayout.setRefreshing(false);
            refreshLayout.setLoadMore(false);
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
        }

        @Override
        public void onNext(List<DevModel> mlist) {
            lod.dismiss();

            mAccountDevices = mlist;

            if (mlist.size() > 0)
            {
              //  mDevices = mlist;
                //DevModel model =  mlist.get(0);
                //SouthUtil.showToast(STApplication.getInstance(),"dev0 name"+model.DevName);

                // if (mAccountDevices == null){
                //     mAccountDevices = mlist;
                // }
                // else
                {
                    for (DevModel model : mlist){
                        boolean exist = false;
                        for (DevModel existModel : mDevices){

                            if (model.SN.equals(existModel.SN)){
                                exist = true;
                                break;
                            }
                        }
                        if (!exist){
                            mDevices.add(model);
                        }
                    }
                }

                for (DevModel model : mDevices){

                    Log.e(tag,"---------------------1 dev0 name"+model.DevName);
                    if (!model.IsConnect())
                    DevModel.threadConnect(ipc,model,false);
                }

                if (mAdapter == null){
                    mAdapter = new DeviceListAdapter(MainDevListActivity.this,mlist,entryType);
                    mAdapter.setOnItemClickListener(MainDevListActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                }
                else{
                    mAdapter.setmDatas(mlist);
                }

            }
            else{
                //MyContext.getInstance()
                mAdapter.setmDatas(mlist);
                Log.e(tag,"---------------------1:no dev");
                SouthUtil.showToast(STApplication.getInstance(),"No dev");
            }

        }
    };

    protected Subscription subscription;

    final String tag = "MainDevListActivity";
    LoadingDialog lod;

    @Override
    public void onItemClick(View view, int position,int tpe) {

        DevModel currentModel = mAccountDevices.get(position);
        DevModel model = null;

        for (DevModel existModel : mDevices){
            if (currentModel.SN.equals(existModel.SN)){
                model = existModel;
                break;
            }              
        }

        if (3 != tpe){
            if (!model.IsConnect()){
                Log.e(tag,"---------------------1:not connect ");
                SouthUtil.showDialog(MainDevListActivity.this,getString(R.string.action_net_not_connect));
                return;
            }
        }

        if (0 == tpe){


            Intent intent = new Intent(STApplication.getInstance(), PlayLiveActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("devModel",model);

            intent.putExtras(bundle);
            Log.e(tag,"to vid devModel NetHandle:"+model.NetHandle);

            startActivity(intent);
        }
        else  if (1 == tpe){


            if (entryType == EnumMainEntry_Login){
                Intent intent = new Intent(STApplication.getInstance(), DeviceShareActivity.class);

                Bundle bundle = new Bundle();
                bundle.putParcelable("devModel",model);

                intent.putExtras(bundle);
                Log.e(tag,"to DeviceShareActivity NetHandle:"+model.NetHandle);

                startActivity(intent);
            }
            else{
                Intent intent = new Intent(STApplication.getInstance(), AddDeviceAP2StaSetup.class);

                Bundle bundle = new Bundle();
                bundle.putParcelable("devModel",model);

                intent.putExtras(bundle);
                Log.e(tag,"to DeviceShareActivity NetHandle:"+model.NetHandle);

                startActivity(intent);
            }

        }

        else  if (2 == tpe){

            if(model.ExistSD == 0){
                SouthUtil.showToast(STApplication.getInstance(),getString(R.string.action_not_exist_sd));
                return;
            }
            Intent intent = new Intent(STApplication.getInstance(), PlayBackListActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("devModel",model);

            intent.putExtras(bundle);
            Log.e(tag,"to PlayBackListActivity NetHandle:"+model.NetHandle);

            startActivity(intent);
        }
        else if(3 == tpe){
            Intent intent = new Intent(STApplication.getInstance(), SettingActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("devModel",model);
            bundle.putSerializable("entry",entryType);
            intent.putExtras(bundle);
            Log.e(tag,"to SettingActivity NetHandle:"+model.NetHandle);

            startActivity(intent);
        }

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
                    mAdapter.notifyDataSetChanged();
                    break;
                case TMsg.Msg_NetConnFail:
                    model = (DevModel) msg.obj;
                    mAdapter.notifyDataSetChanged();
                    Log.e(tag,"NetConnFail:"+model.SN+"DevNode.NetHandle:"+model.NetHandle);
                    break;

                default:
                    break;
            }
        }
    };

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
                ipc_search.sendMessage(Message.obtain(ipc_search, TMsg.Msg_SearchOver, 0, 0, null));
                IsSearching = false;
            }
        }.start();
    }
    public final Handler ipc_search = new Handler()
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
                    searchList = DeviceParseUtil.parseSearchMsg(SearchMsg);
                    if (searchList.size()>0){
                        mAccountDevices.clear();
                        for (SearchDevModel model : searchList){
                            DevModel devModel  = model.exportDevModelForm();
                            mAccountDevices.add(devModel);
                            boolean exist = false;
                            for (DevModel existModel : mDevices){

                                if (devModel.SN.equals(existModel.SN)){
                                    exist = true;
                                    break;
                                }
                            }
                            if (!exist){
                                mDevices.add(devModel);
                            }
                        }

                        for (DevModel model : mDevices){

                            Log.e(tag,"---------------------1 dev0 name"+model.DevName);
                            if (!model.IsConnect())
                                DevModel.threadConnect(ipc,model,false);
                        }


                        if (mAdapter == null){
                            mAdapter = new DeviceListAdapter(MainDevListActivity.this,mAccountDevices,entryType);
                            mAdapter.setOnItemClickListener(MainDevListActivity.this);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                        else{
                            mAdapter.setmDatas(mAccountDevices);
                        }

                    }





                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onLongClick(View view, int position) {

    }

    List<SearchDevModel> searchList;


}
