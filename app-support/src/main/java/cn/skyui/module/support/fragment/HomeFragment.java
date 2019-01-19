package cn.skyui.module.support.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.chenenyu.router.Router;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.Arrays;
import java.util.List;

import cn.skyui.library.base.fragment.BaseFragment;
import cn.skyui.module.support.R;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * @author tianshaojie
 * @date 2018/1/29
 */
public class HomeFragment extends BaseFragment {

    private static final String[] CHANNELS = new String[]{"推荐", "附近动态", "附近的人", "最新"};
    private List<String> mDataList = Arrays.asList(CHANNELS);

    private MagicIndicator magicIndicator;
    private ViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.view_pager);
        magicIndicator = mActivity.findViewById(R.id.magic_indicator);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewPager.setAdapter(new HomeAdapter(getChildFragmentManager()));
        initMagicIndicator();
        // mViewPager.setCurrentItem(1);
    }

    private void initMagicIndicator() {
        CommonNavigator commonNavigator = new CommonNavigator(getActivity());
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return mDataList == null ? 0 : mDataList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ScaleTransitionPagerTitleView(context);
                simplePagerTitleView.setText(mDataList.get(index));
                simplePagerTitleView.setNormalColor(getResources().getColor(R.color.transparent_half_white));
                simplePagerTitleView.setSelectedColor(Color.WHITE);
                simplePagerTitleView.setTextSize(17);
                simplePagerTitleView.setOnClickListener(v -> mViewPager.setCurrentItem(index));
                int padding = UIUtil.dip2px(context, 5);
                simplePagerTitleView.setPadding(padding, 0, padding, 0);
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setLineHeight(UIUtil.dip2px(context, 6));
                indicator.setLineWidth(UIUtil.dip2px(context, 6));
                indicator.setRoundRadius(UIUtil.dip2px(context, 3));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
                indicator.setColors(getResources().getColor(R.color.colorAccent));
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, mViewPager);
    }

    class HomeAdapter extends FragmentPagerAdapter {
        public HomeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new RecommendV3Fragment();
            } else if (position == 1) {
                return new NearbyFeedFragment();
            } else if (position == 2) {
                return new NearbyUserFragment();
            } else if (position == 3) {
                return new NewestFragment();
            } else {
                return new RecommendV3Fragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDataList.get(position);
        }

        @Override
        public int getCount() {
            return mDataList == null ? 0 : mDataList.size();
        }
    }

    public class ScaleTransitionPagerTitleView extends ColorTransitionPagerTitleView {
        private float mMinScale = 0.85f;

        public ScaleTransitionPagerTitleView(Context context) {
            super(context);
        }

        @Override
        public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
            super.onEnter(index, totalCount, enterPercent, leftToRight);    // 实现颜色渐变
            setScaleX(mMinScale + (1.0f - mMinScale) * enterPercent);
            setScaleY(mMinScale + (1.0f - mMinScale) * enterPercent);
        }

        @Override
        public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
            super.onLeave(index, totalCount, leavePercent, leftToRight);    // 实现颜色渐变
            setScaleX(1.0f + (mMinScale - 1.0f) * leavePercent);
            setScaleY(1.0f + (mMinScale - 1.0f) * leavePercent);
        }

        public float getMinScale() {
            return mMinScale;
        }

        public void setMinScale(float minScale) {
            mMinScale = minScale;
        }
    }
}
