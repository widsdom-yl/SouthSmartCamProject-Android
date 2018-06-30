package stcam.stcamproject.Activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.model.DevModel;
import com.model.SDVideoModel;
import com.thSDK.lib;

import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.PlayVoice;
import stcam.stcamproject.View.GLSurfaceViewPlayBack;
import stcam.stcamproject.View.VoiceImageButton;

public class PlayBackActivity extends AppCompatActivity implements View.OnClickListener {
    static final String tag = "PlayBackActivity";
    SDVideoModel model;
    DevModel devModel;
    GLSurfaceViewPlayBack glView;
    ImageButton imagebutton_back;
    VoiceImageButton button_snapshot,button_record;
    MainDevListActivity.EnumMainEntry entryType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        Window window = getWindow();
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_play_back);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = (SDVideoModel) bundle.getSerializable("model");
            devModel = (DevModel) bundle.getParcelable("devModel");
            entryType = (MainDevListActivity.EnumMainEntry) bundle.getSerializable("entry");
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
                if (lib.thNetIsRec(devModel.NetHandle)) {
                    lib.thNetStopRec(devModel.NetHandle);
                    if (FileUtil.isFileEmpty(recordfileName)){
                        FileUtil.delFiles(recordfileName);
                    }
                }
                glView.Stop();
                this.finish(); // back button

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Log.e(tag,"---------------------onKeyDown");
            if (lib.thNetIsRec(devModel.NetHandle)) {
                lib.thNetStopRec(devModel.NetHandle);
                if (FileUtil.isFileEmpty(recordfileName)){
                    FileUtil.delFiles(recordfileName);
                }
            }
            glView.Stop();
            this.finish(); // back button
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

        button_snapshot = findViewById(R.id.button_snapshot);
        imagebutton_back = findViewById(R.id.imagebutton_back);
        button_record = findViewById(R.id.button_record);
        button_record.setEnumSoundWav(PlayVoice.EnumSoundWav.CLICK);
        button_snapshot.setEnumSoundWav(PlayVoice.EnumSoundWav.SNAP);
        button_snapshot.setOnClickListener(this);
        button_record.setOnClickListener(this);
        imagebutton_back.setOnClickListener(this);
//        if (entryType == MainDevListActivity.EnumMainEntry.EnumMainEntry_Visitor){
//            button_record.setVisibility(View.INVISIBLE);
//
//            button_snapshot.setVisibility(View.INVISIBLE);
//        }
    }
    String recordfileName;


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imagebutton_back:
                if (lib.thNetIsRec(devModel.NetHandle)) {
                    lib.thNetStopRec(devModel.NetHandle);
                    if (FileUtil.isFileEmpty(recordfileName)){
                        FileUtil.delFiles(recordfileName);
                    }
                }
                glView.Stop();
                this.finish(); // back button
                break;
            case R.id.button_record:
                if (lib.thNetIsRec(devModel.NetHandle)){
                    lib.thNetStopRec(devModel.NetHandle);
                    if (FileUtil.isFileEmpty(recordfileName)){
                        FileUtil.delFiles(recordfileName);
                    }
                    button_record.setImageResource(R.drawable.btnrecorddefault);

                }
                else{
                    recordfileName = FileUtil.generatePathRecordFileName(devModel.SN);
                    lib.thNetStartRec(devModel.NetHandle,recordfileName);
                    button_record.setImageResource(R.drawable.btnrecorddown);
                }
                break;
            case R.id.button_snapshot:
                String fileName = FileUtil.generatePathSnapShotFileName(devModel.SN);
                if (fileName != null){
                    lib.thNetSaveToJpg(devModel.NetHandle,fileName);
                }
                break;
        }
    }
}
