package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.thSDK.TMsg;
import com.thSDK.lib;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Config.Config;
import stcam.stcamproject.R;
import stcam.stcamproject.Activity.AddDeviceOneStepActivity;

public class AddDeviceOneStepNext extends BaseAppCompatActivity implements View.OnClickListener
{
  Button BtnCancel;
  final static String tag = "AddDeviceOneStepNext";
  String SSID;
  String Password;
  SpinKitView spin_kit;
  boolean IsSmartLinkIng = true;
  TextView edtLeftTime;

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
    setContentView(R.layout.activity_add_device_one_step_next);

    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      setCustomTitle(getString(R.string.SmartConfig_connect), true);
    }

    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null)
    {
      SSID = bundle.getString("ssid");
      Password = bundle.getString("ssid_pwd");
    }

    BtnCancel = findViewById(R.id.button_cancel);
    BtnCancel.setOnClickListener(this);
    spin_kit = findViewById(R.id.spin_kit);
    edtLeftTime = findViewById(R.id.txtSmartLinkLeftTime);
    smartConfigStart();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        this.onClick(null);//zhb add
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    if (keyCode == KeyEvent.KEYCODE_BACK)
    {
      this.onClick(null);//zhb add
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onClick(View view)//退出按键
  {
    AddDeviceOneStepNext.this.SmartConfigStop();//zhb add
    this.finish();
  }

  Handler handler = new Handler()
  {
    public void handleMessage(Message msg)
    {
      switch (msg.what)
      {
        case TMsg.Msg_SmartConfiging:
          String sFormat = getString(R.string.string_LeftTimeSmartLinkOver);
          String Str = String.format(sFormat, msg.arg1);
          edtLeftTime.setText(Str);
          break;

        case TMsg.Msg_SmartConfigClose:
          AddDeviceOneStepNext.this.SmartConfigStop();
          break;

        case TMsg.Msg_SmartConfigOver:
          AddDeviceOneStepNext.this.SmartConfigStop();
          Toast.makeText(AddDeviceOneStepNext.this, R.string.SmartConfigOver, Toast.LENGTH_SHORT).show();
          AddDeviceOneStepNext.this.finish();
          Intent intent4 = new Intent(STApplication.getInstance(), AddDeviceWlanActivity.class);
          startActivity(intent4);
          break;
      }
      super.handleMessage(msg);
    }
  };

  public void SmartConfigStop()
  {
    IsSmartLinkIng = false;
    spin_kit.setVisibility(View.INVISIBLE);
    lib.jsmtStop();
    Log.e(tag, "jsmtStop: ");

  }

  public void smartConfigStart()
  {
    IsSmartLinkIng = true;
    lib.jsmtStop();
    lib.jsmtInit();
    lib.jsmtStart(SSID, Password, "", "", 0);
    Log.e(tag, "jsmtInit jsmtStart: ");
    new Thread()
    {
      public void run()
      {
        long iTimeOut = Config.TIMEOUT_SMARTLINKSEARCH;
        long dt1 = System.currentTimeMillis();
        long dt2;
        int iLeftTime;

        while (true)
        {
          if (!IsSmartLinkIng)
          {
            break;
          }

          dt2 = System.currentTimeMillis();
          iLeftTime = (int) (iTimeOut / 1000) - (int) ((dt2 - dt1) / 1000);
          handler.sendMessage(Message.obtain(handler, TMsg.Msg_SmartConfiging, iLeftTime, 0));
          try
          {
            sleep(500);
          }
          catch (InterruptedException e)
          {
          }
          Log.e(tag, "IsSmartLinkIng:" + IsSmartLinkIng + " run: " + (dt2 - dt1));

          if (dt2 - dt1 > iTimeOut)//超时
          {
            handler.sendEmptyMessage(TMsg.Msg_SmartConfigOver);
            break;
          }
        }
      }
    }.start();
  }


}
