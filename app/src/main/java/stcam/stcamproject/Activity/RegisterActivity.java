package stcam.stcamproject.Activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.model.RetModel;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends BaseAppCompatActivity {



    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mChecknum;
    private EditText editText_confirm_pwd;
    String checkNum;
    Context context;


    Button button_send;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blank_menu, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();



        if(actionBar != null){
//            actionBar.setTitle(R.string.action_Register);
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);

            setCustomTitle(getString(R.string.action_Register),true);

        }


        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.editText_password);

        mChecknum = (EditText) findViewById(R.id.editText_checknum);
        editText_confirm_pwd = findViewById(R.id.editText_confirm_pwd);


        Button mEmailSignInButton = (Button) findViewById(R.id.email_register_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegitster();
            }
        });

        button_send = (Button) findViewById(R.id.sender_check_button);
        button_send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getCheckNum();
            }
        });
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

    private void getCheckNum(){
        // Reset errors.

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();


        if (!isEmailValid(email)){
            SouthUtil.showDialog(this,getString(R.string.error_invalid_email));
            return;
        }

            // Show a progress spinner, and kick off a background task to

            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();
            subscription = ServerNetWork.getCommandApi()
                    .app_user_reg_send_verifycode(email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer_getCheckNum);


    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegitster() {

        // Reset errors.


        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = editText_confirm_pwd.getText().toString();
        String check = mChecknum.getText().toString();


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
            subscription = ServerNetWork.getCommandApi()
                    .app_user_reg(email,password,checkNum)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer_register);

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 4 &&  password.length() <= 16;
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
                SouthUtil.showToast(context,getString(R.string.action_get_checknum_sent_failed));
            }
            else if(m.ret == -1){
                SouthUtil.showToast(context,getString(R.string.action_get_checknum_aleady_register));
            }
            else if(m.ret > 0){
                leftTime = 300;
                button_send.setEnabled(false);
                button_send.setText(leftTime+"s");
                handler_refresh.postDelayed(runnable_fresh,TIME);
                checkNum = ""+m.ret;
                SouthUtil.showToast(context,getString(R.string.action_get_checknum_sent));
            }

        }
    };

    Observer<RetModel> observer_register = new Observer<RetModel>() {
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
                SouthUtil.showToast(context,getString(R.string.action_get_register_failed0));
            }
            else if(m.ret == -1){
                SouthUtil.showToast(context,getString(R.string.action_get_register_failed1));
            }
            else if(m.ret == -2){
                SouthUtil.showToast(context,getString(R.string.action_get_register_failed2));
            }
            else if(m.ret == -3){
                SouthUtil.showToast(context,getString(R.string.action_get_register_failed3));
            }
            else if(m.ret == 1){
                SouthUtil.showToast(context,getString(R.string.action_get_register_success));
                RegisterActivity.this.finish();
            }

        }
    };
    protected Subscription subscription;
    final String tag = "RegisterActivity";
    LoadingDialog lod;

}

