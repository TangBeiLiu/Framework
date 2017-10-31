package com.shunping.shifou.http;


import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shunping.shifou.app.MApplication;
import com.shunping.shifou.bean.UserProfiles;
import com.shunping.shifou.bean.post.Password;
import com.shunping.shifou.constant.Constant;
import com.shunping.shifou.constant.FollowType;
import com.shunping.shifou.constant.HostType;
import com.shunping.shifou.uitl.NetUtil;
import com.shunping.shifou.uitl.PreferencesUtil;
import com.socks.library.KLog;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * //
 * //                       _oo0oo_
 * //                      o8888888o
 * //                      88" . "88
 * //                      (| -_- |)
 * //                      0\  =  /0
 * //                    ___/`---'\___
 * //                  .' \\|     |// '.
 * //                 / \\|||  :  |||// \
 * //                / _||||| -:- |||||- \
 * //               |   | \\\  -  /// |   |
 * //               | \_|  ''\---/''  |_/ |
 * //               \  .-\__  '-'  ___/-. /
 * //             ___'. .'  /--.--\  `. .'___
 * //          ."" '<  `.___\_<|>_/___.' >' "".
 * //         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 * //         \  \ `_.   \_ __\ /__ _/   .-` /  /
 * //     =====`-.____`.___ \_____/___.-`___.-'=====
 * //                       `=---='
 * //
 * //
 * //     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * //
 * //            佛祖保佑         永无bug
 * //
 * Created Date:  2017/4/13.
 * author: tsliu
 * email: liutangbei@gmail.com
 *
 * retrofit配置类;添加了缓存,服务器json数据返回,token请求头3个拦截器;
 * 使用 SparseArray管理不同的单列;具体对应于不同的baseUrl;所有网络请
 * 求都可基于此类
 */

public class RetrofitManager {

    // 设缓存有效期为两天
    private static final long CACHE_STALE_SEC = 60 * 60 * 24 * 2;
    // 30秒内直接读缓存
    private static final long CACHE_AGE_SEC = 0;

    private static volatile OkHttpClient sOkHttpClient;
    // 管理不同HostType的单例
    private static SparseArray<RetrofitManager> sInstanceManager = new SparseArray<>(HostType.values().length);
    private ApiServices mNewsService;
    // 云端响应头拦截器，用来配置缓存策略
    private Interceptor mRewriteCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            // 在这里统一配置请求头缓存策略以及响应头缓存策略
            if (NetUtil.isConnected(MApplication.getContext())) {
                // 在有网的情况下CACHE_AGE_SEC秒内读缓存，大于CACHE_AGE_SEC秒后会重新请求数据
                request = request.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control").header("Cache-Control", "public, max-age=" + CACHE_AGE_SEC).build();
                Response response = chain.proceed(request);
                return response.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control").header("Cache-Control", "public, max-age=" + CACHE_AGE_SEC).build();
            } else {
                // 无网情况下CACHE_STALE_SEC秒内读取缓存，大于CACHE_STALE_SEC秒缓存无效报504
                request = request.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + CACHE_STALE_SEC).build();
                Response response = chain.proceed(request);
                return response.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + CACHE_STALE_SEC).build();
            }

        }
    };

    // 打印返回的json数据拦截器
    private Interceptor mLoggingInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();

            Request.Builder requestBuilder = request.newBuilder();
            requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
            request = requestBuilder.build();

            final Response response = chain.proceed(request);

            KLog.e("请求网址: \n" + request.url() + " \n " + "请求头部信息：\n" + request.headers() + "响应头部信息：\n" + response.headers());

            final ResponseBody responseBody = response.body();
            final long contentLength = responseBody.contentLength();

            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();

            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(charset);
                } catch (UnsupportedCharsetException e) {
                    KLog.e("");
                    KLog.e("Couldn't decode the response body; charset is likely malformed.");
                    return response;
                }
            }

            if (contentLength != 0) {
                KLog.v("--------------------------------------------开始打印返回数据----------------------------------------------------");
                KLog.json(buffer.clone().readString(charset));
                KLog.v("--------------------------------------------结束打印返回数据----------------------------------------------------");
            }

            return response;
        }
    };

    //添加token请求头的拦截器
    private Interceptor mTokenInterceptor = new Interceptor() {
        @Override public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (TextUtils.isEmpty(getToken())) {
                return chain.proceed(originalRequest);
            }

            Request authorised = originalRequest.newBuilder()
                    .header("Content-Type","application/json")
                    .header("Authorization", "Bearer "+getToken())
                    .build();
            return chain.proceed(authorised);
        }
    };

    private String getToken() {

        return PreferencesUtil.getStringPreferences(Constant.TOKEN);
    }

    //私有构造
    private RetrofitManager() {
    }

    private RetrofitManager(HostType hostType) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostType.host(hostType))
                .client(getOkHttpClient())
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mNewsService = retrofit.create(ApiServices.class);


    }

    public ApiServices getApiServices(){
        return mNewsService;
    }

    /**
     * 获取单例
     *
     * @param hostType host类型
     * @return 实例
     */
    public static RetrofitManager getInstance(HostType hostType) {

        int type = HostType.type(hostType);
        RetrofitManager instance = sInstanceManager.get(type);
        if (instance == null) {
            instance = new RetrofitManager(hostType);
            sInstanceManager.put(type, instance);
            return instance;
        } else {
            return instance;
        }
    }

    // 配置OkHttpClient
    private OkHttpClient getOkHttpClient() {
        if (sOkHttpClient == null) {
            synchronized (RetrofitManager.class) {
                if (sOkHttpClient == null) {
                    // OkHttpClient配置是一样的,静态创建一次即可
                    // 指定缓存路径,缓存大小100Mb
                    Cache cache = new Cache(new File(MApplication.getContext().getCacheDir(), "HttpCache"), 1024 * 1024 * 100);

                    sOkHttpClient = new OkHttpClient.Builder()
                            .cache(cache)
                            .addNetworkInterceptor(mRewriteCacheControlInterceptor)
                            .addInterceptor(mRewriteCacheControlInterceptor)
                            .addInterceptor(mLoggingInterceptor)
                            .addInterceptor(mTokenInterceptor)
                            .retryOnConnectionFailure(true)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return sOkHttpClient;
    }

    public Call<List<UserProfiles>> getFollowUserBean(FollowType followType, String id, int page) {
        switch (followType) {
            case FOLLOWING:
                return mNewsService.requestFollowing(id, page);
            case FOLLOWER:
                return mNewsService.requestFollowers(id, page);
        }

        return null;
    }

    public Call<Object> putPassword(Password password) {
        return mNewsService.putPassword(password);
    }
}
