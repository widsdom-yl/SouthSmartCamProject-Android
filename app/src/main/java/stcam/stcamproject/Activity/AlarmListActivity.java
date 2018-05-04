package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.model.AlarmImageModel;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.AlarmListAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class AlarmListActivity extends AppCompatActivity {

    AlarmListAdapter adapter;
    RecyclerView rv;
    List<AlarmImageModel> alarmImageArray;
    final String tag = "AlarmListActivity";
    LoadingDialog lod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_alarm);
        }

        rv = findViewById(R.id.alarm_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this));
        getAlarmList(false);

    }

    void getAlarmList(boolean refresh){
        if (lod == null){
            lod = new LoadingDialog(this);
        }
        if (!refresh)
            lod.dialogShow();
            ServerNetWork.getCommandApi()
                .app_user_getalmfilelst(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance().getDefaultPwd(),"20180504",10,0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_get_alarmlst);
    }

    Observer<List<AlarmImageModel>> observer_get_alarmlst = new Observer<List<AlarmImageModel>>() {
        @Override
        public void onCompleted() {
            lod.dismiss();


            // refreshLayout.setLoading(false);

            Log.e(tag,"---------------------2");

        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();

            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
        }

        @Override
        public void onNext(List<AlarmImageModel> mlist) {
            lod.dismiss();

            if (mlist.size() > 0){
                alarmImageArray = mlist;
                if (adapter == null){
                    adapter = new AlarmListAdapter(alarmImageArray);
                }
                rv.setAdapter(adapter);
            }
            else{
                //MyContext.getInstance()
                Log.e(tag,"---------------------1:no dev");
                SouthUtil.showToast(STApplication.getInstance(),"No dev");
            }

        }
    };


}
