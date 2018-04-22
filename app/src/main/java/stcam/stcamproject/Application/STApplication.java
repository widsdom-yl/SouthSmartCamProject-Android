package stcam.stcamproject.Application;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;
import stcam.stcamproject.Manager.MyContext;

public class STApplication extends Application {
    public static final String TAG = STApplication.class
            .getSimpleName();
    private static STApplication mInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        if (!MyContext.isInitialized()) {
            MyContext.init(getApplicationContext());
        }
        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush

    }
    public static synchronized STApplication getInstance() {

        return mInstance;
    }
}
