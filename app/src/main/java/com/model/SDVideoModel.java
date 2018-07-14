package com.model;

import java.io.Serializable;
import java.text.NumberFormat;

/**
 * Created by gyl1 on 3/30/17.
 */

public class SDVideoModel implements Serializable {
    public String sdVideo;
    public int FileSize;
    public String url;
    public boolean checked;
    public boolean viewed;
    public SDVideoModel()
    {
        checked=false;
        viewed = false;
    }
    public String getSdVideoName(){
        String[]array = sdVideo.split("/");
        return array[array.length-1];
    }



    public String getFileSizeDesc(){
        int kb = FileSize/1024;

        if (kb/1024 > 1024){
            Double size = (float)kb/1024.0/1024.0;
            NumberFormat format = NumberFormat.getNumberInstance() ;
            format.setMaximumFractionDigits(1);
            String result = format.format(size) ;
            return result+"G";

        }
        else if (kb > 1024){
            //return ""+FileSize/1024.0+"M";

            Double size = (float)kb/1024.0;
            NumberFormat format = NumberFormat.getNumberInstance() ;
            format.setMaximumFractionDigits(1);
            String result = format.format(size) ;
            return result+"M";
        }
        else{

            return kb+"K";
        }
    }

}
