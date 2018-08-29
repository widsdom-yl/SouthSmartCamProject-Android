package stcam.stcamproject.Adapter;

import android.view.View;
import android.widget.TextView;

import com.model.DevModel;
import com.model.PushSettingModel;

import java.util.List;

import stcam.stcamproject.Config.Config;
import stcam.stcamproject.R;

public class DeviceSettingAdapter extends BaseAdapter<String>
{
  DevModel devModel;
  int MD_Sensitive = -1;
  PushSettingModel mPushSettingModel;

  public DeviceSettingAdapter(List<String> list)
  {
    super(R.layout.list_setting, list);
  }


  /*
  *  items.add(getString(R.string.device_name));
      items.add(getString(R.string.action_device_pwd));
      items.add(getString(R.string.action_push));
      items.add(getString(R.string.string_DevAdvancedSettings));
      items.add(getString(R.string.string_DevAdvancedSettings_TFManage));
//        items.add(getString(R.string.string_DevAdvancedSettings_MotionSensitivity));
      items.add(getString(R.string.action_version));
      */
  public void setDevModel(DevModel devModel)
  {
    this.devModel = devModel;
  }

  public void setMD_Sensitive(int MD_Sensitive)
  {
    this.MD_Sensitive = MD_Sensitive;
  }

  public void setmPushSettingModel(PushSettingModel mPushSettingModel)
  {
    this.mPushSettingModel = mPushSettingModel;
    this.notifyDataSetChanged();
  }

  protected void convert(BaseHolder holder, String title, int position)
  {
    super.convert(holder, title, position);
    holder.setText(R.id.title_text, title);
    String Str = "";
    if (devModel != null)
    {
      if (0 == position)//设备设置
      {
        Str = devModel.GetDevName() + Config.StrOnNextLevel;
      }
      else if (1 == position)//密码管理
      {
        int length = devModel.pwd.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
          sb.append("*");
        }
        Str = sb.toString() + Config.StrOnNextLevel;
      }
      else if (2 == position)//推送开关
      {
        if (mPushSettingModel != null)
        {
          Str = mPushSettingModel.getPushActiveDesc() + Config.StrOnNextLevel;
        }
      }
      else if (3 == position)//高级设置
      {
        Str = Config.StrOnNextLevel;
      }
      else if (4 == position)//设备版本
      {
        Str = devModel.SoftVersion + Config.StrOnNextLevel;
      }
      holder.setText(R.id.detail_text, Str);
    }

  }
}