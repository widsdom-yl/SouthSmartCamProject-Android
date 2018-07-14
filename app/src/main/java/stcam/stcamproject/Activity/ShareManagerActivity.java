package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.github.nuptboyzhb.lib.SuperSwipeRefreshLayout;
import com.model.RetModel;
import com.model.UserModel;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import stcam.stcamproject.Adapter.ShareManagerAdapter;
import stcam.stcamproject.Manager.AccountManager;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class ShareManagerActivity extends AppCompatActivity implements ShareManagerAdapter.DeleteClickListener {
    static final String tag = "ShareManagerActivity";
    SuperSwipeRefreshLayout refreshLayout;
    RecyclerView rv;
    LoadingDialog lod;
    ShareManagerAdapter mAdapter;
    List<UserModel> mUserList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_manager);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.action_share_account_manager));
        }

        rv = findViewById(R.id.account_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this));

        refreshLayout = findViewById(R.id.swipeRefreshLayout);

        refreshLayout
                .setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {

                    @Override
                    public void onRefresh() {
                        //TODO 开始刷新
                        loadShareUserList(true);
                    }

                    @Override
                    public void onPullDistance(int distance) {
                        //TODO 下拉距离
                    }

                    @Override
                    public void onPullEnable(boolean enable) {
                        //TODO 下拉过程中，下拉的距离是否足够出发刷新
                    }
                });

        loadShareUserList(false);
    }

    void loadShareUserList(boolean refresh){
        if (lod == null){
            lod = new LoadingDialog(this);
        }
        if (!refresh)
            lod.dialogShow();


        ServerNetWork.getCommandApi()
                .app_share_get_lst(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance().getDefaultPwd())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_get_sharelst);
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

    Observer<List<UserModel>> observer_get_sharelst = new Observer<List<UserModel>>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            refreshLayout.setRefreshing(false);

            // refreshLayout.setLoading(false);

            Log.e(tag,"---------------------2");

        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            refreshLayout.setRefreshing(false);
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());
            refreshLayout.setRefreshing(false);
        }

        @Override
        public void onNext(List<UserModel> mlist) {
            lod.dismiss();
            refreshLayout.setRefreshing(false);
            if (mlist.size() > 0){
                if (mAdapter == null){
                    mUserList = mlist;
                    mAdapter = new ShareManagerAdapter(mlist);
                    rv.setAdapter(mAdapter);
                    mAdapter.setmDeleteClickListener(ShareManagerActivity.this);
                }
            }
            else{
                if (mUserList != null && mAdapter != null){
                    mUserList.clear();
                    mAdapter.resetMList(mUserList);
                    mAdapter.notifyDataSetChanged();
                }
            }

        }
    };


    Observer<RetModel> observer_delete = new Observer<RetModel>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            refreshLayout.setRefreshing(false);
            Log.e(tag,"---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            refreshLayout.setRefreshing(false);
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());

            SouthUtil.showDialog(ShareManagerActivity.this,getString(R.string.action_Failed));
        }

        @Override
        public void onNext(RetModel m) {
            lod.dismiss();
            refreshLayout.setRefreshing(false);
            Log.e(tag,"---------------------0:"+m.ret);
            if (1 == m.ret){

            }
            else{
                SouthUtil.showDialog(ShareManagerActivity.this,getString(R.string.action_Failed));
            }

        }
    };


    @Override
    public void onDeleteBtnCilck(View view, int position) {


       String deleteUser = mUserList.get(position).User;

        ServerNetWork.getCommandApi().app_share_del_sub(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance().getDefaultPwd(),
                deleteUser)
                .flatMap(new Func1<RetModel, Observable<List<UserModel>>>() {
                    @Override
                    public Observable<List<UserModel>> call(RetModel model) {
                        // 返回 Observable<Messages>，在订阅时请求消息列表，并在响应后发送请求到的消息列表
                        return ServerNetWork.getCommandApi().app_share_get_lst(AccountManager.getInstance().getDefaultUsr(), AccountManager.getInstance().getDefaultPwd());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer_get_sharelst);

    }
}
