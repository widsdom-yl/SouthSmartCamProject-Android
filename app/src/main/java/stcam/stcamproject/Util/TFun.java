package stcam.stcamproject.Util;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

@SuppressLint("NewApi")
public class TFun extends Activity
{
    public static void Vibrate(Activity activity, long milliseconds)
    {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    public static String getMaxCpuFreq()
    {
        String result = "";
        ProcessBuilder cmd;
        try
        {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1)
            {
                result = result + new String(re);
            }
            in.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    public static String getMinCpuFreq()
    {
        String result = "";
        ProcessBuilder cmd;
        try
        {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1)
            {
                result = result + new String(re);
            }
            in.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    public static String getCurCpuFreq()
    {
        String result = "N/A";
        try
        {
            FileReader fr = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static String getCpuName()
    {
        try
        {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++)
            {
            }
            return array[1];
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private final static String TAG = "jni";

    /**
     * 获取SD卡状态
     *
     * @return true - SD卡已安装，false - SD卡未安装
     */
    public static boolean checkSD()
    {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录路径
     *
     * @return 表示根目录路径字符串（末尾不带“/”，如"/sdcard"),若SD卡未安装，则返回null
     */
    public static String getSDRootPath()
    {
        if (!checkSD())
        {
            return null;
        }
        else
        {
            return Environment.getExternalStorageDirectory().getPath();
        }
    }

    /**
     * 创建目录
     *
     * @param dirPath 目录路径（末尾不带“/”，如"/sdcard"）
     * @return true - 创建成功，false - 创建失败
     */
    public static boolean makeDir(String dirPath)
    {
        // createPath(Environment.getExternalStorageDirectory().getAbsolutePath()+
        // "/record/");
        boolean isSuccessful = true;
        File dir = new File(dirPath);
        if (!dir.exists())
        {
            isSuccessful = dir.mkdirs();
        }
        return isSuccessful;
    }

    /**
     * 以jpg格式保存Bitmap
     *
     * @param filePath 完整的文件路径（含路径、扩展名）
     * @param bitmap
     * @return true - 保存成功， false - 保存失败
     */
    public static boolean saveBitmapInJPG(String filePath, Bitmap bitmap)
    {
        File f = new File(filePath);
        if (f.exists())
        {
            return true;
        }
        try
        {
            f.createNewFile();
            FileOutputStream fOut = null;
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static class CompratorByLastModified implements Comparator<File>
    {
        public int compare(File f1, File f2)
        {
            long diff = f1.lastModified() - f2.lastModified();
            if (diff > 0)
            {
                return -1;
            }
            else if (diff == 0)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }

    /**
     * 获取图片缩略图
     *
     * @param path   图片的完整路径
     * @param width  缩略图宽，单位：像素
     * @param height 缩略图高，单位：像素
     * @return 图片缩略图的Bitmap，若出现异常，返回null
     */
    public static Bitmap getThumbnail(String path, int width, int height)
    {
        Bitmap bitmap = null;
        Bitmap thumbnail = null;

        try
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            bitmap = BitmapFactory.decodeFile(path, options);
            thumbnail = ThumbnailUtils.extractThumbnail(bitmap, width, height);
            bitmap.recycle();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return thumbnail;
    }

    /**
     * 删除指定路径下的所有文件
     *
     * @param path 指定的路径
     */
    public static void delFiles(String path)
    {
        File file = new File(path);
        if (!file.exists())
        {
            return;
        }
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            files[i].delete();
        }
    }

//    public static String httpGet(String url)
//    {
//        String msg = null;
//        try
//        {
//            HttpGet request = new HttpGet(url);
//            TFun.printf(url);
//            HttpClient client = new DefaultHttpClient();
//            client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
//            HttpResponse response = client.execute(request);
//            if (response.getStatusLine().getStatusCode() == 200)
//            {
//                // msg = EntityUtils.toString(response.getEntity(), "gb2312");
//                // msg = EntityUtils.toString(response.getEntity()).trim();
//                msg = EntityUtils.toString(response.getEntity());
//            }
//        }
//        catch (Exception e)
//        {
//            TFun.printf("httpGet:" + url);
//            return null;
//        }
//        return msg;
//    }

    /**
     * 通过http协议获取Bitmap
     *
     * @param url 需包含协议名“http://”
     * @return 服务器返回的Bitmap，若出错，返回null
     */
//    public static Bitmap httpGetBmp(String url)
//    {
//        HttpGet request = new HttpGet(url);
//        TFun.printf(url);
//        Bitmap bitmap = null;
//        try
//        {
//            HttpResponse response = new DefaultHttpClient().execute(request);
//            if (response.getStatusLine().getStatusCode() == 200)
//            {
//                byte[] picBytes = EntityUtils.toByteArray(response.getEntity());
//                bitmap = BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
//            }
//        }
//        catch (Exception e)
//        {
//            TFun.printf("httpGetBmp" + url);
//            return null;
//        }
//        return bitmap;
//    }

    /**
     * 从保存的报警图片文件名中取出时间
     *
     * @param str
     * @return 时间字符串
     */
    public static String getTimeFromFileName(String str)
    {
        if (str == null)
        {
            return "Null File Name";
        }
        String result;
        int startPos = str.indexOf("_");
        int endPos = str.lastIndexOf(".");
        startPos++;
        try
        {
            result = str.substring(startPos, endPos);
        }
        catch (IndexOutOfBoundsException e)
        {
            result = "No Time Info";
        }
        return result;
    }

    public static void printf(String Msg)
    {
        Log.e(TAG, Msg);
    }

    static final int MBCONN_NO = 0;
    static final int MBCONN_WIFI = 1;
    static final int MBCONN_MOBILE = 2;
    static final int MBCONN_NETWORK = 3;
    static final int MBCONN_OTHER = 4;

    public static int getNetConnType(Context context)
    {
        // Context context = act.getApplicationContext();
        try
        {
            final ConnectivityManager Mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Mgr == null)
            {
                return MBCONN_NO;
            }
            final NetworkInfo Info = Mgr.getActiveNetworkInfo();
            if (Info == null)
            {
                return MBCONN_NO;
            }
            if (!Info.isConnected())
            {
                return MBCONN_NO;
            }
            if (Info.getState() == NetworkInfo.State.CONNECTED)
            {
                if (Info.getType() == ConnectivityManager.TYPE_WIFI)
                {
                    return MBCONN_WIFI;
                }
                if (Info.getType() == ConnectivityManager.TYPE_MOBILE)
                {
                    return MBCONN_MOBILE;
                }
                if (Info.getType() == ConnectivityManager.TYPE_ETHERNET)
                {
                    return MBCONN_NETWORK;
                }
            }
        }
        catch (Exception e)
        {
            return MBCONN_NO;
        }
        return MBCONN_NO;
    }

    public static int BoolToInt(boolean value)
    {
        if (value)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static boolean IntToBool(int value)
    {
        if (value == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static void Toast(Context cnt, String Text)
    {
        Toast.makeText(cnt, Text, Toast.LENGTH_SHORT).show();
    }

    public static void Toast(Context cnt, int StringID)
    {
        Toast.makeText(cnt, StringID, Toast.LENGTH_SHORT).show();
    }

    public static boolean IsForeground(Activity act)
    {
        ActivityManager activityManager = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        // 枚举进程
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos)
        {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                if (appProcessInfo.processName.equals(act.getApplicationInfo().processName))
                {
                    return true;
                }
            }
        }

        return false;
    }

}
