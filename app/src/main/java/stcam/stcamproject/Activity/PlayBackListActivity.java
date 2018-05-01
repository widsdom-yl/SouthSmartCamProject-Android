package stcam.stcamproject.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.model.DevModel;
import com.thSDK.lib;

import stcam.stcamproject.R;

public class PlayBackListActivity extends AppCompatActivity {
    static final String tag = "PlayBackListActivity";
    DevModel devModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back_list);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            devModel = (DevModel) bundle.getSerializable("devModel");
            Log.e(tag,"NetHandle:"+devModel.NetHandle+",SN:"+devModel.SN);
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(devModel.DevName);
        }
        GetPlayBackListloadTask task = new GetPlayBackListloadTask();


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
            String url = "http://0.0.0.0:0/User=admin&Psd=admin&MsgID=5&p=0&l=10";

            String ret = lib.thNetHttpGet(devModel.NetHandle,url);
            return ret;
        }
        @Override
        protected void onPostExecute(String result) {
            //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
            //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
            Log.e(tag,"get playback list :"+result);
            super.onPostExecute(result);
        }
    }



}
