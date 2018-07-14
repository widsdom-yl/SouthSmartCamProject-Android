package com.model;

public class RecConfigModel {
    private int Rec_RecStyle;
    private int Rec_IsRecAudio;
    private int Rec_RecStreamType;
    private int Rec_AlmTimeLen;
    private int Rec_NmlTimeLen;
    public void setRec_RecStyle(int Rec_RecStyle) {
        this.Rec_RecStyle = Rec_RecStyle;
    }
    public int getRec_RecStyle() {
        return Rec_RecStyle;
    }

    public void setRec_IsRecAudio(int Rec_IsRecAudio) {
        this.Rec_IsRecAudio = Rec_IsRecAudio;
    }
    public int getRec_IsRecAudio() {
        return Rec_IsRecAudio;
    }

    public void setRec_RecStreamType(int Rec_RecStreamType) {
        this.Rec_RecStreamType = Rec_RecStreamType;
    }
    public int getRec_RecStreamType() {
        return Rec_RecStreamType;
    }

    public void setRec_AlmTimeLen(int Rec_AlmTimeLen) {
        this.Rec_AlmTimeLen = Rec_AlmTimeLen;
    }
    public int getRec_AlmTimeLen() {
        return Rec_AlmTimeLen;
    }

    public void setRec_NmlTimeLen(int Rec_NmlTimeLen) {
        this.Rec_NmlTimeLen = Rec_NmlTimeLen;
    }
    public int getRec_NmlTimeLen() {
        return Rec_NmlTimeLen;
    }

    public void setRec_AlmTimeLenChoice(int choice){
        switch (choice){
            case 0:
                Rec_AlmTimeLen = 5;
                break;
            case 1:
                Rec_AlmTimeLen = 10;
                break;
            case 2:
                Rec_AlmTimeLen = 20;
                break;
            case 3:
                Rec_AlmTimeLen = 30;
                break;
            case 4:
                Rec_AlmTimeLen = 60;
                break;
            default:
                break;
        }
    }
}
