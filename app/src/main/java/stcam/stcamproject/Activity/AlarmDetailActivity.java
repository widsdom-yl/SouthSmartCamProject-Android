package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.model.AlarmImageModel;

import stcam.stcamproject.GlideApp;
import stcam.stcamproject.R;
public class AlarmDetailActivity extends AppCompatActivity {
    ImageView alarm_image;
    AlarmImageModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_detail);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_alarm);
        }

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = (AlarmImageModel) bundle.getSerializable("model");
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
    void initView(){
        alarm_image = findViewById(R.id.alarm_image);
        GlideApp.with(this).asBitmap()
                .load(model.Img)
                .centerCrop()
                .placeholder(R.drawable.imagethumb)
                .into(alarm_image);
    }
}
