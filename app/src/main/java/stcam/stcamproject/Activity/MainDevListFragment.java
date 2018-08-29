package stcam.stcamproject.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.model.DevModel;
import com.model.RetModel;
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
import stcam.stcamproject.network.ServerNetWork;

import static stcam.stcamproject.Activity.MainDevListFragment.TUserMode.UserMode_Login;
import static stcam.stcamproject.Activity.MainDevListFragment.TUserMode.UserMode_Visitor;

public class MainDevListFragment extends Fragment implements DeviceListAdapter.OnItemClickListener, NetworkChangeReceiver
  .OnNetWorkBreakListener, View.OnClickListener
{

  RecyclerView mRecyclerView;
  DeviceListAdapter mAdapter;
  ImageButton add_button;
  Button add_text_button;
  Button search_button;
  public static List<DevModel> mDevices = new ArrayList<>();//这个list中的model，判断了连接状态
  List<DevModel> mAccountDevices = new ArrayList<>();//没有连接状态
  // SuperSwipeRefreshLayout refreshLayout;
  SwipeRefreshLayout swipeContainer;
  TUserMode UserMode;

  boolean hasReceivedNetWorkchange;//是否收到网络监听变化，这个变量在进入此fragement页面中，会收到网络回调，这个值会值1，但是考虑到网络请求，当有设备列表或者搜索设备返回时候，这个值为true
  private IntentFilter intentFilter;
  private NetworkChangeReceiver networkChangeReceiver;


  Request getDevListRequest;//
  OkHttpClient mHttpClient = new OkHttpClient();

  @Override
  public void OnNetWorkBreakListener()
  {
    Log.e(tag, "OnNetWorkBreakListener ---------------0");
    if (mDevices != null)
    {
      for (DevModel tmpNode : mDevices)
      {
        if (tmpNode.IsConnect())
        {
          lib.thNetThreadDisConnFree(tmpNode.NetHandle);
          tmpNode.NetHandle = 0;
        }
      }
      if (mAdapter != null)
      {
        mAdapter.notifyDataSetChanged();
      }
    }
    Log.e(tag, "OnNetWorkBreakListener ---------------1");

  }

  @Override
  public void OnNetWorkChangeListener(int type)
  {
    Log.e(tag, "---------------------1 OnNetWorkChangeListener");
    if (UserMode == UserMode_Login)
    {
      if (type == ConnectivityManager.TYPE_MOBILE)
      {
        if (mDevices != null)
        {
          mDevices.clear();
        }
        Log.e(tag, "loadDevList网络改变MOBILE");
        loadDevList(false);//网络改变MOBILE
      }
      else if (type == ConnectivityManager.TYPE_WIFI)
      {
        if (mDevices != null)
        {
          mDevices.clear();
        }
        Log.e(tag, "loadDevList网络改变WIFI");
        loadDevList(false);//网络改变WIFI
      }
    }
    else if (UserMode == UserMode_Visitor)
    {
      if (mDevices != null)
      {
        mDevices.clear();
      }
      searchDevices();
    }
  }

  @Override
  public void onClick(View view)
  {
    if (view.getId() == R.id.add_button || view.getId() == R.id.add_text_button)
    {
      Intent intent = new Intent(this.getActivity(), AddDeviceActivity.class);
      startActivity(intent);
    }
    else if (view.getId() == R.id.search_button)
    {
      searchDevices();
    }

  }

  public enum TUserMode implements Serializable
  {
    UserMode_NULL,
    UserMode_Login,
    UserMode_Visitor
  }

  // TODO: Rename and change types and number of parameters
  public static MainDevListFragment newInstance(TUserMode param)
  {
    MainDevListFragment fragment = new MainDevListFragment();
    Bundle args = new Bundle();
    args.putSerializable("entry", param);
    fragment.setArguments(args);
    return fragment;
  }


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.e(tag, "PushRegisterID : " + JPushManager.getJPushRegisterID());
    // setContentView(R.layout.activity_main_dev_list);
    // android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    // actionBar.setTitle(R.string.title_main_dev_list);
    Bundle bundle = this.getArguments();
    if (bundle != null)
    {
      UserMode = (TUserMode) bundle.getSerializable("entry");
    }
    hasReceivedNetWorkchange = false;


    intentFilter = new IntentFilter();
    intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
    networkChangeReceiver = new NetworkChangeReceiver();
    networkChangeReceiver.setNetWorkBreakListener(this);
    getActivity().registerReceiver(networkChangeReceiver, intentFilter);


    // mAdapter = new DeviceListAdapter(this,null);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.activity_main_dev_list, container, false);
    initView(view);

    return view;
  }


  public void onResume()
  {
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


  private void onMyResume()
  {
    if (mDevices != null)
    {
      for (DevModel existModel : mDevices)
      {
        existModel.updateUserAndPwd();
      }

      if (hasReceivedNetWorkchange)
      {
        if (UserMode == TUserMode.UserMode_Login)
        {
          Log.e(tag, "loadDevList onMyResume hasReceivedNetWorkchange");
          loadDevList(false);//onMyResume hasReceivedNetWorkchange
        }
        else if (UserMode == UserMode_Visitor)
        {
          searchDevices();
        }
      }

    }


    if (mAdapter != null)
    {
      mAdapter.notifyDataSetChanged();
    }

  }

  public void onDestroy()
  {
    super.onDestroy();
    if (mDevices != null && mDevices.size() > 0)
    {
      for (DevModel tmpNode : mDevices)
      {
        if (tmpNode.IsConnect())
        {
          lib.thNetThreadDisConnFree(tmpNode.NetHandle);
          tmpNode.NetHandle = 0;
        }
      }
      mDevices.clear();
    }
    getActivity().unregisterReceiver(networkChangeReceiver);
  }

  String SearchMsg;
  boolean IsSearching;

  void initView(View view)
  {
    //  refreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    swipeContainer = view.findViewById(R.id.swipeContainer);

    swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
      android.R.color.holo_green_light,
      android.R.color.holo_orange_light,
      android.R.color.holo_red_light);


    mRecyclerView = view.findViewById(R.id.recyler_device);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    mRecyclerView.setLayoutManager(layoutManager);

    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    if (UserMode == TUserMode.UserMode_Login)
    {
      swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
      {
        @Override
        public void onRefresh()
        {
          // Your code to refresh the list here.
          // Make sure you call swipeContainer.setRefreshing(false)
          // once the network request has completed successfully.
          Log.e(tag, "loadDevList initView UserMode_Login onRefresh()");
          loadDevList(true);//initView UserMode_Login onRefresh()
        }
      });


    }
    else
    {
      swipeContainer.setEnabled(false);
    }


    add_button = view.findViewById(R.id.add_button);
    add_button.setOnClickListener(this);
    search_button = view.findViewById(R.id.search_button);

    add_text_button = view.findViewById(R.id.add_text_button);
    add_text_button.setOnClickListener(this);
    add_text_button.setOnClickListener(this);
    search_button.setOnClickListener(this);
    if (UserMode == UserMode_Visitor)
    {
      add_button.setVisibility(View.GONE);
      search_button.setVisibility(View.VISIBLE);
      add_text_button.setVisibility(View.GONE);
    }
    else
    {
      add_text_button.setVisibility(View.VISIBLE);
      add_button.setVisibility(View.VISIBLE);
      search_button.setVisibility(View.GONE);
    }


  }

  View createFooterView()
  {
    View view = LayoutInflater.from(this.getActivity()).inflate(R.layout.load_more, null);

    return view;
  }

  void loadDevList(boolean refresh)
  {
    if (lod == null)
    {
      lod = new LoadingDialog(this.getActivity());
    }

    if (!refresh)
    {
      lod.dialogShow();
    }

    if (getDevListRequest == null)
    {
      //http://xxx.xxx.xxx.xxx:800/app_user_get_devlst.asp??user=aa@bb.com&psd=12345678
      String url = "http://" + Config.ServerIP + ":" + Config.ServerPort + "/app_user_get_devlst.asp?user=" + AccountManager.getInstance
        ().getDefaultUsr() + "&psd=" +
        AccountManager.getInstance().getDefaultPwd() + "&tokenid=" + JPushManager.getJPushRegisterID();
      Log.e(tag, "request url :" + url);
      getDevListRequest = new Request.Builder()
        .url(url)
        .build();
    }

    mHttpClient.newCall(getDevListRequest).enqueue(new Callback()
    {
      @Override
      public void onFailure(Call call, IOException e)
      {
        Activity activity = MainDevListFragment.this.getActivity();
        if (activity == null)
        {
          return;
        }
        hasReceivedNetWorkchange = true;
        MainDevListFragment.this.getActivity().runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            lod.dismiss();
            if (swipeContainer != null)
            {
              swipeContainer.setRefreshing(false);
            }
          }
        });
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException
      {
        Activity activity = MainDevListFragment.this.getActivity();
        if (activity == null)
        {
          return;
        }
        hasReceivedNetWorkchange = true;
        MainDevListFragment.this.getActivity().runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            lod.dismiss();
            if (swipeContainer != null)
            {
              swipeContainer.setRefreshing(false);
            }
          }
        });
        //String retStr = response.body().string();
        final String gb2312Str = new String(response.body().bytes(), "GB2312");
        MainDevListFragment.this.getActivity().runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            parseGetDevListResponse(gb2312Str);
          }
        });
      }//onResponse
    });//mHttpClient
  }//void loadDevList(boolean refresh)

  void parseGetDevListResponse(String response)//zhb from server app_user_get_devlst.asp
  {
    RetModel m = GsonUtil.parseJsonWithGson(response, RetModel.class);//zhb
    if (m != null)
    {
      if (ServerNetWork.RESULT_USER_LOGOUT == m.ret)
      {
        //RESULT_USER_LOGOUT 为收不到推送的情况下，访问服务器时的返回值，收到
        //返回登录界面，取消保存的AutoLogin
        SouthUtil.broadcastLogoutInfo();
        //todo
      }
    }

    List<DevModel> mlist = GsonUtil.parseJsonArrayWithGson(response, DevModel[].class);

    if (mlist == null)
    {
      SouthUtil.showToast(STApplication.getInstance(), getString(R.string.string_nodev));
      return;
    }
    if (mlist.size() > 0)
    {
      mAccountDevices = mlist;
      for (DevModel serverNode : mlist)
      {
        boolean IsExist = false;
        for (DevModel tmpNode : mDevices)
        {
          if (serverNode.SN.equals(tmpNode.SN))
          {
            serverNode.DevName = tmpNode.GetDevName();
            //tmpNode.DevName = serverNode.DevName;
            tmpNode.ConnType = serverNode.ConnType;//zhb
            tmpNode.IPUID = serverNode.IPUID;//zhb
            tmpNode.WebPort = serverNode.WebPort;//zhb
            tmpNode.DataPort = serverNode.DataPort;//zhb
            IsExist = true;
            break;
          }
        }
        if (!IsExist)
        {
          mDevices.add(serverNode);
        }
      }

      for (DevModel tmpNode : mDevices)
      {

        Log.e(tag, "---------------------1 dev0 name" + tmpNode.DevName);
        if (!tmpNode.IsConnect())
        {
          Log.e(tag, "---------------------NetConn:sn" + tmpNode.SN);
          DevModel.threadConnect(ipc, tmpNode);
        }
        //DevModel.threadConnect(ipc, tmpNode);
      }

      if (mAdapter == null)
      {
        mAdapter = new DeviceListAdapter(MainDevListFragment.this.getActivity(), mlist, UserMode);
        mAdapter.setOnItemClickListener(MainDevListFragment.this);
        if (mRecyclerView != null)
        {
          mRecyclerView.setAdapter(mAdapter);
        }
      }
      else
      {
        mAdapter.setmDatas(mlist);
      }

    }
    else
    {
      //MyContext.getInstance()
      if (mAdapter == null)
      {
        mAdapter = new DeviceListAdapter(MainDevListFragment.this.getActivity(), mlist, UserMode);
        mAdapter.setOnItemClickListener(MainDevListFragment.this);
        mRecyclerView.setAdapter(mAdapter);
      }
      else
      {
        mAdapter.setmDatas(mlist);
      }

      Log.e(tag, "---------------------1:no dev");
      SouthUtil.showToast(STApplication.getInstance(), getString(R.string.string_nodev));
    }
  }

  Observer<List<DevModel>> observer_get_devlst = new Observer<List<DevModel>>()
  {
    @Override
    public void onCompleted()
    {
      lod.dismiss();
      if (swipeContainer != null)
      {
        swipeContainer.setRefreshing(false);
      }

      // refreshLayout.setLoading(false);

      Log.e(tag, "---------------------2");
      subscription.unsubscribe();
    }

    @Override
    public void onError(Throwable e)
    {
      lod.dismiss();
      if (swipeContainer != null)
      {
        swipeContainer.setRefreshing(false);
      }
      Log.e(tag, "---------------------1:" + e.getLocalizedMessage());
    }

    @Override
    public void onNext(List<DevModel> mlist)
    {
      lod.dismiss();

      mAccountDevices = mlist;

      if (mlist.size() > 0)
      {
        for (DevModel model : mlist)
        {
          boolean exist = false;
          for (DevModel tmpNode : mDevices)
          {
            if (model.SN.equals(tmpNode.SN))
            {
              model.DevName = tmpNode.GetDevName();
              exist = true;
              break;
            }
          }
          if (!exist)
          {
            mDevices.add(model);
          }
        }

        for (DevModel tmpNode : mDevices)
        {
          if (!tmpNode.IsConnect())
          {
            DevModel.threadConnect(ipc, tmpNode);
          }
        }

        if (mAdapter == null)
        {
          mAdapter = new DeviceListAdapter(MainDevListFragment.this.getActivity(), mlist, UserMode);
          mAdapter.setOnItemClickListener(MainDevListFragment.this);
          mRecyclerView.setAdapter(mAdapter);
        }
        else
        {
          mAdapter.setmDatas(mlist);
        }

      }
      else
      {
        //MyContext.getInstance()
        mAdapter.setmDatas(mlist);
        Log.e(tag, "---------------------1:no dev");
        SouthUtil.showToast(STApplication.getInstance(), getString(R.string.string_nodev));
      }

    }
  };

  protected Subscription subscription;

  final String tag = "MainDevListFragment";
  LoadingDialog lod;

  @Override
  public void onItemClick(View view, int position, int tpe)
  {

    DevModel currentModel = mAccountDevices.get(position);
    DevModel tmpNode = null;

    for (DevModel existModel : mDevices)
    {
      if (currentModel.SN.equals(existModel.SN))
      {
        tmpNode = currentModel;
        tmpNode.NetHandle = existModel.NetHandle;
        tmpNode.ConnType = existModel.ConnType;
        tmpNode.DevCfg = existModel.DevCfg;
        tmpNode.ExistSD = existModel.ExistSD;
        tmpNode.DevType = existModel.DevType;
        tmpNode.Brightness = existModel.Brightness;
        tmpNode.Contrast = existModel.Contrast;
        tmpNode.Sharpness = existModel.Contrast;
        //tmpNode.UID = existModel.UID;
        tmpNode.SoftVersion = existModel.SoftVersion;
        tmpNode.DevName = existModel.GetDevName();
        break;
      }
    }
    if (3 != tpe)
    {
      if (!tmpNode.IsConnect())
      {
        Log.e(tag, "---------------------1:not connect ");
        SouthUtil.showDialog(MainDevListFragment.this.getActivity(), getString(R.string.action_net_not_connect));
        return;
      }
    }

    if (0 == tpe)//Item按下视频
    {
      Intent intent = new Intent(STApplication.getInstance(), PlayLiveActivity.class);

      Bundle bundle = new Bundle();
      bundle.putParcelable("devModel", tmpNode);
      bundle.putSerializable("entry", UserMode);
      intent.putExtras(bundle);
      Log.e(tag, "to vid devModel NetHandle:" + tmpNode.NetHandle);

      startActivity(intent);
    }
    else if (1 == tpe)//Item按下分享
    {
      if (UserMode == UserMode_Login)
      {
        if (tmpNode.IsShare == 0)//去掉？
        {
          SouthUtil.showDialog(this.getActivity(), getString(R.string.string_device_is_share));
          return;
        }

        Intent intent = new Intent(STApplication.getInstance(), DeviceShareActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable("devModel", tmpNode);

        intent.putExtras(bundle);
        Log.e(tag, "to DeviceShareActivity NetHandle:" + tmpNode.NetHandle);

        startActivity(intent);
      }
      else
      {
        Intent intent = new Intent(STApplication.getInstance(), AddDeviceAP2StaSetup.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable("devModel", tmpNode);

        intent.putExtras(bundle);
        Log.e(tag, "to DeviceShareActivity NetHandle:" + tmpNode.NetHandle);

        startActivity(intent);
      }

    }

    else if (2 == tpe)//Item按下回放
    {
      if (UserMode == UserMode_Login)
      {
        /*//去掉 zhb add
        if (tmpNode.IsShare == 0)
        {
          SouthUtil.showDialog(this.getActivity(), getString(R.string.string_device_is_share));
          return;
        }
        */
        if (tmpNode.IsHistory == 0)
        {
          SouthUtil.showToast(STApplication.getInstance(), getString(R.string.string_no_record_permisson));
          return;
        }
      }

      if (tmpNode.ExistSD == 0)
      {
        SouthUtil.showToast(STApplication.getInstance(), getString(R.string.action_not_exist_sd));
        return;
      }

      if (tmpNode.IsRecord)
      {
        SouthUtil.showToast(STApplication.getInstance(), getString(R.string.string_no_record_permisson));
        return;
      }
      Intent intent = new Intent(STApplication.getInstance(), PlayBackListActivity.class);

      Bundle bundle = new Bundle();
      bundle.putParcelable("devModel", tmpNode);
      bundle.putSerializable("entry", UserMode);
      intent.putExtras(bundle);
      Log.e(tag, "to PlayBackListActivity NetHandle:" + tmpNode.NetHandle);

      startActivity(intent);
    }
    else if (3 == tpe)//Item按下设置
    {
      Intent intent = new Intent(STApplication.getInstance(), SettingActivity.class);

      Bundle bundle = new Bundle();
      bundle.putParcelable("devModel", tmpNode);
      bundle.putSerializable("entry", UserMode);
      intent.putExtras(bundle);
      Log.e(tag, "to SettingActivity NetHandle:" + tmpNode.NetHandle);

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
          Log.e(tag, "NetConnSucceed:" + model.SN + "DevNode.NetHandle:" + model.NetHandle);
          mAdapter.notifyDataSetChanged();
          break;
        case TMsg.Msg_NetConnFail:
          model = (DevModel) msg.obj;
          mAdapter.notifyDataSetChanged();
          Log.e(tag, "NetConnFail:" + model.SN + "DevNode.NetHandle:" + model.NetHandle);
          break;

        default:
          break;
      }
    }
  };

  void searchDevices()
  {
    if (lod == null)
    {
      lod = new LoadingDialog(this.getActivity());
    }
    lod.dialogShow();
    SouthUtil.showToast(this.getActivity(), getString(R.string.action_search));
    new Thread()
    {
      @Override
      public void run()
      {
        SearchMsg = lib.thNetSearchDevice(5000, 1);
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
      hasReceivedNetWorkchange = true;
      switch (msg.what)
      {
        case TMsg.Msg_SearchOver:
          lod.dismiss();
          if (SearchMsg == null || SearchMsg.equals("[]"))
          {
            SouthUtil.showDialog(MainDevListFragment.this.getActivity(), MainDevListFragment.this.getString(R.string.string_search_no_device));
            return;
          }
          Log.e(tag, SearchMsg);
          if (mAccountDevices != null)
          {
            mAccountDevices.clear();
          }
          Log.e(tag, "search result is :" + SearchMsg);
          //[{"SN":"80005556","DevModal":"401H","DevName":"IPCAM_80005556","DevMAC":"00:C1:A1:62:55:56",
          // "DevIP":"192.168.0.199","SubMask":"255.255.255.0","Gateway":"192.168.0.1","DNS1":"192.168.0.1",
          // "SoftVersion":"V7.113.1759.00","DataPort":7556,"HttpPort":8556,"rtspPort":554,
          // "DDNSServer":"211.149.199.247","DDNSHost":"80005556.southtech.xyz","UID":"NULL"}]
          searchList = DeviceParseUtil.parseSearchMsg(SearchMsg);

          if (searchList == null)
          {
            return;
          }
          if (searchList.size() > 0)
          {

            for (SearchDevModel model : searchList)
            {
              DevModel devModel = model.exportDevModelForm();
              mAccountDevices.add(devModel);
              boolean exist = false;
              for (DevModel existModel : mDevices)
              {
                if (devModel.SN.equals(existModel.SN))
                {
                  devModel.DevName = existModel.GetDevName();
                  exist = true;
                  break;
                }
              }
              if (!exist)
              {
                mDevices.add(devModel);
              }
            }

            for (DevModel tmpNode : mDevices)
            {

              Log.e(tag, "---------------------1 dev0 name" + tmpNode.DevName);
              if (!tmpNode.IsConnect())
              {
                DevModel.threadConnect(ipc, tmpNode);
              }
            }


            if (mAdapter == null)
            {
              mAdapter = new DeviceListAdapter(MainDevListFragment.this.getActivity(), mAccountDevices, UserMode);
              mAdapter.setOnItemClickListener(MainDevListFragment.this);
              mRecyclerView.setAdapter(mAdapter);
            }
            else
            {
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
  public void onLongClick(View view, int position)
  {

  }

  List<SearchDevModel> searchList;


}
