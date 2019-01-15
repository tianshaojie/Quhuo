package cn.skyui.module.support;

import android.app.Activity;
import android.os.Bundle;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;

import cn.skyui.library.base.activity.BaseActivity;
import cn.skyui.library.web.service.WebViewPreLoadService;

/**
 * @author tianshaojie
 * @date 2019/1/15
 */
@Route("/support/splash")
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BaseActivity.APP_STATUS = BaseActivity.APP_STATUS_NORMAL;
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().postDelayed(this::enter, 500);
        WebViewPreLoadService.startHideService(this);
    }

    private void enter() {
//        if(User.getInstance().isLogin) {
            Bundle bundle = getIntent().getExtras();
            Router.build("/support/main").with(bundle).go(this);
//        } else {
//            Router.build("/support/login").go(this);
//        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().getDecorView().getHandler().removeCallbacksAndMessages(null);
        WebViewPreLoadService.stopHideService(this);
    }
}