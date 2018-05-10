package stcam.stcamproject.Adapter;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.R;

public class MediaDeviceAdapter extends BaseAdapter<DevModel>{
    public MediaDeviceAdapter( List<DevModel> list) {
        super(R.layout.media_device_list_view, list);
    }

    protected void convert(BaseHolder holder, DevModel model,int position) {
        super.convert(holder,model,position);
        holder.setText(R.id.media_device_text,model.DevName).setImageResource(R.id.media_device_image,R.drawable.imagethumb);
//        ImageView imageView = holder.getView(R.id.alarm_image);

    }
}