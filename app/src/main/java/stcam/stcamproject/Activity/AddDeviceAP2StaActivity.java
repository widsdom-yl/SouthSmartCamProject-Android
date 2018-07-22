package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.model.DevModel;
import com.model.SearchDevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import java.util.List;

import stcam.stcamproject.Adapter.AddDeviceAdapter;
import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.DeviceParseUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;

/*这个类包含了ap 2 sta 和 直接ap*/
public class AddDeviceAP2StaActivity extends BaseAppCompatActivity implements BaseAdapter.OnItemClickListener {
    final static  String tag =  "AddDeviceAP2StaActivity";
    AddDeviceAdapter adapter;
    RecyclerView rv;
    LoadingDialog lod;
    String SearchMsg;
    boolean IsSearching;
    int type;//1:ap2sta 2:ap
    List<SearchDevModel> lists;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_ap2_sta);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
           type = bundle.getInt("type",0);
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_add_ap_sta);

            setCustomTitle(getString(R.string.action_add_ap_sta),true);

        }



        rv = findViewById(R.id.device_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this));


        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));


    }
    @Override
    protected void onResume() {
        super.onResume();
        searchDevices();
    }

    void searchDevices(){
        if (lod == null){
            lod = new LoadingDialog(this);
        }
        lod.dialogShow();

        SouthUtil.showToast(this,"search");
        new Thread()
        {
            @Override
            public void run()
            {
                SearchMsg = lib.thNetSearchDevice(3000, 1);
                ipc.sendMessage(Message.obtain(ipc, TMsg.Msg_SearchOver, 0, 0, null));
                IsSearching = false;
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }


    public final Handler ipc = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {


            super.handleMessage(msg);
            switch (msg.what)
            {
                case TMsg.Msg_SearchOver:
                    lod.dismiss();
                    if (SearchMsg.equals(""))
                    {
                        return;
                    }
                    Log.e(tag,SearchMsg);
                    //[{"SN":"80005556","DevModal":"401H","DevName":"IPCAM_80005556","DevMAC":"00:C1:A1:62:55:56",
                    // "DevIP":"192.168.0.199","SubMask":"255.255.255.0","Gateway":"192.168.0.1","DNS1":"192.168.0.1",
                    // "SoftVersion":"V7.113.1759.00","DataPort":7556,"HttpPort":8556,"rtspPort":554,
                    // "DDNSServer":"211.149.199.247","DDNSHost":"80005556.southtech.xyz","UID":"NULL"}]
                    List<SearchDevModel> list = DeviceParseUtil.parseSearchMsg(SearchMsg);
                    if (list.size()>0){
                        lists = list;
                        adapter = new AddDeviceAdapter(lists);
                        rv.setAdapter(adapter);
                        adapter.setOnItemClickListener(AddDeviceAP2StaActivity.this);
                    }

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button

                return true;
            case R.id.action_search:
                searchDevices();
                default:
                    break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onItemClick(View view, int position) {


        final SearchDevModel model = lists.get(position);


        Intent intent1 = new Intent(STApplication.getInstance(), AddDeviceAP2StaSetup.class);
        Bundle bundle1 = new Bundle();
        //bundle1.putParcelable("devModel",model);

        DevModel devModel = model.exportDevModelForm();

        bundle1.putParcelable("devModel",devModel);
        intent1.putExtras(bundle1);
        startActivity(intent1);



       /* View halfview = getLayoutInflater().inflate(R.layout.half_dialog_view, null);
        final EditText editText = (EditText) halfview.findViewById(R.id.dialog_edit);
        if (dbModel == null){
            editText.setText("admin");
        }
        else{
            editText.setText(dbModel.pwd);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_add_ap_sta))//设置对话框的标题
                .setView(halfview)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = editText.getText().toString();
                        if (content.length() < 4 ){
                            SouthUtil.showToast(AddDeviceAP2StaActivity.this,"pwd too short");
                            return;
                        }
                        else{
                            if (dbModel == null){
                                Log.e(tag,"----2,change pwd ,new pwd is  "+content);
                                DevModel devModel = new DevModel();
                                devModel.SN = model.getSN();
                                devModel.usr = "admin";//默认填写admin
                                devModel.pwd = content;
                                boolean ret  = DataManager.getInstance().addDev(devModel);
                                Log.e(tag,"addDev ,ret is "+ret);
                            }
                            else{
                                Log.e(tag,"----3,change pwd ,new pwd is  "+content);
                                dbModel.pwd = content;
                                boolean ret = DataManager.getInstance().updateDev(dbModel);
                                Log.e(tag,"updateDev ,ret is "+ret);
                            }

                            Intent intent1 = new Intent(STApplication.getInstance(), AddDeviceAP2StaSetup.class);
                            Bundle bundle1 = new Bundle();
                            //bundle1.putParcelable("devModel",model);

                            DevModel devModel = model.exportDevModelForm();
                            devModel.pwd = content;
                            bundle1.putParcelable("devModel",devModel);
                            intent1.putExtras(bundle1);
                            startActivity(intent1);

                        }
                        // Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).create()*/



    }



    @Override
    public void onLongClick(View view, int position) {

    }
}
