package stcam.stcamproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.Adapter.BaseAdapter;
import stcam.stcamproject.Adapter.MediaDeviceAdapter;
import stcam.stcamproject.R;
/*图库的设备列表*/
public class MediaFragment extends Fragment implements BaseAdapter.OnItemClickListener {
    final static String tag = "MediaFragment";
    List<DevModel> mDevices;
    RecyclerView rv;
    MediaDeviceAdapter adapter;

    public static MediaFragment newInstance() {
        MediaFragment fragment = new MediaFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.activity_media, container, false);
        rv = view.findViewById(R.id.list_view);
        rv.addItemDecoration(new DividerItemDecoration(this.getContext(),DividerItemDecoration.VERTICAL));
        rv.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mDevices = MainDevListFragment.mDevices;
        if (mDevices != null && mDevices.size() > 0){
            if (adapter == null){
                adapter = new MediaDeviceAdapter(mDevices);
                adapter.setOnItemClickListener(this);
                rv.setAdapter(adapter);
            }

        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    void refreshUI(){
        Log.e(tag,"refreshUI");
        mDevices = MainDevListFragment.mDevices;
        if (mDevices != null && mDevices.size() > 0){
            if (adapter == null){
                adapter = new MediaDeviceAdapter(mDevices);
                adapter.setOnItemClickListener(this);
                if (rv != null){
                    rv.setAdapter(adapter);
                }
            }
            else{
                adapter.resetMList(mDevices);
                adapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onItemClick(View view, int position) {


        Intent intent = new Intent(this.getContext(), MediaPhotoListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("devModel",mDevices.get(position));
        intent.putExtras(bundle);

        startActivity(intent);

    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
