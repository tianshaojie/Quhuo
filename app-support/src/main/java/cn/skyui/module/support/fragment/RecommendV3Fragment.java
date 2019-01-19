package cn.skyui.module.support.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.skyui.library.base.fragment.BaseFragment;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.library.utils.SizeUtils;
import cn.skyui.library.utils.TimeUtils;
import cn.skyui.library.widget.recyclerview.GridSpacingItemDecoration;
import cn.skyui.module.support.R;
import cn.skyui.module.support.helper.UiHelper;
import cn.skyui.module.support.widget.loadmore.CustomLoadMoreView;
import cn.skyui.module.support.data.ApiService;
import cn.skyui.module.ugc.data.model.banner.BannerVO;
import cn.skyui.module.ugc.data.model.user.RecommendItemVO;
import cn.skyui.module.ugc.data.model.user.RecommendVO;

public class RecommendV3Fragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BaseQuickAdapter mAdapter;
    private int pageNum = 1;
    private List<BannerVO> banners;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_newest, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view);
        loadData();
    }

    private void initData() {
        pageNum = 1;
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2, GridLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, SizeUtils.dp2px(3f),false));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        mAdapter = new BaseQuickAdapter<RecommendItemVO, BaseViewHolder>(R.layout.home_recommend_list_item, null) {
            @Override
            protected void convert(BaseViewHolder helper, RecommendItemVO item) {
                helper.getView(R.id.layoutPlay).setVisibility(item.getType() == 1 ? View.GONE : View.VISIBLE);
                String url = item.getType() == 1 ? item.getAvatar() : item.getCover();
                GlideApp.with(mContext).load(ImageConstants.getSmallUrl(url)).into((ImageView) helper.getView(R.id.avatar));
                helper.setText(R.id.textNickname, item.getNickname());
                try {
                    String age = String.valueOf(TimeUtils.getAge(TimeUtils.string2Date(item.getBirthday(), new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())))) + "岁";
                    helper.setText(R.id.textAge, age);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ImageView imageView = helper.getView(R.id.imgStatus);
                if(item.getOnlineStatus() == cn.skyui.module.ugc.data.constant.Constants.User.ONLINE_STATUS_YES) {
                    imageView.setImageResource(R.drawable.ic_fiber_manual_record_green_24dp);
                } else if(item.getOnlineStatus() == cn.skyui.module.ugc.data.constant.Constants.User.ONLINE_STATUS_NO) {
                    imageView.setImageResource(R.drawable.ic_fiber_manual_record_gray_24dp);
                } else if(item.getOnlineStatus() == cn.skyui.module.ugc.data.constant.Constants.User.ONLINE_STATUS_BUSY) {
                    imageView.setImageResource(R.drawable.ic_fiber_manual_record_red_24dp);
                }
            }
        };

        mAdapter.setOnLoadMoreListener(this::loadData, mRecyclerView);
        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mAdapter.setOnItemClickListener((adapter, view1, position) -> {
            RecommendItemVO recommendItemVO = (RecommendItemVO) mAdapter.getItem(position);
            if(recommendItemVO.getType() == 1) {
                UiHelper.showUserActivity(mActivity, recommendItemVO.getUid(), recommendItemVO.getNickname(), recommendItemVO.getAvatar());
            } else {
                cn.skyui.module.ugc.helper.UiHelper.showVideoActivity(mActivity,
                        recommendItemVO.getId(), recommendItemVO.getVideo(), recommendItemVO.getCover(),
                        recommendItemVO.getUid(), recommendItemVO.getNickname(), recommendItemVO.getAvatar());
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            initData();
            loadData();
        });

        mAdapter.setLoadMoreView(new CustomLoadMoreView());
        mSwipeRefreshLayout.setRefreshing(true);
    }

    // add header
    private void addHeaderView() {
        Banner banner = (Banner) getLayoutInflater().inflate(R.layout.head_view,
                (ViewGroup) mRecyclerView.getParent(), false);
        //设置图片加载器
        banner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                GlideApp.with(context).load(ImageConstants.getOriginUrl(path.toString())).into(imageView);
            }
        });
        List<String> images = new ArrayList<>();
        for(BannerVO bannerVO : banners) {
            images.add(bannerVO.getImgUrl());
        }
        //设置图片集合
        banner.setImages(images);
        // 设置banner动画效果
        banner.setBannerAnimation(Transformer.DepthPage);
        // 设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //设置轮播时间
        banner.setDelayTime(3000);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.setOnBannerListener(position -> UiHelper.showWebViewActivity(mActivity, banners.get(position).getLink()));
        //banner设置方法全部调用完毕时最后调用
        banner.start();
        mAdapter.addHeaderView(banner);
    }

    private void loadData() {
        RetrofitFactory.createService(ApiService.class)
                .recommendListV3(pageNum, Constants.DEFAULT_PAGE_SIZE)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<RecommendVO>() {
                    @Override
                    protected void onSuccess(RecommendVO response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        List<RecommendItemVO> list = response.getData();

                        if(list.size() < Constants.DEFAULT_PAGE_SIZE) {
                            mAdapter.loadMoreEnd(false);
                        }  else {
                            mAdapter.loadMoreComplete();
                        }

                        if (pageNum == 1) {
                            mAdapter.removeAllHeaderView();
                            banners = response.getBanners();
                            if(banners != null && banners.size() > 0) {
                                addHeaderView();
                            }
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
