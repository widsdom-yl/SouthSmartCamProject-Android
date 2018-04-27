package stcam.stcamproject.Util;

import android.util.Log;

import com.model.SearchDevModel;

import java.util.List;

public class DeviceParseUtil {
    private static  final String tag = "DeviceParseUtil";
    public  static List<SearchDevModel> parseSearchMsg(String json){
        try {
            List<SearchDevModel> lists = GsonUtil.parseJsonArrayWithGson(json,
                    SearchDevModel[].class);
            return lists;
        }
        catch (Exception e){
            Log.e(tag,e.getLocalizedMessage());
            return null;
        }

    }
}
