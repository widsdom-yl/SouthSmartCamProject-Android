package stcam.stcamproject.Manager;

import cn.jpush.android.api.JPushInterface;
import stcam.stcamproject.Application.STApplication;

public class JPushManager {
    public static String getJPushRegisterID(){
        return JPushInterface.getRegistrationID(STApplication.getInstance());
    }
}
