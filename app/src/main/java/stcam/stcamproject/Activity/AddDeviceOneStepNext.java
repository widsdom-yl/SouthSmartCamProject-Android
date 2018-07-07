package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.thSDK.TMsg;
import com.thSDK.lib;

import stcam.stcamproject.R;
public class AddDeviceOneStepNext extends AppCompatActivity implements View.OnClickListener {
    Button button_cancel;
    final  static  String tag = "AddDeviceOneStepNext";
    String SSID;
    String Password;
    SpinKitView spin_kit;
    boolean isConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_one_step_next);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.SmartConfig_connect);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            SSID = bundle.getString("ssid");
            Password = bundle.getString("ssid_pwd");
        }

        button_cancel = findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(this);
        spin_kit = findViewById(R.id.spin_kit);
        smartConfig();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                isConfig = false;
                this.finish(); // back button

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Log.e(tag,"---------------------onKeyDown");
            isConfig = false;
            this.finish(); // back button
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void SmartConfigStop()
    {
        isConfig = false;
        spin_kit.setVisibility(View.INVISIBLE);
        lib.jsmtStop();

    }
    void smartConfig(){
        isConfig = true;
        lib.jsmtInit();
        lib.jsmtStart(SSID, Password, "", "", 0);
        new Thread()
        {
            public void run()
            {
                long dt = System.currentTimeMillis();

                while (true)
                {
                    try
                    {
                        sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                    }
                    if (!isConfig){
                        handler.sendEmptyMessage(TMsg.Msg_SmartConfigClose);
                    }


                    if (System.currentTimeMillis() - dt > 1000 * 30)
                    {
                        handler.sendEmptyMessage(TMsg.Msg_SmartConfigOver);
                        break;
                    }
                }
            }
        }.start();

    }
    Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {


                case TMsg.Msg_SmartConfigClose:
                    //AddDeviceOneStepNext.this.mydialog.dismiss();
                    AddDeviceOneStepNext.this.SmartConfigStop();
                    break;

                case TMsg.Msg_SmartConfigOver:
                    AddDeviceOneStepNext.this.SmartConfigStop();
                    //actSmartConfig.this.mydialog.dismiss();
                    Toast.makeText(AddDeviceOneStepNext.this, R.string.SmartConfigOver, Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onClick(View view) {
        isConfig = false;
        this.finish();
    }
}
