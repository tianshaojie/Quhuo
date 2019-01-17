package cn.skyui.module.ugc.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.chenenyu.router.Router;

import java.util.ArrayList;

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

    public static void showFansActivity(Context context, Long uid, String nickname, boolean isFans) {
        Router.build("fans").with("uid", uid).with("nickname", nickname).with("isFans", isFans).go(context);
    }

    public static void showPhotoActivity(Context context, ArrayList<String> images, int position, Long ugcId) {
        Router.build("photo").with("images", images).with("position", position).with("ugcId", ugcId).go(context);
    }

    public static void showUserActivity(Context context, Long uid, String nickname, String avatar) {
        Router.build("user").with("uid", uid).with("nickname", nickname).with("avatar", avatar).go(context);
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

    public static void showVideoActivity(Context context, long id, String videoUrl, String videoCoverUrl,
                                         long anchorUid, String anchorNickname, String anchorAvatar) {
//        String videoUrl = "https://vii-img.oss-cn-beijing.aliyuncs.com/user_3955335_video_20180412101941351-943_0.mp4";
//        String videoCoverUrl = "https://vii-img.oss-cn-beijing.aliyuncs.com/user_3955335_video_cover_20180412101940127-3704_0.png";
        Router.build("video")
                .with("id", id).with("videoUrl", videoUrl).with("videoCoverUrl", videoCoverUrl)
                .with("anchorUid", anchorUid).with("anchorNickname", anchorNickname).with("anchorAvatar", anchorAvatar)
                .go(context);
    }

    public static void showLoginActivity(Context context) {
        Router.build("login").go(context);
    }

    public static void showBlacklistActivity(Context context) {
        Router.build("blacklist").go(context);
    }
}
