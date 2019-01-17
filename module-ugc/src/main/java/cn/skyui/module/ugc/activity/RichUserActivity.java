package cn.skyui.module.ugc.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chenenyu.router.annotation.Route;
import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.glide.GlideCircleTransform;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.module.ugc.R;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.data.model.user.SimpleUserVO;
import cn.skyui.module.ugc.helper.UiHelper;

import java.util.List;

/**
 * Created by tiansj on 2018/5/5.
 */
@Route("RichUser")
public class RichUserActivity extends BaseSwipeBackActivity {

    private RecyclerView mRecyclerView;
    private BaseQuickAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rich_user);
        initData();
        initView();
        loadData();
    }

    private void initData() {
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        mAdapter = new BaseQuickAdapter<SimpleUserVO, BaseViewHolder>(R.layout.search_list_item, null) {
            @Override
            protected void convert(BaseViewHolder helper, SimpleUserVO item) {
                ImageView imgAvatar = helper.getView(R.id.imgAvatar);
                GlideApp.with(mContext).load(ImageConstants.getSmallUrl(item.getAvatar()))
                        .apply(new RequestOptions().transform(new GlideCircleTransform(getResources().getColor(R.color.gray_light), 2)))
                        .into(imgAvatar);
                helper.setText(R.id.textNickname, item.getNickname());
                helper.setText(R.id.textId, "ID: " + item.getId());
            }
        };
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            SimpleUserVO user = (SimpleUserVO) adapter.getItem(position);
            if(user != null) {
                UiHelper.showUserActivity(mActivity, user.getId(), user.getNickname(), user.getAvatar());
            }
        });
        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        RetrofitFactory.createService(ApiService.class)
                .getRichUserList()
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<List<SimpleUserVO>>() {
                    @Override
                    protected void onSuccess(List<SimpleUserVO> list) {
                        if(list.size() > 0) {
                            mAdapter.setNewData(list);
                        }
                    }

                    @Override
                    protected void onFailure(ApiException e) {
                        super.onFailure(e);
                        mAdapter.loadMoreFail();
                    }
                });
    }
}
