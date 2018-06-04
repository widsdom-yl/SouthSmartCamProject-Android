package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.SytstmSettingListAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;

public class SystemSettingActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener {
    RecyclerView rv;
    List<String> settingArray = new ArrayList<>();//没有连接状态
    SytstmSettingListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_system_setting);
        }
        rv = findViewById(R.id.system_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this));

        settingArray.add(getString(R.string.action_alarm_sound));
        settingArray.add(getString(R.string.action_app_version));
        settingArray.add(getString(R.string.action_change_system_password));
        settingArray.add(getString(R.string.action_about));

        adapter = new SytstmSettingListAdapter(settingArray);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
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


    @Override
    public void onItemClick(View view, int position) {
        if (2 == position){
            Intent intent = new Intent(STApplication.getInstance(), ChangeLoginPasswordActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
