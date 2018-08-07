package stcam.stcamproject.Application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.model.DevModel;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import cn.jpush.android.api.JPushInterface;
import io.fabric.sdk.android.Fabric;
import stcam.stcamproject.Activity.MainDevListFragment;
import stcam.stcamproject.Manager.MyContext;

public class STApplication extends Application {
    public static final String TAG = STApplication.class
            .getSimpleName();
    private static STApplication mInstance;
    //private c mFirebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());

        mInstance = this;
        if (!MyContext.isInitialized()) {
            MyContext.init(getApplicationContext());
        }
        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush
        ZXingLibrary.initDisplayOpinion(this);
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        initBackgroundCallBack();
    }
    public static synchronized STApplication getInstance() {

        return mInstance;
    }

    private void initBackgroundCallBack() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                appCount++;
                if (isRunInBackground) {
                    //应用从后台回到前台 需要做的操作
                    back2App(activity);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
                if (appCount == 0) {
                    //应用进入后台 需要做的操作
                    leaveApp(activity);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    /**
     * 从后台回到前台需要执行的逻辑
     *
     * @param activity
     */
    private void back2App(Activity activity) {
        isRunInBackground = false;

        for (DevModel model : MainDevListFragment.mDevices){

            if (!model.IsConnect())
                DevModel.threadConnect(null,model,false);
        }

    }

    /**
     * 离开应用 压入后台或者退出应用
     *
     * @param activity
     */
    private void leaveApp(Activity activity) {
        isRunInBackground = true;

        for (DevModel devModel : MainDevListFragment.mDevices){
            if (devModel.IsConnect()){
                devModel.Disconn2();//zhb devModel.Disconn();
            }
        }


    }

    public boolean getIsRunInBackground(){
        return isRunInBackground;
    }
    boolean isRunInBackground = false;
   int appCount = 0;



}
