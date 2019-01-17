package cn.skyui.module.ugc.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.chenenyu.router.annotation.Route;
import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.cache.session.VideoCacheServer;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.data.model.UserVO;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.glide.GlideCircleTransform;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.RetryWhenException;
import cn.skyui.library.utils.StringUtils;
import cn.skyui.library.utils.ToastUtils;
import cn.skyui.module.ugc.R;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.data.model.ugc.UgcDetailVO;
import cn.skyui.module.ugc.helper.UiHelper;
import cn.skyui.module.ugc.widget.VideoView;

import java.lang.reflect.Method;

/**
 * @author tianshaojie
 * @date 2018/3/19
 */
@Route("video")
public class VideoActivity extends BaseSwipeBackActivity {

    private long anchorUid;
    private String anchorNickname;
    private String anchorAvatar;

    private long id;
    private String videoUrl;
    private String videoCoverUrl;

    private VideoView mVideoView;
    private FrameLayout mLayoutAttention;
    private LinearLayout mLayoutUserInfo;
    private TextView mTextNickname;
    private TextView mTextId;
    private Button mBtnFacetime;
    private ImageView mImgAvatar;
    private ImageView mImgAttention;
    private Button mBtnLike;
    private Button mBtnPV;
//    private Button mBtnShare;

    private boolean isLiked;
    private UgcDetailVO ugcDetailVO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();

        anchorUid = intent.getLongExtra("anchorUid", 0);
        anchorNickname = intent.getStringExtra("anchorNickname");
        anchorAvatar = intent.getStringExtra("anchorAvatar");

