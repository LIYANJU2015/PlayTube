package us.shandian.giga.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.admodule.AdModule;
import com.admodule.adfb.IFacebookAd;
import com.admodule.admob.AdMobBanner;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.NativeAd;
import com.google.android.gms.ads.AdListener;

import org.schabi.newpipe.R;

import us.shandian.giga.get.DownloadManager;
import us.shandian.giga.service.DownloadManagerService;
import us.shandian.giga.ui.adapter.MissionAdapter;

public abstract class MissionsFragment extends Fragment {
    private DownloadManager mManager;
    private DownloadManagerService.DMBinder mBinder;

    private SharedPreferences mPrefs;
    private boolean mLinear;
    private MenuItem mSwitch;

    private RecyclerView mList;
    private MissionAdapter mAdapter;
    private GridLayoutManager mGridManager;
    private LinearLayoutManager mLinearManager;
    private Context mActivity;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBinder = (DownloadManagerService.DMBinder) binder;
            mManager = setupDownloadManager(mBinder);
            updateList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // What to do?
        }


    };

    private View setUpNativeAdView(NativeAd nativeAd) {
        nativeAd.unregisterView();

        View adView = LayoutInflater.from(mActivity).inflate(R.layout.home_list_ad_item2, null);

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
        AdChoicesView adChoicesView = new AdChoicesView(mActivity, nativeAd, true);
        adChoicesFrame.addView(adChoicesView, 0);
        adChoicesFrame.setVisibility(View.VISIBLE);

        nativeAd.registerViewForInteraction(adView);

        return adView;
    }

    private FrameLayout mAdFramelayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.missions, container, false);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLinear = mPrefs.getBoolean("linear", false);

        // Bind the service
        Intent i = new Intent();
        i.setClass(getActivity(), DownloadManagerService.class);
        getActivity().bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        // Views
        mList = v.findViewById(R.id.mission_recycler);

        // Init
        mGridManager = new GridLayoutManager(getActivity(), 2);
        mLinearManager = new LinearLayoutManager(getActivity());
        mList.setLayoutManager(mGridManager);

        setHasOptionsMenu(true);

        mAdFramelayout = v.findViewById(R.id.ad_frame);
        NativeAd nativeAd = AdModule.getInstance().getFacebookAd().nextNativieAd();
        if (nativeAd != null && nativeAd.isAdLoaded()) {
            mAdFramelayout.removeAllViews();
            mAdFramelayout.addView(setUpNativeAdView(nativeAd));
        } else {
            initAdMobBanner();
        }

        return v;
    }

    private AdMobBanner adMobBanner;

    private void initAdMobBanner() {
        adMobBanner = AdModule.getInstance().getAdMob().createBannerAdView();
        adMobBanner.setAdRequest(AdModule.getInstance().getAdMob().createAdRequest());
        adMobBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (mActivity == null || !isAdded()
                        || adMobBanner == null) {
                    return;
                }

                adMobBanner.getAdView().setLayoutParams(new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                mAdFramelayout.removeAllViews();
                mAdFramelayout.addView(adMobBanner.getAdView());
            }
        });
    }

    /**
     * Added in API level 23.
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        // Bug: in api< 23 this is never called
        // so mActivity=null
        // so app crashes with nullpointer exception
        mActivity = getActivity();

        AdModule.getInstance().getAdMob().requestNewInterstitial2();
        AdModule.getInstance().getFacebookAd().interstitialLoad("811681725685294_833112496875550",
                new IFacebookAd.FBInterstitialAdListener() {
                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        try {
                            AdModule.getInstance().getFacebookAd().destoryInterstitial();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * deprecated in API level 23,
     * but must remain to allow compatibility with api<23
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unbindService(mConnection);
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            try {
                if (AdModule.getInstance().getFacebookAd().isInterstitialLoaded()) {
                    try {
                        AdModule.getInstance().getFacebookAd().showInterstitial();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        AdModule.getInstance().getFacebookAd().destoryInterstitial();
                        AdModule.getInstance().getAdMob().showInterstitialAd2();
                    }
                } else {
                    AdModule.getInstance().getFacebookAd().destoryInterstitial();
                    AdModule.getInstance().getAdMob().showInterstitialAd2();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            AdModule.getInstance().getFacebookAd().destoryInterstitial();
        }

        if (adMobBanner != null) {
            adMobBanner.destroy();
            adMobBanner = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

		/*switch (item.getItemId()) {
            case R.id.switch_mode:
				mLinear = !mLinear;
				updateList();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}*/
    }

    public void notifyChange() {
        mAdapter.notifyDataSetChanged();
    }

    private void updateList() {
        mAdapter = new MissionAdapter(mActivity, mBinder, mManager, mLinear);

        if (mLinear) {
            mList.setLayoutManager(mLinearManager);
        } else {
            mList.setLayoutManager(mGridManager);
        }

        mList.setAdapter(mAdapter);

        if (mSwitch != null) {
            mSwitch.setIcon(mLinear ? R.drawable.grid : R.drawable.list);
        }

        mPrefs.edit().putBoolean("linear", mLinear).commit();
    }

    protected abstract DownloadManager setupDownloadManager(DownloadManagerService.DMBinder binder);
}
