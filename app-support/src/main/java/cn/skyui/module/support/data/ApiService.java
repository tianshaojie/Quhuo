package cn.skyui.module.support.data;

import java.util.List;
import java.util.Map;

import cn.skyui.library.data.model.UserDetailVO;
import cn.skyui.library.data.model.UserVO;
import cn.skyui.library.http.HttpResponse;
import cn.skyui.module.support.data.model.Music;
import cn.skyui.module.support.data.model.MusicCategory;
import cn.skyui.module.support.data.model.login.LoginResponse;
import cn.skyui.module.ugc.data.model.ugc.FeedVO;
import cn.skyui.module.ugc.data.model.user.RecommendVO;
import cn.skyui.module.ugc.data.model.user.UserBannerVO;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by tiansj on 2017/12/3.
 */

public interface ApiService {

    @POST("v1/api0/user/test")
    Observable<HttpResponse> testRetry();

    @GET("v2/api0/user/list/recommend")
    Observable<HttpResponse<UserBannerVO>> recommendList(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize);

    @GET("v3/api1/user/list/recommend")
    Observable<HttpResponse<RecommendVO>> recommendListV3(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize);

    @GET("v2/api0/user/list/newest")
    Observable<HttpResponse<UserBannerVO>> newestList(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize);

    @POST("v1/api0/sendSms")
    Observable<HttpResponse<String>> sendSms(@Query("mobile") String mobile);

    @POST("v1/api0/login")
    Observable<HttpResponse<LoginResponse>> login(@QueryMap Map<String, String> param);

    @POST("v1/api1/user/update")
    Observable<HttpResponse<Void>> updateUserProfile(@QueryMap Map<String, Object> param);

    @GET("v1/api1/user/detail")
    Observable<HttpResponse<UserDetailVO>> getUserDetailInfo(@Query("uid") Long uid);

    @POST("v1/api1/user/status/update")
    Observable<HttpResponse<Void>> updateUserOnlineStatus(@Query("status") Integer status);

    @GET("v1/api0/test/music/category/list")
    Observable<HttpResponse<List<MusicCategory>>> getMusicCategoryList();

    @GET("v1/api0/test/music/list")
    Observable<HttpResponse<List<Music>>> getMusicList(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize);

    @POST("v1/api0/test/music/category/update")
    Observable<HttpResponse<Void>> updateMusicCategory(@QueryMap Map<String, Object> param);

    @POST("v1/api0/test/music/update")
    Observable<HttpResponse<Void>> updateMusic(@QueryMap Map<String, Object> param);

    @POST("v1/api1/user/location/update")
    Observable<HttpResponse<Void>> updateLocation(@Query("location") String location);

    @GET("v1/api1/user/nearby")
    Observable<HttpResponse<List<UserVO>>> getNearbyUserList(@QueryMap Map<String, Object> nearbyParam);

    @GET("v1/api1/feeds/nearby")
    Observable<HttpResponse<List<FeedVO>>> getNearbyFeedList(@QueryMap Map<String, Object> nearbyParam);

    @POST("v1/api1/report")
    Observable<HttpResponse<Void>> report(@Query("targetUid") Long targetUid, @Query("ugcId") Long ugcId);

    @POST("v1/api1/ugc/like")
    Observable<HttpResponse<Void>> like(@Query("id") Long id);
}
