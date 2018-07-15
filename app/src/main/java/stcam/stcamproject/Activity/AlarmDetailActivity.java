package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.github.chrisbanes.photoview.PhotoView;
import com.model.AlarmImageModel;

import stcam.stcamproject.GlideApp;
import stcam.stcamproject.R;
public class AlarmDetailActivity extends BaseAppCompatActivity {
    PhotoView alarm_image;
    AlarmImageModel model;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blank_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_detail);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){

            setCustomTitle(getString(R.string.action_search),true);


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
