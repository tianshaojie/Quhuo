package cn.skyui.module.support.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.skyui.library.base.fragment.BaseFragment;
import cn.skyui.library.bottomsheet.BottomSheet;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.data.model.User;
import cn.skyui.library.data.model.UserLocation;
import cn.skyui.library.event.LocationChangeEvent;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.glide.GlideCircleTransform;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.library.image.viewer.PhotoPagerViewerLayout;
import cn.skyui.library.utils.BeanMapUtils;
import cn.skyui.library.utils.LocationUtils;
import cn.skyui.library.utils.ScreenUtils;
import cn.skyui.library.utils.SizeUtils;
import cn.skyui.library.utils.StringUtils;
import cn.skyui.library.utils.TimeUtils;
import cn.skyui.library.utils.ToastUtils;
import cn.skyui.module.support.R;
import cn.skyui.module.support.data.ApiService;
import cn.skyui.module.support.data.model.NearbyParam;
import cn.skyui.module.ugc.data.model.ugc.FeedVO;
import cn.skyui.module.ugc.data.model.ugc.UgcAdapterItem;
import cn.skyui.module.ugc.data.model.ugc.UgcItemVO;
import cn.skyui.module.ugc.data.model.user.SimpleUserVO;
import cn.skyui.module.ugc.helper.UiHelper;
import cn.skyui.module.ugc.widget.ninegridimageview.ItemImageClickListener;
import cn.skyui.module.ugc.widget.ninegridimageview.NineGridImageView;
import cn.skyui.module.ugc.widget.ninegridimageview.NineGridImageViewAdapter;
import de.greenrobot.event.EventBus;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * @author tianshaojie
 * @date 2018/1/27
 */
public class NearbyFeedFragment extends BaseFragment {

    private View root;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private PhotoPagerViewerLayout photoPagerViewerLayout;

    private UserLocation userLocation;
    private Long uid;
    private Long lastId;
    public int nearbyType = 0;
    public long zrevrangeMax;
    private List<UgcAdapterItem> ugcAdapterItems;
    private MultipleItemQuickAdapter multipleItemQuickAdapter;
    private HashMap<Long, Void> feedMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return root = inflater.inflate(R.layout.fragment_nearby_feed, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initData();
        initView();
        if(userLocation != null) {
            loadData();
        } else {
            requestLocation();
        }
    }

    private void initData() {
        uid = User.getInstance().userId;
        userLocation = User.getInstance().location;
        lastId = 0L;
        ugcAdapterItems = new ArrayList<>();
        nearbyType = 0;
        zrevrangeMax = System.currentTimeMillis();
        if(feedMap != null) {
            feedMap.clear();
        } else {
            feedMap = new HashMap<>();
        }
    }

