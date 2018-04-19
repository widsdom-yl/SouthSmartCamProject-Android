package com.model;

/**
 * Created by gyl1 on 3/30/17.
 */

public class SnapShotConfigModel {
    public  int Action;
    public  int TotalTime;
    public  int IntervalTime;
    public String toMString(){
        return "SnapShotConfigModel:Action:"+Action+",TotalTime:"+TotalTime+",IntervalTime:"+IntervalTime;
    }
}
