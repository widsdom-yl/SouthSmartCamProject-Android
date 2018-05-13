package com.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ShareModel implements Parcelable {
    public String From;
    public String UID;
    public String SN;
    public int Video;
    public int History;
    public int Push;
    public int Setup;
    public int Control;
    public ShareModel(){

    }
    protected ShareModel(Parcel in) {
        From = in.readString();
        UID = in.readString();
        SN = in.readString();
        Video = in.readInt();
        History = in.readInt();
        Push = in.readInt();
        Setup = in.readInt();
        Control = in.readInt();
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
        parcel.writeInt(Video);
        parcel.writeInt(History);
        parcel.writeInt(Push);
        parcel.writeInt(Setup);
        parcel.writeInt(Control);
    }
}
