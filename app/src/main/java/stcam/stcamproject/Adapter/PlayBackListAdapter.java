package stcam.stcamproject.Adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.model.SDVideoModel;

import java.util.List;

import stcam.stcamproject.Manager.DataManager;
import stcam.stcamproject.R;
public class PlayBackListAdapter extends BaseAdapter<SDVideoModel>{
    public PlayBackListAdapter( List<SDVideoModel> list) {
        super(R.layout.layout_play_back_list, list);
    }


    protected void convert(BaseHolder holder, SDVideoModel model,int position) {
        super.convert(holder,model,position);
        holder.setText(R.id.file_name_text,model.sdVideo);
        TextView file_name_text = holder.getView(R.id.file_name_text);
        SDVideoModel sdVideoModelInData =  DataManager.getInstance().getCertainSDVideo(model);
        if (sdVideoModelInData != null && sdVideoModelInData.viewed){
            file_name_text.setTextColor(Color.rgb(0,0,255));
        }
        else{
            file_name_text.setTextColor(Color.rgb(0,0,0));
        }
    }
}
