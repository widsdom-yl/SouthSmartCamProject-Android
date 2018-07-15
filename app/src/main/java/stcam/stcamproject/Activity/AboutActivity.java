package stcam.stcamproject.Activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.model.UpdateModel;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;
import stcam.stcamproject.network.ServerNetWork;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    TextView text_version;
    
    RelativeLayout layout_checkupdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_about);

        }


        setContentView(R.layout.activity_about);

        layout_checkupdate = findViewById(R.id.layout_checkupdate);
        layout_checkupdate.setOnClickListener(this);

        text_version = findViewById(R.id.text_version);
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0);

            String localVersion = packageInfo.versionName;
            text_version.setText(getString(R.string.app_name)+":v"+localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
    public void onClick(View view) {
        if (view.getId() == R.id.layout_checkupdate) {
            if (lod == null) {
                lod = new LoadingDialog(this);
            }

            lod.dialogShow();
            ServerNetWork.getCommandApi()
                    .app_upgrade_app_check(1,"android")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer_check_update);
        }
    }

    Observer<UpdateModel> observer_check_update = new Observer<UpdateModel>() {
        @Override
        public void onCompleted() {
            lod.dismiss();
            Log.e(tag,"---------------------2");
        }
        @Override
        public void onError(Throwable e) {
            lod.dismiss();
            Log.e(tag,"---------------------1:"+e.getLocalizedMessage());


        }

        @Override
        public void onNext(UpdateModel m) {
            lod.dismiss();
            if (m != null){
                Log.e(tag,"---------------------onNext:"+m.ver);

                try {
                    PackageInfo packageInfo = null;
                    packageInfo = AboutActivity.this.getApplicationContext()
                            .getPackageManager()
                            .getPackageInfo(AboutActivity.this.getPackageName(), 0);

                    String localVersion = packageInfo.versionName;

                    if (!compareStringValue(localVersion,m.ver)){
                        SouthUtil.showDialog(AboutActivity.this,AboutActivity.this.getString(R.string.string_current_older));
                    }
                    else{
                        SouthUtil.showDialog(AboutActivity.this,AboutActivity.this.getString(R.string.string_current_newer));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }



        }
    };

    /*如果是最新版本，返回true*/
    boolean compareStringValue(String localVersion,String serverVersion){
        String[] localVersions = localVersion.split(".");
        String[] serverVersions = serverVersion.split(".");
        if (localVersions.length == 3 && serverVersions.length == 3){
            for (int i = 0;i<localVersions.length ;++i){
                try {
                    int local = Integer.parseInt(localVersions[i]);
                    int server = Integer.parseInt(serverVersions[i]);
                    if (local > server){
                        return true;
                    }
                    else if (local < server){
                        return false;
                    }
                }
                 catch (NumberFormatException e){
                        Log.e(tag,"---------------------NumberFormatException:"+e.toString());
                    }
            }
        }
        return true;
    }
    LoadingDialog lod;
    static final String tag =  "AboutActivity";
}
