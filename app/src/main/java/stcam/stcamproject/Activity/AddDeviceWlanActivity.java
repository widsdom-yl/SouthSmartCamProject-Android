package stcam.stcamproject.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.model.DevModel;
import com.model.RetModel;
import com.model.SSIDModel;
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
import stcam.stcamproject.Config.Config;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.DataManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.DeviceParseUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.Network;
import stcam.stcamproject.network.ServerNetWork;

public class AddDeviceWlanActivity extends BaseAppCompatActivity implements BaseAdapter.OnItemClickListener
{

  AddDeviceAdapter adapter;
  RecyclerView rv;
  final static String tag = "AddDeviceWlanActivity";
  List<SearchDevModel> lists;

  int selectPosition;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_device_wlan);

    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      setCustomTitle(getString(R.string.action_search), true);
    }
    initView();
    searchDevices();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.menu_search, menu);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        this.finish(); // back button
        return true;
      case R.id.action_search:
        searchDevices();
      default:
        break;

    }
    return super.onOptionsItemSelected(item);
  }

  void initView()
  {
    rv = findViewById(R.id.device_list_view);
    // rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    rv.setLayoutManager(new LinearLayoutManager(this));
  }

  void searchDevices()
  {
    if (lod == null)
    {
      lod = new LoadingDialog(this);
    }
    lod.dialogShow();
    SouthUtil.showToast(this, getString(R.string.action_search));
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
          if (SearchMsg == null || SearchMsg.equals(""))
          {
            SouthUtil.showDialog(AddDeviceWlanActivity.this, AddDeviceWlanActivity.this.getString(R.string.string_search_no_device));
            return;
          }
          Log.e(tag, "search ret:" + SearchMsg);
          //[{"SN":"80005556","DevModal":"401H","DevName":"IPCAM_80005556","DevMAC":"00:C1:A1:62:55:56",
          // "DevIP":"192.168.0.199","SubMask":"255.255.255.0","Gateway":"192.168.0.1","DNS1":"192.168.0.1",
          // "SoftVersion":"V7.113.1759.00","DataPort":7556,"HttpPort":8556,"rtspPort":554,
          // "DDNSServer":"211.149.199.247","DDNSHost":"80005556.southtech.xyz","UID":"NULL"}]
          lists = DeviceParseUtil.parseSearchMsg(SearchMsg);
          if (lists == null)
          {
            return;
          }
          if (lists.size() > 0)
          {
            adapter = new AddDeviceAdapter(lists);
            rv.setAdapter(adapter);
            adapter.setOnItemClickListener(AddDeviceWlanActivity.this);
            // addDevice(lists);

          }
          else
          {
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


  void back2TopActivity()
  {
    Intent intent = new Intent(this, MainViewPagerActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);

  }

  @Override
  public void onItemClick(View view, int position)
  {
    selectPosition = position;
    final SearchDevModel model = lists.get(position);
    final DevModel dbModel = DataManager.getInstance().getSNDev(model.getSN());


    View halfview = getLayoutInflater().inflate(R.layout.half_dialog_view, null);
    final EditText editText = (EditText) halfview.findViewById(R.id.dialog_edit);
    if (dbModel == null)
    {
      editText.setText("admin");
    }
    else
    {
      editText.setText(dbModel.pwd);
    }

    editText.setSelection(editText.getText().toString().length());

    AlertDialog dialog = new AlertDialog.Builder(this)
      .setTitle(getString(R.string.action_add_device_search))//设置对话框的标题
      .setView(halfview)
      .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          dialog.dismiss();
        }
      })
      .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          String content = editText.getText().toString();
          if (content.length() < 4)
          {
            SouthUtil.showToast(AddDeviceWlanActivity.this, getString(R.string.error_invalid_password));
          }
          else
          {
            if (dbModel == null)
            {
              Log.e(tag, "----2,change pwd ,new pwd is  " + content);
              DevModel devModel = new DevModel();
              devModel.SN = model.getSN();
              devModel.usr = "admin";//默认填写admin
              devModel.pwd = content;
              boolean ret = DataManager.getInstance().addDev(devModel);
              Log.e(tag, "addDev ,ret is " + ret);
            }
            else
            {
              Log.e(tag, "----3,change pwd ,new pwd is  " + content);
              dbModel.pwd = content;
              boolean ret = DataManager.getInstance().updateDev(dbModel);
              Log.e(tag, "updateDev ,ret is " + ret);
            }

            //先判断这个密码正确不正确
            if (lod == null)
            {
              lod = new LoadingDialog(AddDeviceWlanActivity.this);
            }
            lod.dialogShow();
            DevModel devModelForm = model.exportDevModelForm();
            Network.getCommandApi(devModelForm)
              .getSSIDList(devModelForm.usr, content, lib.Msg_WiFiSearch, 0)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(observer_SSIDList);


          }
          // Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
          dialog.dismiss();
        }
      }).create();
    dialog.show();
  }

  @Override
  public void onLongClick(View view, int position)
  {

  }

  void addDevice(SearchDevModel model)
  {
    if (lod == null)
    {
      lod = new LoadingDialog(AddDeviceWlanActivity.this);
    }
    lod.dialogShow();
    Log.e(tag, "RegisterID is " + JPushManager.getJPushRegisterID());
    ServerNetWork.getCommandApi().app_user_add_dev(
      AccountManager.getInstance().getDefaultUsr(),
      AccountManager.getInstance().getDefaultPwd(),
      JPushManager.getJPushRegisterID(),
      Config.mbtype,
      Config.apptype,
      Config.pushtype,
      model.getSN(),
      1)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(observer_add_dev);
  }

  void addDevice(List<SearchDevModel> devices)
  {
    List<Observable<RetModel>> observables = new ArrayList<>();
    for (SearchDevModel device : devices)
    {
      observables.add(ServerNetWork.getCommandApi().app_user_add_dev(
        AccountManager.getInstance().getDefaultUsr(),
        AccountManager.getInstance().getDefaultPwd(),
        JPushManager.getJPushRegisterID(),
        Config.mbtype,
        Config.apptype,
        Config.pushtype,
        device.getSN(),
        0));
    }
    Observable.combineLatest(observables, new FuncN<RetModel>()
    {

      @Override
      public RetModel call(Object... args)
      {
        RetModel model = new RetModel();
        model.ret = 1;
        for (Object i : args)
        {
          RetModel retModel = (RetModel) i;
          model.ret &= retModel.ret;

        }
        return model;
      }
    }).subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(observer_add_dev);
  }

  Observer<RetModel> observer_add_dev = new Observer<RetModel>()
  {
    @Override
    public void onCompleted()
    {
      lod.dismiss();
      Log.e(tag, "---------------------2");
    }

    @Override
    public void onError(Throwable e)
    {
      lod.dismiss();
      Log.e(tag, "---------------------1:" + e.getLocalizedMessage());
    }

    @Override
    public void onNext(RetModel m)
    {
      lod.dismiss();
      if (ServerNetWork.RESULT_SUCCESS == m.ret)
      {
        SouthUtil.showToast(AddDeviceWlanActivity.this, getString(R.string.string_devAddSuccess));
        back2TopActivity();
      }
      else if (ServerNetWork.RESULT_USER_ISBIND == m.ret)
      {
        SouthUtil.showToast(AddDeviceWlanActivity.this, getString(R.string.string_user_IsBind));
      }
      else if (ServerNetWork.RESULT_USER_LOGOUT == m.ret)
      {
        //RESULT_USER_LOGOUT 为收不到推送的情况下，访问服务器时的返回值，收到
        //返回登录界面，取消保存的AutoLogin
        SouthUtil.broadcastLogoutInfo();
        //需要同时处理推送消息，内容为 "USER_LOGOUT"
        //todo
      }
      else
      {
        SouthUtil.showToast(AddDeviceWlanActivity.this, getString(R.string.string_devAddFail));
      }

    }
  };


  Observer<List<SSIDModel>> observer_SSIDList = new Observer<List<SSIDModel>>()
  {
    @Override
    public void onCompleted()
    {
      //lod.dismiss();
      Log.e(tag, "observer_SSIDList---------------------2");
    }

    @Override
    public void onError(Throwable e)
    {
      lod.dismiss();
      Log.e(tag, "observer_SSIDList ---------------------1:" + e.getLocalizedMessage());

      SouthUtil.showDialog(AddDeviceWlanActivity.this, getString(R.string.error_dev_pass));

    }

    @Override
    public void onNext(List<SSIDModel> ssidModels)
    {
      lod.dismiss();
      SearchDevModel model = lists.get(selectPosition);
      addDevice(model);

    }
  };


}
