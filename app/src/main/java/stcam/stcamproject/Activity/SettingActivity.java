package stcam.stcamproject.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.model.DevModel;
import com.model.RetModel;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imageview_thumb;
    TextView textview_uid;
    TextView textview_name;
    TextView textview_pwd;
    TextView textview_version;
    Button button_change_pwd;
    Button button_delete_device;

    DevModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_setting);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = bundle.getParcelable("devModel");
        }
        initView();
    }
    @Override
    protected void onResume() {
        super.onResume();
        model.updateUserAndPwd();
        refreshView();
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
        imageview_thumb = findViewById(R.id.imageview_thumb);
        textview_uid = findViewById(R.id.textview_uid);
        textview_name = findViewById(R.id.textview_name);
        textview_pwd = findViewById(R.id.textview_pwd);
        textview_version = findViewById(R.id.textview_version);
        button_change_pwd = findViewById(R.id.button_change_pwd);
        button_delete_device = findViewById(R.id.button_delete_device);





        button_change_pwd.setOnClickListener(this);
        button_delete_device.setOnClickListener(this);

    }
    void refreshView(){
        List<String> files= FileUtil.getImagePathFromPath(FileUtil.pathSnapShot(),model.SN);
        if (files.size()>0){

            Bitmap bitmap = BitmapFactory.decodeFile(files.get(0));
            imageview_thumb.setImageBitmap(bitmap);
        }
        textview_uid.setText(model.UID);
        textview_name.setText(getString(R.string.device_name)+":"+model.DevName);
        textview_pwd.setText(getString(R.string.device_pwd)+":"+model.pwd);
        textview_version.setText(getString(R.string.device_version)+":"+model.SoftVersion);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_change_pwd:
                Intent intent = new Intent(this,ChangeDevicePwdActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("model",model);
                bundle.putSerializable("type",ChangeDevicePwdActivity.EnumChangeDevicePwd.DEVICE_SETTING);

                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.button_delete_device:
                if (lod == null){
                    lod = new LoadingDialog(this);
                }
                lod.dialogShow();
                ServerNetWork.getCommandApi()
                        .app_user_del_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                                model.SN, 0)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(observer);
                break;

        }
    }

    Observer<RetModel> observer = new Observer<RetModel>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
            SouthUtil.showToast(getApplicationContext(),"delete failed");
        }

        @Override
        public void onNext(RetModel m) {
            lod.dismiss();
            Log.e(tag,"---------------------0:"+m.ret);
            if (1 == m.ret){
                SouthUtil.showToast(getApplicationContext(),"delete ok");
            }
            else{
                SouthUtil.showToast(getApplicationContext(),"delete failed");
            }

        }
    };


    LoadingDialog lod;
    final String tag = "SettingActivity";
}
