package org.schabi.newpipe.fragments.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.admodule.AdModule;
import com.admodule.admob.AdMobBanner;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.NativeAd;
import com.google.android.gms.ads.AdListener;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.util.AdViewWrapperAdapter;
import org.schabi.newpipe.util.Constants;

import java.util.Queue;

import icepick.State;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseListInfoFragment<I extends ListInfo> extends BaseListFragment<I, ListExtractor.NextItemsResult> {

    @State
    protected int serviceId = Constants.NO_SERVICE_ID;
    @State
    protected String name;
    @State
    protected String url;

    protected I currentInfo;
    protected String currentNextItemsUrl;
    protected Disposable currentWorker;

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);
        setTitle(name);
        showListFooter(hasMoreItems());
        initAdMobBanner();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adMobBanner != null) {
            adMobBanner.destroy();
            adMobBanner = null;
        }

        if (adViewWrapperAdapter != null && !adViewWrapperAdapter.isAddAdView()) {
            AdModule.getInstance().getAdMob().showInterstitialAd();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adMobBanner != null) {
            adMobBanner.pause();
        }

        if (currentWorker != null) currentWorker.dispose();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if it was loading when the fragment was stopped/paused,
        if (wasLoading.getAndSet(false)) {
            if (hasMoreItems() && infoListAdapter.getItemsList().size() > 0) {
                loadMoreItems();
            } else {
                doInitialLoadLogic();
            }
        }

        if (adMobBanner != null) {
            adMobBanner.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentWorker != null) currentWorker.dispose();
        currentWorker = null;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // State Saving
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void writeTo(Queue<Object> objectsToSave) {
        super.writeTo(objectsToSave);
        objectsToSave.add(currentInfo);
        objectsToSave.add(currentNextItemsUrl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readFrom(@NonNull Queue<Object> savedObjects) throws Exception {
        super.readFrom(savedObjects);
        currentInfo = (I) savedObjects.poll();
        currentNextItemsUrl = (String) savedObjects.poll();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public void setTitle(String title) {
        Log.d(TAG, "setTitle() called with: title = [" + title + "]");
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(title);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Load and handle
    //////////////////////////////////////////////////////////////////////////*/

    protected void doInitialLoadLogic() {
        if (DEBUG) Log.d(TAG, "doInitialLoadLogic() called");
        if (currentInfo == null) {
            startLoading(false);
        } else handleResult(currentInfo);
    }

    /**
     * Implement the logic to load the info from the network.<br/>
     * You can use the default implementations from {@link org.schabi.newpipe.util.ExtractorHelper}.
     *
     * @param forceLoad allow or disallow the result to come from the cache
     */
    protected abstract Single<I> loadResult(boolean forceLoad);

    @Override
    public void startLoading(boolean forceLoad) {
        super.startLoading(forceLoad);

        showListFooter(false);
        currentInfo = null;
        if (currentWorker != null) currentWorker.dispose();
        currentWorker = loadResult(forceLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<I>() {
                    @Override
                    public void accept(@NonNull I result) throws Exception {
                        isLoading.set(false);
                        currentInfo = result;
                        currentNextItemsUrl = result.next_streams_url;
                        handleResult(result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        onError(throwable);
                    }
                });
    }

    /**
     * Implement the logic to load more items<br/>
     * You can use the default implementations from {@link org.schabi.newpipe.util.ExtractorHelper}
     */
    protected abstract Single<ListExtractor.NextItemsResult> loadMoreItemsLogic();

    protected void loadMoreItems() {
        isLoading.set(true);

        if (currentWorker != null) currentWorker.dispose();
        currentWorker = loadMoreItemsLogic()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ListExtractor.NextItemsResult>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull ListExtractor.NextItemsResult nextItemsResult) throws Exception {
                        if (nextItemsResult == null) {
                            return;
                        }

                        isLoading.set(false);
                        handleNextItems(nextItemsResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        isLoading.set(false);
                        onError(throwable);
                    }
                });
    }

    @Override
    public void handleNextItems(ListExtractor.NextItemsResult result) {
        super.handleNextItems(result);
        currentNextItemsUrl = result.nextItemsUrl;
        infoListAdapter.addInfoItemList(result.nextItemsList);

        showListFooter(hasMoreItems());
    }

    @Override
    protected boolean hasMoreItems() {
        return !TextUtils.isEmpty(currentNextItemsUrl);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/


    private AdViewWrapperAdapter adViewWrapperAdapter;

    @Override
    public RecyclerView.Adapter onGetAdapter() {
        adViewWrapperAdapter = new AdViewWrapperAdapter(infoListAdapter);
        infoListAdapter.setParentAdapter(adViewWrapperAdapter);
        return adViewWrapperAdapter;
    }

    private View setUpNativeAdView(NativeAd nativeAd) {
        nativeAd.unregisterView();

        View adView = LayoutInflater.from(activity).inflate(R.layout.home_list_ad_item2, null);

        FrameLayout adChoicesFrame = (FrameLayout) adView.findViewById(R.id.fb_adChoices2);
        ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.image_ad);
        TextView nativeAdTitle = (TextView) adView.findViewById(R.id.title);
        TextView nativeAdBody = (TextView) adView.findViewById(R.id.text);
        TextView nativeAdCallToAction = (TextView) adView.findViewById(R.id.call_btn_tv);

        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdTitle.setText(nativeAd.getAdTitle());
        nativeAdBody.setText(nativeAd.getAdBody());

        // Downloading and setting the ad icon.
        NativeAd.Image adIcon = nativeAd.getAdIcon();
        NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

        // Add adChoices icon
        AdChoicesView adChoicesView = new AdChoicesView(activity, nativeAd, true);
        adChoicesFrame.addView(adChoicesView, 0);
        adChoicesFrame.setVisibility(View.VISIBLE);

        nativeAd.registerViewForInteraction(adView);

        return adView;
    }

    private AdMobBanner adMobBanner;

    private void initAdMobBanner() {
        adMobBanner = AdModule.getInstance().getAdMob().createBannerAdView();
        adMobBanner.setAdRequest(AdModule.getInstance().getAdMob().createAdRequest());
        adMobBanner.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (activity == null || !isAdded()
                        || activity.isFinishing()
                        || adMobBanner == null) {
                    return;
                }
                if (adViewWrapperAdapter != null && !adViewWrapperAdapter.isAddAdView()
                        && adViewWrapperAdapter.getItemCount() > 4) {
                    adMobBanner.getAdView().setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                            RecyclerView.LayoutParams.WRAP_CONTENT));
                    adViewWrapperAdapter.addAdView(22, new AdViewWrapperAdapter.
                            AdViewItem(adMobBanner.getAdView(), 2));
                    adViewWrapperAdapter.notifyItemInserted(2);
                }
            }
        });
    }

    @Override
    public void handleResult(@NonNull I result) {
        super.handleResult(result);

        if (!isAdded() || infoListAdapter == null
                || result.related_streams == null) {
            return;
        }

        url = result.getUrl();
        name = result.getName();
        setTitle(name);

        if (infoListAdapter.getItemsList() != null && infoListAdapter.getItemsList().size() == 0) {
            if (result.related_streams.size() > 0) {
                NativeAd nativeAd = AdModule.getInstance().getFacebookAd().getNativeAd();
                if (nativeAd == null || !nativeAd.isAdLoaded()) {
                    nativeAd = AdModule.getInstance().getFacebookAd().nextNativieAd();
                }
                if (nativeAd != null && nativeAd.isAdLoaded() && result.related_streams.size() > 4) {
                    adViewWrapperAdapter.addAdView(22, new AdViewWrapperAdapter.
                            AdViewItem(setUpNativeAdView(nativeAd), 2));
                    infoListAdapter.addInfoItemList2(result.related_streams);
                    adViewWrapperAdapter.notifyDataSetChanged();
                } else if(adMobBanner != null && adMobBanner.isLoaded()
                        && !adViewWrapperAdapter.isAddAdView() && result.related_streams.size() > 4){
                    adMobBanner.getAdView().setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                            RecyclerView.LayoutParams.WRAP_CONTENT));
                    adViewWrapperAdapter.addAdView(22, new AdViewWrapperAdapter.
                            AdViewItem(adMobBanner.getAdView(), 2));
                    infoListAdapter.addInfoItemList2(result.related_streams);
                    adViewWrapperAdapter.notifyDataSetChanged();
                } else {
                    infoListAdapter.addInfoItemList(result.related_streams);
                }
                showListFooter(hasMoreItems());
            } else {
                infoListAdapter.clearStreamItemList();
                showEmptyState();
            }
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    protected void setInitialData(int serviceId, String url, String name) {
        this.serviceId = serviceId;
        this.url = url;
        this.name = !TextUtils.isEmpty(name) ? name : "";
    }
}
