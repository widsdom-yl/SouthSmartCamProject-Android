package stcam.stcamproject.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
public class RegisterActivity extends AppCompatActivity {



    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mChecknum;
    private int checkNum;//验证码
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.action_Register);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);

        mChecknum = (EditText) findViewById(R.id.checknum);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegitster();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_register_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegitster();
            }
        });

        Button sender_check_button = (Button) findViewById(R.id.sender_check_button);
        sender_check_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getCheckNum();
            }
        });
    }


    private void getCheckNum(){
        // Reset errors.
        mEmailView.setError(null);


        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
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
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegitster() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mChecknum.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String checkNum = mChecknum.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(checkNum)) {
            Log.e(tag,checkNum);
            mChecknum.setError(getString(R.string.error_invalid_checknum));
            focusView = mChecknum;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
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
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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
                checkNum = m.ret;
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
            }

        }
    };
    protected Subscription subscription;
    final String tag = "RegisterActivity";
    LoadingDialog lod;

}

