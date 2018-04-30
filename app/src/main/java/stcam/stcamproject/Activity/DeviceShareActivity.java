package stcam.stcamproject.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.model.DevModel;
import com.model.ShareModel;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import stcam.stcamproject.R;
public class DeviceShareActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    static final String tag = "DeviceShareActivity";
    DevModel devModel;
    ShareModel shareModel;
    CheckBox checkbox_vid;
    CheckBox checkbox_control;
    CheckBox checkbox_push;
    CheckBox checkbox_setting;
    ImageView qr_imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_share);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_add_device);
            actionBar.setTitle(R.string.action_device_share);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getSerializable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
        shareModel = new ShareModel();
        shareModel.From = "807510889@qq.com";
        shareModel.UID = devModel.UID;
        initView();

    }
    public void initView(){
        checkbox_vid = findViewById(R.id.checkBox_vid);
        checkbox_control = findViewById(R.id.checkBox_control);
        checkbox_push = findViewById(R.id.checkBox_push);
        checkbox_setting = findViewById(R.id.checkBox_setting);
        checkbox_vid.setOnCheckedChangeListener(this);
        checkbox_control.setOnCheckedChangeListener(this);
        checkbox_push.setOnCheckedChangeListener(this);
        checkbox_setting.setOnCheckedChangeListener(this);
        qr_imageView = findViewById(R.id.qr_imageView);
        findViewById(R.id.btn_generate_share).setOnClickListener(this);
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
        Gson gson =new Gson();
        String json = gson.toJson(shareModel);
        Log.e(tag,json);
        Bitmap mBitmap = CodeUtils.createImage(json, 400, 400, null);
        qr_imageView.setImageBitmap(mBitmap);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.checkBox_vid:
                shareModel.Video = b;
                break;
            case R.id.checkBox_push:
                shareModel.Push = b;
                break;
            case R.id.checkBox_setting:
                shareModel.Setup = b;
                break;
            case R.id.checkBox_control:
                shareModel.Control = b;
                break;
            default:
                break;
        }
    }
}
