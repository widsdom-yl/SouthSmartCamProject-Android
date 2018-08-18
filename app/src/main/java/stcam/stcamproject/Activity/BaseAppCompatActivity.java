package stcam.stcamproject.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;

public class BaseAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {

        super.onResume();
        registerMessageReceiver();
    }

    @Override
    protected void onPause() {

        super.onPause();
        unRegisterMessageReceiver();
    }


    public void setCustomTitle(String title,boolean showback) {

        TextView textView = new TextView(this);
        textView.setText(title);
        //zhb textView.setTextSize(22);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
          LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);


        textView.setTextColor(getResources().getColor(R.color.whiteColor));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(textView);

        if (showback){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "JGPush.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    public void unRegisterMessageReceiver(){
        if(mMessageReceiver != null){
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            }
            catch(Exception e){
                Log.e("BaseAppCompatActivity",e.getLocalizedMessage());
            }

        }
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(KEY_MESSAGE);
                    //  String extras = intent.getStringExtra(KEY_EXTRAS);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(KEY_MESSAGE + " : " + messge + "\n");

                    if (messge.equals("USER_LOGOUT")){
                        //弹出提示框，提示用户退出登录
                        showUserLogOutAlert();
                    }

                }
            } catch (Exception e){
                Log.e("MainViewPagerActivity","error is :"+e.getLocalizedMessage());
            }

            Log.e("MainViewPagerActivity","receive end");
        }
    }

    void showUserLogOutAlert(){

        //到了这一步，为何没显示对话框？
        //需要同时处理推送消息，内容为 "USER_LOGOUT"



        Intent intent = new Intent(this, LoginActivity.class);
        AccountManager.getInstance().saveRemeber(false);
        intent.putExtra("extra","logout");
        //AccountManager.getInstance().saveRemeber(false);
        AccountManager.getInstance().saveAccount("","",false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(intent);



//        android.app.AlertDialog alert = builder.create();
//        alert.show();

    }
}
