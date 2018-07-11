package stcam.stcamproject.Activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.github.nuptboyzhb.lib.SuperSwipeRefreshLayout;
import com.model.DevModel;
import com.model.SearchDevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observer;
import rx.Subscription;
import stcam.stcamproject.Adapter.DeviceListAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Config.Config;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.DeviceParseUtil;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;

import static stcam.stcamproject.Activity.MainDevListFragment.EnumMainEntry.EnumMainEntry_Login;
import static stcam.stcamproject.Activity.MainDevListFragment.EnumMainEntry.EnumMainEntry_Visitor;

public class MainDevListFragment extends Fragment implements DeviceListAdapter.OnItemClickListener, NetworkChangeReceiver.OnNetWorkBreakListener, View.OnClickListener {

    RecyclerView mRecyclerView;
    DeviceListAdapter mAdapter;
    ImageButton add_button;
    Button search_button;
    public static  List<DevModel>mDevices = new ArrayList<>();//这个list中的model，判断了连接状态
    List<DevModel>mAccountDevices = new ArrayList<>();//没有连接状态
    SuperSwipeRefreshLayout refreshLayout;

    EnumMainEntry entryType;

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;


    Request getDevListRequest;//
    OkHttpClient mHttpClient = new OkHttpClient();

    @Override
    public void OnNetWorkBreakListener() {
        for (DevModel devModel : mAccountDevices){
            if (devModel.IsConnect()){
                devModel.Disconn();
            }
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add_button){
            Intent intent = new Intent(this.getActivity(), AddDeviceActivity.class);
            startActivity(intent);
        }
        else if (view.getId() == R.id.search_button){
            searchDevices();
        }

    }

    public enum EnumMainEntry implements Serializable {
        EnumMainEntry_Login,
        EnumMainEntry_Visitor
    }

    // TODO: Rename and change types and number of parameters
    public static MainDevListFragment newInstance(EnumMainEntry param) {
        MainDevListFragment fragment = new MainDevListFragment();
        Bundle args = new Bundle();
        args.putSerializable("entry", param);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //lib.P2PInit();
        Log.e(tag, "PushRegisterID : "+JPushManager.getJPushRegisterID());
        // setContentView(R.layout.activity_main_dev_list);
        // android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // actionBar.setTitle(R.string.title_main_dev_list);
        Bundle bundle = this.getArguments();
        if (bundle != null){
            entryType = (EnumMainEntry) bundle.getSerializable("entry");
        }




        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setNetWorkBreakListener(this);
        getActivity().registerReceiver(networkChangeReceiver, intentFilter);


       // mAdapter = new DeviceListAdapter(this,null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.activity_main_dev_list, container, false);
        initView(view);

        return view;
    }


    public void onResume(){
        super.onResume();
        onMyResume();
    }
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            //相当于Fragment的onResume
//            onMyResume();
//
//        } else {
//            //相当于Fragment的onPause
//        }
//    }




    private void onMyResume() {
        if (mDevices != null) {
            for (DevModel existModel : mDevices){
                 existModel.updateUserAndPwd();
            }
        }
        if (entryType == EnumMainEntry.EnumMainEntry_Login){
            loadDevList(false);
        }
        if (mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }

    }
  
