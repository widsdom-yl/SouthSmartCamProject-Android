package stcam.stcamproject.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.model.UpdateDevModel;
import com.thSDK.lib;
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
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

import static stcam.stcamproject.Activity.MainDevListFragment.TUserMode.UserMode_Visitor;

public class SettingActivity extends BaseAppCompatActivity implements View.OnClickListener, BaseAdapter.OnItemClickListener
{
  ImageView imageview_thumb;
  TextView textview_uid;
  Button button_ap_sta;
  Button button_delete_device;
  MainDevListFragment.TUserMode UserMode;

  RecyclerView mRecyclerView;
  DeviceSettingAdapter mAdapter;
  DevModel devNode;
  PushSettingModel mPushSettingModel;

  List<String> items = new ArrayList<>();

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.blank_menu, menu);
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      setCustomTitle(getString(R.string.action_dev_setting), true);
    }
    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null)
    {
      devNode = bundle.getParcelable("devModel");
      UserMode = (MainDevListFragment.TUserMode) bundle.getSerializable("entry");
    }
    initView();
    initValue();

    if (devNode.IsConnect())
    {
      if (lod == null)
      {
        lod = new LoadingDialog(this);
      }
      lod.dialogShow();
      getPushConfigTask pushConfigTask = new getPushConfigTask();
      pushConfigTask.execute();
    }
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    devNode.updateUserAndPwd();
    refreshView();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        this.finish(); // back button
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  void initView()
  {
    imageview_thumb = findViewById(R.id.imageview_thumb);
    textview_uid = findViewById(R.id.textview_uid);
    button_delete_device = findViewById(R.id.button_delete_device);
    button_ap_sta = findViewById(R.id.button_ap_sta);
    if (UserMode == UserMode_Visitor)
    {
      button_ap_sta.setText(R.string.action_AP_T_STA);
    }
    else
    {
      button_ap_sta.setText(R.string.action_STA_T_AP);
    }

    if (UserMode == UserMode_Visitor)
    {
      button_delete_device.setVisibility(View.INVISIBLE);
    }

    button_delete_device.setOnClickListener(this);
    button_ap_sta.setOnClickListener(this);

    mRecyclerView = findViewById(R.id.recyclerView);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
  }

  void initValue()
  {
    items.add(getString(R.string.device_name));
    items.add(getString(R.string.action_device_pwd));
    items.add(getString(R.string.action_push));
    items.add(getString(R.string.string_DevAdvancedSettings));
    items.add(getString(R.string.action_version));

    mAdapter = new DeviceSettingAdapter(items);
    mAdapter.setDevModel(devNode);
    mAdapter.setOnItemClickListener(this);
    mRecyclerView.setAdapter(mAdapter);
  }

  void refreshView()
  {
    List<String> files = FileUtil.getSNFilesFromPath(FileUtil.pathThumb(), devNode.SN);
    if (files.size() > 0)
    {

      Bitmap bitmap = BitmapFactory.decodeFile(files.get(0));
      imageview_thumb.setImageBitmap(bitmap);

    }
    if (devNode.IPUID.length() == 20)//UID 有UID则多显示一行UID，没有UID则只显示SN
    {
      textview_uid.setText("SN:" + devNode.SN + "\nUID:" + devNode.IPUID);
    }
    else
    {
      textview_uid.setText("SN:" + devNode.SN);
    }
    mAdapter.notifyDataSetChanged();
  }

  private void changeDeviceNameDialog()
  {

    final EditText inputServer = new EditText(this);
    inputServer.setFocusable(true);
    inputServer.setText(devNode.DevName);//zhb

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.action_change_device_name)).setView(inputServer)
      .setNegativeButton(
        getString(R.string.cancel), null);
    builder.setPositiveButton(getString(R.string.OK),
      new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface dialog, int which)
        {
          if (lod == null)
          {
            lod = new LoadingDialog(SettingActivity.this);
          }
          lod.dialogShow();
          String inputName = inputServer.getText().toString();
          if (TextUtils.isEmpty(inputName))//zhb
          {
            SouthUtil.showDialog(SettingActivity.this, getString(R.string.error_field_required));
            lod.dismiss();//zhb
          }
          else
          {
            ChangeDeviceNameTask task = new ChangeDeviceNameTask();
            task.execute(inputName);
          }
        }
      });
    builder.show();
  }


  @Override
  public void onClick(View view)
  {
    switch (view.getId())
    {

      case R.id.button_delete_device:
        new AlertDialog.Builder(this)
          .setTitle(this.getString(R.string.action_delete_device_ask))
          .setPositiveButton(this.getString(R.string.action_ok), new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              if (lod == null)
              {
                lod = new LoadingDialog(SettingActivity.this);
              }
              lod.dialogShow();
              ServerNetWork.getCommandApi()
                .app_user_del_dev(
                  AccountManager.getInstance().getDefaultUsr(),
                  AccountManager.getInstance().getDefaultPwd(),
                  JPushManager.getJPushRegisterID(),
                  devNode.SN,
                  0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_delete);
            }
          })
          .setNegativeButton(this.getString(R.string.action_cancel), null)
          .show();
        break;

      case R.id.button_ap_sta:
        if (UserMode == UserMode_Visitor)
        {
          //设置ap转sta
          Intent intent1 = new Intent(STApplication.getInstance(), AddDeviceAP2StaSetup.class);

          Bundle bundle1 = new Bundle();
          bundle1.putParcelable("devModel", devNode);

          intent1.putExtras(bundle1);
          Log.e(tag, "to DeviceShareActivity NetHandle:" + devNode.NetHandle);

          startActivity(intent1);
        }
        else
        {
          //设置sta转ap

          if (devNode.getConnectType() == DevModel.CONNECT_TYPE.IS_CONN_LAN || devNode.getConnectType() == DevModel.CONNECT_TYPE
            .IS_CONN_DDNS
            || devNode.getConnectType() == DevModel.CONNECT_TYPE.IS_CONN_P2P)
          {
            if (lod == null)
            {
              lod = new LoadingDialog(this);
            }
            lod.dialogShow();
            Sta2ApTask task = new Sta2ApTask();
            task.execute();
          }
          else
          {
            SouthUtil.showDialog(SettingActivity.this, "当前无有效连接，无法转AP模式");
          }
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void onItemClick(View view, int position)
  {
    if (UserMode == UserMode_Visitor)
    {
      SouthUtil.showDialog(this, getString(R.string.string_mode_visitor));
      return;
    }

    if (!devNode.IsConnect())
    {
      SouthUtil.showDialog(SettingActivity.this, getString(R.string.action_net_not_connect));
      return;
    }

    if (0 == position)//修改设备名称
    {
      if (devNode.IsShare == 0)
      {
        SouthUtil.showDialog(this, getString(R.string.string_device_is_share));
        return;
      }

      changeDeviceNameDialog();

    }
    else if (1 == position)//修改密码
    {
      if (devNode.IsShare == 0)
      {
        SouthUtil.showDialog(this, getString(R.string.string_device_is_share));
        return;
      }

      Intent intent = new Intent(this, DevChangePwdActivity.class);
      Bundle bundle = new Bundle();
      bundle.putParcelable("model", devNode);
      bundle.putSerializable("type", DevChangePwdActivity.EnumChangeDevicePwd.DEVICE_SETTING);

      intent.putExtras(bundle);
      startActivity(intent);
    }
    else if (2 == position)//推送开关
    {

      if (devNode.IsPush == 0)
      {
        SouthUtil.showDialog(this, getString(R.string.string_no_push_permisson));
        return;
      }

      dialogChoice1();
    }
    else if (3 == position)//高级设置
    {
      if (devNode.IsShare == 0)
      {
        SouthUtil.showDialog(this, getString(R.string.string_device_is_share));
        return;
      }

      Intent intent = new Intent(this, DevAdvancedSettingsActivity.class);
      Bundle bundle = new Bundle();
      bundle.putParcelable("devModel", devNode);
      intent.putExtras(bundle);
      startActivity(intent);
    }

    else if (4 == position)//设备版本，检查更新
    {
      if (devNode.IsShare == 0)
      {
        SouthUtil.showDialog(this, getString(R.string.string_device_is_share));
        return;
      }
      checkDevUpdate();
    }
  }


  void checkDevUpdate()
  {
    if (lod == null)
    {
      lod = new LoadingDialog(this);
    }

    lod.dialogShow();
    ServerNetWork.getCommandApi()
      .app_upgrade_dev_check(devNode.DevType)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(observer_check_dev_update);
  }


  /*推送开关*/
  private void dialogChoice1()
  {

    final String items[] = {getString(R.string.action_close), getString(R.string.action_open)};
    AlertDialog.Builder builder = new AlertDialog.Builder(this, 3);
    builder.setTitle(getString(R.string.action_push));
    builder.setIcon(R.mipmap.ic_launcher);


    builder.setSingleChoiceItems(items, mPushSettingModel.getPushActive(),
      new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          Log.e(tag, "choose :" + which);
          mPushSettingModel.setPushActive(which);
          mAdapter.notifyDataSetChanged();

        }
      });
    builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        setPushConfigTask task = new setPushConfigTask();
        task.execute();
      }
    });
    builder.create().show();
  }

  @Override
  public void onLongClick(View view, int position)
  {
  }

  class ChangeDeviceNameTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = devNode.getDevURL(lib.Msg_SetDevInfo,"&DevName=" + params[0]);
      String ret = lib.thNetHttpGet(devNode.NetHandle, url);
      RetModel retModel = GsonUtil.parseJsonWithGson(ret, RetModel.class);
      if (retModel != null)
      {
        if (retModel.ret == 1)
        {
          devNode.DevName = params[0];
          devNode.DevNameChange = devNode.DevName;

          for (DevModel tmpNode1 : MainDevListFragment.mDevices)
          {
            if (tmpNode1.SN.equals(devNode.SN))
            {
              tmpNode1.DevName = devNode.DevName;
              tmpNode1.DevNameChange = devNode.DevName;
            }
          }
        }
      }
      return ret;


    }

    @Override
    protected void onPostExecute(String result)
    {
      //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
      //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
      //Log.e(tag,"get playback list :"+result);
      lod.dismiss();

      RetModel retModel = GsonUtil.parseJsonWithGson(result, RetModel.class);
      if (retModel != null)
      {
        if (retModel.ret == 1)
        {
          SouthUtil.showDialog(SettingActivity.this, getString(R.string.action_Success));
          refreshView();
        }
        else
        {
          SouthUtil.showDialog(SettingActivity.this, getString(R.string.action_Failed));
        }
      }
      super.onPostExecute(result);
    }
  }

  class Sta2ApTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = devNode.getDevURL(lib.Msg_SetWiFiCfg,"&wifi_Active=1&wifi_IsAPMode=1");
      String ret = lib.thNetHttpGet(devNode.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
      //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
      //Log.e(tag,"get playback list :"+result);
      lod.dismiss();

      RetModel retModel = GsonUtil.parseJsonWithGson(result, RetModel.class);
      if (retModel != null)
      {
        if (retModel.ret == 1)
        {
          SouthUtil.showDialog(SettingActivity.this, getString(R.string.action_STA_T_AP_Success));
          devNode.NetHandle = 0;
          for (DevModel tmpNode : MainDevListFragment.mDevices)
          {
            if (tmpNode.SN.equals(tmpNode.SN))
            {
              lib.thNetThreadDisConnFree(tmpNode.NetHandle);
              tmpNode.NetHandle = 0;
            }
          }
        }
        else if (retModel.ret == 2)
        {
          // SouthUtil.showDialog(SettingActivity.this,getString(R.string.action_STA_T_AP_Success));
          RebootTask task = new RebootTask();
          task.execute();
        }
        else
        {
          SouthUtil.showDialog(SettingActivity.this, getString(R.string.action_STA_T_AP_Failed));
        }
      }
      super.onPostExecute(result);
    }
  }

  class RebootTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = devNode.getDevURL(lib.Msg_SetDevReboot);
      String ret = lib.thNetHttpGet(devNode.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
      //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
      //Log.e(tag,"get playback list :"+result);
      lod.dismiss();

      RetModel retModel = GsonUtil.parseJsonWithGson(result, RetModel.class);
      if (retModel != null)
      {
        if (retModel.ret == 1)
        {
          SouthUtil.showDialog(SettingActivity.this, getString(R.string.action_STA_T_AP_Success));
          devNode.NetHandle = 0;
          for (DevModel tmpNode : MainDevListFragment.mDevices)
          {
            if (tmpNode.SN.equals(tmpNode.SN))
            {
              lib.thNetThreadDisConnFree(tmpNode.NetHandle);
              tmpNode.NetHandle = 0;
            }
          }
        }
        else
        {
          SouthUtil.showDialog(SettingActivity.this, getString(R.string.action_Failed));
        }
      }
      super.onPostExecute(result);
    }
  }

  Observer<UpdateDevModel> observer_check_dev_update = new Observer<UpdateDevModel>()
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
    public void onNext(final UpdateDevModel m)
    {
      lod.dismiss();
      if (m != null)
      {
        Log.e(tag, "---------------------onNext:" + m.ver);
        String VerNew = m.ver.substring(0, 11);
        String VerOld = devNode.SoftVersion.substring(0, 11);
        int ret = VerNew.compareTo(VerOld);
        if (ret > 0)
        //if (!m.ver.subSequence(0, 10).equals(model.SoftVersion.subSequence(0, 10)))
        {
          //提示升级
          new AlertDialog.Builder(SettingActivity.this)
            .setTitle(SettingActivity.this.getString(R.string.tip_version_old))
            .setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener()
            {
              @Override
              public void onClick(DialogInterface dialogInterface, int i)
              {
                updateDev(m);

              }
            })
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show();
        }
        else
        {
          SouthUtil.showToast(SettingActivity.this, getString(R.string.tip_version_new));
        }
      }


    }
  };

  void updateDev(UpdateDevModel updateModel)
  {
    //升级设备
    if (lod == null)
    {
      lod = new LoadingDialog(this);
    }
    UpdateDevTask task = new UpdateDevTask();
    task.execute(updateModel.ver, "" + updateModel.crc, updateModel.url);
  }


  Observer<RetModel> observer_delete = new Observer<RetModel>()
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
      SouthUtil.showToast(getApplicationContext(), getString(R.string.string_delfail));
    }

    @Override
    public void onNext(RetModel m)
    {
      lod.dismiss();
      if (ServerNetWork.RESULT_SUCCESS == m.ret)
      {
        SouthUtil.showToast(SettingActivity.this, getString(R.string.string_delsuccess));
        DataManager.getInstance().deleteDev(devNode);
        for (DevModel existModel : MainDevListFragment.mDevices)
        {
          if (devNode.SN.equals(existModel.SN))
          {
            lib.thNetThreadDisConnFree(existModel.NetHandle);
            existModel.NetHandle = 0;
            MainDevListFragment.mDevices.remove(existModel);
            //

            break;
          }
        }

        back2TopActivity();
      }
      else if (ServerNetWork.RESULT_USER_LOGOUT == m.ret)
      {
        //RESULT_USER_LOGOUT 为收不到推送的情况下，访问服务器时的返回值，收到
        //返回登录界面，取消保存的AutoLogin
        //SouthUtil.showToast(SettingActivity.this, getString(R.string.string_user_logout));
        //需要同时处理推送消息，内容为 "USER_LOGOUT"
        SouthUtil.broadcastLogoutInfo();
      }
      else
      {
        SouthUtil.showToast(SettingActivity.this, getString(R.string.string_delfail));
      }

    }
  };

  void back2TopActivity()
  {
    Intent intent = new Intent(this, MainViewPagerActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);

  }

  class getPushConfigTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = devNode.getDevURL(lib.Msg_GetPushCfg);
      String ret = lib.thNetHttpGet(devNode.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      lod.dismiss();

      PushSettingModel pushSettingModel = GsonUtil.parseJsonWithGson(result, PushSettingModel.class);
      if (pushSettingModel != null)
      {
        mPushSettingModel = pushSettingModel;
        mAdapter.setmPushSettingModel(mPushSettingModel);
      }
      super.onPostExecute(result);
    }
  }

  class setPushConfigTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = devNode.getDevURL(lib.Msg_SetPushCfg) + "&PushActive=" +
        mPushSettingModel.getPushActive() + "&PushInterval=" + mPushSettingModel.getPushInterval() +
        "&PIRSensitive=" + mPushSettingModel.getPIRSensitive();
      String ret = lib.thNetHttpGet(devNode.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {


      super.onPostExecute(result);
    }
  }

  //升级设备固件版本
  class UpdateDevTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = devNode.getDevURL(lib.Msg_CheckUpgradeBin) + "&ver="
        + params[0] + "&crc=" + params[1] + "&url=" + params[2];
      String ret = lib.thNetHttpGet(devNode.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
      //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
      //Log.e(tag,"get playback list :"+result);
      lod.dismiss();

      RetModel retModel = GsonUtil.parseJsonWithGson(result, RetModel.class);
      if (retModel != null)
      {
        if (retModel.ret == 1)
        {
          SouthUtil.showDialog(SettingActivity.this, getString(R.string.action_updating_device));
        }
        else
        {
          SouthUtil.showToast(SettingActivity.this, getString(R.string.action_Failed));
        }
      }
      else
      {
        SouthUtil.showToast(SettingActivity.this, getString(R.string.action_Failed));
      }
      super.onPostExecute(result);
    }
  }


  LoadingDialog lod;
  final String tag = "SettingActivity";
}
