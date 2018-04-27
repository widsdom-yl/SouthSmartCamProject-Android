package stcam.stcamproject.Util;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

class GsonUtil {
     // 将Json数据解析成相应的映射对象
           public static <T> T parseJsonWithGson(String jsonData, Class<T> type) {
                 Gson gson = new Gson();
                 T result = gson.fromJson(jsonData, type);
                 return result;
            }



    public static <T> List<T> parseJsonArrayWithGson(String s, Class<T[]> clazz) {
        T[] arr = new Gson().fromJson(s, clazz);
        return Arrays.asList(arr);
    }


 }