    private void initView() {
        mSwipeRefreshLayout = root.findViewById(R.id.swipeLayout);
        mRecyclerView = root.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout.setRefreshing(true);

        final LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        mRecyclerView.setLayoutManager(manager);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));


        multipleItemQuickAdapter = new MultipleItemQuickAdapter(ugcAdapterItems);
        multipleItemQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);

        multipleItemQuickAdapter.setOnLoadMoreListener(this::loadData, mRecyclerView);
        multipleItemQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);

        mRecyclerView.setAdapter(multipleItemQuickAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            initData();
            loadData();
        });

        photoPagerViewerLayout = mActivity.findViewById(R.id.photo_pager_nearby);
        photoPagerViewerLayout.init(mActivity).attach(mRecyclerView);;
    }


    @SuppressLint("CheckResult")
    private void requestLocation() {
        new RxPermissions(mActivity).request(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                .delay(300, TimeUnit.MILLISECONDS)
                .compose(bindToLifecycle())
                .subscribe(permission -> {
                    if(!permission) {
                        ToastUtils.showShort("授定位权限，可以认识更多附近的人噢");
                        loadData();
                    } else {
                        updateLocation();
                    }
                });
    }

    private void updateLocation() {
        LocationUtils.getInstance().startLocation(new LocationUtils.OnLocationChangedListener() {
            @Override
            public void onSuccess(AMapLocation mapLocation) {
                if(mapLocation != null && StringUtils.isNotEmpty(mapLocation.getCityCode())) {
                    Logger.i("location success");
                    User.getInstance().location = JSON.parseObject(mapLocation.toStr(), UserLocation.class);
                    RetrofitFactory.createService(ApiService.class)
                            .updateLocation(Base64.encodeToString(mapLocation.toStr().getBytes(), Base64.DEFAULT))
                            .compose(bindToLifecycle())
                            .observeOn(Schedulers.io())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new HttpObserver<Void>() {
                                @Override
                                protected void onSuccess(Void response) {
                                    Logger.i("update user location success!");
                                }
                            });
                }
                loadData();
            }

            @Override
            public void onFail(int errCode, String errInfo) {
                Logger.e("location failed");
                loadData();
            }
        });
    }

    private void loadData() {
        Logger.e("nearby feed loadData-------------------");
        NearbyParam nearbyParam = new NearbyParam();
        nearbyParam.setUid(User.getInstance().userId);
        if(User.getInstance().location == null) {
            nearbyType = 1;
            nearbyParam.setCity("010");
        } else {
            nearbyParam.setCity(User.getInstance().location.getCitycode());
            nearbyParam.setLat(User.getInstance().location.getLat());
            nearbyParam.setLon(User.getInstance().location.getLon());
        }
        nearbyParam.setType(nearbyType);
        nearbyParam.setTime(zrevrangeMax);

        RetrofitFactory.createService(ApiService.class)
                .getNearbyFeedList(BeanMapUtils.toMap(nearbyParam))
                .compose(bindToLifecycle())
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<List<FeedVO>>() {
                    @Override
                    protected void onSuccess(List<FeedVO> list) {
                        int size = list.size();
                        if(nearbyType == 1 && size < Constants.DEFAULT_PAGE_SIZE) {
                            multipleItemQuickAdapter.loadMoreEnd(false);
                        } else if(size >= Constants.DEFAULT_PAGE_SIZE) {
                            multipleItemQuickAdapter.loadMoreComplete();
                        }

                        if(size > 0) {
                            List<FeedVO> noRepeatList = new ArrayList<>();
                            for(FeedVO feedVO : list) {
                                if(!feedMap.containsKey(feedVO.getUgcItem().getId())) {
                                    noRepeatList.add(feedVO);
                                }
                                feedMap.put(feedVO.getUgcItem().getId(), null);
                            }

                            List<UgcAdapterItem> wrapList = new ArrayList<>();
                            for(FeedVO feedVO : noRepeatList) {
                                UgcItemVO itemVO = feedVO.getUgcItem();
                                itemVO.setDistance(feedVO.getDistance());
                                itemVO.setPoiname(feedVO.getPoiname());
                                UgcAdapterItem ugcAdapterItem = new UgcAdapterItem(
                                        (itemVO.getType() == cn.skyui.module.ugc.data.constant.Constants.UGC.TYPE_IMAGE)
                                                ? UgcAdapterItem.IMAGE : UgcAdapterItem.VIDEO);
                                ugcAdapterItem.setUgcItem(itemVO);
                                ugcAdapterItem.setUser(feedVO.getUser());
                                ugcAdapterItem.setNearbyType(feedVO.getNearbyType());
                                wrapList.add(ugcAdapterItem);
                            }

                            if (lastId == 0) {
                                multipleItemQuickAdapter.setNewData(wrapList);
                            } else {
                                multipleItemQuickAdapter.addData(wrapList);
                            }
                            lastId = list.get(size - 1).getUgcItem().getId();
                            zrevrangeMax = list.get(size-1).getTime()-1;
                        } else if(lastId == 0) {
                            multipleItemQuickAdapter.setNewData(null);
                        } else {
                            multipleItemQuickAdapter.loadMoreEnd(false);
                        }

                        if(nearbyType == 0 && size < Constants.DEFAULT_PAGE_SIZE) {
                            nearbyType = 1;
                            zrevrangeMax = System.currentTimeMillis();
                            loadData();
                        }

                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    protected void onFailure(ApiException e) {
                        super.onFailure(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        multipleItemQuickAdapter.loadMoreFail();
                    }
                });
    }

    private NineGridImageViewAdapter<String> ngiAdapter = new NineGridImageViewAdapter<String>() {
        @Override
        protected void onDisplayImage(Context context, ImageView imageView, String s) {
            final RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .dontTransform();
            GlideApp.with(mActivity)
                    .load(ImageConstants.getSmallUrl(s))
                    .apply(options)
                    .placeholder(R.drawable.ic_default_image)
                    .error(R.drawable.ic_default_image)
                    .into(imageView);

        }

        @Override
        protected ImageView generateImageView(Context context) {
            return super.generateImageView(context);
        }

        @Override
        protected void onItemImageClick(Context context, ImageView imageView, int index, List<String> list) {
        }

        @Override
        protected boolean onItemImageLongClick(Context context, ImageView imageView, int index, List<String> list) {
            return true;
        }
    };

    public class MultipleItemQuickAdapter extends BaseMultiItemQuickAdapter<UgcAdapterItem, BaseViewHolder> {

        int singleImageSize;

        public MultipleItemQuickAdapter(List<UgcAdapterItem> data) {
            super(data);
            singleImageSize = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(22)) / 2;
            addItemType(UgcAdapterItem.IMAGE, R.layout.layout_feed_item_image);
            addItemType(UgcAdapterItem.VIDEO, R.layout.layout_feed_item_video);
        }

        @Override
        protected void convert(BaseViewHolder helper, UgcAdapterItem adapterItem) {
            UgcItemVO item = adapterItem.getUgcItem();
            SimpleUserVO user = adapterItem.getUser();
            helper.getView(R.id.imgAvatar).setOnClickListener(v -> {
                UiHelper.showUserActivity(mActivity, user.getId(), user.getNickname(), user.getAvatar());
            });
            helper.getView(R.id.textNickname).setOnClickListener(v -> {
                UiHelper.showUserActivity(mActivity, user.getId(), user.getNickname(), user.getAvatar());
            });

            TextView title = helper.getView(R.id.tv_content);
            if (StringUtils.isNotEmpty(item.getTitle())) {
                title.setVisibility(View.VISIBLE);
                title.setText(item.getTitle());
            } else {
                title.setVisibility(View.GONE);
            }

            helper.setText(R.id.textNickname, user.getNickname());
            helper.setText(R.id.textTime, TimeUtils.getFriendlyTimeSpanByNow(item.getCreateTime()));
            GlideApp.with(mActivity).load(ImageConstants.getSmallUrl(user.getAvatar()))
                    .apply(new RequestOptions().transform(new GlideCircleTransform( getResources().getColor(R.color.black), 1)))
                    .into((ImageView) helper.getView(R.id.imgAvatar));

            TextView textViewDistance = helper.getView(R.id.textDistance);
            // 0：附近5公里，1：同城
            if(adapterItem.getNearbyType() == 1 && User.getInstance().location != null) {
                textViewDistance.setText("同城");
                textViewDistance.setVisibility(View.VISIBLE);
            } else if(item.getDistance() > 0) {
                textViewDistance.setVisibility(View.VISIBLE);
                textViewDistance.setText(item.getDistance() + "KM");
            } else {
                textViewDistance.setVisibility(View.GONE);
            }
            TextView textViewPoinname = helper.getView(R.id.textPoinname);
            if(StringUtils.isNotEmpty(item.getPoiname())) {
                textViewPoinname.setVisibility(View.VISIBLE);
                textViewPoinname.setText(item.getPoiname());
            } else {
                textViewPoinname.setVisibility(View.GONE);
            }
            switch (helper.getItemViewType()) {
                case UgcAdapterItem.VIDEO: {
                    ImageView imageView = helper.getView(R.id.imgCover);
                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                    params.height = singleImageSize;
                    params.width = singleImageSize;
                    imageView.setLayoutParams(params);
                    GlideApp.with(mActivity).load(ImageConstants.getMedium800Url(item.getCover())).into(imageView);
                    imageView.setOnClickListener(v -> {
                        UiHelper.showVideoActivity(mActivity, item.getId(), item.getVideo(), item.getCover(),
                                user.getId(), user.getNickname(), user.getAvatar());
                    });

                    helper.getView(R.id.imgMore).setOnClickListener(v -> {
                        showItemOption(adapterItem.getUgcItem().getId(), helper.getAdapterPosition());
                    });
                    break;
                }
                case UgcAdapterItem.IMAGE: {
                    NineGridImageView<String> mNgiContent = helper.getView(R.id.ngl_images);
                    mNgiContent.setSingleImgSize(singleImageSize);
                    mNgiContent.setAdapter(ngiAdapter);

                    List<String> imageList = JSON.parseArray(item.getImages(), String.class);
                    mNgiContent.setImagesData(imageList, NineGridImageView.STYLE_GRID);
                    mNgiContent.setItemImageClickListener(new ItemImageClickListener<String>() {
                        @Override
                        public void onItemImageClick(Context context, ImageView imageView, int index, List<String> list) {
//                            UiHelper.showPhotoActivity(mActivity, (ArrayList<String>) list, index, item.getId());
                            photoPagerViewerLayout
                                    .setUgcId(item.getId())
                                    .setAdapterPosition(helper.getAdapterPosition())
                                    .setImageViews(mNgiContent.getImageViewList())
                                    .setPhotos(list)
                                    .setPhotoPosition(index)
                                    .show();
                        }
                    });

                    helper.getView(R.id.imgMore).setOnClickListener(v -> {
                        showItemOption(adapterItem.getUgcItem().getId(), helper.getAdapterPosition());
                    });
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }


    private void showItemOption(final long ugcId, final int position) {
        BottomSheet.Builder builder = new BottomSheet.Builder(mActivity);
        builder.addItem(1, "喜欢", getResources().getDrawable(R.drawable.ic_favorite_border_primary_24dp));
        builder.addItem(2, "举报", getResources().getDrawable(R.drawable.ic_error_outline_black_24dp));
        builder.addItem(4, "取消", getResources().getDrawable(R.drawable.ic_close_primary_24dp));
        BottomSheet bottomSheet = builder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && bottomSheet.getWindow() != null) {
            bottomSheet.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        builder.setOnItemClickListener((parent, view, position1, id1) -> {
            if(id1 == 1) {
                httpLike(ugcId);
            } else if(id1 == 2) {
                httpReport(ugcId);
            } else if(id1 == 4) {
                bottomSheet.dismiss();
            }
        });
        bottomSheet.show();
    }

    private void httpLike(final long ugcId) {
        RetrofitFactory.createService(ApiService.class)
                .like(ugcId)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        ToastUtils.showShort("已添加到喜欢");
                    }
                });
    }

    private void httpReport(final long ugcId) {
        RetrofitFactory.createService(ApiService.class)
                .report(uid, ugcId)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        ToastUtils.showShort("已举报");
                    }
                });
    }

    public void onEventMainThread(LocationChangeEvent event) {
        UserLocation newLocation = User.getInstance().location;
        if(userLocation == null || !userLocation.getPoiname().equals(newLocation.getPoiname())) {
            mSwipeRefreshLayout.post(() -> {
                if(!mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
                initData();
                loadData();
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getView()).setFocusableInTouchMode(true);
        Objects.requireNonNull(getView()).requestFocus();
        Objects.requireNonNull(getView()).setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (photoPagerViewerLayout.isShowing()) {
                    photoPagerViewerLayout.hide();
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}