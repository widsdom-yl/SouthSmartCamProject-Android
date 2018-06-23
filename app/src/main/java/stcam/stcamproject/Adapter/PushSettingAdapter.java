package stcam.stcamproject.Adapter;

import java.util.List;

import stcam.stcamproject.R;




public class PushSettingAdapter extends BaseAdapter<String>{

    public PushSettingAdapter( List<String> list) {
        super(R.layout.list_setting, list);
    }

    protected void convert(BaseHolder holder, String title, int position) {
        super.convert(holder,title,position);
        holder.setText(R.id.title_text,title);


    }
}
