package stcam.stcamproject.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.model.DevModel;
import com.model.ShareModel;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;

public class DeviceShareActivity extends BaseAppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
  static final String tag = "DeviceShareActivity";
  DevModel devModel;
  ShareModel shareModel;

  CheckBox chkIsVideo;
  CheckBox chkIsHistory;
  CheckBox chkIsPush;
  CheckBox chkIsControl;
  ImageView qr_imageView;

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
    setContentView(R.layout.activity_device_share);
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      setCustomTitle(getString(R.string.action_device_share), true);
    }
    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null)
    {
      devModel = (DevModel) bundle.getParcelable("devModel");
      Log.e(tag, "NetHandle:" + devModel.NetHandle + ",SN:" + devModel.SN);
    }
    shareModel = new ShareModel();
    shareModel.SN = devModel.SN;
    shareModel.From = AccountManager.getInstance().getDefaultUsr();
    //shareModel.UID = devModel.UID;
    shareModel.Pwd = devModel.pwd;
    initView();

  }

  public void initView()
  {
    chkIsVideo = findViewById(R.id.chkIsVideo);
    chkIsHistory = findViewById(R.id.chkIsHistory);
    chkIsPush = findViewById(R.id.chkIsPush);
    chkIsControl = findViewById(R.id.chkIsControl);

    chkIsVideo.setOnCheckedChangeListener(this);
    chkIsHistory.setOnCheckedChangeListener(this);
    chkIsPush.setOnCheckedChangeListener(this);
    chkIsControl.setOnCheckedChangeListener(this);

    qr_imageView = findViewById(R.id.qr_imageView);
    findViewById(R.id.btn_generate_share).setOnClickListener(this);
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

  @Override
  public void onClick(View view)
  {
    Gson gson = new Gson();
    String json = gson.toJson(shareModel);
    Log.e(tag, json);
    Bitmap mBitmap = CodeUtils.createImage(json, 400, 400, null);
    qr_imageView.setImageBitmap(mBitmap);
  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean b)
  {
//"IsVideo":%d,"IsHistory":%d,"IsPush":%d,"IsSetup":%d,"IsControl":%d,"IsShare":%d,"IsRec":%d,"IsSnapshot":%d
    switch (compoundButton.getId())
    {
      case R.id.chkIsVideo:
        shareModel.IsVideo = b ? 1 : 0;
        break;

      case R.id.chkIsHistory:
        shareModel.IsHistory = b ? 1 : 0;
        break;

      case R.id.chkIsPush:
        shareModel.IsPush = b ? 1 : 0;
        break;

      case R.id.chkIsControl:
        shareModel.IsControl = b ? 1 : 0;
        break;

      default:
        break;
    }
  }
}
