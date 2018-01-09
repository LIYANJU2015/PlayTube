package org.schabi.newpipe.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by liyanju on 2017/11/18.
 */

public interface YoutubeApiService {

    String DEVOTE_KEY = "AIzaSyBUgQUzZJTZrdW9LAY0-hr__UYnKoQRRNU";

    @GET("videos?part=snippet,contentDetails,statistics&chart=mostPopular&maxResults=15&key=" + DEVOTE_KEY)
    Call<YouTubeVideos> getYoutubeVideos(@Query("pageToken") String pageToken);
}
