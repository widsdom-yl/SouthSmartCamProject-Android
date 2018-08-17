package stcam.stcamproject.network;

import android.util.Log;

import com.model.DevModel;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import stcam.stcamproject.Config.Config;
import stcam.stcamproject.network.api.ServerCommandApi;

/**
 * Created by gyl1 on 11/22/16.
 */

public class ServerNetWork
{
  private static String tag = "ServerNetWork";
  private static ServerCommandApi serverCommandApi;
  private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
//            .readTimeout(READ_TIMEOUT,TimeUnit.SECONDS)//
//            .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)//
    .connectTimeout(10, TimeUnit.SECONDS)//zhb 5->10
    .build();
  ;

  private static Converter.Factory gsonConverterFactory = GsonConverterFactory.create();
  private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJavaCallAdapterFactory.create();
  //private static DevModel currentDevModel;

  //服务器返回错误类型 //zhb add
  public static final int RESULT_SUCCESS=1;//成功
  public static final int RESULT_FAIL=0;//失败
  public static final int RESULT_USER_EXISTS=-1;//用户已存在
  public static final int RESULT_USER_VERIFYCODE_ERROR=-2;//验证码错误
  public static final int RESULT_USER_VERIFYCODE_TIMEOUT=-3;//验证码超时
  public static final int RESULT_USER_ISBIND=-4;//用户已绑定
  public static final int RESULT_USER_NOTEXISTS=-5;//用户不存在
  public static final int RESULT_USER_LOGINED=-6;//已有用户登录
  public static final int RESULT_USER_LOGOUT=-7;//强制用户退出登录

  public static ServerCommandApi getCommandApi()
  {
    okHttpClient.connectTimeoutMillis();

    if (serverCommandApi == null)
    {
      Log.i(tag, "Builder retrofit before");

      Retrofit retrofit = new Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("http:" + Config.ServerIP + ":" + Config.ServerPort)
        .addCallAdapterFactory(rxJavaCallAdapterFactory)
        .addConverterFactory(gsonConverterFactory)
        .build();
      Log.i(tag, "Builder retrofit after");

      serverCommandApi = retrofit.create(ServerCommandApi.class);
    }

    return serverCommandApi;
  }
}
