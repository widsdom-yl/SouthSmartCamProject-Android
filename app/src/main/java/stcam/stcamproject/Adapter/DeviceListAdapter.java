package stcam.stcamproject.Adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.model.DevModel;

import java.util.List;

import stcam.stcamproject.R;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.MyHolder> {

    private Context mContext;
    private List<DevModel> mDatas;

    public DeviceListAdapter(Context context, List<DevModel> datas) {
        super();
        this.mContext = context;
        this.mDatas = datas;
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
        holder.deviceImageView.setImageResource(R.drawable.imagethumb);
        holder.deviceNameView.setText(model.DevName);
        if(mItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(holder.itemView, position);

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

        public MyHolder(View view) {
            super(view);
            deviceImageView = (ImageView) view.findViewById(R.id.device_image);
            deviceNameView = (TextView) view.findViewById(R.id.device_name_view);
        }

    }

    public interface OnItemClickListener{
        //条目被点击时触发的回调
        void onItemClick(View view,int position);
        //长按时触发的回调
        void onLongClick(View view,int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    OnItemClickListener mItemClickListener;
}

