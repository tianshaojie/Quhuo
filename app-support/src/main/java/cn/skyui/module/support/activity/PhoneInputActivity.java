package cn.skyui.module.support.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.chenenyu.router.annotation.Route;
import com.orhanobut.logger.Logger;
import com.trello.rxlifecycle2.android.ActivityEvent;

import cn.skyui.library.base.activity.BaseActivity;
import cn.skyui.library.event.LoginSuccessEvent;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.utils.KeyboardUtils;
import cn.skyui.module.support.R;
import cn.skyui.module.support.data.ApiService;
import de.greenrobot.event.EventBus;

/**
 * @author tianshaojie
 * @date 2018/2/6
 */
@Route("/support/login")
public class PhoneInputActivity extends BaseActivity {

    EditText textPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_input);

        // 不显示返回键
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        textPhone = findViewById(R.id.text_phone);

        findViewById(R.id.btn_next).setOnClickListener(v -> {
            sendSms();
        });

        textPhone.postDelayed(() -> {
            textPhone.requestFocus();
            KeyboardUtils.showSoftInput(this);
        }, 200);

        EventBus.getDefault().register(this);
    }

    private void sendSms() {
        String phone = textPhone.getText().toString();
        if (phone.isEmpty() || phone.length() != 11) {
            textPhone.setError("请输入11位手机号");
            return;
        } else {
            textPhone.setError(null);
        }

        RetrofitFactory.createService(ApiService.class).sendSms(phone)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<String>(this) {
                    @Override
                    protected void onSuccess(String msg) {
                        Logger.i(msg);
                        sendSmsSuccess();
                    }
                });

    }

    @Override
    public void finish() {
        KeyboardUtils.hideSoftInput(this);
        super.finish();
    }

    private void sendSmsSuccess() {
        KeyboardUtils.hideSoftInput(this);
        Intent intent = new Intent();
        intent.putExtra("phone", textPhone.getText().toString());
        intent.setClass(this, PhoneCodeInputActivity.class);
        startActivity(intent);
    }

    public void onEventMainThread(LoginSuccessEvent event) {
        finish();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
