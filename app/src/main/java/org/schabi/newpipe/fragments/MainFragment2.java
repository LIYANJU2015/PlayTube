package org.schabi.newpipe.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.admodule.AdModule;
import com.admodule.LogUtils;
import com.admodule.admob.AdMobBanner;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.google.android.gms.ads.AdListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.paginate.Paginate;
import com.tubewebplayer.WebViewPlayerActivity;
import com.tubewebplayer.YouTubePlayerActivity;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.schabi.newpipe.BaseFragment;
import org.schabi.newpipe.R;
import org.schabi.newpipe.api.YouTubeVideos;
import org.schabi.newpipe.api.YoutubeApiService;
import org.schabi.newpipe.info_list.holder.StreamInfoItemHolder;
import org.schabi.newpipe.util.AdViewWrapperAdapter;
import org.schabi.newpipe.util.FilenameUtils;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.RetrofitUtils;

import java.util.ArrayList;

import retrofit2.Response;

/**
 * Created by liyanju on 2017/12/28.
 */

public class MainFragment2 extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, Paginate.Callbacks {

    private static ArrayList<YouTubeVideos.Snippet> mDatas = new ArrayList<>();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private CommonAdapter mCommonAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_fragment_menu, menu);
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
            supportActionBar.setDisplayShowTitleEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                NavigationHelper.openSearchFragment(getFragmentManager(), 0, "");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadMore() {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (mSwipeRefreshLayout.isRefreshing()) {
            return;
        }
        mIsLoadingMore = true;

        requestYoutubeData();
    }

    public void showEmptyView() {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        mIsLoadingMore = false;

        if (mDatas.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        mRecyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private boolean mIsLoadedAll;
    private boolean mIsLoadingMore;

    @Override
    public boolean isLoading() {
        return mIsLoadingMore;
    }

    @Override
    public boolean hasLoadedAllItems() {
        return mIsLoadedAll;
    }

    private View emptyView;
    private View progressBar;

    private static String sNextPageToken = "";

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        activity.getSupportActionBar().setTitle(R.string.app_name);

        emptyView = rootView.findViewById(R.id.empty_state_view);
        progressBar = rootView.findViewById(R.id.loading_progress_bar);

        mSwipeRefreshLayout = rootView.findViewById(R.id.home_swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(activity,
                R.color.light_youtube_primary_color));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = rootView.findViewById(R.id.items_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView.setHasFixedSize(true);

        mCommonAdapter = new CommonAdapter<YouTubeVideos.Snippet>(activity, R.layout.home_video_item, mDatas) {
            @Override
            protected void convert(ViewHolder holder, final YouTubeVideos.Snippet snippet, int position) {
                ImageView imageView = holder.getView(R.id.img);
                if (snippet.thumbnails.getStandard() != null) {
                    imageView.setTag(snippet.thumbnails.getStandard().getUrl());
                    ImageLoader.getInstance()
                            .displayImage(snippet.thumbnails.getStandard().getUrl(),
                                    imageView, StreamInfoItemHolder.DISPLAY_THUMBNAIL_OPTIONS);
                } else if (snippet.thumbnails.getHigh() != null) {
                    imageView.setTag(snippet.thumbnails.getHigh().getUrl());
                    ImageLoader.getInstance()
                            .displayImage(snippet.thumbnails.getHigh().getUrl(),
                                    imageView, StreamInfoItemHolder.DISPLAY_THUMBNAIL_OPTIONS);
                } else if (snippet.thumbnails.getDefaultX() != null) {
                    imageView.setTag(snippet.thumbnails.getDefaultX().getUrl());
                    ImageLoader.getInstance()
                            .displayImage(snippet.thumbnails.getDefaultX().getUrl(),
                                    imageView, StreamInfoItemHolder.DISPLAY_THUMBNAIL_OPTIONS);
                } else {
                    imageView.setImageResource(R.drawable.dummy_thumbnail);
                }

                TextView titleTV = holder.getView(R.id.title);
                titleTV.setText(snippet.title);

                TextView descriptionTV = holder.getView(R.id.description);
                if (snippet.statistics != null) {
                    descriptionTV.setText(FilenameUtils
                            .sizeFormatNum2String(Long.parseLong(snippet.statistics.getViewCount())));
                } else {
                    descriptionTV.setText("");
                }

                TextView timeTV = holder.getView(R.id.time);
                if (snippet.contentDetails != null) {
                    timeTV.setText(FilenameUtils.convertDuration(snippet.contentDetails.duration));
                } else {
                    timeTV.setText("");
                }

                holder.setOnClickListener(R.id.card_view, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NavigationHelper.openVideoDetailFragment(getFragmentManager()
                        , 0, "https://www.youtube.com/watch?v=" + snippet.vid, snippet.title);
//                        WebViewPlayerActivity.launch(activity,
//                                "https://www.youtube.com/watch?v=" + snippet.vid, snippet.title);
                    }
                });
            }
        };

        setAapter();

        if (mDatas.size() == 0) {
            progressBar.setVisibility(View.VISIBLE);
            requestYoutubeData();
        }

        initAdMobBanner();
    }

    private void requestYoutubeData() {
        new AsyncTask<Void, Void, YouTubeVideos>() {
            @Override
            protected YouTubeVideos doInBackground(Void... voids) {
                try {
                    Log.v(TAG, "requestYoutubeData sNextPageToken " + sNextPageToken);
                    Response<YouTubeVideos> response = RetrofitUtils.getRetrofit(activity).create(YoutubeApiService.class)
                            .getYoutubeVideos(sNextPageToken).execute();
                    if (response != null && response.body() != null) {
                        return response.body();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(YouTubeVideos youTubeVideos) {
                super.onPostExecute(youTubeVideos);
                Log.v(TAG, "requestYoutubeData onPostExecute " + youTubeVideos);
                if (youTubeVideos == null) {
                    showEmptyView();
                    return;
                }

                if (activity == null || activity.isFinishing()) {
                    return;
                }

                emptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

                sNextPageToken = youTubeVideos.nextPageToken;
                mIsLoadingMore = false;
                mIsLoadedAll = TextUtils.isEmpty(youTubeVideos.nextPageToken);

                NativeAd nativeAd = AdModule.getInstance().getFacebookAd().getNativeAd();
                if (nativeAd != null && nativeAd.isAdLoaded() && currentAdapter != null
                        && !currentAdapter.isAddAdView()) {
                    Log.v("main", "setadpter>>>>");
                    if (currentAdapter.getItemCount() > 3) {
                        currentAdapter.addAdView(22, new AdViewWrapperAdapter.
                                AdViewItem(setUpNativeAdView(activity, nativeAd), 1));
                    }
                }

                if (!isAddPaginte) {
                    isAddPaginte = true;
                    mPaginate = Paginate.with(mRecyclerView, MainFragment2.this)
                            .setLoadingTriggerThreshold(2)
                            .build();
                    mPaginate.setHasMoreDataToLoad(true);
                }

                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mDatas.clear();
                    mDatas.addAll(youTubeVideos.items);
                    currentAdapter.notifyDataSetChanged();
                } else {
                    int positionStart = currentAdapter.getItemCount();
                    mDatas.addAll(youTubeVideos.items);
                    int itemCount = youTubeVideos.items.size();

                    currentAdapter.notifyItemRangeInserted(positionStart, itemCount);
                }
            }
        }.executeOnExecutor(RetrofitUtils.sSingleThread);
    }

    private boolean isAddPaginte;

    private AdViewWrapperAdapter currentAdapter;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AdModule.getInstance().getFacebookAd().loadAd(false, "811681725685294_811682365685230");
        if (adMobBanner != null) {
            adMobBanner.destroy();
            adMobBanner = null;
        }
    }

    public static View setUpNativeAdView(Activity activity, NativeAd nativeAd) {
        nativeAd.unregisterView();

        View adView = LayoutInflater.from(activity).inflate(R.layout.home_video_ad_item, null);

        FrameLayout adChoicesFrame = (FrameLayout) adView.findViewById(R.id.fb_adChoices);
        ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.fb_half_icon);
        TextView nativeAdTitle = (TextView) adView.findViewById(R.id.fb_banner_title);
        TextView nativeAdBody = (TextView) adView.findViewById(R.id.fb_banner_desc);
        TextView nativeAdCallToAction = (TextView) adView.findViewById(R.id.fb_half_download);
        MediaView nativeAdMedia = (MediaView) adView.findViewById(com.admodule.R.id.fb_half_mv);

        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdTitle.setText(nativeAd.getAdTitle());
        nativeAdBody.setText(nativeAd.getAdBody());

        // Downloading and setting the ad icon.
        NativeAd.Image adIcon = nativeAd.getAdIcon();
        NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

        // Download and setting the cover image.
        NativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
        nativeAdMedia.setNativeAd(nativeAd);

        // Add adChoices icon
        AdChoicesView adChoicesView = new AdChoicesView(activity, nativeAd, true);
        adChoicesFrame.addView(adChoicesView, 0);
        adChoicesFrame.setVisibility(View.VISIBLE);

        nativeAd.registerViewForInteraction(adView);

        return adView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adMobBanner != null) {
            adMobBanner.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adMobBanner != null) {
            adMobBanner.pause();
        }
    }

    private AdMobBanner adMobBanner;

    private void initAdMobBanner() {
        Log.v("main", "initAdMobBanner");
        adMobBanner = AdModule.getInstance().getAdMob().createBannerAdView();
        adMobBanner.setAdRequest(AdModule.getInstance().getAdMob().createAdRequest());
        adMobBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.v("main", "initAdMobBanner onAdLoaded");
                if (activity == null || !isAdded()
                        || activity.isFinishing()
                        || adMobBanner == null) {
                    return;
                }
                if (currentAdapter != null && !currentAdapter.isAddAdView()
                        && currentAdapter.getItemCount() > 3) {
                    adMobBanner.getAdView().setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                            RecyclerView.LayoutParams.WRAP_CONTENT));
                    currentAdapter.addAdView(22, new AdViewWrapperAdapter.
                            AdViewItem(adMobBanner.getAdView(), 1));
                    currentAdapter.notifyItemInserted(1);
                }
            }
        });
    }

    public void setAapter() {
        currentAdapter = new AdViewWrapperAdapter(mCommonAdapter);
        mRecyclerView.setAdapter(currentAdapter);
    }

    @Override
    public void onRefresh() {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (mIsLoadingMore) {
            return;
        }
        sNextPageToken = "";
        mSwipeRefreshLayout.setRefreshing(true);
        requestYoutubeData();
    }

    private Paginate mPaginate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(activity).inflate(R.layout.fragment_main2, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPaginate != null) {
            mPaginate.unbind();
        }
    }
}
