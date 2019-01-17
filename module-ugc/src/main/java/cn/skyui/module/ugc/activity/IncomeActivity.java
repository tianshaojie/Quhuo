package cn.skyui.module.ugc.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.TextView;

import com.chenenyu.router.annotation.Route;

import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.data.model.User;
import cn.skyui.library.data.model.UserDetailVO;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.R;

/**
 * Created by tiansj on 2018/5/7.
 */
@Route("income")
public class IncomeActivity extends BaseSwipeBackActivity {

    private TextView mTextBalance;
    private Button mBtnWithdraw;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        initView();
        loadData();
    }

    private void initView() {
        mTextBalance = findViewById(R.id.text_balance);
        mBtnWithdraw = findViewById(R.id.btn_withdraw);

        mBtnWithdraw.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("目前还不支持自动提现，请联系客服小七进行提现，微信号：shidianx17")
                    .setCancelable(true)
                    .setNegativeButton("知道了", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }).show();
        });
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
                        mTextBalance.setText(String.valueOf(response.getAccount().getAvailableIncomeCoin()));
                    }
                });
    }


}
