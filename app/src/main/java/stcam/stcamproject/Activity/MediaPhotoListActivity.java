package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.model.DevModel;

import java.util.ArrayList;
import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.MediaPhotoGridAdapter;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;

public class MediaPhotoListActivity extends BaseAppCompatActivity implements BaseAdapter.OnItemClickListener, MediaPhotoGridAdapter.OnImageCheckListner {
    final static String tag = "MediaPhotoListActivity";
    DevModel model;
    RecyclerView rv;
    MediaPhotoGridAdapter adapter;
    List<String> mediaFiles = new ArrayList<>();//包含图片和视频的数组
    List<String> imagefiles;
    List<String> videofiles;
    List<String> checkFile = new ArrayList<>();
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

        imagefiles= FileUtil.getSNFilesFromPath(FileUtil.pathSnapShot(),model.SN);
        videofiles = FileUtil.getSNFilesFromPath(FileUtil.pathRecord(),model.SN);
        Log.e(tag,"videofiles size:"+videofiles.size());
        mediaFiles.clear();
        mediaFiles.addAll(imagefiles);
        mediaFiles.addAll(videofiles);
        Log.e(tag,"mediaFiles size:"+mediaFiles.size());

        adapter = new MediaPhotoGridAdapter(mediaFiles);
        adapter.setCheckFile(checkFile);
        adapter.setOnItemClickListener(this);
        adapter.setOnImageCheckListner(this);
        rv.setAdapter(adapter);

    }
    @Override
    protected void onResume() {
        super.onResume();
        reloadMediaList();
        adapter.resetMList(mediaFiles);
        adapter.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;
    }

    void reloadMediaList(){
        imagefiles= FileUtil.getSNFilesFromPath(FileUtil.pathSnapShot(),model.SN);
        videofiles = FileUtil.getSNFilesFromPath(FileUtil.pathRecord(),model.SN);
        mediaFiles.clear();
        mediaFiles.addAll(imagefiles);
        mediaFiles.addAll(videofiles);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case R.id.action_delete:
                for (String file :checkFile){
                    FileUtil.delFiles(file);
                }
                checkFile.clear();
                reloadMediaList();
                adapter.resetMList(mediaFiles);
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_select_all:
                checkFile.clear();
                for (String file : mediaFiles){
                    checkFile.add(file);
                }
                adapter.setCheckFile(checkFile);
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_unselect_all:
                checkFile.clear();
                adapter.setCheckFile(checkFile);
                adapter.notifyDataSetChanged();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
       //

        String mediaPath = mediaFiles.get(position);
        if (FileUtil.checkIsImageFile(mediaPath)){
            Intent intent = new Intent(this, MediaPhotoDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("imagePath",mediaPath);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else if(FileUtil.checkIsVideoFile(mediaPath)){
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.parse(mediaPath), "video/mp4");
//             startActivity(intent);


            Intent intent = new Intent(this, VideoPlayerAct.class);
            intent.putExtra("url",mediaPath);
            startActivity(intent);

        }

    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void OnImageChecked(View view, int position, boolean checked) {
        Log.e(tag,"position "+position+",checked "+checked);
        String file = mediaFiles.get(position);
        if (checked){
            if (!checkFile.contains(file)){
                checkFile.add(file);
            }
        }
        else{
            if (checkFile.contains(file)){
                checkFile.remove(file);
            }
        }
        Log.e(tag,"checked file size is  "+checkFile.size());
    }
}
