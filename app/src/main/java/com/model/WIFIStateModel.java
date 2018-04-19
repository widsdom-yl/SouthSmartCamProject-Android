package com.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gyl1 on 4/7/17.
 */

public class WIFIStateModel implements Parcelable {
    public int wifi_Active;
    public int wifi_IsAPMode;
    public String wifi_SSID_AP;
    public String wifi_Password_AP;
    public String wifi_SSID_STA;
    public String wifi_Password_STA;

    public WIFIStateModel(){

    }
    protected WIFIStateModel(Parcel in) {
        wifi_Active = in.readInt();
        wifi_IsAPMode = in.readInt();
        wifi_SSID_AP = in.readString();
        wifi_Password_AP = in.readString();
        wifi_SSID_STA = in.readString();
        wifi_Password_STA = in.readString();
    }

    public static final Creator<WIFIStateModel> CREATOR = new Creator<WIFIStateModel>() {
        @Override
        public WIFIStateModel createFromParcel(Parcel in) {
            return new WIFIStateModel(in);
        }

        @Override
        public WIFIStateModel[] newArray(int size) {
            return new WIFIStateModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(wifi_Active);
        parcel.writeInt(wifi_IsAPMode);
        parcel.writeString(wifi_SSID_AP);
        parcel.writeString(wifi_Password_AP);
        parcel.writeString(wifi_SSID_STA);
        parcel.writeString(wifi_Password_STA);
    }
}
