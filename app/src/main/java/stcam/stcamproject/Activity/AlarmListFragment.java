package stcam.stcamproject.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.nuptboyzhb.lib.SuperSwipeRefreshLayout;
import com.model.AlarmImageModel;
import com.model.RetModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.AlarmListAdapter;
import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.Manager.JPushManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class AlarmListFragment extends Fragment implements BaseAdapter.OnItemClickListener, View.OnClickListener, AlarmListAdapter
  .DeleteClickListener
{

  AlarmListAdapter adapter;
  RecyclerView rv;
  List<AlarmImageModel> alarmImageArray;
  final String tag = "AlarmListActivity";
  LoadingDialog lod;
  SuperSwipeRefreshLayout refreshLayout;
  Button clear_button, refresh_button;

  // TODO: Rename and change types and number of parameters
  public static AlarmListFragment newInstance()
  {
    AlarmListFragment fragment = new AlarmListFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.activity_alarm_list, container, false);
    rv = view.findViewById(R.id.alarm_list_view);
    refreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    rv.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));
    rv.setLayoutManager(new LinearLayoutManager(this.getContext()));

    clear_button = view.findViewById(R.id.clear_button);
    refresh_button = view.findViewById(R.id.refresh_button);
    getAlarmList(true);

    refreshLayout
      .setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener()
      {

        @Override
        public void onRefresh()
        {
          //TODO 开始刷新
          getAlarmList(true);
        }

        @Override
        public void onPullDistance(int distance)
        {
          //TODO 下拉距离
        }

        @Override
        public void onPullEnable(boolean enable)
        {
          //TODO 下拉过程中，下拉的距离是否足够出发刷新
        }
      });

    clear_button.setOnClickListener(this);
    refresh_button.setOnClickListener(this);

    return view;
  }


  void getAlarmList(boolean refresh)
  {
    if (lod == null)
    {
      lod = new LoadingDialog(this.getContext());
    }
    if (!refresh)
    {
      lod.dialogShow();
    }
    Date d = new Date();
    System.out.println(d);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    String dateNowStr = sdf.format(d);

    ServerNetWork.getCommandApi()
      .app_user_getalmfilelst(
        AccountManager.getInstance().getDefaultUsr(),
        AccountManager.getInstance().getDefaultPwd(),
        JPushManager.getJPushRegisterID(),
        dateNowStr,
        100,
        0)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(observer_get_alarmlst)
    ;

  }

  void deleAlarmList(String ids)
  {
    Log.e(tag, "deleAlarmList,ids:" + ids);
    Date d = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    final String dateNowStr = sdf.format(d);
    ServerNetWork.getCommandApi().app_user_delalmfile(
      AccountManager.getInstance().getDefaultUsr(),
      AccountManager.getInstance().getDefaultPwd(),
      JPushManager.getJPushRegisterID(),
      ids)
      .flatMap(new Func1<RetModel, Observable<List<AlarmImageModel>>>()
      {
        @Override
        public Observable<List<AlarmImageModel>> call(RetModel model)
        {
          // 返回 Observable<Messages>，在订阅时请求消息列表，并在响应后发送请求到的消息列表
          return ServerNetWork.getCommandApi().app_user_getalmfilelst(
            AccountManager.getInstance().getDefaultUsr(),
            AccountManager.getInstance().getDefaultPwd(),
            JPushManager.getJPushRegisterID(),
            dateNowStr,
            100,
            0);
        }
      })
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(observer_get_alarmlst);


//        ServerNetWork.getCommandApi()
//                .app_user_delalmfile(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance().getDefaultPwd(), ids)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(observer_del_alarmlst);
  }

  Observer<List<AlarmImageModel>> observer_get_alarmlst = new Observer<List<AlarmImageModel>>()
  {
    @Override
    public void onCompleted()
    {
      lod.dismiss();


      // refreshLayout.setLoading(false);

      Log.e(tag, "---------------------2");
      refreshLayout.setRefreshing(false);
    }

    @Override
    public void onError(Throwable e)
    {
      lod.dismiss();

      Log.e(tag, "---------------------1:" + e.getLocalizedMessage());
      refreshLayout.setRefreshing(false);
    }

    @Override
    public void onNext(List<AlarmImageModel> mlist)
    {
      lod.dismiss();

      if (mlist.size() > 0)
      {
        alarmImageArray = mlist;
        if (adapter == null)
        {
          adapter = new AlarmListAdapter(alarmImageArray);
          adapter.setOnItemClickListener(AlarmListFragment.this);
          adapter.setmDeleteClickListener(AlarmListFragment.this);
          rv.setAdapter(adapter);
        }
        else
        {
          adapter.resetMList(alarmImageArray);
          adapter.notifyDataSetChanged();
        }

      }
      else
      {
        if (alarmImageArray != null && adapter != null)
        {
          alarmImageArray.clear();
          adapter.resetMList(alarmImageArray);
          adapter.notifyDataSetChanged();
        }


        //MyContext.getInstance()
        Log.e(tag, "---------------------1:no dev");
//                SouthUtil.showToast(STApplication.getInstance(),"No Alarm");


      }

    }
  };

  Observer<RetModel> observer_del_alarmlst = new Observer<RetModel>()
  {
    @Override
    public void onCompleted()
    {
      lod.dismiss();


      // refreshLayout.setLoading(false);

      Log.e(tag, "---------------------2");

    }

    @Override
    public void onError(Throwable e)
    {
      lod.dismiss();

      Log.e(tag, "---------------------1:" + e.getLocalizedMessage());

    }

    @Override
    public void onNext(RetModel retModel)
    {
      lod.dismiss();
      if (retModel.ret == ServerNetWork.RESULT_SUCCESS)
      {
        if (alarmImageArray != null)
        {
          alarmImageArray.clear();
        }
        adapter.resetMList(alarmImageArray);
        adapter.notifyDataSetChanged();
      }
      else
      {
        SouthUtil.showDialog(AlarmListFragment.this.getContext(), AlarmListFragment.this.getContext().getString(R.string.action_Failed));
      }


    }
  };


  @Override
  public void onItemClick(View view, int position)
  {
    AlarmImageModel model = alarmImageArray.get(position);
    Intent intent = new Intent(STApplication.getInstance(), AlarmDetailActivity.class);

    Bundle bundle = new Bundle();
    bundle.putSerializable("model", model);

    intent.putExtras(bundle);
    startActivity(intent);
  }

  @Override
  public void onLongClick(View view, int position)
  {

  }

  @Override
  public void onClick(View view)
  {
    if (view.getId() == R.id.clear_button)
    {
      new AlertDialog.Builder(this.getContext())
        .setTitle(this.getContext().getString(R.string.action_clear_alarm_image))
        .setPositiveButton(this.getContext().getString(R.string.action_ok), new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
            if (alarmImageArray == null || alarmImageArray.size() == 0)
            {
              return;
            }
            if (lod == null)
            {
              lod = new LoadingDialog(AlarmListFragment.this.getContext());
            }
            lod.dialogShow();

            StringBuilder stringBuilder = new StringBuilder();
            int size = alarmImageArray.size();
            for (int ii = 0; ii < size; ++ii)
            {
              stringBuilder.append(alarmImageArray.get(ii).ID);
              if (ii != size - 1)
              {
                stringBuilder.append("@");
              }
            }
            deleAlarmList(stringBuilder.toString());
          }
        })
        .setNegativeButton(this.getContext().getString(R.string.action_cancel), null)
        .show();


    }
    else if (view.getId() == R.id.refresh_button)
    {
      getAlarmList(false);
    }


  }

  @Override
  public void onDeleteBtnCilck(View view, int position)
  {
    deleAlarmList("" + alarmImageArray.get(position).ID);
  }

  @Override
  public void onItemCilck(View view, int position)
  {
    AlarmImageModel model = alarmImageArray.get(position);
    Intent intent = new Intent(STApplication.getInstance(), AlarmDetailActivity.class);

    Bundle bundle = new Bundle();
    bundle.putSerializable("model", model);

    intent.putExtras(bundle);
    startActivity(intent);
  }
}
