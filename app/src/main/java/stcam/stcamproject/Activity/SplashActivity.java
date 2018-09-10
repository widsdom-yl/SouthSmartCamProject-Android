package stcam.stcamproject.Activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import stcam.stcamproject.R;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener
{
  Button btnNext;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    getSupportActionBar().hide();

    setContentView(R.layout.activity_splash);
    btnNext = (Button) findViewById(R.id.next_btn);
    btnNext.setOnClickListener(this);

    TextView tv = (TextView) findViewById(R.id.sample_text);
    try
    {
      PackageInfo packageInfo = this.getApplicationContext()
        .getPackageManager()
        .getPackageInfo(this.getPackageName(), 0);
      String localVersion = packageInfo.versionName;
      tv.setText("Version:" + localVersion);
    }
    catch (PackageManager.NameNotFoundException e)
    {
      e.printStackTrace();
    }

    new Handler().postDelayed(new Runnable()
    {
      public void run()
      {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
      }
    }, 1 * 1000);
  }

  @Override
  public void onClick(View view)
  {
    Intent intent = new Intent(this, MainViewPagerActivity.class);
    startActivity(intent);
    finish();
  }

}