    public void onDestroy()  {
        super.onDestroy();
        getActivity().unregisterReceiver(networkChangeReceiver);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (entryType == EnumMainEntry.EnumMainEntry_Login){
//            getMenuInflater().inflate(R.menu.menu_main, menu);
//        }
//        else{
//            getMenuInflater().inflate(R.menu.menu_search, menu);
//        }
//
//        return true;
//    }
    String SearchMsg;
    boolean IsSearching;

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        if (item.getItemId() == R.id.action_add) {
//            Intent intent = new Intent(this.getActivity(), AddDeviceActivity.class);
//            startActivity(intent);
//            return true;
//        }
//        if (item.getItemId() == R.id.action_alarm) {
//            Intent intent = new Intent(this.getActivity(), AlarmListActivity.class);
//            startActivity(intent);
//            return true;
//        }
//        if (item.getItemId() == R.id.action_media) {
//            Intent intent = new Intent(this.getActivity(), MediaActivity.class);
//            Bundle bundle = new Bundle();
//            //bundle.putSerializable("devModel",model);
//            ArrayList<DevModel> devices = (ArrayList<DevModel>) mDevices;
//            bundle.putParcelableArrayList("devices",devices);
//            intent.putExtras(bundle);
//
//            startActivity(intent);
//            return true;
//        }
//        if (item.getItemId() == R.id.action_search){
//            searchDevices();
//        }
//        if (item.getItemId() == R.id.action_setting){
//            Intent intent = new Intent(this, SystemSettingActivity.class);
//            startActivity(intent);
//        }
//        if (item.getItemId() == android.R.id.home){
//            Log.e(tag,"---------------------back to home");
//            disconnectDev();
//            this.finish(); // back button
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//            Log.e(tag,"---------------------onKeyDown");
//            disconnectDev();
//
//            this.finish(); // back button
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
    void disconnectDev(){
        new Thread() {
            @Override
            public void run(){
                Log.e(tag,"disconnectDev thread start");
                for (DevModel model : mDevices){
                    if (model.IsConnect()){
                        Log.e(tag,"---------------------disconnect:"+model.SN);
                        model.Disconn();
                    }
                }
                mDevices.clear();
                lib.P2PFree();
            }
        }.run();

    }
    void initView(View view){
        refreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = view.findViewById(R.id.recyler_device);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
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


        add_button = view.findViewById(R.id.add_button);
        add_button.setOnClickListener(this);
        search_button = view.findViewById(R.id.search_button);
        search_button.setOnClickListener(this);
        if (entryType == EnumMainEntry_Visitor){
            add_button.setVisibility(View.GONE);
            search_button.setVisibility(View.VISIBLE);
        }
        else{
            add_button.setVisibility(View.VISIBLE);
            search_button.setVisibility(View.GONE);
        }


    }

    View createFooterView(){
        View view = LayoutInflater.from(this.getActivity()).inflate(R.layout.load_more, null);

        return view;
    }
    void loadDevList(boolean refresh){
        if (lod == null){
            lod = new LoadingDialog(this.getActivity());
        }
        if (!refresh)
            lod.dialogShow();
        if (getDevListRequest == null){
            //http://xxx.xxx.xxx.xxx:800/app_user_get_devlst.asp??user=aa@bb.com&psd=12345678
            String url = "http://"+ Config.ServerIP+":"+Config.ServerPort+"/app_user_get_devlst.asp?user="+AccountManager.getInstance().getDefaultUsr()+"&psd="+
                    AccountManager.getInstance().getDefaultPwd();
            Log.e(tag,"request url :"+url);
            getDevListRequest = new Request.Builder()
                    .url(url)
                    .build();
        }


        mHttpClient.newCall(getDevListRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Activity activity = MainDevListFragment.this.getActivity();
                if (activity == null) {
                    return;
                }
                MainDevListFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lod.dismiss();
                        refreshLayout.setRefreshing(false);
                        refreshLayout.setLoadMore(false);
                    }
                });


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Activity activity = MainDevListFragment.this.getActivity();
                if (activity == null) {
                    return;
                }
                MainDevListFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lod.dismiss();
                        refreshLayout.setRefreshing(false);
                        refreshLayout.setLoadMore(false);
                    }
                });
                //String retStr = response.body().string();
                final String gb2312Str = new String(response.body().bytes(), "GB2312");
                MainDevListFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseGetDevListResponse(gb2312Str);
                    }
                });


            }
        });


//        subscription = ServerNetWork.getCommandApi()
////                .app_user_get_devlst("4719373@qq.com","admin111")
//                .app_user_get_devlst(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance().getDefaultPwd())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(observer_get_devlst);
    }

    void parseGetDevListResponse(String response){
        List<DevModel> mlist = GsonUtil.parseJsonArrayWithGson(response,DevModel[].class);


        if (mlist == null){
            SouthUtil.showToast(STApplication.getInstance(),"No dev");
            return;
        }
        if (mlist.size() > 0)
        {
                mAccountDevices = mlist;
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

            for (DevModel model : mDevices){

                Log.e(tag,"---------------------1 dev0 name"+model.DevName);
                if (!model.IsConnect())
                    Log.e(tag,"---------------------NetConn:sn"+model.SN);
                    DevModel.threadConnect(ipc,model,false);
            }

            if (mAdapter == null){
                mAdapter = new DeviceListAdapter(MainDevListFragment.this.getActivity(),mlist,entryType);
                mAdapter.setOnItemClickListener(MainDevListFragment.this);
                mRecyclerView.setAdapter(mAdapter);
            }
            else{
                mAdapter.setmDatas(mlist);
            }

        }
        else{
            //MyContext.getInstance()
            if (mAdapter == null){
                mAdapter = new DeviceListAdapter(MainDevListFragment.this.getActivity(),mlist,entryType);
                mAdapter.setOnItemClickListener(MainDevListFragment.this);
                mRecyclerView.setAdapter(mAdapter);
            }
            else{
                mAdapter.setmDatas(mlist);
            }

            Log.e(tag,"---------------------1:no dev");
            SouthUtil.showToast(STApplication.getInstance(),"No dev");
        }
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
                    mAdapter = new DeviceListAdapter(MainDevListFragment.this.getActivity(),mlist,entryType);
                    mAdapter.setOnItemClickListener(MainDevListFragment.this);
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

    final String tag = "MainDevListFragment";
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
                SouthUtil.showDialog(MainDevListFragment.this.getActivity(),getString(R.string.action_net_not_connect));
                return;
            }
        }

        if (0 == tpe){


            Intent intent = new Intent(STApplication.getInstance(), PlayLiveActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("devModel",model);
            bundle.putSerializable("entry",entryType);
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
            bundle.putSerializable("entry",entryType);
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
            lod = new LoadingDialog(this.getActivity());
        }
        lod.dialogShow();
        SouthUtil.showToast(this.getActivity(),"search");
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
                            mAdapter = new DeviceListAdapter(MainDevListFragment.this.getActivity(),mAccountDevices,entryType);
                            mAdapter.setOnItemClickListener(MainDevListFragment.this);
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
