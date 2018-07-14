package stcam.stcamproject.Adapter;

import com.model.PushSettingModel;
import com.model.RecConfigModel;

import java.util.List;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;




public class PushSettingAdapter extends BaseAdapter<String>{
    PushSettingModel mPushSettingModel;
    int MD_Sensitive = -1;
    RecConfigModel mRecConfigModel;
    public PushSettingAdapter( List<String> list) {
        super(R.layout.list_setting, list);
    }
    public void setPushSettingModel(PushSettingModel pushSettingModel) {
        this.mPushSettingModel = pushSettingModel;
    }
    public void setmRecConfigModel(RecConfigModel mRecConfigModel) {
        this.mRecConfigModel = mRecConfigModel;
        this.notifyDataSetChanged();
    }
    public void setMD_Sensitive(int MD_Sensitive){
        this.MD_Sensitive = MD_Sensitive;
    }
    protected void convert(BaseHolder holder, String title, int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.title_text,title);
        if (mPushSettingModel != null){
            if(0 == position){
                holder.setText(R.id.detail_text,mPushSettingModel.getPushIntervalDesc());
            }
            else if (1 == position){
                if( MD_Sensitive != -1){
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
            }
            else if(2 == position){
                holder.setText(R.id.detail_text,mPushSettingModel.getPIRSensitiveDesc());
            }
            else if(4 == position){
                if (mRecConfigModel != null){
                    holder.setText(R.id.detail_text,mRecConfigModel.getRec_AlmTimeLen()+STApplication.getInstance().getString(R.string.string_second));
                }
            }

        }


    }
}
