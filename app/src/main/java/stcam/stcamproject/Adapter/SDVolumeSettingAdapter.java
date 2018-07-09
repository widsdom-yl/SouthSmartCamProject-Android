package stcam.stcamproject.Adapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.model.SDInfoModel;

import java.util.List;

import stcam.stcamproject.R;

public class SDVolumeSettingAdapter extends BaseAdapter<String> implements View.OnClickListener {

    SDInfoModel sdInfoModel;
    boolean _record;

    public SDVolumeSettingAdapter( List<String> list) {
        super(R.layout.list_setting, list);
    }

    public void setIsRecord(boolean record){
        _record = record;

    }
    public void setSdInfoModel(SDInfoModel sdInfoModel){
        this.sdInfoModel = sdInfoModel;
    }

    protected void convert(BaseHolder holder, String title, int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.title_text,title);
        ImageButton button = holder.getView(R.id.ImageButton_switch);
        TextView detail_text = holder.getView(R.id.detail_text);

        if (_record){
            button.setImageResource(R.drawable.check_on);
        }
        else{
            button.setImageResource(R.drawable.check_off);
        }
        if (sdInfoModel != null){
            if (0 == position){
                holder.setText(R.id.detail_text,sdInfoModel.getTotalSize());
                detail_text.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
            }
//            else if(1 == position){
//                holder.setText(R.id.detail_text,sdInfoModel.getRecordSize());
//            }
            else if (1 == position){
                holder.setText(R.id.detail_text,sdInfoModel.getFreeSize());
                detail_text.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
            }
            else if(2 == position){
                detail_text.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener( this);
                button.setSelected(_record);
            }
        }

    }

    @Override
    public void onClick(View view) {
        _record = !_record;
        if (mOnRecordClickListener != null){
            mOnRecordClickListener.onRecordClickListener(_record);
        }
        this.notifyDataSetChanged();
    }

    public interface OnRecordClickListener{
        //条目被点击时触发的回调
        //tpe:0-item被点击 1-share被点击 2-回放被点击 3-设置被点击
        void onRecordClickListener(boolean record);

    }

    public void setOnRecordClickListener(OnRecordClickListener onRecordClickListener) {
        mOnRecordClickListener = onRecordClickListener;
    }
    OnRecordClickListener mOnRecordClickListener;
}