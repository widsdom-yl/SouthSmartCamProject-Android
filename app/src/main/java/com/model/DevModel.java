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
        IsVideo = in.readInt();
        IsHistory = in.readInt();
        IsPush = in.readInt();
        IsSetup = in.readInt();
        IsControl = in.readInt();
        IsShare = in.readInt();
        IsRec = in.readInt();
        IsSnapshot = in.readInt();
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
        parcel.writeInt(IsVideo );
        parcel.writeInt(IsHistory);
        parcel.writeInt(IsPush);
        parcel.writeInt(IsSetup);
        parcel.writeInt(IsControl);
        parcel.writeInt(IsShare);
        parcel.writeInt(IsRec);
        parcel.writeInt(IsSnapshot);
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
    public int IsVideo;
    public int IsHistory;
    public int IsPush;
    public int IsSetup;
    public int IsControl;
    public int IsShare;
    public int IsRec;
    public int IsSnapshot;


    public DevModel()
    {
        usr = "admin";
        pwd = "admin";

    }


}
