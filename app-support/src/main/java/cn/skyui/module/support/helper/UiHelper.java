package cn.skyui.module.support.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.chenenyu.router.Router;

import cn.skyui.library.web.activity.WebViewActivity;

/**
 * 所有页面跳转中转站
 * @author tianshaojie
 * @date 2018/2/26
 */
public class UiHelper {

    public static void showWebViewActivity(Activity activity, String url) {
        Intent intent = new Intent();
        intent.putExtra("url", url);
        intent.setClass(activity, WebViewActivity.class);
        activity.startActivity(intent);
    }

    public static void showCustomWebViewActivity(Activity activity, String url) {
        Intent intent = new Intent();
        intent.putExtra("url", url);
        intent.setClass(activity, WebViewActivity.class);
        activity.startActivity(intent);
    }

    public static void showFansActivity(Context context, Long uid, String nickname, boolean isFans) {
        Router.build("fans").with("uid", uid).with("nickname", nickname).with("isFans", isFans).go(context);
    }

    public static void showUserActivity(Context context, Long uid, String nickname, String avatar) {
        Router.build("user")
                .with("uid", uid).with("nickname", nickname).with("avatar", avatar)
                .go(context);
    }

    public static void showUserLikesActivity(Context context, Long uid, String nickname) {
        Router.build("likes")
                .with("uid", uid).with("nickname", nickname)
                .go(context);
    }

    public static void showFaceTimeActivity(Context context, long anchorUid, String anchorNickname, String anchorAvatar) {
        Router.build("facetime")
                .with("chatType", 1)
                .with("anchorUid", anchorUid)
                .with("anchorNickname", anchorNickname)
                .with("anchorAvatar", anchorAvatar)
                .go(context);
    }

    public static void showProfileActivity(Context context) {
        Router.build("profile").go(context);
    }

    public static void showPublishActivity(Context context) {
        Router.build("publish").go(context);
    }

    public static void showVideoPlayActivity(Context context) {
        String videoUrl = "https://vii-img.oss-cn-beijing.aliyuncs.com/user_3955335_video_20180412101941351-943_0.mp4";
        String videoCoverUrl = "https://vii-img.oss-cn-beijing.aliyuncs.com/user_3955335_video_cover_20180412101940127-3704_0.png";
        Router.build("video").with("videoUrl", videoUrl).with("videoCoverUrl", videoCoverUrl).go(context);
    }

    private void showPracticeActivity(Context context) {
        Router.build("practice").go(context);
    }

    public static void showSearchActivity(Context context) {
        Router.build("search").go(context);
    }

    public static void showRichUserActivity(Context context) {
        Router.build("RichUser").go(context);
    }

    public static void showRechargeActivity(Context context) {
        Router.build("recharge").go(context);
    }

    public static void showIncomeActivity(Context context) {
        Router.build("income").go(context);
    }

    public static void showSettingActivity(Context context) {
        Router.build("setting").go(context);
    }
}
