package stcam.stcamproject.Adapter;

import com.model.PushSettingModel;

import java.util.List;

import stcam.stcamproject.R;




public class PushSettingAdapter extends BaseAdapter<String>{
    PushSettingModel mPushSettingModel;
    public PushSettingAdapter( List<String> list) {
        super(R.layout.list_setting, list);
    }
    public void setPushSettingModel(PushSettingModel pushSettingModel) {
        this.mPushSettingModel = pushSettingModel;
    }
    protected void convert(BaseHolder holder, String title, int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.title_text,title);
        if (mPushSettingModel != null){
            if (0 == position){
                holder.setText(R.id.detail_text,mPushSettingModel.getPushActiveDesc());
            }
            else if(1 == position){
                holder.setText(R.id.detail_text,mPushSettingModel.getPushIntervalDesc());
            }
            else if(2 == position){
                holder.setText(R.id.detail_text,mPushSettingModel.getPIRSensitiveDesc());
            }

        }


    }
}
