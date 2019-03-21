package cn.skyui.module.ugc.manager;

import android.app.Application;

import com.orhanobut.logger.Logger;

import cn.skyui.library.http.HttpResponse;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.utils.oss.OssClient;
import cn.skyui.module.ugc.data.ApiService;
import retrofit2.Call;

/**
 * Created by tianshaojie on 18/4/20.
 */

public class UgcInitManager {

    public static final String ENDPOINT = "oss-cn-beijing.aliyuncs.com";
    public static final String BUCKET_NAME = "astatic";

    public static void init(Application application) {
        initSocial(application);
    }

    private static void initSocial(Application application) {
        // SocialManager.init(application);

        OssClient.init(ENDPOINT, BUCKET_NAME, application, () -> {
            try {
                Call<HttpResponse> result = RetrofitFactory.createServiceOriginal(ApiService.class).getOssToken();
                HttpResponse response = result.execute().body();
                if (response != null && response.getCode() == 200) {
                    return response.getBody().toString();
                }
            } catch (Exception e) {
                Logger.e("get oss token exception : " + e.getMessage());
            }
            return null;
        });
    }
}
