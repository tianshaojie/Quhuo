package cn.skyui.module.ugc.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.tbruyelle.rxpermissions2.RxPermissions;
import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.bottomsheet.BottomSheet;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.data.model.User;
import cn.skyui.library.data.model.UserVO;
import cn.skyui.library.event.LoginSuccessEvent;
import cn.skyui.library.event.ugc.UgcEvent;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.glide.GlideCircleTransform;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.library.http.exception.RetryWhenException;
import cn.skyui.library.utils.ScreenUtils;
import cn.skyui.library.utils.SizeUtils;
import cn.skyui.library.utils.StringUtils;
import cn.skyui.library.utils.TimeUtils;
import cn.skyui.library.utils.ToastUtils;
import cn.skyui.module.ugc.R;
import cn.skyui.library.image.viewer.PhotoViewerLayout;
import cn.skyui.library.image.viewer.PhotoPagerViewerLayout;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.data.constant.Constants.UGC;
import cn.skyui.module.ugc.data.model.ugc.UgcAdapterItem;
import cn.skyui.module.ugc.data.model.ugc.UgcItemVO;
import cn.skyui.module.ugc.helper.UiHelper;
import cn.skyui.module.ugc.widget.BackgroundDrawable;
import cn.skyui.module.ugc.widget.ninegridimageview.ItemImageClickListener;
import cn.skyui.module.ugc.widget.ninegridimageview.NineGridImageView;
import cn.skyui.module.ugc.widget.ninegridimageview.NineGridImageViewAdapter;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import io.rong.imkit.ImHelper;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

//import cn.skyui.library.im.ImHelper;

/**
 * Created by tiansj on 2018/4/8.
 */
@Route("user")
public class UserActivity extends BaseSwipeBackActivity {

    Activity mActivity;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private Button mBtnFaceTime;

    private RelativeLayout mLayoutShapeBackground;
    private TextView mTextNickname;
    private LinearLayout mLayoutUserInfo;
    private TextView mTextUid;
    private ImageView mImgSex;
    private TextView mTextAge;
    private TextView mTextCity;
    private ImageView mImgAvatar;
    private FrameLayout mLayoutAttention;
    private ImageView mBtnAttention;
    private LinearLayout mLayoutFansInfo;
    private TextView mTextFansCount;
    private LinearLayout mLayoutAttentionInfo;
    private TextView mTextAttentionCount;
    private TextView mTextDescription;
    private TextView mTextPrice;
    private PhotoPagerViewerLayout photoPagerViewerLayout;
    private PhotoViewerLayout photoViewerLayout;

    List<UgcAdapterItem> ugcAdapterItems;
    private MultipleItemQuickAdapter multipleItemQuickAdapter;

    private Long uid;
    private String nickname;
    private String avatar;
    private Long lastId;
    private boolean isAttention;
    private boolean isBlack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_user);
        registerEventBus();
        initIntentData();
        initData();
        initView();

        httpGetUserInfo();
        loadData();
    }

    private void initIntentData() {
        Intent intent = getIntent();
        uid = intent.getLongExtra("uid", 0L);
        nickname = intent.getStringExtra("nickname");
        avatar = intent.getStringExtra("avatar");
    }

    private void initData() {
        lastId = 0L;
        ugcAdapterItems = new ArrayList<>();
    }


    private void initView() {
        if(uid == 0) {
            finish();
            return;
        }
        mBtnFaceTime = findViewById(R.id.btn_facetime);
        mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
        mRecyclerView = findViewById(R.id.recyclerView);
        mSwipeRefreshLayout.setRefreshing(true);

        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));


        multipleItemQuickAdapter = new MultipleItemQuickAdapter(ugcAdapterItems);
        multipleItemQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);

        multipleItemQuickAdapter.setOnLoadMoreListener(this::loadData, mRecyclerView);
        multipleItemQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);

        mRecyclerView.setAdapter(multipleItemQuickAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            triggerAutoRefresh();
        });
        mBtnFaceTime.setOnClickListener(v -> {
            UiHelper.showFaceTimeActivity(mActivity, uid, nickname, avatar);
        });

        // add header
        View headView = getLayoutInflater().inflate(R.layout.layout_user_header, (ViewGroup) mRecyclerView.getParent(), false);
        multipleItemQuickAdapter.addHeaderView(headView);

        mLayoutShapeBackground = headView.findViewById(R.id.layoutShapeBackground);
        mTextNickname = headView.findViewById(R.id.textNickname);
        mLayoutUserInfo = headView.findViewById(R.id.layoutUserInfo);
        mTextUid = headView.findViewById(R.id.textUid);
        mImgSex = headView.findViewById(R.id.imgSex);
        mTextAge = headView.findViewById(R.id.textAge);
        mTextCity = headView.findViewById(R.id.textCity);
        mImgAvatar = headView.findViewById(R.id.imgAvatar);
        mLayoutAttention = headView.findViewById(R.id.layoutAttention);
        mBtnAttention = headView.findViewById(R.id.btnAttention);
        mLayoutFansInfo = headView.findViewById(R.id.layoutFansInfo);
        mTextFansCount = headView.findViewById(R.id.textFansCount);
        mLayoutAttentionInfo = headView.findViewById(R.id.layoutAttentionInfo);
        mTextAttentionCount = headView.findViewById(R.id.textAttentionCount);
        mTextDescription = headView.findViewById(R.id.textDescription);
        mTextPrice = headView.findViewById(R.id.textPrice);

        BackgroundDrawable drawable = BackgroundDrawable.builder()
                .left(82)//设置左侧斜切点的高度（取值范围是大于0，小于100）
                .right(36)//设置右侧侧斜切点的高度（取值范围是大于0，小于100）
