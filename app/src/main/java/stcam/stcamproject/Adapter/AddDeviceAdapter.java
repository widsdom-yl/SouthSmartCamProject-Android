package stcam.stcamproject.Adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.model.DevModel;
import com.model.SearchDevModel;

import java.util.List;

import stcam.stcamproject.Activity.MainDevListFragment;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;

public class AddDeviceAdapter extends BaseAdapter<SearchDevModel>
{
  public AddDeviceAdapter(List<SearchDevModel> list)
  {
    super(R.layout.add_device_view, list);
  }


  protected void convert(BaseHolder holder, SearchDevModel model, int position)
  {
    String Str;
    super.convert(holder, model, position);

    TextView device_name_view = holder.getView(R.id.device_name_view);
    TextView device_sn_view = holder.getView(R.id.device_sn_view);
    boolean exist = false;

    for (DevModel devModel : MainDevListFragment.mDevices)
    {
      if (devModel.SN.equals(model.getSN()))
      {
        exist = true;
        break;
      }
    }

    Str = model.getDevIP();
    String Str1 = model.getDevName();
    holder.setText(R.id.device_name_view, Str).setText(R.id.device_sn_view, Str1);

    if (exist)
    {
      Str = model.getDevName() + STApplication.getInstance().getString(R.string.string_aleady_added);
      holder.setText(R.id.device_sn_view, Str);
      device_name_view.setTextColor(Color.rgb(0, 255, 0));
      device_sn_view.setTextColor(Color.rgb(0, 255, 0));
    }
    else
    {
      device_name_view.setTextColor(Color.rgb(0, 0, 0));
      device_sn_view.setTextColor(Color.rgb(0, 0, 0));
    }
  }
}
