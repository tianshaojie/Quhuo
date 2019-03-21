package cn.skyui.module.ugc.data;

import java.util.List;
import java.util.Map;

import cn.skyui.library.data.model.UserDetailVO;
import cn.skyui.library.data.model.UserVO;
import cn.skyui.library.http.HttpResponse;
import cn.skyui.module.ugc.data.model.ugc.FeedVO;
import cn.skyui.module.ugc.data.model.ugc.UgcDetailVO;
import cn.skyui.module.ugc.data.model.ugc.UgcItemVO;
import cn.skyui.module.ugc.data.model.ugc.UgcLikeVO;
import cn.skyui.module.ugc.data.model.user.SimpleUserVO;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by tiansj on 2018/4/11.
 */

public interface ApiService {

    // 注意返回值：在 Retrofit 2中，每个请求被包装成一个 Call 对象
    @GET("v1/api1/aliyun/oss/token")
    Call<HttpResponse> getOssToken();

    @GET("v1/api1/user/detail")
    Observable<HttpResponse<UserDetailVO>> getUserDetailInfo(@Query("uid") Long uid);

    @GET("v1/api1/user/get")
    Observable<HttpResponse<UserVO>> getUserInfo(@Query("uid") Long uid);

    @GET("v1/api1/ugc/list")
    Observable<HttpResponse<List<UgcItemVO>>> ugcList(@Query("uid") Long uid, @Query("lastId") Long lastId, @Query("pageSize") int pageSize);

    @GET("v1/api1/ugc/likes")
    Observable<HttpResponse<List<UgcLikeVO>>> ugcLikes(@Query("uid") Long uid, @Query("lastId") Long lastId, @Query("pageSize") int pageSize);

    @GET("v1/api0/ugc/attention/count")
    Observable<HttpResponse<Integer>> getAttentionCountByUid(@Query("uid") Long uid);

    @GET("v1/api0/ugc/fans/count")
    Observable<HttpResponse<Integer>> getFansCountByUid(@Query("uid") Long uid);

    @GET("v1/api1/ugc/attention/check")
    Observable<HttpResponse<Boolean>> checkIsAttention(@Query("targetUid") Long targetUid);

    @POST("v1/api1/ugc/attention/save")
    Observable<HttpResponse<Boolean>> attention(@Query("targetUid") Long targetUid);

    @POST("v1/api1/ugc/attention/cancel")
    Observable<HttpResponse<Boolean>> cancelAttention(@Query("targetUid") Long targetUid);

    @GET("v1/api0/ugc/fans/list")
    Observable<HttpResponse<List<UserVO>>> getFansListByUid(@Query("uid") Long uid, @Query("pageNum") Integer pageNum, @Query("pageSize") Integer pageSize);

    @GET("v1/api0/ugc/attention/list")
    Observable<HttpResponse<List<UserVO>>> getAttentionListByUid(@Query("uid") Long uid, @Query("pageNum") Integer pageNum, @Query("pageSize") Integer pageSize);

    @POST("v1/api1/user/black/add")
    Observable<HttpResponse<Void>> addBlack(@Query("targetUid") Long targetUid);

    @POST("v1/api1/user/black/cancel")
    Observable<HttpResponse<Void>> cancelBlack(@Query("targetUid") Long targetUid);

    @POST("v1/api1/report")
    Observable<HttpResponse<Void>> report(@Query("targetUid") Long targetUid, @Query("ugcId") Long ugcId);

    @GET("v1/api1/ugc/detail")
    Observable<HttpResponse<UgcDetailVO>> getUgcDetail(@Query("id") Long id);

    @POST("v1/api1/ugc/like")
    Observable<HttpResponse<Void>> like(@Query("id") Long id);

    @POST("v1/api1/ugc/like/cancel")
    Observable<HttpResponse<Void>> cancelLike(@Query("id") Long id);


    @POST("v1/api1/ugc/content/delete")
    Observable<HttpResponse<Void>> delete(@Query("id") Long id);

    @FormUrlEncoded
    @POST("v2/api1/ugc/content/save/")
    Observable<HttpResponse<Long>> saveUgc(@Field("type") Integer type,
                                           @Field("title") String title,
                                           @Field("images") String images,
                                           @Field("location") String location);

    @POST("v2/api1/ugc/content/save/")
    Observable<HttpResponse<Long>> saveUgc(@FieldMap Map<String, String> fields);

    @GET("v1/api0/user/search")
    Observable<HttpResponse<List<UserVO>>> search(@Query("pageNum") Integer pageNum,
                                                  @Query("pageSize") Integer pageSize,
                                                  @Query("loginUid") Long loginUid,
                                                  @Query("keyword") String keyword);

    @GET("v1/api0/user/list/rich")
    Observable<HttpResponse<List<SimpleUserVO>>> getRichUserList();

    @POST("v1/api1/logout")
    Observable<HttpResponse<Void>> logout();

    @GET("v1/api1/user/black/list")
    Observable<HttpResponse<List<UserVO>>> blacklist(@Query("pageNum") Integer pageNum, @Query("pageSize") Integer pageSize);

    @GET("v1/api1/feeds/attention")
    Observable<HttpResponse<List<FeedVO>>> getAttentionFeeds(@Query("uid") Long uid, @Query("lastId") Long lastId, @Query("pageSize") int pageSize);

}