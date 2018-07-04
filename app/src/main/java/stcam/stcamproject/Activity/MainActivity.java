package stcam.stcamproject.Activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;

import stcam.stcamproject.Config.Config;
import stcam.stcamproject.R;

public class MainActivity extends FragmentActivity {
    Fragment f1;
    LinearLayout content_container;
    private TextView tx1, tx2, tx3,tx4;
    ImageButton image_1,image_2,image_3,image_4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Window window = this.getWindow();
//        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        //设置状态栏颜色
//        window.setStatusBarColor(Config.greenColor);

        setContentView(R.layout.activity_main);

        StatusBarCompat.setStatusBarColor(this, Config.greenColor, true);


        init();
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_container,f1)
                    .commit();
        }
        changeHomepage();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
    private void init(){
        content_container = (LinearLayout) findViewById(R.id.content_container);

        tx1 = findViewById(R.id.tx_1);
        tx2 = findViewById(R.id.tx_2);
        tx3 = findViewById(R.id.tx_3);
        tx4 = findViewById(R.id.tx_4);

        image_1 = findViewById(R.id.image_1);
        image_2 = findViewById(R.id.image_2);
        image_3 = findViewById(R.id.image_3);
        image_4 = findViewById(R.id.image_4);


        f1 = MainDevListFragment.newInstance(MainDevListFragment.EnumMainEntry.EnumMainEntry_Login);

    }

    private void changeHomepage() {
        getSupportFragmentManager().beginTransaction()
                .show(f1)
                .commit();


        this.settab();
        tx1.setTextColor(Config.greenColor);
        image_1.setSelected(true);

    }
    public void settab(){

        //
        tx1.setTextColor(Color.parseColor("#666666"));
        tx2.setTextColor(Color.parseColor("#666666"));
        tx3.setTextColor(Color.parseColor("#666666"));
        tx4.setTextColor(Color.parseColor("#666666"));
        image_1.setSelected(false);
        image_2.setSelected(false);
        image_3.setSelected(false);
        image_4.setSelected(false);

    }


}
