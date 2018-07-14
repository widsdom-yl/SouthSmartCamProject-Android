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

import com.model.DevModel;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.


    Button btnNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_splash);
        btnNext = (Button)findViewById(R.id.next_btn);
        btnNext.setOnClickListener(this);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(lib.testGetFfmpeg());
        try {
            PackageInfo packageInfo = this.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0);
            int localVersion = packageInfo.versionCode;
            tv.setText(""+localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        new Handler().postDelayed(new Runnable() {
            public void run() {
                // do something

                for (DevModel existModel : MainDevListFragment.mDevices){
                    if (existModel.IsConnect()){
                        existModel.Disconn();
                        existModel.NetHandle = 0;
                    }
                }
                MainDevListFragment.mDevices.clear();
                boolean isRemeber = AccountManager.getInstance().getIsRemeberAccount();
                String usr = AccountManager.getInstance().getDefaultUsr();
                String pwd = AccountManager.getInstance().getDefaultPwd();
                if (isRemeber && usr.length()>0 && pwd.length()>0){
                    Intent intent = new Intent(STApplication.getInstance(), MainActivity.class);
                    intent.putExtra("entry", MainDevListFragment.EnumMainEntry.EnumMainEntry_Login);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }

        }, 1 * 1000);


    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