//                .topColor(Color.parseColor("#292836"))//设置上半部分的颜色（默认是白色）
                .topColor(getResources().getColor(R.color.colorPrimary))//设置上半部分的颜色（默认是白色）
//                .bottomColor(Color.parseColor("#f8f8f8"))//设置下半部分的颜色（默认是白色）
                .bottomColor(getResources().getColor(R.color.background_color))//设置下半部分的颜色（默认是白色）
                .build();//调用build进行创建。
        //将这个drawable设置给View
        mLayoutShapeBackground.setBackgroundDrawable(drawable);
        setTitle(nickname + "的主页");
        mTextUid.setText("ID: " + uid);
        mTextNickname.setText(nickname);

        GlideApp.with(mActivity).asBitmap()
                .load(ImageConstants.getMedium800Url(avatar))
                .override(Target.SIZE_ORIGINAL)
                .into(mImgAvatar);

        if(uid == User.getInstance().userId) {
            mBtnFaceTime.setVisibility(View.GONE);
            mLayoutAttention.setVisibility(View.GONE);
        }

        mBtnAttention.setOnClickListener(v -> {
            if(isAttention) {
                httpCancelAttention();
            } else {
                httpAddAttention();
            }
        });

        mLayoutFansInfo.setOnClickListener(v -> {
            UiHelper.showFansActivity(mActivity, uid, nickname, true);
        });

        mLayoutAttentionInfo.setOnClickListener(v -> {
            UiHelper.showFansActivity(mActivity, uid, nickname, false);
        });

        /**
         * SCROLL_STATE_DRAGGING      正在滚动
         * SCROLL_STATE_SETTLING      手指做了抛的动作（手指离开屏幕前，用力滑了一下）
         * SCROLL_STATE_IDLE          停止滚动
         */
        RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    GlideApp.with(mActivity).resumeRequests();
                } else {
                    GlideApp.with(mActivity).pauseRequests();
                }
            }
        };

        mRecyclerView.addOnScrollListener(mOnScrollListener);

        photoPagerViewerLayout = findViewById(R.id.photo_pager_layout);
        photoPagerViewerLayout.init(mActivity).attach(mRecyclerView);

        photoViewerLayout = findViewById(R.id.photo_layout);
        photoViewerLayout.attach(mImgAvatar);
    }

    private void triggerAutoRefresh() {
        initData();
        httpGetUserInfo();
        loadData();
    }

    public void onEventMainThread(UgcEvent.PublishSuccess event) {
        mSwipeRefreshLayout.postDelayed(() -> {
            if(mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(true);
                triggerAutoRefresh();
            }
        }, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlideApp.with(mActivity).resumeRequests();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlideApp.with(mActivity).pauseRequests();
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
            addItemType(UgcAdapterItem.IMAGE, R.layout.layout_ugc_list_item_image);
            addItemType(UgcAdapterItem.VIDEO, R.layout.layout_ugc_list_item_video);
        }

        @Override
        protected void convert(BaseViewHolder helper, UgcAdapterItem adapterItem) {
            UgcItemVO item = adapterItem.getUgcItem();
            TextView title = helper.getView(R.id.tv_content);
            if (StringUtils.isNotEmpty(item.getTitle())) {
                title.setVisibility(View.VISIBLE);
                title.setText(item.getTitle());
            } else {
                title.setVisibility(View.GONE);
            }

            helper.setText(R.id.textNickname, nickname);
            helper.setText(R.id.textTime, TimeUtils.getFriendlyTimeSpanByNow(item.getCreateTime()));
            GlideApp.with(mActivity)
                    .load(ImageConstants.getSmallUrl(avatar))
                    .optionalTransform(new GlideCircleTransform( getResources().getColor(R.color.black), 1))
                    .into((ImageView) helper.getView(R.id.imgAvatar));

            switch (helper.getItemViewType()) {
                case UgcAdapterItem.VIDEO: {
                    ImageView imageView = helper.getView(R.id.imgCover);
                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                    params.height = singleImageSize;
                    params.width = singleImageSize;
                    imageView.setLayoutParams(params);
                    GlideApp.with(mActivity).load(ImageConstants.getOriginUrl(item.getCover())).into(imageView);
                    imageView.setOnClickListener(v -> {
                        UiHelper.showVideoActivity(mActivity, item.getId(), item.getVideo(), item.getCover(),
                                uid, nickname, avatar);
                    });

                    helper.getView(R.id.imgMore).setOnClickListener(v -> {
                        showItemOption(adapterItem.getUgcItem().getId(), helper.getAdapterPosition());
                    });
                    break;
                }
                case UgcAdapterItem.IMAGE: {
                    TextView textViewPoinname = helper.getView(R.id.textPoinname);
                    if(StringUtils.isNotEmpty(item.getPoiname())) {
                        textViewPoinname.setVisibility(View.VISIBLE);
                        textViewPoinname.setText(item.getPoiname());
                    } else {
                        textViewPoinname.setVisibility(View.GONE);
                    }

                    NineGridImageView<String> mNgiContent = helper.getView(R.id.ngl_images);
                    mNgiContent.setSingleImgSize(singleImageSize);
                    mNgiContent.setAdapter(ngiAdapter);

                    List<String> imageList = JSON.parseArray(item.getImages(), String.class);
                    mNgiContent.setImagesData(imageList, NineGridImageView.STYLE_GRID);
                    mNgiContent.setItemImageClickListener(new ItemImageClickListener<String>() {
                        @Override
                        public void onItemImageClick(Context context, ImageView imageView, int index, List<String> list) {
//                            UiHelper.showPhotoActivity(mActivity, (ArrayList<String>) list, index, item.getId());
//                            ImageShowActivity.startImageActivity(mActivity, mNgiContent.getImageViewList(), imageList, index);
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


    private void httpGetUserInfo() {
        RetrofitFactory.createService(ApiService.class)
                .getUserInfo(uid)
                .compose(RxSchedulers.io2main())
                .retryWhen(new RetryWhenException())
                .subscribe(new HttpObserver<UserVO>() {
                    @Override
                    protected void onSuccess(UserVO user) {
                        user.setId(uid);
                        ImHelper.refreshUserInfoCache(String.valueOf(uid), user.getNickname(), user.getAvatar());

                        isBlack = user.getIsBlack();
                        String blackTitle = isBlack ? "取消黑名单" : "加入黑名单";
                        if(menuItemBlack != null) {
                            menuItemBlack.setTitle(blackTitle);
                        }
                        int res = user.getSex() == 1 ? R.drawable.ic_gender_male : R.drawable.ic_gender_female;
                        mImgSex.setImageResource(res);
                        try {
                            mTextAge.setText(String.valueOf(TimeUtils.getAge(TimeUtils.string2Date(user.getBirthday(), new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())))) + "岁");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(StringUtils.isNotEmpty(user.getCity())) {
                            String[] arr = user.getCity().split("-");
                            if(arr.length > 1) {
                                mTextCity.setText(arr[1]);
                            } else {
                                mTextCity.setText(user.getCity());
                            }
                        } else {
                            mTextCity.setText("火星");
                        }
                        if(StringUtils.isNotEmpty(user.getDescription())) {
                            mTextDescription.setText(user.getDescription());
                        }
                        mTextPrice.setText(user.getPrice() + "钻/分钟");
                    }
                });

        RetrofitFactory.createService(ApiService.class)
                .checkIsAttention(uid)
                .compose(RxSchedulers.io2main())
                .retryWhen(new RetryWhenException())
                .subscribe(new HttpObserver<Boolean>() {
                    @Override
                    protected void onSuccess(Boolean response) {
                        isAttention = response;
                        int res = response ? R.drawable.ic_remove_circle_black_24dp : R.drawable.ic_add_circle_black_24dp;
                        mBtnAttention.setImageResource(res);
                    }
                });

        RetrofitFactory.createService(ApiService.class)
                .getAttentionCountByUid(uid)
                .compose(RxSchedulers.io2main())
                .retryWhen(new RetryWhenException())
                .subscribe(new HttpObserver<Integer>() {
                    @Override
                    protected void onSuccess(Integer response) {
                        mTextAttentionCount.setText(String.valueOf(response));
                    }
                });

        RetrofitFactory.createService(ApiService.class)
                .getFansCountByUid(uid)
                .compose(RxSchedulers.io2main())
                .retryWhen(new RetryWhenException())
                .subscribe(new HttpObserver<Integer>() {
                    @Override
                    protected void onSuccess(Integer response) {
                        mTextFansCount.setText(String.valueOf(response));
                    }
                });
    }

    private void loadData() {
        RetrofitFactory.createService(ApiService.class)
                .ugcList(uid, lastId, Constants.DEFAULT_PAGE_SIZE)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<List<UgcItemVO>>() {
                    @Override
                    protected void onSuccess(List<UgcItemVO> list) {
                        if(list.size() < Constants.DEFAULT_PAGE_SIZE) {
                            multipleItemQuickAdapter.loadMoreEnd(false);
                        }  else {
                            multipleItemQuickAdapter.loadMoreComplete();
                        }

                        if(list.size() > 0) {
                            List<UgcAdapterItem> wrapList = new ArrayList<>();
                            for(UgcItemVO itemVO : list) {
                                UgcAdapterItem ugcAdapterItem = new UgcAdapterItem((itemVO.getType() == UGC.TYPE_IMAGE) ? UgcAdapterItem.IMAGE : UgcAdapterItem.VIDEO);
                                ugcAdapterItem.setUgcItem(itemVO);
                                wrapList.add(ugcAdapterItem);
                            }

                            if (lastId == 0) {
                                multipleItemQuickAdapter.setNewData(wrapList);
                            } else {
                                multipleItemQuickAdapter.addData(wrapList);
                            }
                            lastId = list.get(list.size() - 1).getId();
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

    private void httpAddAttention() {
        RetrofitFactory.createService(ApiService.class)
                .attention(uid)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Boolean>() {
                    @Override
                    protected void onSuccess(Boolean response) {
                        isAttention = true;
                        mBtnAttention.setImageResource(R.drawable.ic_remove_circle_black_24dp);
                        ToastUtils.showShort("关注成功");
                    }
                });
    }

    private void httpCancelAttention() {
        RetrofitFactory.createService(ApiService.class)
                .cancelAttention(uid)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Boolean>() {
                    @Override
                    protected void onSuccess(Boolean response) {
                        isAttention = false;
                        mBtnAttention.setImageResource(R.drawable.ic_add_circle_black_24dp);
                        ToastUtils.showShort("已取消关注");
                    }
                });
    }

    private void showItemOption(final long ugcId, final int position) {
        BottomSheet.Builder builder = new BottomSheet.Builder(mActivity);
        builder.addItem(1, "喜欢", getResources().getDrawable(R.drawable.ic_favorite_border_primary_24dp));
        builder.addItem(2, "举报", getResources().getDrawable(R.drawable.ic_error_outline_black_24dp));
        if(uid == User.getInstance().userId) {
            builder.addItem(3, "删除", getResources().getDrawable(R.drawable.ic_delete_primary_24dp));
        }
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
            } else if(id1 == 3) {
                deleteUgc(ugcId, position);
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

    private void deleteUgc(final long ugcId, final int position) {
        new AlertDialog.Builder(this)
                .setMessage("确定要删除内容吗？")
                .setCancelable(true)
                .setNegativeButton("取消", (dialogInterface, i) -> {
                })
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    httpDelete(ugcId, position);
                }).show();
    }

    private void httpDelete(final long ugcId, int position) {
        RetrofitFactory.createService(ApiService.class)
                .delete(ugcId)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
//                        multipleItemQuickAdapter.remove(position);
                        multipleItemQuickAdapter.getData().remove(position-1);
                        multipleItemQuickAdapter.notifyDataSetChanged();
//                        multipleItemQuickAdapter.notifyItemChanged(position);
                        int ugcCount = User.getInstance().detail.getUser().getUgcCount();
                        User.getInstance().detail.getUser().setUgcCount(ugcCount-1);
                        ToastUtils.showShort("内容已删除");
                    }
                });
    }

    MenuItem menuItemBlack;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        menuItemBlack = menu.findItem(R.id.action_black);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 动态设置ToolBar状态
        if(uid == User.getInstance().userId) {
            menu.findItem(R.id.action_publish_photo).setVisible(true);
            menu.findItem(R.id.action_chat).setVisible(false);
            menu.findItem(R.id.action_report).setVisible(false);
            menu.findItem(R.id.action_black).setVisible(false);
        } else {
            menu.findItem(R.id.action_publish_photo).setVisible(false);
            menu.findItem(R.id.action_chat).setVisible(true);
            menu.findItem(R.id.action_report).setVisible(true);
            menu.findItem(R.id.action_black).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_chat) {
            ImHelper.startConversation(mActivity, String.valueOf(uid), nickname);
        }
//        else if (id == R.id.action_qrcode) {
//            ToastUtils.showShort("Coming soon");
//        }
        else if (id == R.id.action_report) {
            httpReport(null);
        } else if (id == R.id.action_black) {
            if(isBlack) {
                httpCancelBlack(item);
            } else {
                httpAddBlack(item);
            }
        } else if (id == R.id.action_publish_photo) {
            actionPublish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("CheckResult")
    private void actionPublish() {
        BottomSheet.Builder builder = new BottomSheet.Builder(mActivity);
        builder.setTitle("发布动态");
        builder.addItem(1, "照片", getResources().getDrawable(R.drawable.ic_insert_photo_primary_24dp));
        builder.addItem(2, "小视频", getResources().getDrawable(R.drawable.ic_photo_camera_primary_24dp));
        // builder.addDivider();
        builder.addItem(3, "取消", getResources().getDrawable(R.drawable.ic_close_primary_24dp));
        BottomSheet bottomSheet = builder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && bottomSheet.getWindow() != null) {
            bottomSheet.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        builder.setOnItemClickListener((parent, view, position, id1) -> {
            if(id1 == 1) {
                Router.build("publish").go(mActivity);
            } else if(id1 == 2) {
                new RxPermissions(mActivity).request(CAMERA, RECORD_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                        .subscribe(permission -> {
                            if(permission) {
                                Router.build("videoRecord").go(mActivity);
                            } else {
                                ToastUtils.showShort("请授权录制和存储权限");
                            }
                        });
            } else if(id1 == 3) {
                bottomSheet.dismiss();
            }
        });
        bottomSheet.show();
    }

    /**
     * 使隐藏菜单项显示icon
     */
    @SuppressLint("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try{
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    private void httpAddBlack(MenuItem item) {
        RetrofitFactory.createService(ApiService.class)
                .addBlack(uid)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        isBlack = true;
                        item.setTitle("取消黑名单");
                        ToastUtils.showShort("已加入黑名单");
                    }
                });
    }

    private void httpCancelBlack(MenuItem item) {
        RetrofitFactory.createService(ApiService.class)
                .cancelBlack(uid)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        isBlack = false;
                        item.setTitle("加入黑名单");
                        ToastUtils.showShort("已取消黑名单");
                    }
                });
    }

    private void httpReport(final Long ugcId) {
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

    @Override
    public void onBackPressed() {
        if (photoPagerViewerLayout.isShowing()) {
            photoPagerViewerLayout.hide();
        } else if (photoViewerLayout.isShowing()) {
            photoViewerLayout.hide();
        } else {
            super.onBackPressed();
        }
    }
}
