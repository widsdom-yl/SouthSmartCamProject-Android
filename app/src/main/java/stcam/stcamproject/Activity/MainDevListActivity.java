package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.model.DevModel;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.DeviceListAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class MainDevListActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    DeviceListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dev_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.title_main_dev_list);
        initView();
       // mAdapter = new DeviceListAdapter(this,null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDevList();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    String SearchMsg;
    boolean IsSearching;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(this, AddDeviceActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    void initView(){
        mRecyclerView = findViewById(R.id.recyler_device);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);


    }


    void loadDevList(){
        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();
        subscription = ServerNetWork.getCommandApi()
//                .app_user_get_devlst("4719373@qq.com","admin111")
                .app_user_get_devlst("807510889@qq.com","111111")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_get_devlst);
    }

    Observer<List<DevModel>> observer_get_devlst = new Observer<List<DevModel>>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"---------------------2");
            subscription.unsubscribe();
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
        }

        @Override
        public void onNext(List<DevModel> mlist) {
            lod.dismiss();

            if (mlist.size() > 0){
                DevModel model =  mlist.get(0);
                SouthUtil.showToast(STApplication.getInstance(),"dev0 name"+model.DevName);
                Log.e(tag,"---------------------1 dev0 name"+model.DevName);
                if (mAdapter == null){
                    mAdapter = new DeviceListAdapter(MainDevListActivity.this,mlist);
                    mRecyclerView.setAdapter(mAdapter);
                }
                else{
                    mAdapter.setmDatas(mlist);
                }

            }
            else{
                //MyContext.getInstance()
                Log.e(tag,"---------------------1:no dev");
                SouthUtil.showToast(STApplication.getInstance(),"No dev");
            }

        }
    };

    protected Subscription subscription;

    final String tag = "MainDevListActivity";
    LoadingDialog lod;
}
