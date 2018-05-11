package stcam.stcamproject.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.model.DevModel;
import com.model.SearchDevModel;
import com.thSDK.TMsg;
import com.thSDK.lib;

import java.util.ArrayList;
import java.util.List;

import stcam.stcamproject.Adapter.AddDeviceAdapter;
import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.DeviceParseUtil;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.LoadingDialog;

/*这个类包含了ap 2 sta 和 直接ap*/
public class AddDeviceAP2StaActivity extends AppCompatActivity implements BaseAdapter.OnItemClickListener {
    final static  String tag =  "AddDeviceAP2StaActivity";
    AddDeviceAdapter adapter;
    RecyclerView rv;
    ArrayList<DevModel> mDevices;
    LoadingDialog lod;
    String SearchMsg;
    boolean IsSearching;
    int type;//1:ap2sta 2:ap
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
            if (1 == type)
                actionBar.setTitle(R.string.action_add_ap_sta);
            else if (2 == type)
                actionBar.setTitle(R.string.action_add_ap);
        }

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

        rv = findViewById(R.id.device_list_view);
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this));

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
                    List<SearchDevModel> lists = DeviceParseUtil.parseSearchMsg(SearchMsg);
                    if (lists.size()>0){
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
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
