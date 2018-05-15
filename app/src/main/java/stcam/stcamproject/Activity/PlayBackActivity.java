package stcam.stcamproject.Activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.model.DevModel;
import com.model.SDVideoModel;

import stcam.stcamproject.R;
import stcam.stcamproject.View.GLSurfaceViewPlayBack;

public class PlayBackActivity extends AppCompatActivity {
    static final String tag = "PlayBackActivity";
    SDVideoModel model;
    DevModel devModel;
    GLSurfaceViewPlayBack glView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = (SDVideoModel) bundle.getSerializable("model");
            devModel = (DevModel) bundle.getParcelable("devModel");
            Log.e(tag,"play sdVideo:"+model.sdVideo);
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(model.sdVideo);
        }
        initView();
        glView.Play();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        glView.Stop();
//        glView.Play();
    }
    void initView(){
        glView = findViewById(R.id.glPlayBack);
        glView.setModel(devModel,model);
    }
}
