package com.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gyl1 on 12/21/16.
 */



public class DevModel implements Parcelable {

    protected DevModel(Parcel in) {
        SN = in.readString();
        DevName = in.readString();
        usr = in.readString();
        pwd = in.readString();
        ConnType = in.readString();
        IPUID = in.readString();
        WebPort = in.readInt();
        DataPort = in.readInt();
        IsVideo = in.readByte() != 0;
        IsHistory = in.readByte() != 0;
        IsPush = in.readByte() != 0;
        IsSetup = in.readByte() != 0;
        IsControl = in.readByte() != 0;
        IsShare = in.readByte() != 0;
        IsRec = in.readByte() != 0;
        IsSnapshot = in.readByte() != 0;
    }

    public static final Creator<DevModel> CREATOR = new Creator<DevModel>() {
        @Override
        public DevModel createFromParcel(Parcel in) {
            return new DevModel(in);
        }

        @Override
        public DevModel[] newArray(int size) {
            return new DevModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(SN);
        parcel.writeString(DevName);
        parcel.writeString(usr);
        parcel.writeString(pwd);
        parcel.writeString(ConnType);
        parcel.writeString(IPUID);
        parcel.writeInt(WebPort);
        parcel.writeInt(DataPort);
        parcel.writeByte((byte) (IsVideo ? 1 : 0));
        parcel.writeByte((byte) (IsHistory ? 1 : 0));
        parcel.writeByte((byte) (IsPush ? 1 : 0));
        parcel.writeByte((byte) (IsSetup ? 1 : 0));
        parcel.writeByte((byte) (IsControl ? 1 : 0));
        parcel.writeByte((byte) (IsShare ? 1 : 0));
        parcel.writeByte((byte) (IsRec ? 1 : 0));
        parcel.writeByte((byte) (IsSnapshot ? 1 : 0));
    }

    public enum EnumOnlineState {
        Unkown,Online,Offline
    }


    public String SN;
    public String DevName;
    public String usr;
    public String pwd;
    public String ConnType;
    public String IPUID;
    public int WebPort;
    public int DataPort;
    public boolean IsVideo;
    public boolean IsHistory;
    public boolean IsPush;
    public boolean IsSetup;
    public boolean IsControl;
    public boolean IsShare;
    public boolean IsRec;
    public boolean IsSnapshot;


    public DevModel()
    {
        usr = "admin";
        pwd = "admin";

    }


}
