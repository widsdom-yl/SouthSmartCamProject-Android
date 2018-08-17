package com.model;

import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.thSDK.TMsg;
import com.thSDK.lib;

import org.json.JSONObject;

import stcam.stcamproject.Manager.DataManager;
import stcam.stcamproject.Util.TFun;

/**
 * Created by gyl1 on 12/21/16.
 */


public class DevModel implements Parcelable
{
  final static String tag = "DevModel";


  public final static int IS_CONN_NODEV = 0;
  public final static int IS_CONN_OFFLINE = 1;
  public final static int IS_CONN_LAN = 2;
  public final static int IS_CONN_DDNS = 3;
  public final static int IS_CONN_P2P = 4;
  public final static int IS_CONN_NOWAY = 5;

  public enum CONNECT_TYPE
  {
    IS_CONN_NODEV,
    IS_CONN_OFFLINE,
    IS_CONN_LAN,
    IS_CONN_DDNS,
    IS_CONN_P2P,
    IS_CONN_NOWAY
  }

  public int Index;
  public long NetHandle = 0;
  public boolean IsConnecting = false;
  public JSONObject DevCfg = null;

  public int ExistSD = 0;
  public int DevType = 0;
  public int Brightness;
  public int Contrast;
  public int Sharpness;
  public String SoftVersion = "";
  public int VideoChlMask = 0;
  public int AudioChlMask = 1;
  public int SubVideoChlMask = 1;
  public String UID;

  public String SN;
  public String DevName;
  public String DevNameChange = "";
  public String usr;
  public String pwd;
  public String ConnType;
  public String IPUID;
  public int WebPort;
  public int DataPort;

  public int IsVideo;//权限    有
  public int IsHistory;//权限  有
  public int IsPush;//权限     有
  public int IsControl;//权限  有
  public int IsSetup;//权限    未用到
  public int IsShare;//权限    未用到
  public int IsRec;//权限      未用到
  public int IsSnapshot;//权限 未用到

  public boolean IsAudioMute = true;
  public boolean IsRecord = false;

  public String GetDevName()
  {
    if (!(DevNameChange == null || DevNameChange.equals("")))
    {
      return DevNameChange;
    }
    else
    {
      return DevName;
    }
  }

  protected DevModel(Parcel in)
  {
    Index = in.readInt();
    NetHandle = in.readLong();
    IsConnecting = in.readByte() != 0;
    ExistSD = in.readInt();
    Brightness = in.readInt();
    Contrast = in.readInt();
    Sharpness = in.readInt();
    SoftVersion = in.readString();
    VideoChlMask = in.readInt();
    AudioChlMask = in.readInt();
    SubVideoChlMask = in.readInt();
    UID = in.readString();
    IsAudioMute = in.readByte() != 0;
    IsRecord = in.readByte() != 0;
    SN = in.readString();
    DevName = in.readString();
    usr = in.readString();
    pwd = in.readString();
    ConnType = in.readString();
    IPUID = in.readString();
    WebPort = in.readInt();
    DataPort = in.readInt();
    IsVideo = in.readInt();
    IsHistory = in.readInt();
    IsPush = in.readInt();
    IsSetup = in.readInt();
    IsControl = in.readInt();
    IsShare = in.readInt();
    IsRec = in.readInt();
    IsSnapshot = in.readInt();
    DevType = in.readInt();
  }

  public static final Creator<DevModel> CREATOR = new Creator<DevModel>()
  {
    @Override
    public DevModel createFromParcel(Parcel in)
    {
      return new DevModel(in);
    }

    @Override
    public DevModel[] newArray(int size)
    {
      return new DevModel[size];
    }
  };

