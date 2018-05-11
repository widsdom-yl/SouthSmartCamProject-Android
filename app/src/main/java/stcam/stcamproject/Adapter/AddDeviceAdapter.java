package stcam.stcamproject.Adapter;

import com.model.SearchDevModel;

import java.util.List;

import stcam.stcamproject.R;

public class AddDeviceAdapter extends BaseAdapter<SearchDevModel>{
    public AddDeviceAdapter( List<SearchDevModel> list) {
        super(R.layout.add_device_view, list);
    }

    protected void convert(BaseHolder holder, SearchDevModel model, int position) {
        super.convert(holder,model,position);
        holder.setText(R.id.device_name_view,model.getDevName()).setText(R.id.device_sn_view,model.getSN());

    }
}
