package stcam.stcamproject.Adapter;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.R;

public class DeviceSettingAdapter extends BaseAdapter<String>{
    DevModel devModel;
    public DeviceSettingAdapter( List<String> list) {
        super(R.layout.list_setting, list);
    }

    public void setDevModel(DevModel devModel) {
        this.devModel = devModel;
    }

    protected void convert(BaseHolder holder, String title, int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.title_text,title);
        if (devModel != null){
            if (0 == position){
                holder.setText(R.id.detail_text,devModel.DevName);
            }
            else if(1 == position){
                holder.setText(R.id.detail_text,devModel.pwd);
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