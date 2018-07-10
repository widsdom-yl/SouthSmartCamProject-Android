package stcam.stcamproject.Adapter;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;

public class DeviceSettingAdapter extends BaseAdapter<String>{
    DevModel devModel;
    int MD_Sensitive = -1;
    public DeviceSettingAdapter( List<String> list) {
        super(R.layout.list_setting, list);
    }

    public void setDevModel(DevModel devModel) {
        this.devModel = devModel;
    }
    public void setMD_Sensitive(int MD_Sensitive){
        this.MD_Sensitive = MD_Sensitive;
    }

    protected void convert(BaseHolder holder, String title, int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.title_text,title);
        if (devModel != null){
            if (0 == position){
                holder.setText(R.id.detail_text,devModel.DevName);
            }
            else if(1 == position){
                int length = devModel.pwd.length();
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0;i<length;i++){
                    stringBuilder.append("*");
                }

                holder.setText(R.id.detail_text,stringBuilder.toString());
            }
            else if(4 == position && MD_Sensitive != -1){
                if (MD_Sensitive <= 100){
                    holder.setText(R.id.detail_text, STApplication.getInstance().getString(R.string.action_level_low));
                }
                else if(MD_Sensitive <= 150){
                    holder.setText(R.id.detail_text, STApplication.getInstance().getString(R.string.action_level_middle));
                }
                else if(MD_Sensitive <= 200){
                    holder.setText(R.id.detail_text, STApplication.getInstance().getString(R.string.action_level_high));
                }
            }
            else if (5 == position){
                holder.setText(R.id.detail_text,devModel.SoftVersion);
            }
            else{
                holder.setText(R.id.detail_text,"");
            }
        }

    }
}