package cn.skyui.module.ugc.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chenenyu.router.annotation.Route;
import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.data.model.UserVO;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.glide.GlideCircleTransform;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.module.ugc.R;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.helper.UiHelper;

import java.util.List;

/**
 * Created by tiansj on 2018/5/5.
 */
@Route("blacklist")
public class BlacklistActivity extends BaseSwipeBackActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private BaseQuickAdapter mAdapter;
    private int pageNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);
        initData();
        initView();
        loadData();
    }

    private void initData() {
        pageNum = 1;
    }

    private void initView() {
        mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        mAdapter = new BaseQuickAdapter<UserVO, BaseViewHolder>(R.layout.search_list_item, null) {
            @Override
            protected void convert(BaseViewHolder helper, UserVO item) {
                ImageView imgAvatar = helper.getView(R.id.imgAvatar);
                GlideApp.with(mContext).load(ImageConstants.getSmallUrl(item.getAvatar()))
                        .apply(new RequestOptions().transform(new GlideCircleTransform(getResources().getColor(R.color.gray_light), 2)))
                        .into(imgAvatar);
                helper.setText(R.id.textNickname, item.getNickname());
                helper.setText(R.id.textId, "ID: " + item.getId());
            }
        };
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            UserVO user = (UserVO) adapter.getItem(position);
            if(user != null) {
                UiHelper.showUserActivity(mActivity, user.getId(), user.getNickname(), user.getAvatar());
            }
        });
        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            initData();
            loadData();
        });
        mSwipeRefreshLayout.setRefreshing(true);
    }

    private void loadData() {
        RetrofitFactory.createService(ApiService.class)
                .blacklist(pageNum, Constants.DEFAULT_PAGE_SIZE)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<List<UserVO>>() {
                    @Override
                    protected void onSuccess(List<UserVO> list) {
                        mSwipeRefreshLayout.setRefreshing(false);

                        if (list.size() < Constants.DEFAULT_PAGE_SIZE) {
                            mAdapter.loadMoreEnd(false);
                        } else {
                            mAdapter.loadMoreComplete();
                        }

                        if (pageNum == 1) {
                            mAdapter.setNewData(list);
                        } else {
                            mAdapter.addData(list);
                        }

                        pageNum++;
                    }

                    @Override
                    protected void onFailure(ApiException e) {
                        super.onFailure(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mAdapter.loadMoreFail();
                    }
                });
    }
}
