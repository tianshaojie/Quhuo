package cn.skyui.module.ugc.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.chenenyu.router.annotation.Route;
import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.utils.AppUtils;
import cn.skyui.module.ugc.R;

/**
 * Created by tiansj on 2018/5/7.
 */
@Route("about")
public class AboutActivity extends BaseSwipeBackActivity {

    private TextView mTextVersion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        mTextVersion = findViewById(R.id.textVersion);
        mTextVersion.setText(String.format("V%s", AppUtils.getAppVersionName()));
    }

}
