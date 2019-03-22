package cn.skyui.module.ugc.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.mmkv.MMKV;

import java.io.File;

import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.cache.util.StorageUtils;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.model.User;
import cn.skyui.library.glide.GlideConfiguration;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.library.utils.AppUtils;
import cn.skyui.library.utils.FileUtils;
import cn.skyui.library.utils.Utils;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.helper.UiHelper;
import cn.skyui.module.ugc.R;

/**
 * Created by tiansj on 2018/5/7.
 */
@Route("setting")
public class SettingActivity extends BaseSwipeBackActivity {

    private RelativeLayout mLayoutBlacklist;
    private RelativeLayout mLayoutCleanCache;
    private RelativeLayout mLayoutUpdate;
    private TextView mTextCache;
    private TextView mTextVersion;
    private RelativeLayout mLayoutLogout;
    private RelativeLayout mLayoutAbout;
    private RelativeLayout mLayoutHelp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initView();
    }

    private void initView() {
        mLayoutBlacklist = findViewById(R.id.layoutBlacklist);
        mLayoutCleanCache = findViewById(R.id.layoutCleanCache);
        mTextCache = findViewById(R.id.textCache);
        mTextVersion = findViewById(R.id.textVersion);
        mLayoutLogout = findViewById(R.id.layoutLogout);
        mLayoutUpdate = findViewById(R.id.layoutUpdate);
        mLayoutAbout = findViewById(R.id.layoutAbout);

        mLayoutBlacklist.setOnClickListener(v -> {
            UiHelper.showBlacklistActivity(mActivity);
        });

        mLayoutLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("确定要退出登录吗？")
                    .setCancelable(true)
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                    })
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        logout();
                    }).show();
        });

        File videoCacheDir = StorageUtils.getIndividualCacheDirectory(mActivity);
        File imageCacheDir = new File(Utils.getApp().getCacheDir() + "/" + GlideConfiguration.GLIDE_CARCH_DIR);
        long videoCacheByte = FileUtils.getDirLength(videoCacheDir);
        long imageCacheByte = FileUtils.getDirLength(imageCacheDir);
        mTextCache.setText(FileUtils.byte2FitMemorySize(videoCacheByte + imageCacheByte));

        mLayoutCleanCache.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("确定要清除缓存吗？")
                    .setCancelable(true)
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                    })
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        FileUtils.deleteAllInDir(videoCacheDir);
                        FileUtils.deleteAllInDir(imageCacheDir);
                        mTextCache.setText("0.0B");
                    }).show();
        });


        mTextVersion.setText(AppUtils.getAppVersionName());
        mLayoutUpdate.setOnClickListener(v -> Beta.checkUpgrade());
        loadUpgradeInfo();

        mLayoutAbout.setOnClickListener(v -> Router.build("about").go(mActivity));

//        mLayoutHelp = findViewById(R.id.layoutHelp);
//        mLayoutHelp.setOnClickListener(v -> UiHelper.showWebViewActivity(mActivity, RetrofitFactory.BASE_URL + "help.html"));
    }

    private void logout() {
        User.getInstance().clear();
        MMKV.defaultMMKV().removeValueForKey(Constants.SharedPreferences.USER);
        RetrofitFactory.createService(ApiService.class)
                .logout()
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        Router.build("/support/login").go(mActivity);
                        finish();
                    }

                    @Override
                    protected void onFailure(ApiException e) {
                        Router.build("/support/login").go(mActivity);
                        finish();
                    }
                });
    }

    private void loadUpgradeInfo() {
        /***** 获取升级信息 *****/
        UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();

        if (upgradeInfo == null) {
            // ToastUtils.showShort("已是最新版本");
            Log.i("update", "无升级信息");
            return;
        }
        StringBuilder info = new StringBuilder();
        info.append("id: ").append(upgradeInfo.id).append("\n");
        info.append("标题: ").append(upgradeInfo.title).append("\n");
        info.append("升级说明: ").append(upgradeInfo.newFeature).append("\n");
        info.append("versionCode: ").append(upgradeInfo.versionCode).append("\n");
        info.append("versionName: ").append(upgradeInfo.versionName).append("\n");
        info.append("发布时间: ").append(upgradeInfo.publishTime).append("\n");
        info.append("安装包Md5: ").append(upgradeInfo.apkMd5).append("\n");
        info.append("安装包下载地址: ").append(upgradeInfo.apkUrl).append("\n");
        info.append("安装包大小: ").append(upgradeInfo.fileSize).append("\n");
        info.append("弹窗间隔（ms）: ").append(upgradeInfo.popInterval).append("\n");
        info.append("弹窗次数: ").append(upgradeInfo.popTimes).append("\n");
        info.append("发布类型（0:测试 1:正式）: ").append(upgradeInfo.publishType).append("\n");
        info.append("弹窗类型（1:建议 2:强制 3:手工）: ").append(upgradeInfo.upgradeType).append("\n");
        info.append("图片地址：").append(upgradeInfo.imageUrl);
        Log.i("update", info.toString());
        mTextVersion.setText(String.format("发现新版本%s", upgradeInfo.versionName));
    }
}
