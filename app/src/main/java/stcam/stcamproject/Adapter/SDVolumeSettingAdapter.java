package stcam.stcamproject.Adapter;

import com.model.SDInfoModel;

import java.util.List;

import stcam.stcamproject.R;

public class SDVolumeSettingAdapter extends BaseAdapter<String>{

    SDInfoModel sdInfoModel;

    public SDVolumeSettingAdapter( List<String> list) {
        super(R.layout.list_setting, list);
    }

    public void setSdInfoModel(SDInfoModel sdInfoModel){
        this.sdInfoModel = sdInfoModel;
    }

    protected void convert(BaseHolder holder, String title, int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.title_text,title);
        if (sdInfoModel != null){
            if (0 == position){
                holder.setText(R.id.detail_text,sdInfoModel.getTotalSize());
            }
            else if(1 == position){
                holder.setText(R.id.detail_text,sdInfoModel.getRecordSize());
            }
            else if (2 == position){
                holder.setText(R.id.detail_text,sdInfoModel.getFreeSize());
            }
        }

    }
}