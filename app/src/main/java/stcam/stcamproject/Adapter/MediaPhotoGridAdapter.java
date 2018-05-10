package stcam.stcamproject.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.util.List;

import stcam.stcamproject.R;

public class MediaPhotoGridAdapter extends BaseAdapter<String>{
    public MediaPhotoGridAdapter( List<String> list) {
        super(R.layout.media_photo_grid_view, list);
    }

    protected void convert(BaseHolder holder, String path,int position) {
        super.convert(holder,path,position);
        ImageView imageView = holder.getView(R.id.media_photo_image);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(bitmap);

    }
}
