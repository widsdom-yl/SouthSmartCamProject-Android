package stcam.stcamproject.Manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteAssetHelper extends SQLiteOpenHelper{
    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "cam.db";
    public static final String TABLE_NAME_NODE = "Node";
    public static final String TABLE_NAME_IMAGE = "Image";
    public static final String TABLE_NAME_VIDEO = "Video";//是否已经查看过视频
    public SQLiteAssetHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table if not exists " + TABLE_NAME_NODE + " ("
                + "Id integer primary key AUTOINCREMENT, "
                + "resolute integer default 0,"
                + "sn text unique,"
                + "name text, "
                + "ip text,"
                + "dataport text,"
                + "httpport text,"
                + "usr text,"
                + "pwd text)";
        sqLiteDatabase.execSQL(sql);
        String sql1 = "create table if not exists " + TABLE_NAME_IMAGE + " ("
                + "Id integer primary key AUTOINCREMENT, "
                + "sn text,"
                + "date text,"
                + "fm text)";
        sqLiteDatabase.execSQL(sql1);

        String sql2 = "create table if not exists " + TABLE_NAME_VIDEO + " ("
                + "Id integer primary key AUTOINCREMENT, "
                + "name text unique,"
                + "url text,"
                + "viewed integer default 0)";
        sqLiteDatabase.execSQL(sql2);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME_NODE;
        sqLiteDatabase.execSQL(sql);
        String sql1 = "DROP TABLE IF EXISTS " + TABLE_NAME_IMAGE;
        sqLiteDatabase.execSQL(sql1);
        String sql2 = "DROP TABLE IF EXISTS " + TABLE_NAME_VIDEO;
        sqLiteDatabase.execSQL(sql2);
        onCreate(sqLiteDatabase);
    }
}
