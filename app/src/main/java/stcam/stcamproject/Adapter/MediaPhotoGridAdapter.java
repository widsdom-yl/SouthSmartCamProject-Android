package stcam.stcamproject.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.List;

import stcam.stcamproject.R;

public class MediaPhotoGridAdapter extends BaseAdapter<String>{
    OnImageCheckListner onImageCheckListner;
    List<String> mCheckFile;
    public MediaPhotoGridAdapter( List<String> list) {
        super(R.layout.media_photo_grid_view, list);
    }
    public void setCheckFile(List<String> checkFile){
        mCheckFile = checkFile;
    }

    protected void convert(BaseHolder holder, String path, final int position) {
        super.convert(holder,path,position);
        ImageView imageView = holder.getView(R.id.media_photo_image);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(bitmap);
        final CheckBox checkBox = holder.getView(R.id.checkbox);

        checkBox.setChecked(false);
        if (mCheckFile.contains(path)){
            checkBox.setChecked(true);
        }
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = checkBox.isChecked();
                if (onImageCheckListner != null){
                    onImageCheckListner.OnImageChecked(view,position,checked);
                }
            }
        });
    }
    public void setOnImageCheckListner(OnImageCheckListner onImageCheckListner) {
        this.onImageCheckListner = onImageCheckListner;
    }

    public interface OnImageCheckListner {
        void OnImageChecked(View view, int position,boolean checked);
    }


}
