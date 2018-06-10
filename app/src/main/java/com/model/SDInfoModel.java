package com.model;

import java.text.NumberFormat;

public class SDInfoModel {
    public String DiskName;
    public int DiskSize;
    public int FreeSize;
    public int MinFreeSize;
    public String getTotalSize(){
        if (DiskSize > 1024){
            Double size = (float)DiskSize/1024.0;
            NumberFormat format = NumberFormat.getNumberInstance() ;
            format.setMaximumFractionDigits(1);
            String result = format.format(size) ;
            return result+"G";

        }
        else{
            return ""+DiskSize+"M";
        }
    }
    public String getRecordSize(){
        if (DiskSize > 1024){
            Double size = (float)DiskSize/1024.0;
            NumberFormat format = NumberFormat.getNumberInstance() ;
            format.setMaximumFractionDigits(1);
            String result = format.format(size) ;
            return result+"G";

        }
        else{
            return ""+DiskSize+"M";
        }
    }
    public String getFreeSize(){
        if (FreeSize > 1024){
            Double size = (float)FreeSize/1024.0;
            NumberFormat format = NumberFormat.getNumberInstance() ;
            format.setMaximumFractionDigits(1);
            String result = format.format(size) ;
            return result+"G";

        }
        else{
            return ""+FreeSize+"M";
        }
    }
}
