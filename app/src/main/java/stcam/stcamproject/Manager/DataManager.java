package stcam.stcamproject.Manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.model.DevModel;
import com.model.ImageModel;
import com.model.SDVideoModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class DataManager{
	private static final String TAG = "DataManager";
	private Context context;
	private SQLiteAssetHelper dBHelper;
	private final String[] ORDER_COLUMNS_NODE = new String[] {"SN","usr","pwd"};
	private final String[] ORDER_COLUMNS_IMAGE = new String[] {"Id","date", "fm","sn"};
	private final String[] ORDER_COLUMNS_VIDEO = new String[] {"name","url","viewed"};
	private final String[] ORDER_COLUMNS_IMAGE_DATE = new String[] {"date"};
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock w = rwl.writeLock();
	private static class DataManagerHolder {
		private static final DataManager INSTANCE = new DataManager(MyContext.getInstance().getApplicationContext());
	}
	private DataManager (Context context)
	{
		this.context = context;
		dBHelper = new SQLiteAssetHelper(context);
	}
	public static final DataManager getInstance() {
		return DataManagerHolder.INSTANCE;
	}


	public boolean addImage(ImageModel image){
		SQLiteDatabase db = null;
		try {
			db = dBHelper.getWritableDatabase();
			db.beginTransaction();
			ContentValues contentValues = new ContentValues();
			contentValues.put("date", image.date);
			contentValues.put("fm", image.fm);
			contentValues.put("sn", image.sn);

			db.insertOrThrow(dBHelper.TABLE_NAME_IMAGE, null, contentValues);
			db.setTransactionSuccessful();
		}catch (Exception e){
			Log.e(TAG, "", e);
		}finally {
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}
		return true;
	}

	public boolean addDev(DevModel model){
		SQLiteDatabase db = null;
		try {
			db = dBHelper.getWritableDatabase();
			db.beginTransaction();
			ContentValues contentValues = new ContentValues();
			contentValues.put("SN", model.SN);
			contentValues.put("usr", model.usr);
			contentValues.put("pwd", model.pwd);
			db.insertOrThrow(dBHelper.TABLE_NAME_NODE, null, contentValues);
			db.setTransactionSuccessful();
		}catch (Exception e){
			Log.e(TAG, "", e);
			return false;
		}finally {
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}
		return true;
	}
/*根据SN获得*/
	public DevModel getSNDev(String SN){
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_NODE, ORDER_COLUMNS_NODE, null, null, null, null, null);
			cursor = db.query(dBHelper.TABLE_NAME_NODE,
					ORDER_COLUMNS_NODE,
					"SN = ?",
					new String[] {SN},
					null, null, null);
			if (cursor.getCount() > 0) {

				while (cursor.moveToNext()) {
					return parseNode(cursor);
				}
				return null;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return null;
	}
	/**
	 * 查询数据库中所有设备数据
	 */
	public List<DevModel> getAllDev(){
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<DevModel> orderList = new ArrayList();
		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_NODE, ORDER_COLUMNS_NODE, null, null, null, null, null);

			if (cursor.getCount() > 0) {

				while (cursor.moveToNext()) {
					orderList.add(parseNode(cursor));
				}
				return orderList;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return orderList;
	}

	/**
	 * 是否存在录像
	 */
	public boolean isSDVideoExist(SDVideoModel model){
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_VIDEO,
					ORDER_COLUMNS_VIDEO,
					"name = ?",
					new String[] {model.sdVideo},
					null, null, null);

			if (cursor.getCount() > 0) {
				if (cursor != null) {
					cursor.close();
				}
				if (db != null) {
					db.close();
				}
				return true;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return false;

	}

	/**
	 * 获得某段录像
	 */
	public SDVideoModel getCertainSDVideo(SDVideoModel model){
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_VIDEO,
					ORDER_COLUMNS_VIDEO,
					"name = ?",
					new String[] {model.sdVideo},
					null, null, null);

			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					SDVideoModel tempmodel =  parseSDVideoModel(cursor);
					if (cursor != null) {
						cursor.close();
					}
					if (db != null) {
						db.close();
					}
					return tempmodel;
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return null;

	}

	/**
	 * 删除录像
	 */
	public boolean deleteSDVidel(SDVideoModel model){
		SQLiteDatabase db = null;
		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			db.delete(dBHelper.TABLE_NAME_IMAGE , "name =? ", new String[]{model.sdVideo});
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
		finally {
			if (db != null) {
				db.close();
			}
		}
		return true;





	}

	/**
	 * 查询数据库中所有录像数据
	 */
	public List<SDVideoModel> getAllSDVideos(){
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<SDVideoModel> orderList = new ArrayList();
		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_VIDEO, ORDER_COLUMNS_VIDEO, null, null, null, null, null);

			if (cursor.getCount() > 0) {

				while (cursor.moveToNext()) {
					orderList.add(parseSDVideoModel(cursor));
				}
				return orderList;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return orderList;
	}

	/**
	 * 更新录像数据
	 */
	public boolean updateSDVideoModel(SDVideoModel model){
		SQLiteDatabase db = null;
		int ret = 0;
		try {
			db = dBHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put("viewed", model.viewed ? 1 : 0);
			ret = db.update(dBHelper.TABLE_NAME_VIDEO, cv, "name = ?", new String[]{model.sdVideo});
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
		finally {
			if (db != null) {
				db.close();
			}
		}

		return  true;
	}

	/**
	 * 添加录像查看情况
	 * @param model
	 * @return
	 */
	public boolean addSDVideoModel(SDVideoModel model){

		if (isSDVideoExist(model)){
			updateSDVideoModel(model);
			return true;
		}

		SQLiteDatabase db = null;
		try {
			db = dBHelper.getWritableDatabase();
			db.beginTransaction();
			ContentValues contentValues = new ContentValues();
			contentValues.put("name", model.sdVideo);
			contentValues.put("url", model.url);
			contentValues.put("viewed", model.viewed?1:0);
			db.insertOrThrow(dBHelper.TABLE_NAME_VIDEO, null, contentValues);
			db.setTransactionSuccessful();
		}catch (Exception e){
			Log.e(TAG, "", e);
		}finally {
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}
		return true;
	}

	/**
	 * 是否存在设备
	 */
	public boolean isDevExist(DevModel model){
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_NODE,
					ORDER_COLUMNS_NODE,
					"SN = ?",
					new String[] {model.SN},
					null, null, null);

			if (cursor.getCount() > 0) {
				if (cursor != null) {
					cursor.close();
				}
				if (db != null) {
					db.close();
				}
				return true;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return false;

	}

	/**
	 * 是否设备sn的图片
	 */
	public boolean isDevImageExist(DevModel model){
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_IMAGE,
					ORDER_COLUMNS_IMAGE,
					"sn = ?",
					new String[] {model.SN},
					null, null, null);

			if (cursor.getCount() > 0) {

				return true;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return false;

	}

	/**
	 * 更新node
	 */
	public boolean updateDev(DevModel model){
		SQLiteDatabase db = null;
		int ret = 0;

		try {
			db = dBHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put("SN", model.SN);
			cv.put("usr", model.usr);
			cv.put("pwd", model.pwd);

			Log.e(TAG, "update in manager sn:" + model.SN );
			ret = db.update(dBHelper.TABLE_NAME_NODE, cv, "SN = ?", new String[]{model.SN});
		}
		catch (Exception e) {
			return false;
		}
		finally {
			if (db != null) {
				db.close();
			}
		}
		return  true;



	}
	/**
	 * 删除设备数据
	 */
	public boolean deleteDev(DevModel model){
		SQLiteDatabase db = null;
		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			db.delete(dBHelper.TABLE_NAME_NODE , "SN =? ", new String[]{model.SN});
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
		finally {
			if (db != null) {
				db.close();
			}
		}
		return true;





	}

	/**
	 * 删除所有设备数据
	 */
	public boolean deleteAllDev(){
		SQLiteDatabase db = null;
		try {
			db = dBHelper.getReadableDatabase();
			db.execSQL("delete from " + dBHelper.TABLE_NAME_NODE);

		}
		catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
		finally {
			if (db != null) {
				db.close();
			}
		}
		return true;


	}


	/**
	 * 查询数据库中Dev对应的最新的一张图片
	 */
	public ImageModel getDevImage(DevModel model){
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = dBHelper.getWritableDatabase();
			// select * from Orders

			cursor = db.query(dBHelper.TABLE_NAME_IMAGE,
					ORDER_COLUMNS_IMAGE,
					"sn = ?",
					new String[] {model.SN},
					null, null,"Id desc");


			if (cursor.getCount() > 0) {
				if (cursor.moveToNext()) {
					return parseImage(cursor);
				}

			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return null;
	}


	/**
	 * 查询数据库中所有图片
	 */
	public List<ImageModel> getAllImages(){
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_IMAGE, ORDER_COLUMNS_IMAGE, null, null, null, null, null);

			if (cursor.getCount() > 0) {
				List<ImageModel> imageList = new ArrayList<ImageModel>(cursor.getCount());
				while (cursor.moveToNext()) {
					imageList.add(parseImage(cursor));
				}
				return imageList;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return null;
	}

	/**
	 * 查询数据库中某天对应的所有图片
	 */
	public List<ImageModel> getAllImages(String date){
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = dBHelper.getReadableDatabase();
			// select * from Orders
			cursor = db.query(dBHelper.TABLE_NAME_IMAGE,
					ORDER_COLUMNS_IMAGE,
					"date = ?",
					new String[] {date},
					null, null, null);

			if (cursor.getCount() > 0) {
				List<ImageModel> imageList = new ArrayList<ImageModel>(cursor.getCount());
				while (cursor.moveToNext()) {
					imageList.add(parseImage(cursor));
				}
				return imageList;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return null;
	}
	/**
	 * 查询图片表中所有日期
	 */
	public List<String> getAllDates(){
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = dBHelper.getReadableDatabase();

			cursor = db.query(true,dBHelper.TABLE_NAME_IMAGE,
					ORDER_COLUMNS_IMAGE_DATE,
					null,
					null,
					null, null, null, null);

			if (cursor.getCount() > 0) {
				List<String> dateList = new ArrayList<String>(cursor.getCount());
				while (cursor.moveToNext()) {
					dateList.add(cursor.getString(cursor.getColumnIndex("date")));
				}
				return dateList;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return null;
	}
	/**
	 * 将查找到的数据转换成NodeBean类
	 */
	private DevModel parseNode(Cursor cursor){
		DevModel model = new DevModel();
		model.SN = (cursor.getString(cursor.getColumnIndex("SN")));
		//model.DevName = (cursor.getString(cursor.getColumnIndex("DevName")));
		model.usr = (cursor.getString(cursor.getColumnIndex("usr")));
		model.pwd = (cursor.getString(cursor.getColumnIndex("pwd")));
		return model;
	}

	/**
	 * 将查找到的数据转换成ImageBean类
	 */
	private ImageModel parseImage(Cursor cursor){
		ImageModel image = new ImageModel();
		image.date = (cursor.getString(cursor.getColumnIndex("date")));
		image.fm = (cursor.getString(cursor.getColumnIndex("fm")));
		image.sn = (cursor.getString(cursor.getColumnIndex("sn")));
		return image;
	}

	/**
	 * 将查找到的数据转换成SDVideoModel类
	 */
	private SDVideoModel parseSDVideoModel(Cursor cursor){
		SDVideoModel model = new SDVideoModel();
		model.sdVideo = (cursor.getString(cursor.getColumnIndex("name")));
		model.url = (cursor.getString(cursor.getColumnIndex("url")));
		model.viewed = cursor.getInt(cursor.getColumnIndex("viewed")) == 1;
		return model;
	}
}

