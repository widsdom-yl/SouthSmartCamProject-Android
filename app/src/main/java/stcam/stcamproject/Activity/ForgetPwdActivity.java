package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    Button button_send;
    LoadingDialog lod;
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
        button_send = findViewById(R.id.button_send);
        button_send.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        editText_email.setError(null);
        String email =  editText_email.getText().toString();
        if (isEmailValid(email)){
            if (lod == null){
                lod = new LoadingDialog(this);
            }
            lod.dialogShow();
            ServerNetWork.getCommandApi()
                    .app_user_forgotpsd(email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
        else{
            editText_email.setError(getString(R.string.error_invalid_email));
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

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
    final String tag = "ForgetPwdActivity";
}
