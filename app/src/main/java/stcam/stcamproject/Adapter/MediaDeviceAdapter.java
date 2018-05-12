package stcam.stcamproject.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;

public class MediaDeviceAdapter extends BaseAdapter<DevModel>{
    public MediaDeviceAdapter( List<DevModel> list) {
        super(R.layout.media_device_list_view, list);
    }

    protected void convert(BaseHolder holder, DevModel model,int position) {
        super.convert(holder,model,position);
        holder.setText(R.id.media_device_text,model.DevName).setImageResource(R.id.media_device_image,R.drawable.imagethumb);
        ImageView imageView = holder.getView(R.id.media_device_image);

        List<String> files= FileUtil.getImagePathFromPath(FileUtil.pathSnapShot(),model.SN);
        if (files.size()>0){

            Bitmap bitmap = BitmapFactory.decodeFile(files.get(0));
            imageView.setImageBitmap(bitmap);
        }
        else{
            imageView.setImageResource(R.drawable.imagethumb);
        }


    }
}