        id = intent.getLongExtra("id", 0);
        videoUrl = intent.getStringExtra("videoUrl");
        videoCoverUrl = intent.getStringExtra("videoCoverUrl");
    }

    private void initView() {
        mVideoView = findViewById(R.id.videoView);
        mVideoView.setVisibility(View.VISIBLE);
        VideoCacheServer.getInstanceProxy().start();
        String proxyUrl = VideoCacheServer.getInstanceProxy().getProxyUrl(videoUrl);
        mVideoView.setPlayUrl(proxyUrl);
        mVideoView.setVideoCover(videoCoverUrl);
        mVideoView.play();

        mLayoutAttention = findViewById(R.id.layoutAttention);
        mLayoutUserInfo = findViewById(R.id.layoutUserInfo);
        mTextNickname = findViewById(R.id.textNickname);
        mTextId = findViewById(R.id.textId);
        mBtnFacetime = findViewById(R.id.btn_facetime);

        mImgAvatar = findViewById(R.id.imgAvatar);
        mImgAttention = findViewById(R.id.imgAttention);
        mBtnLike = findViewById(R.id.btnLike);
        mBtnPV = findViewById(R.id.btnPV);
//        mBtnShare = findViewById(R.id.btnShare);

        mTextId.setText("ID: " + String.valueOf(anchorUid));
        if(StringUtils.isNotEmpty(anchorAvatar)) {
            GlideApp.with(this).load(ImageConstants.getSmallUrl(anchorAvatar))
                    .apply(new RequestOptions().transform(new GlideCircleTransform(getResources().getColor(R.color.white), 5)))
                    .into(mImgAvatar);
        }
        if(StringUtils.isNotEmpty(anchorNickname)) {
            mTextNickname.setText(anchorNickname);
        } else {
            httpGetUserInfo();
        }

        mImgAttention.setOnClickListener(v -> {
            httpAddAttention();
        });

        mBtnLike.setOnClickListener(v -> {
            if(isLiked) {
                httpCancelLike();
            } else {
                httpLike();
            }
        });

//        mBtnShare.setOnClickListener(v -> {
//            SocialManager.share(mActivity, new UMShareListener() {
//                @Override
//                public void onStart(SHARE_MEDIA share_media) {
//
//                }
//
//                @Override
//                public void onResult(SHARE_MEDIA share_media) {
//
//                }
//
//                @Override
//                public void onError(SHARE_MEDIA share_media, Throwable throwable) {
//
//                }
//
//                @Override
//                public void onCancel(SHARE_MEDIA share_media) {
//
//                }
//            });
//        });

        mImgAvatar.setOnClickListener(v -> {
            UiHelper.showUserActivity(mActivity, anchorUid, anchorNickname, anchorAvatar);
        });

        mLayoutUserInfo.setOnClickListener(v -> {
            UiHelper.showUserActivity(mActivity, anchorUid, anchorNickname, anchorAvatar);
        });

        mBtnFacetime.setOnClickListener(v -> {
            UiHelper.showFaceTimeActivity(mActivity, anchorUid, anchorNickname, anchorAvatar);
        });
    }

    private void httpGetUserInfo() {
        RetrofitFactory.createService(ApiService.class)
                .getUserInfo(anchorUid)
                .compose(RxSchedulers.io2main())
                .retryWhen(new RetryWhenException())
                .subscribe(new HttpObserver<UserVO>() {
                    @Override
                    protected void onSuccess(UserVO user) {
                        anchorNickname = user.getNickname();
                        anchorAvatar = user.getAvatar();
                        GlideApp.with(mActivity).load(ImageConstants.getSmallUrl(user.getAvatar()))
                                .apply(new RequestOptions().transform(new GlideCircleTransform(getResources().getColor(R.color.white), 5)))
                                .into(mImgAvatar);
                        mTextNickname.setText(user.getNickname());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        httpCheckIsAttention();
        httpGetUgcDetail();
    }

    private void httpCheckIsAttention() {
        RetrofitFactory.createService(ApiService.class)
                .checkIsAttention(anchorUid)
                .compose(RxSchedulers.io2main())
                .retryWhen(new RetryWhenException())
                .subscribe(new HttpObserver<Boolean>() {
                    @Override
                    protected void onSuccess(Boolean response) {
                        if(response) {
                            mLayoutAttention.setVisibility(View.GONE);
                        } else {
                            mLayoutAttention.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void httpGetUgcDetail() {
        RetrofitFactory.createService(ApiService.class)
                .getUgcDetail(id)
                .compose(RxSchedulers.io2main())
                .retryWhen(new RetryWhenException())
                .subscribe(new HttpObserver<UgcDetailVO>() {
                    @Override
                    protected void onSuccess(UgcDetailVO response) {
                        isLiked = response.getIsLiked();
                        ugcDetailVO = response;
                        mBtnLike.setText(String.valueOf(response.getLikes()));
                        mBtnPV.setText(String.valueOf(response.getPv()));

                        int res = isLiked ? R.drawable.ic_favorite_accent_32dp : R.drawable.ic_favorite_white_32dp;
                        Drawable drawable= getResources().getDrawable(res);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        mBtnLike.setCompoundDrawables(null, drawable, null, null);
                    }
                });
    }

    private void httpAddAttention() {
        RetrofitFactory.createService(ApiService.class)
                .attention(anchorUid)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Boolean>() {
                    @Override
                    protected void onSuccess(Boolean response) {
                        mLayoutAttention.setVisibility(View.GONE);
                        ToastUtils.showShort("关注成功");
                    }
                });
    }

    private void httpLike() {
        RetrofitFactory.createService(ApiService.class)
                .like(id)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        isLiked = true;
                        Drawable drawable= getResources().getDrawable(R.drawable.ic_favorite_accent_32dp);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        mBtnLike.setCompoundDrawables(null, drawable, null, null);
                        ugcDetailVO.setLikes(ugcDetailVO.getLikes() + 1);
                        mBtnLike.setText(String.valueOf(ugcDetailVO.getLikes()));
                    }
                });
    }

    private void httpCancelLike() {
        RetrofitFactory.createService(ApiService.class)
                .cancelLike(id)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        isLiked = false;
                        Drawable drawable= getResources().getDrawable(R.drawable.ic_favorite_white_32dp);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        mBtnLike.setCompoundDrawables(null, drawable, null, null);
                        ugcDetailVO.setLikes(ugcDetailVO.getLikes() - 1);
                        mBtnLike.setText(String.valueOf(ugcDetailVO.getLikes()));
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoCacheServer.getInstanceProxy().stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_report) {
            httpReport();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 使隐藏菜单项显示icon
     */
    @SuppressLint("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
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

    private void httpReport() {
        RetrofitFactory.createService(ApiService.class)
                .report(anchorUid, id)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        ToastUtils.showShort("已举报");
                    }
                });
    }
}
