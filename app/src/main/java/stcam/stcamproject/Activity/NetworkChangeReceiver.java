package stcam.stcamproject.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.thSDK.lib;

import stcam.stcamproject.Application.STApplication;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

class NetworkChangeReceiver extends BroadcastReceiver {
    OnNetWorkBreakListener mNetWorkBreakListener;
    public void setNetWorkBreakListener(OnNetWorkBreakListener netWorkBreakListener){
        mNetWorkBreakListener  = netWorkBreakListener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            if (STApplication.getInstance().getIsRunInBackground()){
                return;
            }
            switch (networkInfo.getType()) {
                case TYPE_MOBILE:
                    Toast.makeText(context, "正在使用2G/3G/4G网络", Toast.LENGTH_SHORT).show();
                    //
                    if (mNetWorkBreakListener != null){
                        mNetWorkBreakListener.OnNetWorkBreakListener();
                    }
                    lib.P2PFree();
                    lib.P2PInit();
                    if (mNetWorkBreakListener != null){
                        mNetWorkBreakListener.OnNetWorkChangeListener(0);
                    }
                    break;
                case TYPE_WIFI:
                    if (mNetWorkBreakListener != null){
                        mNetWorkBreakListener.OnNetWorkBreakListener();
                    }
                    lib.P2PFree();
                    lib.P2PInit();
                    if (mNetWorkBreakListener != null){
                        mNetWorkBreakListener.OnNetWorkChangeListener(1);
                    }
                    Toast.makeText(context, "正在使用wifi上网", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        } else {
            //断开所有连接
            Toast.makeText(context, "当前无网络连接", Toast.LENGTH_SHORT).show();
            if (mNetWorkBreakListener != null){
                mNetWorkBreakListener.OnNetWorkBreakListener();
            }
        }
    }
    public interface OnNetWorkBreakListener{
        //监听网络断开
        void OnNetWorkBreakListener();
        void OnNetWorkChangeListener(int type);//0-手机网络，1-wlan
    }
}

