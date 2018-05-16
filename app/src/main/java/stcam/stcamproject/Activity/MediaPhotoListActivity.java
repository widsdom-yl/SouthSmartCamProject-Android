package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.MediaPhotoGridAdapter;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;

public class MediaPhotoListActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener {
    final static String tag = "MediaPhotoListActivity";
    DevModel model;
    RecyclerView rv;
    MediaPhotoGridAdapter adapter;
    List<String> files;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_photo_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_media);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            model = (DevModel) bundle.getParcelable("devModel");
            Log.e(tag,"devmodel SN:"+model.SN);
        }
        rv = findViewById(R.id.photo_grid_view);
        GridLayoutManager layoutManage = new GridLayoutManager(this, 3);
        rv.setLayoutManager(layoutManage);

        files= FileUtil.getImagePathFromPath(FileUtil.pathSnapShot(),model.SN);
        adapter = new MediaPhotoGridAdapter(files);
        adapter.setOnItemClickListener(this);
        rv.setAdapter(adapter);

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
       //
        Intent intent = new Intent(this, MediaPhotoDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("imagePath",files.get(position));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
