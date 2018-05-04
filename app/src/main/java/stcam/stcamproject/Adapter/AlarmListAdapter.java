package stcam.stcamproject.Adapter;

import com.model.AlarmImageModel;

import java.util.List;

import stcam.stcamproject.R;
public class AlarmListAdapter extends BaseAdapter<AlarmImageModel>{
    public AlarmListAdapter( List<AlarmImageModel> list) {
        super(R.layout.alarm_list_view, list);
    }

    protected void convert(BaseHolder holder, AlarmImageModel model,int position) {
        super.convert(holder,model,position);
        holder.setText(R.id.file_name_text,model.AlmName);
    }
}
