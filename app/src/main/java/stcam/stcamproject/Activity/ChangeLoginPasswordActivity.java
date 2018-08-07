package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.model.RetModel;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class ChangeLoginPasswordActivity extends BaseAppCompatActivity implements View.OnClickListener {
    EditText editText_confirm_pwd;
    EditText editText_new_pwd;
    EditText editText_old_pwd;
    Button confirmButton;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blank_menu, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_login_password);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){

            setCustomTitle(getString(R.string.action_change_login_password),true);


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
        editText_confirm_pwd = findViewById(R.id.editText_confirm_pwd);
        editText_new_pwd = findViewById(R.id.editText_password);
        editText_old_pwd = findViewById(R.id.editText_old_password);
        confirmButton = findViewById(R.id.button_next);
        confirmButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (!editText_old_pwd.getText().toString().equals(AccountManager.getInstance().getDefaultPwd())){
            //原密码不正确
            SouthUtil.showDialog(ChangeLoginPasswordActivity.this,"old password is wrong");
            return;
        }
        else if(editText_new_pwd.getText().length() < 4){
            //密码小于4位
            SouthUtil.showDialog(ChangeLoginPasswordActivity.this,"Password length should not less than 4!");
            return;
        }
        else if(!editText_new_pwd.getText().toString().equals(editText_confirm_pwd.getText().toString())){
            SouthUtil.showDialog(ChangeLoginPasswordActivity.this,"Confirm password is not the same as new password");
            //两次密码不一致
            return;
        }

        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();


        ServerNetWork.getCommandApi()
                .app_user_changepsd(AccountManager.getInstance().getDefaultUsr(),AccountManager.getInstance().getDefaultPwd(),
                        editText_new_pwd.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
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
                SouthUtil.showToast(ChangeLoginPasswordActivity.this,getString(R.string.string_ChangePsdSuccess));
            }
            else{
                SouthUtil.showToast(ChangeLoginPasswordActivity.this,getString(R.string.string_ChangePsdFail));
            }

        }
    };
    LoadingDialog lod;
    final String tag = "ChangePasswordActivity";

}
