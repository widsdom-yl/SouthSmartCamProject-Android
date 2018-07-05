package stcam.stcamproject.Activity;

import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.SytstmSettingListAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;

public class SystemSettingFragment extends Fragment implements BaseAdapter.OnItemClickListener {
    RecyclerView rv;
    List<String> settingArray = new ArrayList<>();//没有连接状态
    SytstmSettingListAdapter adapter;

    public static SystemSettingFragment newInstance() {
        SystemSettingFragment fragment = new SystemSettingFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.activity_system_setting, container, false);
        rv = view.findViewById(R.id.system_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this.getContext(),DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this.getContext()));

        settingArray.add(getString(R.string.action_alarm_sound));
        settingArray.add(getString(R.string.action_app_version));
        settingArray.add(getString(R.string.action_change_system_password));
        settingArray.add(getString(R.string.action_about));

        adapter = new SytstmSettingListAdapter(settingArray);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        return view;
    }




    @Override
    public void onItemClick(View view, int position) {
        if (0 == position){
            dialogChoice1();
        }
        else if (2 == position){
            Intent intent = new Intent(STApplication.getInstance(), ChangeLoginPasswordActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onLongClick(View view, int position) {

    }


    /*开关*/
    private void dialogChoice1() {

        final String items[] = {getString(R.string.action_open), getString(R.string.action_close)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(),3);
        builder.setTitle(getString(R.string.action_alarm_sound));
        builder.setIcon(R.mipmap.ic_launcher);

        boolean alarmSoundOpen = AccountManager.getInstance().getAlarmSoundSetting();
        builder.setSingleChoiceItems(items, alarmSoundOpen?0:1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       if (0 == which){
                           AccountManager.getInstance().setAlarmSoundSetting(true);
                           setNotification1();
                       }
                       else{
                           AccountManager.getInstance().setAlarmSoundSetting(false);
                           setNotification3();
                       }
                       adapter.notifyDataSetChanged();
                    }
                });
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    //自定义报警通知（震动铃声都要）
    public void setNotification1(){
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SystemSettingFragment.this.getContext());
        builder.statusBarDrawable = R.mipmap.ic_launcher;//消息栏显示的图标
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_SOUND| Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS;// 设置为铃声与震动都要
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }
    //自定义报警通知（铃声）
    public void setNotification2(){
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SystemSettingFragment.this.getContext());
        builder.statusBarDrawable = R.mipmap.ic_launcher;//消息栏显示的图标</span>
                builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS;// 设置为铃声与震动都要
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }
    //自定义报警通知（震动）
    public void setNotification3(){
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SystemSettingFragment.this.getContext());
        builder.statusBarDrawable = R.mipmap.ic_launcher;//消息栏显示的图标</span>
                builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS;// 震动
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }
    //自定义报警通知（震动铃声都不要）
    public void setNotification4(){
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SystemSettingFragment.this.getContext());
        builder.statusBarDrawable = R.mipmap.ic_launcher;
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_LIGHTS;// 设置为铃声与震动都不要
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }


}
