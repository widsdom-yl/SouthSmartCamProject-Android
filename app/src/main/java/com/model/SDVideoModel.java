package com.model;

import java.io.Serializable;

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
    public String getsdVideoUrl(DevModel model){
        return "http://"+model.IPUID+":"+model.WebPort+""+sdVideo;
    }
    public String getSdVideoName(){
        if (sdVideo.contains("aaaa")){
            return "aaaa.mp4";
        }
        else{
            String[] array = sdVideo.split("/");
            return array[array.length-1];
        }

    }
    public boolean isSDVideo(){
        return sdVideo.contains("mp4");
    }
}
