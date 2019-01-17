package cn.skyui.module.ugc.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.chenenyu.router.annotation.Route;
import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.RetryWhenException;
import cn.skyui.library.image.picker.widget.ViewPagerFixed;
import cn.skyui.library.utils.ToastUtils;
import cn.skyui.module.ugc.R;
import cn.skyui.module.ugc.adapter.PhotoPageAdapter;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.data.model.ugc.UgcDetailVO;

import java.util.ArrayList;

/**
 * Created by tiansj on 2018/4/11.
 */
@Route("photo")
public class PhotoActivity extends BaseSwipeBackActivity {

    public static final String IMAGES = "images";
    public static final String POSITION = "position";
    public static final String UGC_ID = "ugcId";

    private ArrayList<String> mImageItems;
    private int mCurrentPosition = 0;
    private Long ugcId;

    private boolean isLiked;
    private UgcDetailVO ugcDetailVO;

    private ViewPagerFixed mViewPager;
    private PhotoPageAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        initData();
        initView();
        httpGetUgcDetail();
    }

    private void initData() {
        Intent intent = getIntent();
        mCurrentPosition = intent.getIntExtra(POSITION, 0);
        mImageItems = (ArrayList<String>) intent.getSerializableExtra(IMAGES);
        ugcId = intent.getLongExtra(UGC_ID, 0L);
    }

    private void initView() {
        if(mImageItems == null || mImageItems.size() == 0) {
            finish();
            return;
        }
        if(mImageItems.size() > 1) {
            setTitle(mCurrentPosition + 1  + " / " + mImageItems.size());
        }

        mViewPager = findViewById(R.id.viewPager);
        mAdapter = new PhotoPageAdapter(this, mImageItems);
        mAdapter.setPhotoViewClickListener((view, v, v1) -> mActivity.finish());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCurrentPosition, false);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setTitle(position + 1  + " / " + mImageItems.size());
            }
        });

    }

    private void httpGetUgcDetail() {
        if(ugcId <= 0) {
            return;
        }
        RetrofitFactory.createService(ApiService.class)
                .getUgcDetail(ugcId)
                .compose(RxSchedulers.io2main())
                .retryWhen(new RetryWhenException())
                .subscribe(new HttpObserver<UgcDetailVO>() {
                    @Override
                    protected void onSuccess(UgcDetailVO response) {
                        isLiked = response.getIsLiked();
                        ugcDetailVO = response;
                        int res = isLiked ? R.drawable.ic_favorite_accent_24dp : R.drawable.ic_favorite_white_24dp;
                        menuItemFavorite.setIcon(res);
                    }
                });
    }


    MenuItem menuItemFavorite;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(ugcId > 0) {
            getMenuInflater().inflate(R.menu.menu_photo, menu);
            menuItemFavorite = menu.findItem(R.id.action_favorite);

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorite) {
            if(isLiked) {
                httpCancelLike();
            } else {
                httpLike();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void httpLike() {
        RetrofitFactory.createService(ApiService.class)
                .like(ugcId)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        isLiked = true;
                        ToastUtils.showShort("已添加喜欢");
                        menuItemFavorite.setIcon(R.drawable.ic_favorite_accent_24dp);
                    }
                });
    }

    private void httpCancelLike() {
        RetrofitFactory.createService(ApiService.class)
                .cancelLike(ugcId)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Void>() {
                    @Override
                    protected void onSuccess(Void response) {
                        isLiked = false;
                        ToastUtils.showShort("已取消喜欢");
                        menuItemFavorite.setIcon(R.drawable.ic_favorite_white_24dp);
                    }
                });
    }
}
