package stcam.stcamproject.Adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.Activity.MainDevListFragment;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;

import static stcam.stcamproject.Activity.MainDevListFragment.EnumMainEntry.EnumMainEntry_Login;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.MyHolder> {

    private Context mContext;
    private List<DevModel> mDatas;
    private MainDevListFragment.EnumMainEntry mEntryType;

    public static String tag = "DeviceListAdapter";
    public DeviceListAdapter(Context context, List<DevModel> datas,MainDevListFragment.EnumMainEntry entryType) {
        super();
        this.mContext = context;
        this.mDatas = datas;
        mEntryType = entryType;
    }
    public void setmDatas(List<DevModel> datas){
        mDatas = datas;
        this.notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return mDatas.size();
    }

    @Override
    // 填充onCreateViewHolder方法返回的holder中的控件
    public void onBindViewHolder(final MyHolder holder, final int position) {
        // TODO Auto-generated method stub
        //holder.imageView.setImageResource(mDatas.get(position));
        DevModel model = mDatas.get(position);
//        GlideApp.with(mContext)
//                .asBitmap()
//                .load("")
//                .placeholder(R.drawable.imagethumb)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(holder.deviceImageView);
        List<String> files= FileUtil.getSNFilesFromPath(FileUtil.pathThumb(),model.SN);
        if (files.size()>0){

            Bitmap bitmap = BitmapFactory.decodeFile(files.get(0));
            holder.deviceImageView.setImageBitmap(bitmap);
        }
        else{
            holder.deviceImageView.setImageResource(R.drawable.imagethumb);
        }

        DevModel chooseModel = null;
//1111111111111111111
        for (DevModel tmpNode : MainDevListFragment.mDevices){
            if (model.SN.equals(tmpNode.SN)){
                chooseModel = model;
                chooseModel.ConnType = tmpNode.ConnType;
                chooseModel.NetHandle = tmpNode.NetHandle;

                Log.e(tag,"NetConn reloadview get exist model:"+chooseModel.SN+",isconnect is "+chooseModel.IsConnect()+",mdevice size "+MainDevListFragment.mDevices.size()+",handel is "+chooseModel.NetHandle);
                break;
            }
        }

        if (chooseModel == null){
            return;
        }
        Log.e(tag,"NetConn reloadview sn:"+chooseModel.SN);
        if (chooseModel.IsConnect()){
            if (chooseModel.getConnectType() == DevModel.CONNECT_TYPE.IS_CONN_P2P){
                holder.online_status_tx.setTextColor(Color.rgb(0, 255, 0));
                holder.online_status_img.setImageResource(R.drawable.shape_online_p2p);
            }
            else if(chooseModel.getConnectType() == DevModel.CONNECT_TYPE.IS_CONN_DDNS){
                holder.online_status_tx.setTextColor(Color.rgb(0, 0x55, 0xaa));
                holder.online_status_img.setImageResource(R.drawable.shape_online_ddns);
            }
            else{
                holder.online_status_tx.setTextColor(Color.rgb(0x22, 0x77, 0x11));
                holder.online_status_img.setImageResource(R.drawable.shape_online_lan);
            }
           // 227711
            holder.online_status_tx.setText(chooseModel.getConnectTypeDesc());

        }
        else{
            holder.online_status_tx.setText(R.string.status_offline);
            holder.online_status_tx.setTextColor(Color.rgb(255, 0, 0));
           // holder.online_status_img.setImageResource(R.drawable.shape_offline);
            holder.online_status_img.setImageResource(R.drawable.shape_offline);

        }

        //holder.deviceNameView.setText(model.DevName);
        holder.deviceNameView.setText(model.GetDevName());//zhb
        if(mItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(holder.itemView, position,0);

                }
            });
            holder.shareBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(holder.shareBtn, position,1);

                }
            });

            holder.playBackBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(holder.playBackBtn, position,2);

                }
            });

            holder.settingBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(holder.settingBtn, position,3);

                }
            });
            if (chooseModel.IsShare == 1){
                holder.shareBtn.setVisibility(View.VISIBLE);
            }
            else{
                holder.shareBtn.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    // 重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    public MyHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        // 填充布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_device, null);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // 定义内部类继承ViewHolder
    class MyHolder extends RecyclerView.ViewHolder {

        private ImageView deviceImageView;
        private ImageView online_status_img;
        private TextView deviceNameView;
        private ImageButton shareBtn;
        private ImageButton playBackBtn;
        private ImageButton settingBtn;
        private TextView online_status_tx;

        public MyHolder(View view) {
            super(view);
            deviceImageView = view.findViewById(R.id.device_image);
            deviceNameView =  view.findViewById(R.id.device_name_view);
            online_status_img = view.findViewById(R.id.online_status_img);
            shareBtn = view.findViewById(R.id.btn_device_share);
            playBackBtn = view.findViewById(R.id.btn_device_play_record);
            settingBtn = view.findViewById(R.id.btn_device_setting);
            online_status_tx = view.findViewById(R.id.online_status_tx);

            if (mEntryType == EnumMainEntry_Login){
                shareBtn.setVisibility(View.VISIBLE);
            }
            else {
               // shareBtn.setText(R.string.action_AP_T_STA);

                shareBtn.setVisibility(View.INVISIBLE);
            }
        }

    }

    public interface OnItemClickListener{
        //条目被点击时触发的回调
        //tpe:0-item被点击 1-share被点击 2-回放被点击 3-设置被点击
        void onItemClick(View view,int position,int tpe);
        //长按时触发的回调
        void onLongClick(View view,int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    OnItemClickListener mItemClickListener;
}

