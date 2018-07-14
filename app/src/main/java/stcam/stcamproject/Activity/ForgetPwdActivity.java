package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.model.RetModel;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class ForgetPwdActivity extends AppCompatActivity implements View.OnClickListener {
    EditText editText_email;
    EditText editText_password;
    EditText editText_confirm_pwd;
    EditText editText_checknum;
    Button button_next;

    Button button_send;
    LoadingDialog lod;
    String checkNum;

    int leftTime =0;
    int TIME = 1000;
    Handler handler_refresh = new Handler();
    Runnable runnable_fresh = new Runnable() {
        @Override
        public void run() {
            //

            if (--leftTime>0){
                button_send.setText(leftTime+"s");
                handler_refresh.postDelayed(runnable_fresh,TIME);
            }
            else{
                button_send.setEnabled(true);
                button_send.setText(getString(R.string.action_send_verification_number));
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_Forgetpwd);
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
    public void initView(){
        editText_email = findViewById(R.id.editText_email);
        button_send = findViewById(R.id.sender_check_button);
        button_next = findViewById(R.id.button_next);
        editText_password = findViewById(R.id.editText_password);
        editText_confirm_pwd = findViewById(R.id.editText_confirm_pwd);
        editText_checknum = findViewById(R.id.editText_checknum);


        button_send.setOnClickListener(this);
        button_next.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.sender_check_button:
                sendVerifyCode();
                break;
            case R.id.button_next:
                forgetPassword();
                break;
                default:
                    break;
        }


    }

    public void forgetPassword(){
        String email = editText_email.getText().toString();
        String password = editText_password.getText().toString();
        String confirmPassword = editText_confirm_pwd.getText().toString();
        String check = editText_checknum.getText().toString();


        if (TextUtils.isEmpty(email)) {
            SouthUtil.showDialog(this,getString(R.string.error_field_required));
            return;

        } else if (!isEmailValid(email)) {
            SouthUtil.showDialog(this,getString(R.string.error_invalid_email));
            return;

        }


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {

            SouthUtil.showDialog(this,getString(R.string.error_invalid_password));
            return;
        }

        if (!password.equals(confirmPassword)){
            SouthUtil.showDialog(this,getString(R.string.error_invalid_confirm_password));
            return;
        }


        if (TextUtils.isEmpty(checkNum)) {
            Log.e(tag,checkNum);
            SouthUtil.showDialog(this,getString(R.string.error_invalid_checknum));
            return;

        }

        if (!check.equals(checkNum)) {
            Log.e(tag,checkNum);
            SouthUtil.showDialog(this,getString(R.string.error_invalid_checknum));
            return;

        }





        // Check for a valid email address.



        // Show a progress spinner, and kick off a background task to
        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();
        ServerNetWork.getCommandApi()
                .app_user_forgotpsd(email,password,checkNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_resetPassword);
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
        }

        @Override
        public void onNext(RetModel m) {
            lod.dismiss();
            Log.e(tag,"---------------------0:"+m.ret);
            if (1 == m.ret){
                SouthUtil.showDialog(ForgetPwdActivity.this,ForgetPwdActivity.this.getString(R.string.action_send_pws_success));


            }
            else{
                SouthUtil.showDialog(ForgetPwdActivity.this,ForgetPwdActivity.this.getString(R.string.action_send_pws_failed));
            }

        }
    };

    void sendVerifyCode(){
        editText_email.setError(null);
        String email =  editText_email.getText().toString();
        if (isEmailValid(email)){
            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();
             ServerNetWork.getCommandApi()
                    .app_user_forgotpsd_send_verifycode(email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer_getCheckNum);
        }
        else{
            editText_email.setError(getString(R.string.error_invalid_email));
        }
    }

    Observer<RetModel> observer_getCheckNum = new Observer<RetModel>() {
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
            if (m.ret == 0){
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_get_checknum_sent_failed));
            }
            if (m.ret == -5){
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_no_user));
            }
            else if(m.ret == -1){
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_get_checknum_aleady_register));
            }
            else if(m.ret > 0){
                checkNum = ""+m.ret;
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_get_checknum_sent));
                leftTime = 300;
                button_send.setEnabled(false);
                button_send.setText(leftTime+"s");
                handler_refresh.postDelayed(runnable_fresh,TIME);
            }

        }
    };


    Observer<RetModel> observer_resetPassword = new Observer<RetModel>() {
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
            if (m.ret == 0){
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_reset_pwdr_failed0));
            }
            else if(m.ret == -1){
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_reset_pwd_failed1));
            }
            else if(m.ret == -2){
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_reset_pwd_failed2));
            }
            else if(m.ret == -3){
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_reset_pwd_failed3));
            }
            else if(m.ret == 1){
                SouthUtil.showToast(ForgetPwdActivity.this,getString(R.string.action_reset_pwd_success));
            }

        }
    };


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 4 &&  password.length() <= 16;
    }

    final String tag = "ForgetPwdActivity";
}
