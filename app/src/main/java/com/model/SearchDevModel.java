package com.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2018-04-27 23:21:10
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class SearchDevModel implements Parcelable {

    private String SN;
    private String DevModal;
    private String DevName;
    private String DevMAC;
    private String DevIP;
    private String SubMask;
    private String Gateway;
    private String DNS1;
    private String SoftVersion;
    private int DataPort;
    private int HttpPort;
    private int rtspPort;
    private String DDNSServer;
    private String DDNSHost;
    //private String UID;

    protected SearchDevModel(Parcel in) {
        SN = in.readString();
        DevModal = in.readString();
        DevName = in.readString();
        DevMAC = in.readString();
        DevIP = in.readString();
        SubMask = in.readString();
        Gateway = in.readString();
        DNS1 = in.readString();
        SoftVersion = in.readString();
        DataPort = in.readInt();
        HttpPort = in.readInt();
        rtspPort = in.readInt();
        DDNSServer = in.readString();
        DDNSHost = in.readString();
        //UID = in.readString();
    }

    public static final Creator<SearchDevModel> CREATOR = new Creator<SearchDevModel>() {
        @Override
        public SearchDevModel createFromParcel(Parcel in) {
            return new SearchDevModel(in);
        }

        @Override
        public SearchDevModel[] newArray(int size) {
            return new SearchDevModel[size];
        }
    };

    public void setSN(String SN) {
        this.SN = SN;
    }
    public String getSN() {
        return SN;
    }

    public void setDevModal(String DevModal) {
        this.DevModal = DevModal;
    }
    public String getDevModal() {
        return DevModal;
    }

    public void setDevName(String DevName) {
        this.DevName = DevName;
    }
    public String getDevName() {
        return DevName;
    }

    public void setDevMAC(String DevMAC) {
        this.DevMAC = DevMAC;
    }
    public String getDevMAC() {
        return DevMAC;
    }

    public void setDevIP(String DevIP) {
        this.DevIP = DevIP;
    }
    public String getDevIP() {
        return DevIP;
    }

    public void setSubMask(String SubMask) {
        this.SubMask = SubMask;
    }
    public String getSubMask() {
        return SubMask;
    }

    public void setGateway(String Gateway) {
        this.Gateway = Gateway;
    }
    public String getGateway() {
        return Gateway;
    }

    public void setDNS1(String DNS1) {
        this.DNS1 = DNS1;
    }
    public String getDNS1() {
        return DNS1;
    }

    public void setSoftVersion(String SoftVersion) {
        this.SoftVersion = SoftVersion;
    }
    public String getSoftVersion() {
        return SoftVersion;
    }

    public void setDataPort(int DataPort) {
        this.DataPort = DataPort;
    }
    public int getDataPort() {
        return DataPort;
    }

    public void setHttpPort(int HttpPort) {
        this.HttpPort = HttpPort;
    }
    public int getHttpPort() {
        return HttpPort;
    }

    public void setRtspPort(int rtspPort) {
        this.rtspPort = rtspPort;
    }
    public int getRtspPort() {
        return rtspPort;
    }

    public void setDDNSServer(String DDNSServer) {
        this.DDNSServer = DDNSServer;
    }
    public String getDDNSServer() {
        return DDNSServer;
    }

    public void setDDNSHost(String DDNSHost) {
        this.DDNSHost = DDNSHost;
    }
    public String getDDNSHost() {
        return DDNSHost;
    }

    //public void setUID(String UID) {
    //    this.UID = UID;
   // }
    ////public String getUID() {
    //    return UID;
    //}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(SN);
        parcel.writeString(DevModal);
        parcel.writeString(DevName);
        parcel.writeString(DevMAC);
        parcel.writeString(DevIP);
        parcel.writeString(SubMask);
        parcel.writeString(Gateway);
        parcel.writeString(DNS1);
        parcel.writeString(SoftVersion);
        parcel.writeInt(DataPort);
        parcel.writeInt(HttpPort);
        parcel.writeInt(rtspPort);
        parcel.writeString(DDNSServer);
        parcel.writeString(DDNSHost);
        //parcel.writeString(UID);
    }
/*
* 根据searchmodel 构造DevModel
* */
    public DevModel exportDevModelForm(){
        if (devModel == null){
            devModel = new DevModel();
        }



        devModel.SN = this.getSN();
        //devModel.UID = this.getUID();
       // if (devModel.UID.equals("NULL") || devModel.UID == null){
       //     devModel.UID = DevName;
       // }
        devModel.IPUID = this.getDevIP();
        devModel.DataPort = this.getDataPort();
        devModel.WebPort = this.getHttpPort();
        devModel.DevName = this.getDevName();
        return devModel;
    }
    DevModel devModel;
}