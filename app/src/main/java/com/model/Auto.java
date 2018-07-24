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
public class Auto {

    private int Delay;
    private int Lux;
    private int Brightness;
    Auto(){
        Brightness = 10;
        Delay = 20;
    }
    public void setDelay(int Delay) {
         this.Delay = Delay;
     }
     public int getDelay() {
         return Delay;
     }

    public void setLux(int Lux) {
         this.Lux = Lux;
     }
     public int getLux() {
         return Lux;
     }
    public void setBrightness(int Brightness) {
        this.Brightness = Brightness;
    }
    public int getBrightness() {
        return Brightness;
    }

}