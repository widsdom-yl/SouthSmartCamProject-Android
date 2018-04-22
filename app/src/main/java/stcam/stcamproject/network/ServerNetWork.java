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

public class ServerNetWork {
    private static String  tag = "ServerNetWork";
    private static ServerCommandApi serverCommandApi;
    private static OkHttpClient okHttpClient =  new OkHttpClient.Builder()
//            .readTimeout(READ_TIMEOUT,TimeUnit.SECONDS)//
//            .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)//
            .connectTimeout(5, TimeUnit.SECONDS)//
            .build();  ;

    private static Converter.Factory gsonConverterFactory = GsonConverterFactory.create();
    private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJavaCallAdapterFactory.create();
    private static DevModel currentDevModel;
    public static ServerCommandApi getCommandApi() {
        okHttpClient.connectTimeoutMillis();

        if (serverCommandApi == null) {

            Log.i(tag,"Builder retrofit before");

            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl("http:"+ Config.ServerIP+":"+Config.ServerPort)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .addConverterFactory(gsonConverterFactory)
                    .build();
            Log.i(tag,"Builder retrofit after");

            serverCommandApi = retrofit.create(ServerCommandApi.class);
        }

        return serverCommandApi;
    }
}
