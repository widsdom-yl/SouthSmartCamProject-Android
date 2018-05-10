package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.model.DevModel;

import java.util.ArrayList;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.MediaDeviceAdapter;
import stcam.stcamproject.R;
/*图库的设备列表*/
public class MediaActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener {
    final static String tag = "MediaActivity";
    ArrayList<DevModel> mDevices;
    RecyclerView rv;
    MediaDeviceAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_media);
        }

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            mDevices = bundle.getParcelableArrayList("devices");
            Log.e(tag,"mDevices count is :"+mDevices.size());
        }
        rv = findViewById(R.id.list_view);
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this));
        if (mDevices != null){
            if (adapter == null){
                adapter = new MediaDeviceAdapter(mDevices);
                adapter.setOnItemClickListener(this);
            }
            rv.setAdapter(adapter);
        }
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


        Intent intent = new Intent(this, MediaPhotoListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("devModel",mDevices.get(position));
        intent.putExtras(bundle);

        startActivity(intent);

    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
