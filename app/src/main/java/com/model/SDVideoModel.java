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
    public String getSdVideoName(){
        String[]array = sdVideo.split("/");
        return array[array.length-1];
    }

}