  @Override
  public int describeContents()
  {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i)
  {
    parcel.writeInt(Index);
    parcel.writeLong(NetHandle);
    parcel.writeByte((byte) (IsConnecting ? 1 : 0));
    parcel.writeInt(ExistSD);
    parcel.writeInt(Brightness);
    parcel.writeInt(Contrast);
    parcel.writeInt(Sharpness);
    parcel.writeString(SoftVersion);
    parcel.writeInt(VideoChlMask);
    parcel.writeInt(AudioChlMask);
    parcel.writeInt(SubVideoChlMask);
    parcel.writeString(UID);
    parcel.writeByte((byte) (IsAudioMute ? 1 : 0));
    parcel.writeByte((byte) (IsRecord ? 1 : 0));
    parcel.writeString(SN);
    parcel.writeString(DevName);
    parcel.writeString(usr);
    parcel.writeString(pwd);
    parcel.writeString(ConnType);
    parcel.writeString(IPUID);
    parcel.writeInt(WebPort);
    parcel.writeInt(DataPort);
    parcel.writeInt(IsVideo);
    parcel.writeInt(IsHistory);
    parcel.writeInt(IsPush);
    parcel.writeInt(IsSetup);
    parcel.writeInt(IsControl);
    parcel.writeInt(IsShare);
    parcel.writeInt(IsRec);
    parcel.writeInt(IsSnapshot);
    parcel.writeInt(DevType);
  }

  /*==============================================*/


  public CONNECT_TYPE getConnectType()
  {
    if (ConnType == null)
    {
      return CONNECT_TYPE.IS_CONN_LAN;
    }
    if (ConnType.equals("IS_CONN_NODEV"))
    {
      return CONNECT_TYPE.IS_CONN_NODEV;
    }
    else if (ConnType.equals("IS_CONN_OFFLINE"))
    {
      return CONNECT_TYPE.IS_CONN_OFFLINE;
    }
    else if (ConnType.equals("IS_CONN_LAN"))
    {
      return CONNECT_TYPE.IS_CONN_LAN;
    }
    else if (ConnType.equals("IS_CONN_DDNS"))
    {
      return CONNECT_TYPE.IS_CONN_DDNS;
    }
    else if (ConnType.equals("IS_CONN_P2P"))
    {
      return CONNECT_TYPE.IS_CONN_P2P;
    }
    else if (ConnType.equals("IS_CONN_NOWAY"))
    {
      return CONNECT_TYPE.IS_CONN_NOWAY;
    }
    return CONNECT_TYPE.IS_CONN_NOWAY;
  }

  public String getConnectTypeDesc()
  {
    CONNECT_TYPE type = getConnectType();
    switch (type)
    {
      case IS_CONN_LAN:
        return "LAN";
      case IS_CONN_DDNS:
        return "DDNS";
      case IS_CONN_P2P:
        return "P2P";
      default:
        return "OFFLINE";
    }
  }

  public DevModel()
  {
    usr = "admin";
    pwd = "admin";
  }

  public void updateUserAndPwd()
  {
    DevModel dbModel = DataManager.getInstance().getSNDev(SN);
    if (dbModel != null)
    {
      usr = dbModel.usr;
      pwd = dbModel.pwd;
      Log.e(tag, "SN:" + SN + ",pwd:" + pwd);
    }
  }

  public String GetAllCfg()
  {
    return lib.thNetGetAllCfg(NetHandle);
  }

  public boolean Disconn()
  {
    new Thread()
    {
      @Override
      public void run()
      {
        boolean ret = lib.thNetDisConn(NetHandle);
        if (ret)
        {
          lib.thNetFree(NetHandle);
          NetHandle = 0;
        }

      }
    }.start();
    return true;
    //return lib.thNetDisConn(NetHandle);

  }

  public boolean IsConnect()
  {
    boolean ret = false;
    if (NetHandle == 0)
    {
      return ret;
    }
    ret = lib.thNetIsConnect(NetHandle);
    TFun.printf(SN + "(" + IPUID + ") thNetIsConnect:" + ret);
    return ret;
  }

  public boolean Connect()
  {
    boolean ret;
    Log.e(tag, "Connect:NetHandle:" + NetHandle + ",usr:" + usr + ",pwd:" + pwd + ",IPUID:" + IPUID + ",DataPort:" + DataPort);
    if (IPUID == null || IPUID.length() <= 0)
    {
      return false;
    }

    if (IsConnecting)
    {
      return false;
    }
    IsConnecting = true;
    //Log.e(tag,"NetHandle:"+NetHandle+",usr:"+usr+",pwd:"+pwd+",IPUID:"+IPUID+",DataPort:"+DataPort);
    ret = lib.thNetConnect(NetHandle, usr, pwd, IPUID, DataPort, 10 * 1000);
    if (ret)
    {

    }
    IsConnecting = false;
    return ret;
  }

