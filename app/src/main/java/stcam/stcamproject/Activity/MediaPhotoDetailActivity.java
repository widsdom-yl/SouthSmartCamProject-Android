package stcam.stcamproject.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;

public class MediaPhotoDetailActivity extends AppCompatActivity {
    String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_photo_detail);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            imagePath = (String) bundle.getSerializable("imagePath");

        }
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete_alone, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case R.id.action_delete:
                FileUtil.delFiles(imagePath);
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    void initView(){
        ImageView imageView = findViewById(R.id.detail_image);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);

        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();



        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = screenWidth;
        lp.height =  bitmap.getHeight()*screenWidth/bitmap.getWidth();
        imageView.setLayoutParams(lp);



    }
}
