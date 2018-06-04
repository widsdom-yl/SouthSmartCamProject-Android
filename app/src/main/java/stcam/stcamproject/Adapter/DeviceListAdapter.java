package stcam.stcamproject.Adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.Activity.MainDevListActivity;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.FileUtil;

import static stcam.stcamproject.Activity.MainDevListActivity.EnumMainEntry.EnumMainEntry_Login;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.MyHolder> {

    private Context mContext;
    private List<DevModel> mDatas;
    private MainDevListActivity.EnumMainEntry mEntryType;


    public DeviceListAdapter(Context context, List<DevModel> datas,MainDevListActivity.EnumMainEntry entryType) {
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
        List<String> files= FileUtil.getImagePathFromPath(FileUtil.pathSnapShot(),model.SN);
        if (files.size()>0){

            Bitmap bitmap = BitmapFactory.decodeFile(files.get(0));
            holder.deviceImageView.setImageBitmap(bitmap);
        }
        else{
            holder.deviceImageView.setImageResource(R.drawable.imagethumb);
        }

        DevModel chooseModel = null;

        for (DevModel existModel : MainDevListActivity.mDevices){
            if (model.SN.equals(existModel.SN)){
                chooseModel = existModel;
                break;
            }
        }


        if (chooseModel.IsConnect()){
            holder.online_status_tx.setText(R.string.status_online);
            holder.online_status_tx.setTextColor(Color.rgb(0, 255, 0));
        }
        else{
            holder.online_status_tx.setText(R.string.status_offline);
            holder.online_status_tx.setTextColor(Color.rgb(255, 0, 0));
        }
        holder.deviceNameView.setText(model.DevName);
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
        private TextView deviceNameView;
        private Button shareBtn;
        private Button playBackBtn;
        private Button settingBtn;
        private TextView online_status_tx;

        public MyHolder(View view) {
            super(view);
            deviceImageView = view.findViewById(R.id.device_image);
            deviceNameView =  view.findViewById(R.id.device_name_view);
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

