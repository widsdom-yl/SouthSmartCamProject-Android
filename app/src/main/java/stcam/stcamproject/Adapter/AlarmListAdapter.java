package stcam.stcamproject.Adapter;

import android.widget.ImageView;

import com.model.AlarmImageModel;

import java.util.List;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.GlideApp;
import stcam.stcamproject.R;
public class AlarmListAdapter extends BaseAdapter<AlarmImageModel>{
    public AlarmListAdapter( List<AlarmImageModel> list) {
        super(R.layout.alarm_list_view, list);
    }

    protected void convert(BaseHolder holder, AlarmImageModel model,int position) {
        super.convert(holder,model,position);
        holder.setText(R.id.file_name_text,model.DevName).setText(R.id.time_text,model.AlmTime);
        ImageView imageView = holder.getView(R.id.alarm_image);
        GlideApp.with(STApplication.getInstance()).asBitmap()
                .load(model.Img)
                .centerCrop()
                .placeholder(R.drawable.imagethumb)
                .into(imageView);
    }
}
