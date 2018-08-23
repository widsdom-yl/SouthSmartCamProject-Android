package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.model.DevModel;
import com.model.RetModel;
import com.model.SearchDevModel;
import com.model.ShareModel;
import com.thSDK.lib;

import java.io.Serializable;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Config.Config;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.DataManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class ChangeDevicePwdActivity extends BaseAppCompatActivity implements View.OnClickListener
{

  final String tag = "ChangeDevicePwdActivity";

  public enum EnumChangeDevicePwd implements Serializable
  {
    SHARE,
    WLAN,
    STA,//ap to sta
    AP,//ap 游客模式
    DEVICE_SETTING
  }

  EnumChangeDevicePwd enumType;
  SearchDevModel model;
  ShareModel shareModel;
  DevModel setModel;
  DevModel dbModel;
  EditText editText_confirm_pwd;
  EditText editText_new_pwd;
  EditText editText_old_pwd;
  Button confirmButton;
  String SN;

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
    setContentView(R.layout.activity_change_device_pwd);
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {

      setCustomTitle(getString(R.string.action_device_password_manager), true);

    }
    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null)
    {
      enumType = (EnumChangeDevicePwd) bundle.getSerializable("type");
      switch (enumType)
      {
        case SHARE:
          //ShareModel model
          shareModel = bundle.getParcelable("model");
          dbModel = DataManager.getInstance().getSNDev(shareModel.SN);

          SN = shareModel.SN;
          break;
        case WLAN:
        case STA:
        case AP:
          model = bundle.getParcelable("model");
          dbModel = DataManager.getInstance().getSNDev(model.getSN());
          SN = model.getSN();
          break;
        case DEVICE_SETTING:
          setModel = bundle.getParcelable("model");
          dbModel = DataManager.getInstance().getSNDev(setModel.SN);
          SN = setModel.SN;
        default:
          break;
      }
    }

    initView();
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
    editText_confirm_pwd = findViewById(R.id.editText_confirm_pwd);
    editText_new_pwd = findViewById(R.id.editText_password);
    editText_old_pwd = findViewById(R.id.editText_old_pwd);
    confirmButton = findViewById(R.id.button_next);
    confirmButton.setOnClickListener(this);

    if (dbModel != null)
    {
      editText_old_pwd.setText(dbModel.pwd);
      editText_old_pwd.setSelection(editText_old_pwd.getText().toString().length());
    }
  }

  String PasswordOld = null;
  String PasswordNew = null;
  String PasswordNew1 = null;

  @Override
  public void onClick(View view)
  {
    if (view.getId() != R.id.button_next)
    {
      return;
    }

    PasswordOld = editText_old_pwd.getText().toString();
    PasswordNew = editText_new_pwd.getText().toString();
    PasswordNew1 = editText_confirm_pwd.getText().toString();

    if (PasswordNew.equals(PasswordOld))
    {
      SouthUtil.showDialog(ChangeDevicePwdActivity.this, getString(R.string.confirm_password_same));
      return;
    }

    if (PasswordNew.length() >= 20)
    {
      SouthUtil.showDialog(ChangeDevicePwdActivity.this, getString(R.string.action_dev_pwd_limit_lessthan_19));
      return;
    }
    if (!PasswordNew.equals(PasswordNew1))
    {
      SouthUtil.showDialog(ChangeDevicePwdActivity.this, getString(R.string.confirm_password_nosame));
      return;
    }
    if (PasswordNew.length() <= 4)
    {
      SouthUtil.showDialog(ChangeDevicePwdActivity.this, getString(R.string.password_length_limit));
      return;
    }

    if (dbModel == null)
    {
      DevModel devModel = new DevModel();
      devModel.SN = SN;
      devModel.usr = Config.DEFAULTPASSWORD;//默认填写admin
      devModel.pwd = PasswordNew;
      boolean ret = DataManager.getInstance().addDev(devModel);
    }
    else
    {
      dbModel.pwd = PasswordNew;
      boolean ret = DataManager.getInstance().updateDev(dbModel);
    }

    //修改密码


    if (enumType == EnumChangeDevicePwd.WLAN)
    {
      if (lod == null)
      {
        lod = new LoadingDialog(this);
      }
      lod.dialogShow();
      ServerNetWork.getCommandApi().app_user_add_dev(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance()
          .getDefaultPwd(),
        JPushManager.getJPushRegisterID(),
        Config.mbtype,
        Config.apptype,
        Config.pushtype,
        model.getSN(),
        0)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer_add_dev);
    }
    else if (enumType == EnumChangeDevicePwd.STA)
    {
      // AddDeviceAP2StaSetup
      Intent intent = new Intent(this, AddDeviceAP2StaSetup.class);
      intent.putExtra("model", model);
      startActivity(intent);
    }
    else if (enumType == EnumChangeDevicePwd.SHARE)
    {
      if (lod == null)
      {
        lod = new LoadingDialog(this);
      }
      lod.dialogShow();
      ServerNetWork.getCommandApi().app_share_add_dev(
        AccountManager.getInstance().getDefaultUsr(),
        AccountManager.getInstance().getDefaultPwd(),
        shareModel.From,
        JPushManager.getJPushRegisterID(),
        Config.mbtype,
        Config.apptype,
        Config.pushtype,
        shareModel.SN,
        shareModel.IsVideo,
        shareModel.IsHistory,
        shareModel.IsPush,
        1,//shareModel.IsSetup,
        shareModel.IsControl
      ).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer_add_dev);
    }
    else if (enumType == EnumChangeDevicePwd.DEVICE_SETTING)
    {
      if (lod == null)
      {
        lod = new LoadingDialog(this);
      }
      lod.dialogShow();
      ChangeDevicePwdTask task = new ChangeDevicePwdTask();
      task.execute();
      // finish();
    }
  }

  class ChangeDevicePwdTask extends AsyncTask<String, Void, String>
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
      String url = "http://" + setModel.IPUID + ":" + setModel.WebPort + "/cfg1.cgi?User=admin&Psd=" + editText_old_pwd.getText()
        .toString() + "&MsgID=" + lib.Msg_SetUserLst + "&UserName0=admin&Password0=" + editText_new_pwd.getText().toString();
      Log.e(tag, url + ",NetHandle is " + setModel.NetHandle);
      String ret = lib.thNetHttpGet(setModel.NetHandle, url);
      Log.e(tag, "ret :" + ret);

      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
      //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
      //Log.e(tag,"get playback list :"+result);


      RetModel retModel = GsonUtil.parseJsonWithGson(result, RetModel.class);
      if (retModel != null)
      {
        if (retModel.ret == 1)
        {
          lod.dismiss();
          SouthUtil.showDialog(ChangeDevicePwdActivity.this, getString(R.string.action_Success));
        }
        else if (retModel.ret == 2)
        {
          RebootTask task = new RebootTask();
          task.execute();
        }
        else
        {
          lod.dismiss();
          SouthUtil.showDialog(ChangeDevicePwdActivity.this, getString(R.string.action_Failed));
        }
      }
      else
      {
        lod.dismiss();
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
      String url = "http://" + setModel.IPUID + ":" + setModel.WebPort + "/cfg1.cgi?User=admin&Psd=" + editText_old_pwd.getText()
        .toString() + "&MsgID=" + lib.Msg_SetDevReboot;
      String ret = lib.thNetHttpGet(setModel.NetHandle, url);

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
          SouthUtil.showDialog(ChangeDevicePwdActivity.this, getString(R.string.action_Success));
        }
        else
        {
          SouthUtil.showDialog(ChangeDevicePwdActivity.this, getString(R.string.action_Failed));
        }
      }
      super.onPostExecute(result);
    }
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
      if (lib.RESULT_SUCCESS == m.ret)
      {
        SouthUtil.showToast(ChangeDevicePwdActivity.this, getString(R.string.string_devAddSuccess));
      }
      else if (ServerNetWork.RESULT_USER_ISBIND == m.ret)
      {
        String Str;
        if (!TextUtils.isEmpty(m.Info))
        {
          String sFormat = getString(R.string.string_user_IsBind1);
          Str = String.format(sFormat, m.Info);
        }
        else
        {
          Str = getString(R.string.string_user_IsBind);
        }
        SouthUtil.showToast(ChangeDevicePwdActivity.this, Str);
      }
      else
      {
        SouthUtil.showToast(ChangeDevicePwdActivity.this, getString(R.string.string_devAddFail));
      }

    }
  };
  LoadingDialog lod;

}
