package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.model.DevModel;

import info.hoang8f.android.segmented.SegmentedGroup;
import stcam.stcamproject.R;

public class LedControlActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    DevModel devModel;
    static final String tag = "LedControlActivity";

    RadioButton btn_mode_1;
    RadioButton btn_mode_2;
    RadioButton btn_mode_3;
    RadioButton btn_mode_4;

    SegmentedGroup modeGroup ;
    RelativeLayout relativelayout_time_span;
    RelativeLayout layout_sensitive;
    RelativeLayout layout_time;
    RelativeLayout layout_brintness;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_led_control);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getParcelable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
        initView();

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

    public void initView(){
        btn_mode_1 = findViewById(R.id.btn_mode_1);
        btn_mode_2 = findViewById(R.id.btn_mode_2);
        btn_mode_3 = findViewById(R.id.btn_mode_3);
        btn_mode_4 = findViewById(R.id.btn_mode_4);
        modeGroup = findViewById(R.id.segmented2);

        relativelayout_time_span = findViewById(R.id.relativelayout_time_span);
        layout_sensitive = findViewById(R.id.layout_sensitive);
        layout_time = findViewById(R.id.layout_time);
        layout_brintness = findViewById(R.id.layout_brintness);


        modeGroup.setOnCheckedChangeListener(this);
        modeGroup.check(R.id.btn_mode_3);
    }

    void hideAllLayout(){
        relativelayout_time_span.setVisibility(View.GONE);
        layout_sensitive.setVisibility(View.GONE);
        layout_time.setVisibility(View.GONE);
        layout_brintness.setVisibility(View.GONE);
    }
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == modeGroup){
            hideAllLayout();
            switch (checkedId){
                case R.id.btn_mode_1:
                    relativelayout_time_span.setVisibility(View.VISIBLE);
                    layout_sensitive.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_mode_2:
                    layout_brintness.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_mode_3:
                    layout_time.setVisibility(View.VISIBLE);
                    layout_brintness.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_mode_4:
                    layout_brintness.setVisibility(View.VISIBLE);
                    layout_sensitive.setVisibility(View.VISIBLE);
                    break;
                default:
                     break;
            }
        }
    }
}
