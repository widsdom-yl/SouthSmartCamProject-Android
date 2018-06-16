package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.model.DevModel;
import com.model.SDVideoModel;
import com.thSDK.lib;

import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.PlayBackListAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.GsonUtil;

public class PlayBackListActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener {
    static final String tag = "PlayBackListActivity";
    DevModel devModel;
    PlayBackListAdapter adapter;
    RecyclerView rv;
    List<SDVideoModel> videoArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back_list);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getParcelable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(devModel.DevName);
        }

        rv = findViewById(R.id.play_back_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this));
        GetPlayBackListloadTask task = new GetPlayBackListloadTask();
        task.execute();


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onGetPlayBackListResponse(String json){
        if (json != null && json.length() >0){
            List<SDVideoModel> lists = GsonUtil.parseJsonArrayWithGson(json,
                    SDVideoModel[].class);
            if(lists.size()>0){
                videoArray = lists;
            }
            if (adapter == null){
                adapter = new PlayBackListAdapter(lists);
                adapter.setOnItemClickListener(PlayBackListActivity.this);
            }
            rv.setAdapter(adapter);
        }

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.e(tag,"onItemClick,item:"+position);

        Intent intent = new Intent(STApplication.getInstance(), PlayBackActivity.class);

        Bundle bundle = new Bundle();
        SDVideoModel model =  videoArray.get(position);
        bundle.putSerializable("model",model);
        bundle.putParcelable("devModel",devModel);
        intent.putExtras(bundle);
        Log.e(tag,"to PlayBackActivity sdVideo:"+model.sdVideo);

        startActivity(intent);

    }

    @Override
    public void onLongClick(View view, int position) {

    }

    class GetPlayBackListloadTask extends AsyncTask<Integer, Void, String> {
        // AsyncTask<Params, Progress, Result>
        //后面尖括号内分别是参数（例子里是线程休息时间），进度(publishProgress用到)，返回值类型
        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Integer... params) {
            //第二个执行方法,onPreExecute()执行完后执行
            String url = "http://0.0.0.0:0/cfg1.cgi?User=admin&Psd=admin&MsgID=5&p=0&l=10";

            String ret = lib.thNetHttpGet(devModel.NetHandle,url);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            Log.e(tag,"get playback list :"+result);
            onGetPlayBackListResponse(result);

            super.onPostExecute(result);
        }
    }



}
