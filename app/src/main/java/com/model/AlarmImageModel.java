package com.model;

import java.io.Serializable;

public class AlarmImageModel implements Serializable {
    public int ID;
    public int AlmType;
    public String AlmName;
    public String Img;
    public String AlmTime;
    public String DevName;
//    "ID": 4431,
//            "SN": "80001035",
//            "AlmType": 1,
//            "AlmName": "移动警报",
//            "AlmTime": "2018-07-11 13:37:11",
//            "Img": ""
    public String getAlarmDescribe(){
        switch (AlmType){
            case 0:
                return "";
            case 1:
                return "移动警报";
            case 2:
                return "传感器警报";
            case 3:
                return "声音警报";
            case 4:
                return "网络短线";
            case 5:
                return "网络重连";
            case 6:
                return "磁盘空间满";
            case 7:
                return "视频遮挡";
            case 8:
                return "视频丢失";
            case 9:
                return "门铃出发";
            case 10:
                return "其他警报";
            default:
                return "其他警报";

        }

    }
}
