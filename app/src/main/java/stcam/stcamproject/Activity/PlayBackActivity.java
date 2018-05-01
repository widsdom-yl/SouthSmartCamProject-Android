package stcam.stcamproject.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.model.DevModel;

import stcam.stcamproject.R;
public class PlayBackActivity extends AppCompatActivity {
    static final String tag = "PlayBackActivity";
    DevModel devModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getSerializable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
    }
}
