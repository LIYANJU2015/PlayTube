package org.schabi.newpipe.util;

import android.os.Bundle;

import com.facebook.appevents.AppEventsLogger;

import org.schabi.newpipe.App;

/**
 * Created by liyanju on 2017/12/13.
 */

public class FacebookReport {

    public static void logSentReferrer(String referrer)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("cus_referrer", referrer);
        logger.logEvent("ReferrerReceiver",bundle);
    }

    public static void logSentCountry(String country)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("country", country);
        bundle.putString("phone", android.os.Build.MODEL);
        logger.logEvent("ReferrerReceiverCountry",bundle);
    }

    public static void logSendSearchPage() {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("isSpecail", String.valueOf(App.isSpecial()));
        logger.logEvent("AppSearchPage",bundle);
    }

    public static void logSendMainPage() {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("isSpecail", String.valueOf(App.isSpecial()));
        logger.logEvent("AppMainPage",bundle);
    }

    public static void logSendVideoDetail() {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        logger.logEvent("AppVideoDetail");
    }

    public static void logSendDownload(String action) {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        logger.logEvent("AppDownloadPage",bundle);
    }

    public static void logSendAppRating(String star) {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("start", star);
        logger.logEvent("AppRating",bundle);
    }

    public static void logSendSearchPage(String search) {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("search", search);
        logger.logEvent("AppSearchPage",bundle);
    }

    public static void logSentReferrer2(String campaign, String source) {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("utm_campaign", campaign);
        bundle.putString("utm_source", source);
        logger.logEvent("ReferrerReceiver2",bundle);
    }

    public static void logSentReferrer4(String linkData) {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("linkData", linkData);
        logger.logEvent("ReferrerReceiver4",bundle);
    }

    public static void logSentReferrer2(String from) {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("special", "success " + from);
        logger.logEvent("ReferrerReceiver3",bundle);
    }

    public static void logSentUSOpen() {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        logger.logEvent("us open");
    }
}
