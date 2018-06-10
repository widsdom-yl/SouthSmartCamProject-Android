package stcam.stcamproject.Activity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;

public class AddDeviceOneStepActivity extends AppCompatActivity implements View.OnClickListener {
    EditText edittext_ssid_name;
    EditText edittext_ssid_pwd;
    Button button_next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.SmartConfig);
        }

        setContentView(R.layout.activity_add_device_one_step);
        initView();
    }
    void initView(){
        edittext_ssid_name = findViewById(R.id.spiner_ssid_name);
        edittext_ssid_pwd = findViewById(R.id.edittext_ssid_pwd);
        button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(this);

        WifiManager mWifiManager = ((WifiManager) getSystemService("wifi"));
        if (mWifiManager.isWifiEnabled())
        {
            WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            edittext_ssid_name.setText(wifiInfo.getSSID().replaceAll("\"",""));
            edittext_ssid_pwd.setFocusable(true);
            edittext_ssid_pwd.requestFocus();
        }





    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        edittext_ssid_name.setError(null);
        edittext_ssid_pwd.setError(null);

        String SSID = this.edittext_ssid_name.getText().toString();
        String Password = this.edittext_ssid_pwd.getText().toString();
        if (SSID.equals(""))
        {
            edittext_ssid_pwd.setError(getString(R.string.error_field_required));
            this.edittext_ssid_name.setFocusable(true);
            this.edittext_ssid_name.requestFocus();
            return;
        }

        if (Password.equals(""))
        {
            edittext_ssid_pwd.setError(getString(R.string.error_field_required));
            this.edittext_ssid_pwd.setFocusable(true);
            this.edittext_ssid_pwd.requestFocus();
            return;
        }
        if (edittext_ssid_name.getText().toString().length() > 0 && edittext_ssid_pwd.getText().toString().length() >0){
            Intent intent = new Intent(this, AddDeviceOneStepNext.class);
            Bundle bundle = new Bundle();
            bundle.putString("ssid",edittext_ssid_name.getText().toString());
            bundle.putString("ssid_pwd",edittext_ssid_pwd.getText().toString());
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else{
            SouthUtil.showDialog(this,getString(R.string.action_request_ssid));
        }

    }
}
