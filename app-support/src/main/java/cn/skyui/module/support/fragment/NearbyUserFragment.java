package cn.skyui.module.support.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.orhanobut.logger.Logger;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.skyui.library.base.fragment.BaseFragment;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.data.model.User;
import cn.skyui.library.data.model.UserLocation;
import cn.skyui.library.data.model.UserVO;
import cn.skyui.library.event.LocationChangeEvent;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.library.utils.BeanMapUtils;
import cn.skyui.library.utils.LocationUtils;
import cn.skyui.library.utils.SizeUtils;
import cn.skyui.library.utils.TimeUtils;
import cn.skyui.library.utils.ToastUtils;
import cn.skyui.library.widget.recyclerview.GridSpacingItemDecoration;
import cn.skyui.module.support.R;
import cn.skyui.module.support.helper.UiHelper;
import cn.skyui.module.support.widget.loadmore.CustomLoadMoreView;
import cn.skyui.module.support.data.ApiService;
import cn.skyui.module.support.data.model.NearbyParam;
import de.greenrobot.event.EventBus;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * @author tianshaojie
 * @date 2018/2/8
 */
public class NearbyUserFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BaseQuickAdapter mAdapter;
    private UserLocation userLocation;
    private int pageNum = 1;
    public int nearbyType = 0;
    public long zrevrangeMax;
    private HashMap<Long, Void> userMap;

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
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if(userLocation != null) {
            loadData();
        } else {
            autoLocation();
        }
    }

    private void initData() {
        userLocation = User.getInstance().location;
        pageNum = 1;
        nearbyType = 0;
        zrevrangeMax = System.currentTimeMillis();
        if(userMap != null) {
            userMap.clear();
        } else {
            userMap = new HashMap<>();
        }
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2, GridLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, SizeUtils.dp2px(3f),false));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        mAdapter = new BaseQuickAdapter<UserVO, BaseViewHolder>(R.layout.home_newest_list_item, null) {
            @Override
            protected void convert(BaseViewHolder helper, UserVO item) {
                GlideApp.with(mContext).load(ImageConstants.getSmallUrl(item.getAvatar())).into((ImageView) helper.getView(R.id.avatar));
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

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(final BaseQuickAdapter adapter, final View view, final int position) {
                UserVO userVO = (UserVO) mAdapter.getItem(position);
                UiHelper.showUserActivity(mActivity, userVO.getId(), userVO.getNickname(), userVO.getAvatar());
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            initData();
            loadData();
        });

        mAdapter.setLoadMoreView(new CustomLoadMoreView());
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @SuppressLint("CheckResult")
    private void autoLocation() {
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
                .getNearbyUserList(BeanMapUtils.toMap(nearbyParam))
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<List<UserVO>>() {
                    @Override
                    protected void onSuccess(List<UserVO> list) {
                        int size = list.size();
                        if(nearbyType == 1 && size < Constants.DEFAULT_PAGE_SIZE) {
                            mAdapter.loadMoreEnd(false);
                        } else if(size >= Constants.DEFAULT_PAGE_SIZE) {
                            mAdapter.loadMoreComplete();
                        }

                        if(size > 0) {
                            List<UserVO> noRepeatList = new ArrayList<>();
                            for(UserVO userVO : list) {
                                if(!userMap.containsKey(userVO.getId())) {
                                    noRepeatList.add(userVO);
                                }
                                userMap.put(userVO.getId(), null);
                            }
                            if (pageNum == 1) {
                                mAdapter.setNewData(noRepeatList);
                            } else {
                                mAdapter.addData(noRepeatList);
                            }
                            zrevrangeMax = list.get(size - 1).getTime() - 1;
                            pageNum++;
                        } else if(pageNum == 1) {
                            mAdapter.setNewData(null);
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
                        mAdapter.loadMoreFail();
                    }
                });
    }

    public void onEventMainThread(LocationChangeEvent event) {
        UserLocation newLocation = User.getInstance().location;
        if(userLocation == null || !userLocation.getPoiname().equals(newLocation.getPoiname())) {
            mSwipeRefreshLayout.post(() -> {
                mSwipeRefreshLayout.setRefreshing(true);
                initData();
                loadData();
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
