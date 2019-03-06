package cn.skyui.module.ugc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chenenyu.router.annotation.Route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.library.image.viewer.PhotoPagerViewerLayout;
import cn.skyui.library.utils.SizeUtils;
import cn.skyui.library.widget.recyclerview.SpaceItemDecoration;
import cn.skyui.module.ugc.R;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.data.constant.Constants.UGC;
import cn.skyui.module.ugc.data.model.ugc.UgcItemVO;
import cn.skyui.module.ugc.data.model.ugc.UgcLikeVO;
import cn.skyui.module.ugc.helper.UiHelper;

/**
 * Created by tiansj on 2018/4/13.
 */
@Route("likes")
public class LikesActivity extends BaseSwipeBackActivity {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BaseQuickAdapter mAdapter;
    private PhotoPagerViewerLayout photoPagerViewerLayout;

    private Long uid;
    private String nickname;

    private Long lastId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        initData();
        initIntentData();
        initView();
        loadData();
    }

    private void initData() {
        lastId = 0L;
    }

    private void initIntentData() {
        Intent intent = getIntent();
        uid = intent.getLongExtra("uid", 0L);
        nickname = intent.getStringExtra("nickname");
    }

    private void initView() {
        if(uid == 0) {
            finish();
            return;
        }
        setTitle(nickname + "的喜欢");

        mRecyclerView = findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(3, SizeUtils.dp2px(2f)));

        mAdapter = new BaseQuickAdapter<UgcLikeVO, BaseViewHolder>(R.layout.layout_ugc_list_item, null) {
            @Override
            protected void convert(BaseViewHolder helper, UgcLikeVO item) {
                UgcItemVO ugcItem = item.getUgcItem();
                if(ugcItem == null) {
                    return;
                }
                TextView textView = helper.getView(R.id.text_photo_count);
                textView.setVisibility(View.GONE);
                String url;
                if(ugcItem.getType() == UGC.TYPE_IMAGE) {
                    List<String> imageList = JSON.parseArray(ugcItem.getImages(), String.class);
                    url = imageList.get(0);
                    if(imageList.size() > 1) {
                        textView.setText(String.valueOf(imageList.size()));
                        textView.setVisibility(View.VISIBLE);
                    }
                } else {
                    url = ugcItem.getCover();
                }

                ImageView imageView = helper.getView(R.id.avatar);

                final RequestOptions options = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .dontTransform();
                GlideApp.with(mActivity)
                        .load(ImageConstants.getSmallUrl(url))
                        .apply(options)
                        .placeholder(R.drawable.ic_default_image)
                        .error(R.drawable.ic_default_image)
                        .into(imageView);

//                GlideApp.with(mContext).load(ImageConstants.getSmallUrl(url)).into((ImageView) helper.getView(R.id.avatar));

                FrameLayout frameLayout = helper.getView(R.id.layoutPlay);
                if(ugcItem.getType() == UGC.TYPE_VIDEO) {
                    frameLayout.setVisibility(View.VISIBLE);
                } else {
                    frameLayout.setVisibility(View.GONE);
                }

                imageView.setOnClickListener(v -> {
                    if(ugcItem.getType() == UGC.TYPE_VIDEO) {
                        UiHelper.showVideoActivity(mActivity, ugcItem.getId(), ugcItem.getVideo(), ugcItem.getCover(),
                                ugcItem.getUid(), "", "");
                    } else {
                        List<String> images = JSON.parseArray(ugcItem.getImages(), String.class);
//                      UiHelper.showPhotoActivity(mActivity, (ArrayList<String>) images, 0, item.getId());
                        List<ImageView> views = new ArrayList<>();
                        views.add(imageView);
                        photoPagerViewerLayout
                                .setUgcId(ugcItem.getId())
                                .setAdapterPosition(helper.getAdapterPosition())
                                .setImageViews(views)
                                .setPhotos(images)
                                .setPhotoPosition(0)
                                .show();
                    }
                });
            }
        };

        mAdapter.setOnLoadMoreListener(this::loadData, mRecyclerView);
        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);

        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            initData();
            loadData();
        });
        mSwipeRefreshLayout.setRefreshing(true);

        photoPagerViewerLayout = findViewById(R.id.photo_pager_layout);
        photoPagerViewerLayout.init(mActivity).attach(mRecyclerView);
    }


    private void loadData() {
        RetrofitFactory.createService(ApiService.class)
                .ugcLikes(uid, lastId, Constants.DEFAULT_PAGE_SIZE)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<List<UgcLikeVO>>() {
                    @Override
                    protected void onSuccess(List<UgcLikeVO> list) {
                        if(list.size() < Constants.DEFAULT_PAGE_SIZE) {
                            mAdapter.loadMoreEnd(false);
                        }  else {
                            mAdapter.loadMoreComplete();
                        }

                        if(list.size() > 0) {
                            // 防止ugc已删除
                            Iterator<UgcLikeVO> iterator = list.iterator();
                            while (iterator.hasNext()) {
                                UgcLikeVO vo = iterator.next();
                                if(vo.getUgcItem() == null) {
                                    iterator.remove();;
                                }
                            }

                            if (lastId == 0) {
                                mAdapter.setNewData(list);
                            } else {
                                mAdapter.addData(list);
                            }

                            lastId = list.get(list.size() - 1).getUserContentLike().getId();
                        }

                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    protected void onFailure(ApiException e) {
                        super.onFailure(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mAdapter.loadMoreFail();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (photoPagerViewerLayout.isShowing()) {
            photoPagerViewerLayout.hide();
        } else {
            super.onBackPressed();
        }
    }

}
