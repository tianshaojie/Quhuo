package cn.skyui.module.ugc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chenenyu.router.annotation.Route;
import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.data.model.User;
import cn.skyui.library.data.model.UserVO;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.glide.GlideCircleTransform;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.HttpResponse;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.module.ugc.R;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.helper.UiHelper;

import java.util.List;

/**
 * Created by tiansj on 2018/4/11.
 */
@Route("fans")
public class FansActivity extends BaseSwipeBackActivity {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BaseQuickAdapter mAdapter;
    private Long uid;
    private String nickname;
    private int pageNum;

    private boolean isFans;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fans);
        initData();
        initIntentData();
        initView();
        loadData();
    }

    private void initData() {
        pageNum = 1;
    }

    private void initIntentData() {
        Intent intent = getIntent();
        uid = intent.getLongExtra("uid", 0L);
        nickname = intent.getStringExtra("nickname");
        isFans = intent.getBooleanExtra("isFans", true);
    }

    private void initView() {
        if(uid == 0) {
            finish();
            return;
        }
        String name = uid == User.getInstance().userId ? "我" : nickname;
        String title = isFans ? "的粉丝" : "的关注";
        setTitle(name + title);
        mRecyclerView = findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
//        mRecyclerView.addItemDecoration(new RecycleViewDivider(
//                mActivity, LinearLayoutManager.HORIZONTAL, 1, getResources().getColor(R.color.gray_light)));

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        mAdapter = new BaseQuickAdapter<UserVO, BaseViewHolder>(R.layout.fans_list_item, null) {
            @Override
            protected void convert(BaseViewHolder helper, UserVO item) {
                ImageView imgAvatar = helper.getView(R.id.imgAvatar);
                Button btnAttention = helper.getView(R.id.btnAttention);
                GlideApp.with(mContext).load(ImageConstants.getSmallUrl(item.getAvatar()))
                        .apply(new RequestOptions().transform(new GlideCircleTransform(getResources().getColor(R.color.gray_light), 2)))
                        .into(imgAvatar);
                helper.setText(R.id.textNickname, item.getNickname());
                helper.setText(R.id.textId, "ID: " + item.getId());

                if(uid == User.getInstance().userId) {
                    btnAttention.setVisibility(View.VISIBLE);
                } else {
                    btnAttention.setVisibility(View.GONE);
                }

                btnAttention.setText(item.getIsAttention() ? "已关注" : "未关注");
                btnAttention.setOnClickListener(v -> {
                    if(item.getIsAttention()) {
                        RetrofitFactory.createService(ApiService.class)
                                .cancelAttention(uid)
                                .compose(RxSchedulers.io2main())
                                .subscribe(new HttpObserver<Boolean>() {
                                    @Override
                                    protected void onSuccess(Boolean response) {
                                        item.setIsAttention(false);
                                        btnAttention.setText("未关注");
                                    }
                                });
                    } else {
                        RetrofitFactory.createService(ApiService.class)
                                .attention(uid)
                                .compose(RxSchedulers.io2main())
                                .subscribe(new HttpObserver<Boolean>() {
                                    @Override
                                    protected void onSuccess(Boolean response) {
                                        item.setIsAttention(true);
                                        btnAttention.setText("已关注");
                                    }
                                });
                    }
                });
            }
        };

        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            UserVO user = (UserVO) adapter.getItem(position);
            if(user != null) {
                UiHelper.showUserActivity(mActivity, user.getId(), user.getNickname(), user.getAvatar());
            }
        });

        mAdapter.setOnLoadMoreListener(this::loadData, mRecyclerView);
        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);

        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            initData();
            loadData();
        });

//        View footerView = getLayoutInflater().inflate(R.layout.head_view, (ViewGroup) mRecyclerView.getParent(), false);
//        mAdapter.addFooterView(footerView);
//        mAdapter.setLoadMoreView(new HomeLoadMoreView());

        mSwipeRefreshLayout.setRefreshing(true);
    }

    private void loadData() {
        io.reactivex.Observable<HttpResponse<List<UserVO>>> observable;
        if(isFans) {
            observable = RetrofitFactory.createService(ApiService.class)
                    .getFansListByUid(uid, pageNum, Constants.DEFAULT_PAGE_SIZE);
        } else {
            observable = RetrofitFactory.createService(ApiService.class)
                    .getAttentionListByUid(uid, pageNum, Constants.DEFAULT_PAGE_SIZE);
        }
        observable.compose(RxSchedulers.io2main())
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
