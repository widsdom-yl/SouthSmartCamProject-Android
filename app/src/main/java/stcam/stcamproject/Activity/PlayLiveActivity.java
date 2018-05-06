package stcam.stcamproject.Activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import com.model.DevModel;
import com.thSDK.lib;

import stcam.stcamproject.R;
import stcam.stcamproject.Util.ConstraintUtil;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.View.GLSurfaceViewLive;

public class PlayLiveActivity extends AppCompatActivity implements View.OnClickListener {

    GLSurfaceViewLive glView;
    DevModel devModel;
    TableLayout layout_control;
    RelativeLayout layout_land;
    Button button_snapshot,button_snapshot_o;
    Button button_speech,button_speech_o;
    Button button_record,button_record_o;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getSerializable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(devModel.DevName);
        }
        setContentView(R.layout.activity_play_live);
        initView();

        glView.Play();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                glView.Stop();

                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configurationChanged();

    }
    void configurationChanged(){
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            layout_land.setVisibility(View.INVISIBLE);
            layout_control.setVisibility(View.VISIBLE);


        }else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            layout_land.setVisibility(View.VISIBLE);
            layout_control.setVisibility(View.INVISIBLE);

        }
    }
    void initView(){
        glView = findViewById(R.id.glPlayLive);
        layout_control = findViewById(R.id.layout_control);
        glView.setModel(devModel);
        layout_land = findViewById(R.id.layout_land);
        configurationChanged();

        button_snapshot = findViewById(R.id.button_snapshot);
        button_snapshot_o = findViewById(R.id.button_snapshot_o);
        button_speech = findViewById(R.id.button_speech);
        button_speech_o = findViewById(R.id.button_speech_o);
        button_record = findViewById(R.id.button_record);
        button_record_o = findViewById(R.id.button_record_o);
        button_snapshot.setOnClickListener(this);
        button_snapshot_o.setOnClickListener(this);
        button_speech.setOnClickListener(this);
        button_speech_o.setOnClickListener(this);
        button_record.setOnClickListener(this);
        button_record_o.setOnClickListener(this);

    }
    ConstraintUtil constraintUtil;
    static final String tag = "PlayLiveActivity";

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_snapshot:
            case R.id.button_snapshot_o:
                String fileName = FileUtil.generatePathSnapShotFileName(devModel.SN);
                if (fileName != null){
                    lib.thNetSaveToJpg(devModel.NetHandle,fileName);
                }
                break;
            case R.id.button_speech:
            case R.id.button_speech_o:
                break;
            case R.id.button_record:
            case R.id.button_record_o:

                if (lib.thNetIsRec(devModel.NetHandle)){
                    lib.thNetStopRec(devModel.NetHandle);
                }
                else{
                   String recordfileName = FileUtil.generatePathRecordFileName(devModel.SN);
                   lib.thNetStartRec(devModel.NetHandle,recordfileName);
                }

                break;
            default:
                break;
        }
    }
}
