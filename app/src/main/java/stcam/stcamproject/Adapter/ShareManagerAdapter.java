package stcam.stcamproject.Adapter;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.model.UserModel;

import java.util.List;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;
import stcam.stcamproject.Util.SouthUtil;
import stcam.stcamproject.View.SlidingButtonView;



public class ShareManagerAdapter extends BaseAdapter<UserModel> implements SlidingButtonView.IonSlidingButtonListener {

    private SlidingButtonView mMenu;
    public ShareManagerAdapter( List<UserModel> list) {
        super(R.layout.share_manager_list_delete_view, list);
    }

    protected void convert(final BaseHolder holder, UserModel model, int position) {
        super.convert(holder,model,position);

        RelativeLayout layout_content = holder.getView(R.id.layout_content);


        SlidingButtonView slidingButtonView = holder.getView(R.id.slidingButtonView);

        slidingButtonView.setSlidingButtonListener(this);

        holder.setText(R.id.file_name_text,model.User);


        layout_content.getLayoutParams().width = SouthUtil.getScreenWidth(STApplication.getInstance());

        TextView tv_Delete = holder.getView(R.id.tv_delete);
        tv_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int n = holder.getLayoutPosition();

                if (mDeleteClickListener != null)
                    mDeleteClickListener.onDeleteBtnCilck(view,n);
            }
        });

        layout_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (menuIsOpen()) {
                    closeMenu();//关闭菜单
                }
            }
        });
    }

    @Override
    public void onMenuIsOpen(View view) {
        mMenu = (SlidingButtonView) view;
    }

    @Override
    public void onDownOrMove(SlidingButtonView slidingDeleteView) {
        if(menuIsOpen())
        {
            if(mMenu != slidingDeleteView)
            {
                closeMenu();
            }
        }
    }

    /**
     * 关闭菜单
     */
    public void closeMenu() {
        mMenu.closeMenu();
        mMenu = null;

    }
    /**
     * 判断是否有菜单打开
     */
    public Boolean menuIsOpen() {
        if(mMenu != null){
            return true;
        }
        return false;
    }

    public void setmDeleteClickListener(DeleteClickListener listener) {
        mDeleteClickListener = listener;
    }

    public interface DeleteClickListener {
        void onDeleteBtnCilck(View view,int position);
    }

    DeleteClickListener mDeleteClickListener;
}