  public static void threadConnect(final Handler ipc, final DevModel tmpNode)
  {
    DevModel dbModel = DataManager.getInstance().getSNDev(tmpNode.SN);
    if (dbModel != null)
    {

      tmpNode.usr = dbModel.usr;
      tmpNode.pwd = dbModel.pwd;
      Log.e(tag, "SN:" + tmpNode.SN + ",pwd:" + tmpNode.pwd);
    }

    new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          if (tmpNode.NetHandle == 0)
          {
            tmpNode.NetHandle = lib.thNetInit(
              true,
              false,
              false,
              false,
              tmpNode.SN
            );
            if (tmpNode.NetHandle > 0)
            {
              lib.thManageAddDevice(tmpNode.SN, tmpNode.NetHandle);
            }
          }

          if (!tmpNode.IsConnect())
          {
            tmpNode.Connect();
          }

          if (tmpNode.IsConnect())
          {
            if (ipc != null)
            {
              ipc.sendMessage(Message.obtain(ipc, TMsg.Msg_NetConnSucceed, tmpNode.Index, 0, tmpNode));
            }
            String tmpStr = tmpNode.GetAllCfg();
            //Log.e("java", "tmpStr is :" + tmpStr);
            JSONObject json = new JSONObject(tmpStr);
            tmpNode.DevCfg = json;
            tmpNode.ExistSD = json.getJSONObject("DevInfo").getInt("ExistSD");
            tmpNode.DevType= json.getJSONObject("DevInfo").getInt("DevType");
            tmpNode.Brightness = json.getJSONObject("Video").getInt("Brightness");
            tmpNode.Contrast = json.getJSONObject("Video").getInt("Contrast");
            tmpNode.Sharpness = json.getJSONObject("Video").getInt("Sharpness");
            tmpNode.UID = json.getJSONObject("P2P").getString("P2P_UID");
            tmpNode.DevName = json.getJSONObject("DevInfo").getString("DevName");
            tmpNode.SoftVersion = json.getJSONObject("DevInfo").getString("SoftVersion");
            Log.e("java", "SoftVersion is :" + tmpNode.SoftVersion + ",uid is " + tmpNode.UID);
          }
          else
          {
            if (ipc != null)
            {
              ipc.sendMessage(Message.obtain(ipc, TMsg.Msg_NetConnFail, tmpNode.Index, 0, tmpNode));
            }
            return;
          }
        }
        catch (Exception e)
        {
          return;
        }
      }
    }.start();
  }

  public boolean Play(int sub)
  {
    SubVideoChlMask = sub;
    VideoChlMask = 1 - sub;
    TFun.printf("Play:NetHandle:" + NetHandle);
    return lib.thNetPlay(NetHandle, VideoChlMask, AudioChlMask, SubVideoChlMask);
  }

  public boolean Stop()
  {
    return lib.thNetStop(NetHandle);
  }

  public static void threadStartPlay(final Handler ipc, final DevModel DevNode)
  {
    new Thread()
    {
      @Override
      public void run()
      {
        DevNode.Play(1);

        lib.thNetAudioPlayOpen(DevNode.NetHandle);
        DevNode.IsAudioMute = true;
        lib.thNetSetAudioIsMute(DevNode.NetHandle, DevNode.IsAudioMute);
      }
    }.start();
  }

  public static void threadStopPlay(final Handler ipc, final DevModel DevNode)
  {
    new Thread()
    {
      @Override
      public void run()
      {
        DevNode.Stop();

        lib.thNetTalkClose(DevNode.NetHandle);

        if (DevNode.IsRecord)
        {
          DevNode.IsRecord = false;
          lib.thNetStopRec(DevNode.NetHandle);
        }
        lib.thNetAudioPlayClose(DevNode.NetHandle);
      }
    }.start();
  }

  /*获取包含用户名和密码啊的cfg1 url*/
  public String getHttpCfg1UsrPwd()
  {
    return "http://" + IPUID + ":" + WebPort + "/cfg1.cgi?User=admin&Psd=" + pwd;
  }

}
