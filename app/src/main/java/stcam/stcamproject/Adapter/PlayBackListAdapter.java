package stcam.stcamproject.Adapter;

import com.model.SDVideoModel;

import java.util.List;

import stcam.stcamproject.R;
public class PlayBackListAdapter extends BaseAdapter<SDVideoModel>{
    public PlayBackListAdapter( List<SDVideoModel> list) {
        super(R.layout.layout_play_back_list, list);
    }


    protected void convert(BaseHolder holder, SDVideoModel model,int position) {
        super.convert(holder,model,position);
        holder.setText(R.id.file_name_text,model.sdVideo);
    }
}
