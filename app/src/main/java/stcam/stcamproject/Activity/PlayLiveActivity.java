package stcam.stcamproject.Activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.model.DevModel;

import stcam.stcamproject.R;
import stcam.stcamproject.View.GLSurfaceViewLive;

public class PlayLiveActivity extends AppCompatActivity {

    GLSurfaceViewLive glView;
    DevModel devModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getSerializable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
        setContentView(R.layout.activity_play_live);
        initView();

        glView.Play();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        glView.Stop();
//        glView.Play();
    }
    void initView(){
        glView = findViewById(R.id.glPlayLive);
        glView.setModel(devModel);
    }
    static final String tag = "PlayLiveActivity";
}
