package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.model.DevModel;
import com.model.RetModel;
import com.model.SearchDevModel;
import com.model.ShareModel;

import java.io.Serializable;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.DataManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class ChangeDevicePwdActivity extends AppCompatActivity implements View.OnClickListener {

    final String tag = "ChangeDevicePwdActivity";

    public enum EnumChangeDevicePwd implements Serializable{
        SHARE,
        WLAN,
        STA,//ap to sta
        AP,//ap 游客模式
        DEVICE_SETTING
    }

    EnumChangeDevicePwd enumType;
    SearchDevModel model;
    ShareModel shareModel;
    DevModel setModel;
    DevModel dbModel;
    TextView text_uid;
    EditText editText_confirm_pwd;
    EditText editText_new_pwd;
    EditText editText_old_pwd;
    Button confirmButton;
    String SN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_device_pwd);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_change_password);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            enumType = (EnumChangeDevicePwd) bundle.getSerializable("type");
            switch (enumType){
                case SHARE:
                    //ShareModel model
                    shareModel = bundle.getParcelable("model");
                    dbModel = DataManager.getInstance().getSNDev(shareModel.SN);

                    SN = shareModel.SN;
                    break;
                case WLAN:
                case STA:
                case AP:
                    model = bundle.getParcelable("model");
                    dbModel = DataManager.getInstance().getSNDev(model.getSN());
                    SN = model.getSN();
                    break;
                case DEVICE_SETTING:
                    setModel = bundle.getParcelable("model");
                    dbModel = DataManager.getInstance().getSNDev(setModel.SN);
                    SN = setModel.SN;
                default:
                        break;
            }


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
        text_uid = findViewById(R.id.text_uid);
        editText_confirm_pwd = findViewById(R.id.editText_confirm_pwd);
        editText_new_pwd = findViewById(R.id.editText_new_pwd);
        editText_old_pwd = findViewById(R.id.editText_old_pwd);
        confirmButton = findViewById(R.id.button_next);


        switch (enumType) {
            case SHARE:
                //ShareModel model
                text_uid.setText("UID  " + shareModel.UID);
                break;
            case WLAN:
            case STA:
            case AP:
                text_uid.setText("UID  " + model.getUID());
                break;
            case DEVICE_SETTING:
                text_uid.setText("UID  " + setModel.UID);
            default:
                break;
        }



        confirmButton.setOnClickListener(this);

        if (dbModel != null){
            editText_old_pwd.setText(dbModel.pwd);
        }
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_next){
            Log.e(tag,"----0,change pwd ,new pwd is  "+editText_new_pwd.getText().toString());
            if (editText_new_pwd.getText().toString().equals(editText_confirm_pwd.getText().toString()) && editText_new_pwd.getText().toString().length() >= 4){
                Log.e(tag,"----1,change pwd ,new pwd is  "+editText_new_pwd.getText().toString());
                if (dbModel == null){
                    Log.e(tag,"----2,change pwd ,new pwd is  "+editText_new_pwd.getText().toString());
                    DevModel devModel = new DevModel();
                    devModel.SN = SN;
                    devModel.usr = "admin";//默认填写admin
                    devModel.pwd = editText_new_pwd.getText().toString();
                    boolean ret  = DataManager.getInstance().addDev(devModel);
                    Log.e(tag,"addDev ,ret is "+ret);
                }
                else{
                    Log.e(tag,"----3,change pwd ,new pwd is  "+editText_new_pwd.getText().toString());
                    dbModel.pwd = editText_new_pwd.getText().toString();
                    boolean ret = DataManager.getInstance().updateDev(dbModel);
                    Log.e(tag,"updateDev ,ret is "+ret);
                }

                if (enumType == EnumChangeDevicePwd.WLAN){
                    if (lod == null){
                        lod = new LoadingDialog(this);
                    }
                    lod.dialogShow();
                    ServerNetWork.getCommandApi().app_user_add_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                            JPushManager.getJPushRegisterID(),
                            1,0,0,model.getSN(),0)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(observer_add_dev);
                }
                else if(enumType == EnumChangeDevicePwd.STA){
                   // AddDeviceAP2StaSetup
                    Intent intent = new Intent(this,AddDeviceAP2StaSetup.class);
                    intent.putExtra("model" ,model);
                    startActivity(intent);
                }
                else if(enumType == EnumChangeDevicePwd.SHARE){
                    if (lod == null){
                        lod = new LoadingDialog(this);
                    }
                    lod.dialogShow();
                    ServerNetWork.getCommandApi().app_share_add_dev(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                            shareModel.From,JPushManager.getJPushRegisterID(),1,0,0,shareModel.SN,shareModel.Video,shareModel.History,shareModel.Push,
                            shareModel.Setup,shareModel.Control).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(observer_add_dev);
                }

            }
        }

    }
    Observer<RetModel> observer_add_dev = new Observer<RetModel>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
        }

        @Override
        public void onNext(RetModel m) {
            lod.dismiss();
            Log.e(tag,"---------------------0:"+m.ret);
            if (1 == m.ret){
                SouthUtil.showToast(ChangeDevicePwdActivity.this,"add success");
            }
            else{
                SouthUtil.showToast(ChangeDevicePwdActivity.this,"add failed");
            }

        }
    };
    LoadingDialog lod;

}
