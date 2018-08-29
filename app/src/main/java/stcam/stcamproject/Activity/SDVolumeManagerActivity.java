package stcam.stcamproject.Activity;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.model.DevModel;
import com.model.RecConfigModel;
import com.model.RetModel;
import com.model.SDInfoModel;
import com.thSDK.lib;

import java.util.ArrayList;
import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.SDVolumeSettingAdapter;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;

public class SDVolumeManagerActivity extends BaseAppCompatActivity implements BaseAdapter.OnItemClickListener, View.OnClickListener,
  RadioGroup.OnCheckedChangeListener, SDVolumeSettingAdapter.OnRecordClickListener
{

  List<String> items = new ArrayList<>();
  SDVolumeSettingAdapter mAdapter;
  DevModel model;
  RecyclerView mRecyclerView;
  Button button_format;
  int isRecord;
  SDInfoModel sdInfoModel;
  RecConfigModel mRecConfigModel = new RecConfigModel();

  RadioGroup record_group;

//    TextView textView_left;
//    TextView textView_used;

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.blank_menu, menu);
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sdvolume_manager);

    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null)
    {
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(R.string.string_DevAdvancedSettings_TFManage);

      setCustomTitle(getString(R.string.string_DevAdvancedSettings_TFManage), true);

    }
    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null)
    {
      model = bundle.getParcelable("devModel");
    }


    initView();
    initValue();


    if (lod == null)
    {
      lod = new LoadingDialog(this);
    }
    lod.dialogShow();


    GetSDInfoTask task1 = new GetSDInfoTask();
    task1.execute();

    GetRecordStateTask task = new GetRecordStateTask();
    task.execute();

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        this.finish(); // back button
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  void initView()
  {
    mRecyclerView = findViewById(R.id.recyclerView);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    button_format = findViewById(R.id.button_format);
    button_format.setOnClickListener(this);

//        record_group = findViewById(R.id.record_group);

//        textView_left = findViewById(R.id.textView_left);
//        textView_used = findViewById(R.id.textView_used);


  }

  void initValue()
  {
    items.add(getString(R.string.action_volume_total));
//        items.add(getString(R.string.action_volume_record));
    items.add(getString(R.string.action_volume_left));
    items.add(getString(R.string.action_record_recycle));


    mAdapter = new SDVolumeSettingAdapter(items);
    mAdapter.setOnItemClickListener(this);
    mAdapter.setOnRecordClickListener(this);
    mRecyclerView.setAdapter(mAdapter);

  }

  @Override
  public void onItemClick(View view, int position)
  {

  }

  @Override
  public void onLongClick(View view, int position)
  {

  }

  @Override
  public void onClick(View view)
  {
    if (view == button_format)
    {

      new AlertDialog.Builder(this)
        .setTitle(this.getString(R.string.action_format_sd_ask))
        .setPositiveButton(this.getString(R.string.action_ok), new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
            if (lod == null)
            {
              lod = new LoadingDialog(SDVolumeManagerActivity.this);
            }
            lod.dialogShow();
            SDFormatTask task = new SDFormatTask();
            task.execute();
          }
        })
        .setNegativeButton(this.getString(R.string.action_cancel), null)
        .show();


    }
  }

  @Override
  public void onCheckedChanged(RadioGroup radioGroup, int checkedId)
  {
//        switch (checkedId) {
//            case R.id.record_stop_radio:
//                ControlRecordStateTask task = new ControlRecordStateTask();
//                task.execute(22);
//                break;
//            case R.id.record_recycle_radio:
//                ControlRecordStateTask task1 = new ControlRecordStateTask();
//                task1.execute(21);
//                break;
//            default:
//                break;
//        }

  }

  @Override
  public void onRecordClickListener(boolean record)
  {
    if (!record)
    {
      ControlRecordStateTask task = new ControlRecordStateTask();
      task.execute(3);
    }
    else
    {
      ControlRecordStateTask task1 = new ControlRecordStateTask();
      task1.execute(1);
    }


  }


  class SDFormatTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = model.getDevURL(lib.Msg_FormattfCard);
      String ret = lib.thNetHttpGet(model.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      lod.dismiss();

      RetModel retModel = GsonUtil.parseJsonWithGson(result, RetModel.class);
      if (retModel != null)
      {
        if (retModel.ret == 1)
        {
          SouthUtil.showDialog(SDVolumeManagerActivity.this, getString(R.string.action_Success));
        }
        else if (retModel.ret == 2)
        {
          SouthUtil.showDialog(SDVolumeManagerActivity.this, getString(R.string.action_Success_Reboot));
        }
        else
        {
          SouthUtil.showDialog(SDVolumeManagerActivity.this, getString(R.string.action_Failed));
        }
      }
      super.onPostExecute(result);
    }
  }

  class GetRecordStateTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = model.getDevURL(lib.Msg_GetRecCfg);
      String ret = lib.thNetHttpGet(model.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      lod.dismiss();


      if (result != null && result.length() > 0)
      {
        RecConfigModel recConfigModel = GsonUtil.parseJsonWithGson(result, RecConfigModel.class);
        if (recConfigModel != null)
        {
          mRecConfigModel = recConfigModel;
          //mAdapter.setmRecConfigModel(mRecConfigModel);
          mAdapter.setIsRecord(mRecConfigModel.getRec_RecStyle() == 1 ? true : false);
          mAdapter.notifyDataSetChanged();
        }
      }
      //record_group.setOnCheckedChangeListener(SDVolumeManagerActivity.this);

      super.onPostExecute(result);
    }
  }

  class GetSDInfoTask extends AsyncTask<String, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params)
    {
      String url = model.getDevURL(lib.Msg_GetDiskCfg);
      String ret = lib.thNetHttpGet(model.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
      //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
      //Log.e(tag,"get playback list :"+result);
      lod.dismiss();

      sdInfoModel = GsonUtil.parseJsonWithGson(result, SDInfoModel.class);
      if (sdInfoModel != null)
      {
        mAdapter.setSdInfoModel(sdInfoModel);
        mAdapter.notifyDataSetChanged();
//                textView_left.setText(SDVolumeManagerActivity.this.getString(R.string.action_volume_left_percent)+sdInfoModel
// .getFreePercent());
//                textView_used.setText(SDVolumeManagerActivity.this.getString(R.string.action_volume_left_percent)+sdInfoModel
// .getUsedPercent());
      }

      super.onPostExecute(result);
    }
  }

  class ControlRecordStateTask extends AsyncTask<Integer, Void, String>
  {
    // AsyncTask<Params, Progress, Result>
    //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
    @Override
    protected void onPreExecute()
    {
      //第一个执行方法
      super.onPreExecute();
    }

    @Override
    protected String doInBackground(Integer... params)
    {
      String url = model.getDevURL(lib.Msg_SetRecCfg) + "&Rec_RecStyle=" + params[0];
      String ret = lib.thNetHttpGet(model.NetHandle, url);
      return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
      //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
      //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
      //Log.e(tag,"get playback list :"+result);
      lod.dismiss();

      RetModel retModel = GsonUtil.parseJsonWithGson(result, RetModel.class);
      if (retModel != null)
      {
        if (retModel.ret == 1)
        {
          SouthUtil.showDialog(SDVolumeManagerActivity.this, getString(R.string.action_Success));
        }
        else
        {
          SouthUtil.showDialog(SDVolumeManagerActivity.this, getString(R.string.action_Failed));
        }
      }

      super.onPostExecute(result);
    }
  }


  LoadingDialog lod;
  final String tag = "SDVolumeManagerActivity";
}
