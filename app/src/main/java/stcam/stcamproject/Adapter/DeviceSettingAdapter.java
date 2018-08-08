package stcam.stcamproject.Adapter;

import com.model.DevModel;
import com.model.PushSettingModel;

import java.util.List;

import stcam.stcamproject.R;

public class DeviceSettingAdapter extends BaseAdapter<String>{
    DevModel devModel;
    int MD_Sensitive = -1;
    PushSettingModel mPushSettingModel;
    public DeviceSettingAdapter( List<String> list) {
        super(R.layout.list_setting, list);
    }


    /*
    *  items.add(getString(R.string.device_name));
        items.add(getString(R.string.action_device_pwd));
        items.add(getString(R.string.action_push));
        items.add(getString(R.string.action_manager_senior));
        items.add(getString(R.string.action_manager_volume));
//        items.add(getString(R.string.action_manager_alarm_level));
        items.add(getString(R.string.action_version));
        */
    public void setDevModel(DevModel devModel) {
        this.devModel = devModel;
    }
    public void setMD_Sensitive(int MD_Sensitive){
        this.MD_Sensitive = MD_Sensitive;
    }
    public void setmPushSettingModel(PushSettingModel mPushSettingModel){
        this.mPushSettingModel = mPushSettingModel;
        this.notifyDataSetChanged();
    }
    protected void convert(BaseHolder holder, String title, int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.title_text,title);
        if (devModel != null){
            if (0 == position){
                holder.setText(R.id.detail_text,devModel.GetDevName());
            }
            else if(1 == position){
                int length = devModel.pwd.length();
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0;i<length;i++){
                    stringBuilder.append("*");
                }

                holder.setText(R.id.detail_text,stringBuilder.toString());
            }
            else if(2 == position){
                //推送开关
                if (mPushSettingModel != null){
                    holder.setText(R.id.detail_text,mPushSettingModel.getPushActiveDesc());
                }
            }
//            else if(4 == position && MD_Sensitive != -1){
//                if (MD_Sensitive <= 100){
//                    holder.setText(R.id.detail_text, STApplication.getInstance().getString(R.string.action_level_low));
//                }
//                else if(MD_Sensitive <= 150){
//                    holder.setText(R.id.detail_text, STApplication.getInstance().getString(R.string.action_level_middle));
//                }
//                else if(MD_Sensitive <= 200){
//                    holder.setText(R.id.detail_text, STApplication.getInstance().getString(R.string.action_level_high));
//                }
//            }
            else if (4 == position){
                holder.setText(R.id.detail_text,devModel.SoftVersion);
            }
            else{
                holder.setText(R.id.detail_text,"");
            }
        }

    }
}