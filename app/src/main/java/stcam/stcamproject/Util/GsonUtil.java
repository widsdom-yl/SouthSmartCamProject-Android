package stcam.stcamproject.Util;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

public class GsonUtil
{
  // 将Json数据解析成相应的映射对象
  public static <T> T parseJsonWithGson(String jsonData, Class<T> type)
  {
    if (jsonData == null)
    {
      return null;
    }
    Gson gson = new Gson();
    try
    {
      T result = gson.fromJson(jsonData, type);
      return result;
    }
    catch (Exception e)
    {
      return null;
    }

  }


  public static <T> List<T> parseJsonArrayWithGson(String s, Class<T[]> clazz)
  {
    if (s == null)
    {
      return null;
    }
    T[] arr = new Gson().fromJson(s, clazz);
    return Arrays.asList(arr);
  }


}
