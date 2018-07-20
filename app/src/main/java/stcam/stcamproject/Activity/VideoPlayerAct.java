package stcam.stcamproject.Activity;


import android.drm.DrmErrorEvent;
import android.drm.DrmManagerClient;
import android.drm.DrmManagerClient.OnErrorListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;

public class VideoPlayerAct extends AppCompatActivity implements OnPreparedListener, OnErrorListener{
    private VideoView vv_video;
    private MediaController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        Window window = getWindow();
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_playvid);
        vv_video=(VideoView) findViewById(R.id.vv_video);
        Bundle bundle = this.getIntent().getExtras();
        String url  = bundle.getString("url");
        // 实例化MediaController
        mController=new MediaController(this);
        Uri uri = Uri.parse(url);
        {
            vv_video.setVideoURI(uri);
            // vv_video.setVideoPath(file.getAbsolutePath());
            // 为VideoView指定MediaController
            vv_video.setMediaController(mController);
            // 为MediaController指定控制的VideoView
            mController.setMediaPlayer(vv_video);
            mController.setKeepScreenOn(true);
            vv_video.setOnPreparedListener(this);
            // vv_video.start();
            // 增加监听上一个和下一个的切换事件，默认这两个按钮是不显示的
            mController.setPrevNextListeners(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Toast.makeText(VideoPlayerAct.this, "下一个",0).show();
                }
            }, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Toast.makeText(VideoPlayerAct.this, "上一个",0).show();
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        handler_enterbackground.postDelayed(runnable_enterbackground,500);


    }

    Handler handler_enterbackground = new Handler();
    Runnable runnable_enterbackground = new Runnable() {
        @Override
        public void run() {
            //
            if (STApplication.getInstance().getIsRunInBackground()){

                vv_video.pause();
                VideoPlayerAct.this.finish();
            }



        }
    };
    public void back(View v){
        vv_video.pause();
        this.finish();
    }
    @Override
    public void onError(DrmManagerClient arg0, DrmErrorEvent arg1) {
        // TODO Auto-generated method stub

    }
    @Override
    public void onPrepared(MediaPlayer arg0) {
        // TODO Auto-generated method stub
        vv_video.start();

    }
}
