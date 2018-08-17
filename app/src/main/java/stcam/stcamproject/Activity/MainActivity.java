package stcam.stcamproject.Activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;

import stcam.stcamproject.Config.Config;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;

public class MainActivity extends FragmentActivity implements View.OnClickListener
{
  Fragment f1;
  Fragment f2;
  Fragment f3;
  Fragment f4;
  LinearLayout content_container;
  private TextView tx1, tx2, tx3, tx4;
  ImageButton image_1, image_2, image_3, image_4;

  MainDevListFragment.EnumMainEntry entryType;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);


    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null)
    {
      entryType = (MainDevListFragment.EnumMainEntry) bundle.getSerializable("entry");
    }
    setContentView(R.layout.activity_main);

    StatusBarCompat.setStatusBarColor(this, Config.greenColor, true);


    init();
    if (savedInstanceState == null)
    {
      if (entryType == MainDevListFragment.EnumMainEntry.EnumMainEntry_Login)
      {
        getSupportFragmentManager().beginTransaction()
          .add(R.id.content_container, f1)
          .add(R.id.content_container, f2)
          .add(R.id.content_container, f3)
          .add(R.id.content_container, f4)
          .commit();
      }
      else
      {
        getSupportFragmentManager().beginTransaction()
          .add(R.id.content_container, f1)
          .commit();
      }

    }
    changeHomePage();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);

  }

  private void init()
  {
    content_container = (LinearLayout) findViewById(R.id.content_container);

    tx1 = findViewById(R.id.tx_1);
    tx2 = findViewById(R.id.tx_2);
    tx3 = findViewById(R.id.tx_3);
    tx4 = findViewById(R.id.tx_4);

    image_1 = findViewById(R.id.image_1);
    image_2 = findViewById(R.id.image_2);
    image_3 = findViewById(R.id.image_3);
    image_4 = findViewById(R.id.image_4);

    tx1.setOnClickListener(this);
    tx2.setOnClickListener(this);
    tx3.setOnClickListener(this);
    tx4.setOnClickListener(this);

    image_1.setOnClickListener(this);
    image_2.setOnClickListener(this);
    image_3.setOnClickListener(this);
    image_4.setOnClickListener(this);


    f1 = MainDevListFragment.newInstance(entryType);

    f2 = AlarmListFragment.newInstance();

    f3 = MediaFragment.newInstance();
    f4 = SystemSettingFragment.newInstance();
  }

  private void changeHomePage()
  {
    getSupportFragmentManager().beginTransaction()
      .hide(f2)
      .hide(f3)
      .hide(f4)
      .show(f1)
      .commit();

//    getSupportFragmentManager().beginTransaction()
//            .replace(R.id.content_container,f1).commit();


    this.settab();
    tx1.setTextColor(Config.greenColor);
    image_1.setSelected(true);

  }

  private void changeAlarmPage()
  {
    getSupportFragmentManager().beginTransaction()
      .hide(f1)
      .show(f2)
      .hide(f3)
      .hide(f4)
      .commit();

//    getSupportFragmentManager().beginTransaction()
//            .replace(R.id.content_container,f2).commit();


    this.settab();
    tx2.setTextColor(Config.greenColor);
    image_2.setSelected(true);

  }

  private void changeMediaPage()
  {
    MediaFragment fragment = (MediaFragment) f3;
    fragment.refreshUI();
    getSupportFragmentManager().beginTransaction()
      .hide(f1)
      .hide(f2)
      .hide(f4)
      .show(f3)
      .commit();

//    getSupportFragmentManager().beginTransaction()
//            .replace(R.id.content_container,f3).commit();


    this.settab();
    tx3.setTextColor(Config.greenColor);
    image_3.setSelected(true);

  }

  private void changeMinePage()
  {

    getSupportFragmentManager().beginTransaction()
      .hide(f1)
      .hide(f2)
      .hide(f3)
      .show(f4)
      .commit();

//    getSupportFragmentManager().beginTransaction()
//            .replace(R.id.content_container,f4).commit();

    this.settab();
    tx4.setTextColor(Config.greenColor);
    image_4.setSelected(true);

  }

  public void settab()
  {

    //
    tx1.setTextColor(Color.parseColor("#666666"));
    tx2.setTextColor(Color.parseColor("#666666"));
    tx3.setTextColor(Color.parseColor("#666666"));
    tx4.setTextColor(Color.parseColor("#666666"));
    image_1.setSelected(false);
    image_2.setSelected(false);
    image_3.setSelected(false);
    image_4.setSelected(false);

  }


  boolean isLoginMode()
  {
    if (entryType != MainDevListFragment.EnumMainEntry.EnumMainEntry_Login)
    {
      SouthUtil.showDialog(this, getString(R.string.action_visitor_mode_disable_excute));
      return false;
    }
    return true;
  }

  @Override
  public void onClick(View view)
  {
    switch (view.getId())
    {
      case R.id.tx_1:
      case R.id.image_1://设备列表
        changeHomePage();
        break;

      case R.id.tx_2:
      case R.id.image_2://警报
        if (isLoginMode())
        {
          changeAlarmPage();
        }
        break;
      case R.id.tx_3:
      case R.id.image_3://图像
        if (isLoginMode())
        {
          changeMediaPage();
        }
        break;

      case R.id.tx_4:
      case R.id.image_4://我
        if (isLoginMode())
        {
          changeMinePage();
        }
        break;
      default:
        break;
    }
  }
}
