/**
  * Copyright 2018 bejson.com 
  */
package com.model;

/**
 * Auto-generated: 2018-05-21 22:26:33
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class LedStatusModel {

    private int Status;
    private int Mode;
    private Auto Auto;
    private Manual Manual;
    private Timer Timer;
    private D2D D2D;
    public LedStatusModel(){
        Mode = 1;
        Auto = new Auto();
        Manual = new Manual();
        Timer = new Timer();
        D2D = new D2D();
    }
    public void setMode(int Mode) {
         this.Mode = Mode;
     }
     public int getMode() {
         return Mode;
     }

    public void setStatus(int Status) {
        this.Status = Status;
    }
    public int getStatus() {
        return Status;
    }

    public void setAuto(Auto Auto) {
         this.Auto = Auto;
     }
     public Auto getAuto() {
         return Auto;
     }

    public void setManual(Manual Manual) {
         this.Manual = Manual;
     }
     public Manual getManual() {
         return Manual;
     }

    public void setTimer(Timer Timer) {
         this.Timer = Timer;
     }
     public Timer getTimer() {
         return Timer;
     }

    public void setD2D(D2D D2D) {
         this.D2D = D2D;
     }
     public D2D getD2D() {
         return D2D;
     }

}