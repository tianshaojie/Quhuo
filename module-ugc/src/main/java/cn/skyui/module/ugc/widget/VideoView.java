package cn.skyui.module.ugc.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.media.player.base.impl.IjkPlayerCore;
import cn.skyui.library.media.player.controller.ShortVideoController;
import cn.skyui.library.media.player.helper.ShortVideoTransHelper;
import cn.skyui.library.media.player.view.PlayerView;
import cn.skyui.library.utils.NetworkUtils;
import cn.skyui.library.utils.ScreenUtils;
import cn.skyui.library.utils.StringUtils;
import cn.skyui.module.ugc.R;

import de.greenrobot.event.EventBus;

/**
 * Created by liuli on 2017/2/20.
 */
public class VideoView extends FrameLayout implements
        ShortVideoController.OnStateListener,
        SeekBar.OnSeekBarChangeListener,
        View.OnClickListener {

    private PlayerView playerView;
    private ShortVideoController controller;
    private ImageView mImageCover;
    private static final int MESSAGE_RESUMEVIDEO = 11;
    private static final int MESSAGE_STARTCOUNT = 10;
    private TextView mtvPlay;
    private ProgressBar mProgressBar;
    private final static int SCALETYPE_FITCENTER = 2;
    private final static int SCALETYPE_CENTERCROP = 1;
    private final static int SCALETYPE_FITROOM = 3;
    private int mScaleType = SCALETYPE_CENTERCROP;

    private boolean mMute;
    private boolean mLooping;
    private boolean isDetail;
    private String mPlayUrl;

    NetworkUtils.NetworkType  networkType;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            int what = message.what;
            if (what == MESSAGE_RESUMEVIDEO) {
                resume();
            } else if (what == MESSAGE_STARTCOUNT) {
                pause();
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(MESSAGE_RESUMEVIDEO, 100);
            }
            return false;
        }
    });
    private ValueAnimator valueAnimator;


    /***
     * 获取播放地址的回调
     */
    public interface LFLiteGetVideoUrlListener {
        void start();

        void end(String playUrl);
    }

    private LFLiteGetVideoUrlListener mLFLiteGetVideoUrlListener;

    public void setLFLiteGetVideoUrlListener(LFLiteGetVideoUrlListener l) {
        this.mLFLiteGetVideoUrlListener = l;
    }

    public interface LFLiteVideoListener {
        void onStart();

        void onComplete();

        void onPlay();
    }

    private LFLiteVideoListener listener;

    public void setListener(LFLiteVideoListener listener) {
        this.listener = listener;
    }

    public VideoView(Context context) {
        super(context);
        init(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context) {
        init(context, null);
    }

    public void init(final Context context, AttributeSet attrs) {
//        EventBus.getDefault().register(this);
        View.inflate(context, R.layout.layout_lite_video, this);
        playerView = (PlayerView) findViewById(R.id.lf_lite_video_player);
        mImageCover = (ImageView) findViewById(R.id.lf_lite_video_cover);
        mProgressBar = (ProgressBar) findViewById(R.id.lf_lite_progress);
        mtvPlay = (TextView) findViewById(R.id.lf_tv_player);
        mtvPlay.setOnClickListener(this);
        mProgressBar.setMax(1000);
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.LF_lite_video_view);
        mScaleType = t.getInt(R.styleable.LF_lite_video_view_lf_liteVideo_scaleType, 1);
        mMute = t.getBoolean(R.styleable.LF_lite_video_view_lf_liteVideo_mute, false);
        mLooping = t.getBoolean(R.styleable.LF_lite_video_view_lf_liteVideo_looping, false);
        isDetail = t.getBoolean(R.styleable.LF_lite_video_view_lf_liteVideo_isDetail, false);
        if (!isDetail) {
            FrameLayout.LayoutParams layoutParams = (LayoutParams) mProgressBar.getLayoutParams();
            layoutParams.gravity = Gravity.BOTTOM;
            mProgressBar.setLayoutParams(layoutParams);
        } else {
            mtvPlay.setVisibility(GONE);
        }
        t.recycle();
        this.setClickable(true);
        playerView.setOnClickListener(null);
        playerView.setClickable(false);
        networkType = NetworkUtils.getNetworkType();
    }


    public void play() {
        if (!NetworkUtils.isConnected()) {
            mtvPlay.setText("网络不稳定，加载失败。刷新试试吧!");
            return;
        }
//        if (networkType != NetworkUtils.NetworkType.NETWORK_WIFI) {
//            return;
//        }
        if (controller == null) {
            if (initPlayer()) {
                doPlay(mPlayUrl);
            }
        } else {
            doPlay(mPlayUrl);
        }
    }

    private void doPlay(String s) {
        if (listener != null) {
            listener.onStart();
        }
        if (mLFLiteGetVideoUrlListener != null) {
            mLFLiteGetVideoUrlListener.end(s);
        }
        if (controller != null) {
            mPlayUrl = s;
            doPlayAnimation();
            controller.setUrl(s);
            controller.play();

        }
    }

    private void doPlayAnimation() {
        mProgressBar.setProgress(0);
        mtvPlay.setVisibility(GONE);
    }

    public void setFailedInfo(String info) {
        if (mtvPlay != null && !StringUtils.isEmpty(info)) {
            mtvPlay.setText(info);
            mtvPlay.setVisibility(VISIBLE);
        }
    }

    private boolean initPlayer() {
        try {
            controller = new ShortVideoController();
            controller.setPlayerCore(new IjkPlayerCore());
            controller.setPlayerView(playerView);
            controller.setStateListener(this);
            controller.init(getContext());
            controller.setOpenPlayerLog(false);
            //小视频详情页
            if (isDetail) {
                controller.setTransMaxSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
            } else {
                controller.setTransMaxSize(getMeasuredWidth(), getMeasuredHeight());
            }
            controller.mute(mMute);
            controller.setLooping(mLooping);
            if (mScaleType == SCALETYPE_CENTERCROP) {
                controller.setTransType(ShortVideoTransHelper.Type.FILL);
            } else if (mScaleType == SCALETYPE_FITCENTER) {
                controller.setTransType(ShortVideoTransHelper.Type.WRAP);
            } else if (mScaleType == SCALETYPE_FITROOM) {
                controller.setTransType(ShortVideoTransHelper.Type.ROOM);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setVideoCover(String coverUrl) {
        GlideApp.with(getContext()).load(ImageConstants.getOriginUrl(coverUrl)).into(mImageCover);
    }

    public void release() {
        release(false);
    }

    public void release(boolean clearSurface) {
        if (controller == null) {
            return;
        }
        mHandler.removeCallbacksAndMessages(null);
        cancleAnimate();
        controller.release(clearSurface);
        controller = null;
    }

    public void pause() {
        if (controller != null) {
            controller.pause();
        }
    }

    public void resume() {
        if (controller != null) {
            controller.start();
        }
    }

    public boolean isPlaying() {
        if (controller != null) {
            return controller.isPlaying();
        }
        return false;
    }

    private void cancleAnimate() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (!isDetail) {
            mtvPlay.setVisibility(VISIBLE);
        }
        mtvPlay.setText("");
        mImageCover.setVisibility(VISIBLE);
    }


    @Override
    public synchronized void onPlay() {
        mImageCover.setVisibility(GONE);
        if (controller == null) {
            return;
        }
        if (listener != null) {
            listener.onPlay();
        }
    }

    @Override
    public void onLoad() {
        excuteLoadStart();
    }

    private void excuteLoadStart() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MESSAGE_STARTCOUNT, 5000);
    }

    private void excuteLoadEnd() {
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onEndLoad() {
        excuteLoadEnd();
    }

    @Override
    public void onComplete() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.setIntValues(mProgressBar.getProgress(), 1000);
            valueAnimator.start();
        }

        if (listener != null) {
            listener.onComplete();
        }
    }

    ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mProgressBar.setProgress((int) animation.getAnimatedValue());
        }
    };

    @Override
    public void onProgressUpdate(int i) {
        doProgressAnimator(i);
    }

    private void doProgressAnimator(final int i) {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(mProgressBar.getProgress(), i);
        } else {
            valueAnimator.cancel();
            if (i < mProgressBar.getProgress()) {
                mProgressBar.setProgress(0);
                return;
            }

            valueAnimator.setIntValues(mProgressBar.getProgress(), i);
        }

        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(mAnimatorUpdateListener);
        valueAnimator.start();
    }

    @Override
    public void onTimeTextUpdate(String s) {

    }


    @Override
    public void onError() {
        excuteLoadEnd();
        mtvPlay.setVisibility(GONE);
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {
        excuteLoadEnd();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.lf_tv_player) {
            play();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        release();
    }

    /**
     * 网络变更 todo
     */
//    public void onEventMainThread(ConnectivityChangedEvent event) {
//        NetworkState.ConnectivityType connectivityType = event.getConnectivityType();
//        if (connectivityType == NetworkState.ConnectivityType.WIFI) {
//            MyLog.d(TAG, "Network WIFI");
//            conntype = "WIFI";
//
//        } else if (connectivityType == NetworkState.ConnectivityType.MOBILE) {
//            MyLog.d(TAG, "Network Mobile");
//            conntype = "4G";
//        } else {
//            MyLog.d(TAG, "Network None");
//        }
//    }


    public void setPlayUrl(String playUrl) {
        mPlayUrl = playUrl;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }
}
