package cn.skyui.module.support.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;

import com.chenenyu.router.Router;
import com.orhanobut.logger.Logger;
import com.tencent.mmkv.MMKV;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.HashMap;
import java.util.Map;

import cn.skyui.library.base.activity.BaseActivity;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.model.User;
import cn.skyui.library.event.LoginSuccessEvent;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.library.utils.DeviceUtils;
import cn.skyui.library.utils.KeyboardUtils;
import cn.skyui.library.utils.StringUtils;
import cn.skyui.library.utils.ToastUtils;
import cn.skyui.module.support.R;
import cn.skyui.module.support.data.ApiService;
import cn.skyui.module.support.data.model.login.LoginResponse;
import de.greenrobot.event.EventBus;

/**
 * @author tianshaojie
 * @date 2018/2/6
 */
public class PhoneCodeInputActivity extends BaseActivity {

    String phone;
    EditText textPhoneCode;
    Button btnSms;
    Button btnNext;
    TimeCounter timeCounter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_code_input);

        phone = getIntent().getStringExtra("phone");
        if(StringUtils.isEmpty(phone)) {
            finish();
            return;
        }

        textPhoneCode = findViewById(R.id.text_phone_code);
        btnSms = findViewById(R.id.btn_sms);
        btnNext = findViewById(R.id.btn_next);

        btnSms.setOnClickListener(v -> sendSms());
        btnNext.setOnClickListener(v -> login());

        timeCounter = new TimeCounter(60000, 1000);
        timeCounter.start();
        textPhoneCode.postDelayed(() -> {
            textPhoneCode.requestFocus();
            KeyboardUtils.showSoftInput(this);
        }, 200);
    }

    private void sendSms() {
        RetrofitFactory.createService(ApiService.class).sendSms(phone)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<String>(this) {
                    @Override
                    protected void onSuccess(String msg) {
                        Logger.i(msg);
                        ToastUtils.showShort("验证码已发送");
                        timeCounter.start();
                    }
                });
    }


    private void login() {
        String code = textPhoneCode.getText().toString();
        if (StringUtils.isEmpty(code)) {
            ToastUtils.showShort("请输入验证码");
            btnNext.setEnabled(true);
            return;
        }
        btnNext.setEnabled(false);

        final Map<String, String> param = new HashMap<>();
        param.put("mobile", phone);
        param.put("verifyCode", code);
        param.put("terminalType", "1");
        param.put("osVersion", DeviceUtils.getOSVersion());
        param.put("brand", DeviceUtils.getBrand());
        param.put("model", DeviceUtils.getModel());
        param.put("deviceId", DeviceUtils.getAndroidID());

        RetrofitFactory.createService(ApiService.class).login(param)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<LoginResponse>(this) {
                    @Override
                    protected void onSuccess(LoginResponse response) {
                        loginSuccess(response);
                    }

                    @Override
                    protected void onFailure(ApiException e) {
                        super.onFailure(e);
                        btnNext.setEnabled(true);
                    }
                });
    }

    private void loginSuccess(LoginResponse response) {
//        SPUtils.getInstance().put(Constants.SharedPreferences.USER, response.toString());
        MMKV.defaultMMKV().encode(Constants.SharedPreferences.USER, response.toString());
        User.getInstance().init();
        ToastUtils.showShort("登录成功");
        Logger.i(User.getInstance().token);
        KeyboardUtils.hideSoftInput(this);
        showMainActivity();
        EventBus.getDefault().post(new LoginSuccessEvent());
        finish();
    }

    private void showMainActivity() {
        Router.build("/support/main").go(this);
    }

    @Override
    public void finish() {
        KeyboardUtils.hideSoftInput(this);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timeCounter != null) {
            timeCounter.cancel();
        }
    }

    class TimeCounter extends CountDownTimer {

        TimeCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            btnSms.setText("重新获取");
            btnSms.setEnabled(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btnSms.setEnabled(false);
            btnSms.setText(millisUntilFinished / 1000 + "s");
        }
    }

}
