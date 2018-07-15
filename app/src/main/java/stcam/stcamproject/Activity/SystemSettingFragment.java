package stcam.stcamproject.Activity;

import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.model.DevModel;
import com.model.UpdateModel;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.SytstmSettingListAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class SystemSettingFragment extends Fragment implements BaseAdapter.OnItemClickListener, View.OnClickListener {
    RecyclerView rv;
    List<String> settingArray = new ArrayList<>();//没有连接状态
    SytstmSettingListAdapter adapter;
    Button exit_button;
    TextView textview_user;

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
        textview_user= view.findViewById(R.id.textview_user);
        exit_button = view.findViewById(R.id.exit_button);
        rv.addItemDecoration(new DividerItemDecoration(this.getContext(),DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this.getContext()));

        textview_user.setText(AccountManager.getInstance().getDefaultUsr());
        settingArray.add(getString(R.string.action_alarm_sound));
        settingArray.add( getString(R.string.action_change_system_password));
        settingArray.add( getString(R.string.action_share_manager));
        settingArray.add(getString(R.string.action_app_version));
        settingArray.add(getString(R.string.action_about));

        adapter = new SytstmSettingListAdapter(settingArray);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        exit_button.setOnClickListener(this);
        return view;
    }




    @Override
    public void onItemClick(View view, int position) {
        if (0 == position){
            dialogChoice1();
        }

        else if (1 == position){
            Intent intent = new Intent(STApplication.getInstance(), ChangeLoginPasswordActivity.class);
            startActivity(intent);
        }
        else if(2 == position){
            Intent intent = new Intent(STApplication.getInstance(), ShareManagerActivity.class);
            startActivity(intent);
        }
        else if(3 ==  position){
            checkUpdate();
        }
        else if(4 == position){

            Intent intent = new Intent(STApplication.getInstance(), AboutActivity.class);
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


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.exit_button){


            new AlertDialog.Builder(this.getContext())
                    .setTitle(this.getContext().getString(R.string.action_exit_ask))
                    .setPositiveButton(this.getContext().getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (DevModel existModel : MainDevListFragment.mDevices){
                                if (existModel.IsConnect()){
                                    existModel.Disconn();
                                    existModel.NetHandle = 0;
                                }
                            }
                            MainDevListFragment.mDevices.clear();
                            Intent intent = new Intent(SystemSettingFragment.this.getContext(), LoginActivity.class);
                            AccountManager.getInstance().saveRemeber(false);
                            startActivity(intent);
                            SystemSettingFragment.this.getActivity().finish();

                        }
                    })
                    .setNegativeButton(this.getContext().getString(R.string.action_cancel), null)
                    .show();


        }

    }

    void checkUpdate(){
        if (lod == null) {
            lod = new LoadingDialog(this.getContext());
        }

        lod.dialogShow();
        ServerNetWork.getCommandApi()
                .app_upgrade_app_check(1,"android")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_check_update);
    }

    Observer<UpdateModel> observer_check_update = new Observer<UpdateModel>() {
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
        public void onNext(UpdateModel m) {
            lod.dismiss();
            if (m != null){
                Log.e(tag,"---------------------onNext:"+m.ver);

                try {
                    PackageInfo packageInfo = null;
                    packageInfo = STApplication.getInstance()
                            .getPackageManager()
                            .getPackageInfo(STApplication.getInstance().getPackageName(), 0);

                    String localVersion = packageInfo.versionName;

                    if (!compareStringValue(localVersion,m.ver)){
                        SouthUtil.showDialog(SystemSettingFragment.this.getContext(),STApplication.getInstance().getString(R.string.string_current_older));
                    }
                    else{
                        SouthUtil.showDialog(SystemSettingFragment.this.getContext(),STApplication.getInstance().getString(R.string.string_current_newer));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }



        }
    };

    /*如果是最新版本，返回true*/
    boolean compareStringValue(String localVersion,String serverVersion){
        String[] localVersions = localVersion.split(".");
        String[] serverVersions = serverVersion.split(".");
        if (localVersions.length == 3 && serverVersions.length == 3){
            for (int i = 0;i<localVersions.length ;++i){
                try {
                    int local = Integer.parseInt(localVersions[i]);
                    int server = Integer.parseInt(serverVersions[i]);
                    if (local > server){
                        return true;
                    }
                    else if (local < server){
                        return false;
                    }
                }
                catch (NumberFormatException e){
                    Log.e(tag,"---------------------NumberFormatException:"+e.toString());
                }
            }
        }
        return true;
    }
    String tag = "SystemSettingFragment";
    LoadingDialog lod;
}
