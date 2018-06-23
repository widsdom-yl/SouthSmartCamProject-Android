package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.PushSettingAdapter;
import stcam.stcamproject.R;

public class PushSettingActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener {
    List<String> items = new ArrayList<>();
    RecyclerView mRecyclerView;
    PushSettingAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_manager_push);
        }
        setContentView(R.layout.activity_push_setting);

        initView();
        initValue();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.back(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){


//            SetLedStatusTask task1 = new SetLedStatusTask();
//            task1.execute(0);
            this.back(); // back button

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    void back(){

        this.finish(); // back button
    }

    void initView(){


        mRecyclerView = findViewById(R.id.push_setting_list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }
    void initValue(){
        items.add(getString(R.string.action_push));
        items.add(getString(R.string.action_push_interval));
        items.add(getString(R.string.action_pir_sensitivity));
        mAdapter = new PushSettingAdapter(items);

        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
