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
import stcam.stcamproject.network.api.CommandApi;

/**
 * Created by gyl1 on 11/22/16.
 */

public class Network {
    private static String  tag = "Network";
    private static CommandApi commandApi;
    private static OkHttpClient okHttpClient =  new OkHttpClient.Builder()
//            .readTimeout(READ_TIMEOUT,TimeUnit.SECONDS)//
//            .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)//
            .connectTimeout(2, TimeUnit.SECONDS)//
            .build();  ;

    private static Converter.Factory gsonConverterFactory = GsonConverterFactory.create();
    private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJavaCallAdapterFactory.create();
    private static DevModel currentDevModel;
    public static CommandApi getCommandApi(DevModel model) {
        okHttpClient.connectTimeoutMillis();

        if (commandApi == null) {
            currentDevModel = model;
            Log.i(tag,"Builder retrofit before");

            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl("http:"+model.IPUID+":"+model.WebPort)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .addConverterFactory(gsonConverterFactory)
                    .build();
            Log.i(tag,"Builder retrofit after");

            commandApi = retrofit.create(CommandApi.class);
        }
        else if(!currentDevModel.IPUID.equals(model.IPUID) || currentDevModel.WebPort == model.WebPort){
            currentDevModel = model;
            Log.i(tag,"Builder retrofit before");
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl("http:"+model.IPUID+":"+model.WebPort)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .addConverterFactory(gsonConverterFactory)
                    .build();
            Log.i(tag,"Builder retrofit after");

            commandApi = retrofit.create(CommandApi.class);
        }
        return commandApi;
    }
}
