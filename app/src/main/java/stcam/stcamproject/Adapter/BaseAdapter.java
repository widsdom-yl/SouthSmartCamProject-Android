package stcam.stcamproject.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BaseAdapter<T> extends RecyclerView.Adapter<BaseHolder> {
    private List<T> mList = new ArrayList<>();
    private int layoutId;
    public BaseAdapter(int layoutId,List<T> list){
        this.layoutId=layoutId;
        this. mList=list;
    }
    public void resetMList(List<T> list){
        this. mList=list;
    }
    //onCreateViewHolder用来给rv创建缓存
    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //参数3 判断条件 true  1.打气 2.添加到paraent
        // false 1.打气 （参考parent的宽度）
        View view =   LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        BaseHolder holder = new BaseHolder(view);
        return holder;
    }
    //onBindViewHolder给缓存控件设置数据
    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        T item = mList.get(position);
        convert(holder,item,position);
    }
    protected void convert(final BaseHolder holder, T item, final int position) {
        //什么都没有做
        if(mItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(holder.itemView, position);

                }
            });
        }
    }
    //获取记录数据
    @Override
    public int getItemCount() {
        return mList.size();
    }
    public interface OnItemClickListener{
        //条目被点击时触发的回调
        //tpe:0-item被点击 1-share被点击 2-回放被点击 3-设置被点击
        void onItemClick(View view,int position);
        //长按时触发的回调
        void onLongClick(View view,int position);
    }
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }
    OnItemClickListener mItemClickListener;
}
