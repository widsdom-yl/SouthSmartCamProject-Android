package com.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ShareModel implements Parcelable {
//"IsVideo":%d,"IsHistory":%d,"IsPush":%d,"IsSetup":%d,"IsControl":%d,"IsShare":%d,"IsRec":%d,"IsSnapshot":%d
    public String From;
    public String UID;
    public String SN;
    public String Pwd;
    public int IsVideo;
    public int IsHistory;
    public int IsPush;
    public int IsSetup;
    public int IsControl;
    public ShareModel(){

    }
    protected ShareModel(Parcel in) {
        From = in.readString();
        UID = in.readString();
        SN = in.readString();
        Pwd = in.readString();
        IsVideo = in.readInt();
        IsHistory = in.readInt();
        IsPush = in.readInt();
        IsSetup = in.readInt();
        IsControl = in.readInt();
    }

    public static final Creator<ShareModel> CREATOR = new Creator<ShareModel>() {
        @Override
        public ShareModel createFromParcel(Parcel in) {
            return new ShareModel(in);
        }

        @Override
        public ShareModel[] newArray(int size) {
            return new ShareModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(From);
        parcel.writeString(UID);
        parcel.writeString(SN);
        parcel.writeString(Pwd);
        parcel.writeInt(IsVideo);
        parcel.writeInt(IsHistory);
        parcel.writeInt(IsPush);
        parcel.writeInt(IsSetup);
        parcel.writeInt(IsControl);
    }
}
