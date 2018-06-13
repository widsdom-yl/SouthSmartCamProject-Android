package stcam.stcamproject.Application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import cn.jpush.android.api.JPushInterface;
import io.fabric.sdk.android.Fabric;
import stcam.stcamproject.Manager.MyContext;

public class STApplication extends Application {
    public static final String TAG = STApplication.class
            .getSimpleName();
    private static STApplication mInstance;
    //private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        mInstance = this;
        if (!MyContext.isInitialized()) {
            MyContext.init(getApplicationContext());
        }
        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush
        ZXingLibrary.initDisplayOpinion(this);
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
    public static synchronized STApplication getInstance() {

        return mInstance;
    }
}
