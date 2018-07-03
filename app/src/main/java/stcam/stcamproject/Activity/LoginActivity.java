package stcam.stcamproject.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.model.RetModel;

import java.util.ArrayList;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    boolean remeber;

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    AppCompatCheckBox checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestPermisson();

        Log.e(tag, "onCreate: "+ JPushManager.getJPushRegisterID());
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        checkbox = findViewById(R.id.checkbox);
        remeber = AccountManager.getInstance().getIsRemeberAccount();
        checkbox.setChecked(remeber);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

//        mEmailView.setText("4719373@qq.com");
//        mPasswordView.setText("admin111");

        if (remeber){
            mEmailView.setText(AccountManager.getInstance().getDefaultUsr());
            mPasswordView.setText(AccountManager.getInstance().getDefaultPwd());
        }



        Button mEmailSignInButton = (Button) findViewById(R.id.email_login_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                step2Register();
            }
        });


        Button visitor_button = (Button) findViewById(R.id.visitor_button);
        visitor_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(STApplication.getInstance(), MainDevListActivity.class);
                intent.putExtra("entry", MainDevListActivity.EnumMainEntry.EnumMainEntry_Visitor);
                startActivity(intent);
            }
        });

        Button forget_button = findViewById(R.id.forget_button);
        forget_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(STApplication.getInstance(), ForgetPwdActivity.class);
                startActivity(intent);
            }
        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //do something
                remeber = isChecked;
            }
        });
    }

    private String[] denied;
    void requestPermisson(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (PermissionChecker.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permissions[i]);
                }
            }
            if (list.size() != 0) {
                denied = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Log.e(tag,"add deny:"+i);
                    denied[i] = list.get(i);
                    ActivityCompat.requestPermissions(this, denied, i);
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            Log.e(tag,"onRequestPermissionsResult");
            boolean isDenied = false;
            for (int i = 0; i < denied.length; i++) {
                String permission = denied[i];
                for (int j = 0; j < permissions.length; j++) {
                    if (permissions[j].equals(permission)) {
                        if (grantResults[j] != PackageManager.PERMISSION_GRANTED) {
                            isDenied = true;
                            break;
                        }
                    }
                }
            }
            if (isDenied) {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
            } else {


            }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    void step2Register(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {


        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
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
//            Intent intent = new Intent(STApplication.getInstance(), MainDevListActivity.class);
//            startActivity(intent);
//            return;
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();
            ServerNetWork.getCommandApi()
                    .app_user_login(email,password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer_login);

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



    Observer<RetModel> observer_login = new Observer<RetModel>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
            SouthUtil.showDialog(LoginActivity.this,getString(R.string.error_network_status));

        }

        @Override
        public void onNext(RetModel m) {
            lod.dismiss();
            Log.e(tag,"---------------------0:"+m.ret);
            if (1 == m.ret){
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();
                AccountManager.getInstance().saveAccount(email,password,remeber);
                Intent intent = new Intent(STApplication.getInstance(), MainDevListActivity.class);
                intent.putExtra("entry", MainDevListActivity.EnumMainEntry.EnumMainEntry_Login);
                startActivity(intent);
                LoginActivity.this.finish();
            }
            else{
                SouthUtil.showDialog(LoginActivity.this,getString(R.string.error_login_failed));
            }

        }
    };


    final String tag = "LoginActivity";
    LoadingDialog lod;


}

