package stcam.stcamproject.Adapter;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
public class SytstmSettingListAdapter extends BaseAdapter<String>{
    public SytstmSettingListAdapter( List<String> list) {
        super(R.layout.system_setting_list_view, list);
    }

    protected void convert(BaseHolder holder, String title ,int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.file_name_text,title);

        if (0 == position){
            boolean alarmSoundOpen = AccountManager.getInstance().getAlarmSoundSetting();
            holder.setText(R.id.detail_info_text,alarmSoundOpen?STApplication.getInstance().getString(R.string.action_open):STApplication.getInstance().getString(R.string.action_close));
        }
        if (1 == position){
            try {
                PackageInfo packageInfo = STApplication.getInstance()
                        .getPackageManager()
                        .getPackageInfo(STApplication.getInstance().getPackageName(), 0);
                String localVersion = packageInfo.versionName;
                holder.setText(R.id.detail_info_text,localVersion);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

    }
}
