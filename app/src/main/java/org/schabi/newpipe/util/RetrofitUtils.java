package org.schabi.newpipe.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.schabi.newpipe.api.YouTubeVideos;
import org.schabi.newpipe.api.YoutubeSnippetDeserializer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by liyanju on 2018/1/1.
 */

public class RetrofitUtils {

    private static Retrofit retrofit;

    public static ExecutorService sSingleThread = Executors.newSingleThreadExecutor();

    public static Retrofit getRetrofit(Context context) {
        if (retrofit != null) {
            return retrofit;
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(YouTubeVideos.class, new YoutubeSnippetDeserializer());
        Gson gson = gsonBuilder.create();
        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);

        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(FilenameUtils.getHttpCacheDir(context), Constants.HTTP_CACHE_SIZE))
                .connectTimeout(Constants.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(Constants.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_API_URL_YOUTUBE)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build();
        return retrofit;
    }
}
