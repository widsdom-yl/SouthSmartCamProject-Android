package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import stcam.stcamproject.R;

public class AddDeviceOneStepNext extends AppCompatActivity implements View.OnClickListener {
    Button button_cancel;
    final  static  String tag = "AddDeviceOneStepNext";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_one_step_next);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_add_ap_sta);
        }
        button_cancel = findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(this);

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
        this.finish();
    }
}
