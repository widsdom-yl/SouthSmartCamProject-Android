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

import stcam.stcamproject.R;
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
            actionBar.setTitle(R.string.action_add_one_key_setting);
        }

        setContentView(R.layout.activity_add_device_one_step);
        initView();
    }
    void initView(){
        edittext_ssid_name = findViewById(R.id.spiner_ssid_name);
        edittext_ssid_pwd = findViewById(R.id.edittext_ssid_pwd);
        button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(this);

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        edittext_ssid_name.setText(wifiInfo.getSSID().replaceAll("\"",""));

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
        Intent intent = new Intent(this, AddDeviceOneStepNext.class);
        startActivity(intent);
    }
}
