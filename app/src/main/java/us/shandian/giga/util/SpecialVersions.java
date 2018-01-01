package us.shandian.giga.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.applinks.AppLinkData;
import com.facebook.stetho.common.LogUtil;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;
import org.schabi.newpipe.util.Constants;
import org.schabi.newpipe.util.FilenameUtils;

import java.net.URLDecoder;

/**
 * Created by liyanju on 2018/1/1.
 */

public class SpecialVersions {


    public static void setSpecial() {
        SpecialVersionHandler.setSpecial();
    }

    public static void initSpecial() {
        SpecialVersionHandler.initSpecial();
    }

    public static boolean isSpecial() {
        return SpecialVersionHandler.isSpecial();
    }

    public static void fetchDeferredAppLinkData(Context context) {
        AppLinkDataHandler.fetchDeferredAppLinkData(context);
    }

    public static InstallReferrerReceiverHandler createInstallReferrerReceiverHandler() {
        return new InstallReferrerReceiverHandler();
    }

    public static class SpecialVersionHandler {

        private static volatile boolean isSpecial = false;

        public static void setSpecial() {
            isSpecial = true;
            App.sPreferences.edit().putBoolean(Constants.KEY_SPECIAL, true).apply();
        }

        public static void initSpecial() {
            isSpecial = App.sPreferences.getBoolean(Constants.KEY_SPECIAL, false);
        }

        public static boolean isSpecial() {
            return isSpecial;
        }

        public static boolean isReferrerOpen(String source, String campaign) {
            if (TextUtils.isEmpty(source)) {
                return false;
            }
            if (TextUtils.isEmpty(campaign)) {
                return false;
            }

            if (App.UTM_CAMPAIGN.equals(campaign.trim())
                    && App.UTM_SOURCES.contains(source.trim())) {
                return true;
            }

            return false;
        }
    }

    public static class AppLinkDataHandler {

        public static void fetchDeferredAppLinkData(Context context) {
            int count = App.sPreferences.getInt("fetchcount", 0);
            if (count < 2) {
                count++;
                App.sPreferences.getInt("fetchcount", count);
                AppLinkData.fetchDeferredAppLinkData(context, context.getString(R.string.facebook_app_id),
                        new AppLinkData.CompletionHandler() {
                            @Override
                            public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                                LogUtil.v("xx", " onDeferredAppLinkDataFetched>>>>");
                                if (appLinkData != null && appLinkData.getTargetUri() != null) {
                                    LogUtil.v("xx", " onDeferredAppLinkDataFetched111>>>>");
                                    String deepLinkStr = appLinkData.getTargetUri().toString();
                                    if (App.DEEPLINK.equals(deepLinkStr)) {
                                        App.setSpecial();
                                    }
                                }
                                App.sPreferences.edit().putInt("fetchcount", 2).apply();
                            }
                        });
            }
        }
    }

    public static class InstallReferrerReceiverHandler {

        public void onHandleIntent(Context context, Intent intent) {
            String referrer = intent.getStringExtra("referrer");
            if (referrer == null) {
                return;
            }
            Log.e("Referrer:::::", referrer);

            String source = FilenameUtils.parseRefererSource(referrer);
            String campaign = FilenameUtils.parseRefererCampaign(referrer);
            Log.v("referrer", " source: " + source + " campaign: " + campaign);
            if (SpecialVersionHandler.isReferrerOpen(source, campaign)) {
                Log.v("referrer", "isReferrerOpen true");
                setSpecial();
            }
        }
    }
}
