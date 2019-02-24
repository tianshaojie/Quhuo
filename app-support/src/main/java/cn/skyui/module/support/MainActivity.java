package cn.skyui.module.support;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;

import cn.skyui.library.base.activity.BaseActivity;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.data.model.User;
import cn.skyui.library.data.model.UserDetailVO;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.glide.GlideCircleTransform;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.module.support.data.ApiService;
import cn.skyui.module.support.fragment.HomeFragment;
import cn.skyui.module.support.helper.UiHelper;
import jp.wasabeef.blurry.Blurry;

/**
 * @author tianshaojie
 * @date 2019/1/15
 */
@Route("/support/main")
public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ImageView mImgAvatar;
    TextView mTextUserId;
    TextView mTextNickname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        loadData();
        showHomeFragment();
    }

    private void showHomeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_container, new HomeFragment(), "HomeFragment").commit();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        mImgAvatar = headerLayout.findViewById(R.id.iv_avatar);
        mTextUserId = headerLayout.findViewById(R.id.tv_uid);
        mTextNickname = headerLayout.findViewById(R.id.tv_nickname);
        mImgAvatar.setOnClickListener(v -> UiHelper.showProfileActivity(mActivity));
    }

    private void loadData() {
        RetrofitFactory.createService(ApiService.class)
                .getUserDetailInfo(User.getInstance().userId)
                .compose(RxSchedulers.io2main())
                .compose(bindToLifecycle())
                .subscribe(new HttpObserver<UserDetailVO>() {
                    @Override
                    protected void onSuccess(UserDetailVO response) {
                        User.getInstance().detail = response;
                        updateView();
                    }
                });
    }

    private String avatarUrl = "";
    private void updateView() {
        UserDetailVO detail = User.getInstance().detail;
        // 比较内存，User.getInstance().detail被外部修改再更新View
        if (!detail.getUser().getAvatar().equals(avatarUrl)) {
            GlideApp.with(mActivity)
                    .load(ImageConstants.getSmallUrl(detail.getUser().getAvatar()))
                    .placeholder(R.drawable.ic_account_circle_white_24dp)
                    .optionalTransform(new GlideCircleTransform(getResources().getColor(R.color.white), 6))
                    .into(mImgAvatar);
            GlideApp.with(mActivity)
                    .asBitmap()
                    .load(ImageConstants.getSmallUrl(detail.getUser().getAvatar()))
                    .into(new SimpleTarget<Bitmap>(50, 50) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            Blurry.with(mActivity).radius(10).from(resource)
//                                    .into(zoomView.findViewById(R.id.iv_zoom));
                        }
                    });
        }

        mTextNickname.setText(detail.getUser().getNickname());
        mTextUserId.setText(String.format("趣火号: %s", User.getInstance().userId));

//        mTextRecharge.setText(String.valueOf(detail.getAccount().getAvailableRechargeCoin()));
//        mTextIncome.setText(String.valueOf(detail.getAccount().getAvailableIncomeCoin()));
//        mTextMyUgc.setText(String.valueOf(detail.getUser().getUgcCount()));
//        mTextMyLikes.setText(String.valueOf(detail.getUser().getLikesCount()));
//        mTextMyAttention.setText(String.valueOf(detail.getUser().getAttentionCount()));
//        mTextMyFans.setText(String.valueOf(detail.getUser().getFansCount()));

        avatarUrl = detail.getUser().getAvatar();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            UiHelper.showSearchActivity(mActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            UiHelper.showUserActivity(mActivity, User.getInstance().userId,
                    User.getInstance().detail.getUser().getNickname(),
                    User.getInstance().detail.getUser().getAvatar());
        } else if (id == R.id.nav_gallery) {
            UiHelper.showUserLikesActivity(mActivity, User.getInstance().userId,
                    User.getInstance().detail.getUser().getNickname());
        } else if (id == R.id.nav_slideshow) {
            UiHelper.showFansActivity(mActivity, User.getInstance().userId, "", false);
        } else if (id == R.id.nav_manage) {
            UiHelper.showFansActivity(mActivity, User.getInstance().userId, "", true);
        } else if (id == R.id.nav_setting) {
            UiHelper.showSettingActivity(mActivity);
        } else if (id == R.id.nav_about) {
            Router.build("about").go(mActivity);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
