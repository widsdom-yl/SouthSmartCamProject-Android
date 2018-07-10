package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.model.AlarmImageModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.AlarmListAdapter;
import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class AlarmListFragment extends Fragment implements BaseAdapter.OnItemClickListener {

    AlarmListAdapter adapter;
    RecyclerView rv;
    List<AlarmImageModel> alarmImageArray;
    final String tag = "AlarmListActivity";
    LoadingDialog lod;

    // TODO: Rename and change types and number of parameters
    public static AlarmListFragment newInstance() {
        AlarmListFragment fragment = new AlarmListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.activity_alarm_list, container, false);
        rv = view.findViewById(R.id.alarm_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this.getContext(),DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this.getContext()));
        getAlarmList(false);
        return view;
    }





    void getAlarmList(boolean refresh){
        if (lod == null){
            lod = new LoadingDialog(this.getContext());
        }
        if (!refresh)
            lod.dialogShow();
        Date d = new Date();
        System.out.println(d);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateNowStr = sdf.format(d);

            ServerNetWork.getCommandApi()
                .app_user_getalmfilelst(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance().getDefaultPwd(),dateNowStr,100,0)
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
                    adapter.setOnItemClickListener(AlarmListFragment.this);
                }
                rv.setAdapter(adapter);
            }
            else{
                //MyContext.getInstance()
                Log.e(tag,"---------------------1:no dev");
//                SouthUtil.showToast(STApplication.getInstance(),"No Alarm");
            }

        }
    };


    @Override
    public void onItemClick(View view, int position) {
        AlarmImageModel model = alarmImageArray.get(position);
        Intent intent = new Intent(STApplication.getInstance(), AlarmDetailActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("model",model);

        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}