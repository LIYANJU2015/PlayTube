package org.schabi.newpipe;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.admodule.AdModule;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.tubewebplayer.YouTubePlayerActivity;

import org.schabi.newpipe.api.YoutubeApiService;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.settings.SettingsActivity;
import org.schabi.newpipe.util.ExtractorHelper;
import org.schabi.newpipe.util.StateSaver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;

import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import us.shandian.giga.util.SpecialVersions;

/*
 * Copyright (C) Hans-Christoph Steiner 2016 <hans@eds.org>
 * App.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class App extends Application {
    protected static final String TAG = App.class.toString();



    public static Context sContext;

    public static final String UTM_SOURCES = "google";
    public static final String UTM_CAMPAIGN = "tube_player_recom";
    public static final String DEEPLINK = "tubeplayer://player/6666";


    public static SharedPreferences sPreferences;

    public static boolean isCoolStart = false;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    public static void addShortcut(Context context, Class clazz, String appName, int ic_launcher) {
        // 安装的Intent
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.putExtra("tName", appName);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
        shortcutIntent.setClassName(context, clazz.getName());
        //        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 快捷名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.app_name));
        // 快捷图标是否允许重复
        shortcut.putExtra("duplicate", false);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        // 快捷图标
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context, ic_launcher);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        // 发送广播
        context.sendBroadcast(shortcut);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        isCoolStart = true;
        sPreferences = PreferenceManager.getDefaultSharedPreferences(this);

//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);

        SpecialVersions.initSpecial();
        SpecialVersions.fetchDeferredAppLinkData(sContext);

        if (!sPreferences.getBoolean("addShortcut", false)) {
            sPreferences.edit().putBoolean("addShortcut", true).apply();
            addShortcut(sContext, MainActivity.class, getString(R.string.app_name), R.mipmap.ic_launcher);
        }

        // Initialize settings first because others inits can use its values
        SettingsActivity.initSettings(this);

        NewPipe.init(Downloader.getInstance());
        NewPipeDatabase.init(this);
        StateSaver.init(this);
        initNotificationChannel();

        // Initialize image loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        configureRxJavaErrorHandler();

        AdModule.init(new AdModule.AdCallBack() {
            @Override
            public Application getApplication() {
                return App.this;
            }

            @Override
            public String getAppId() {
                return "ca-app-pub-9880857526519562~1189686181";
            }

            @Override
            public boolean isAdDebug() {
                return false;
            }

            @Override
            public boolean isLogDebug() {
                return false;
            }

            @Override
            public String getAdMobNativeAdId() {
                return null;
            }

            @Override
            public String getBannerAdId() {
                return "ca-app-pub-9880857526519562/6119359485";
            }

            @Override
            public String getInterstitialAdId() {
                return "ca-app-pub-9880857526519562/3432706143";
            }

            @Override
            public String getTestDevice() {
                return null;
            }

            @Override
            public String getRewardedVideoAdId() {
                return null;
            }

            @Override
            public String getFBNativeAdId() {
                return "811681725685294_811682365685230";
            }
        });

        AdModule.getInstance().getAdMob().initInterstitialAd();
        AdModule.getInstance().getAdMob().requestNewInterstitial();

        AdModule.getInstance().getAdMob().initInterstitialAd2("ca-app-pub-9880857526519562/2152197880");
        AdModule.getInstance().getAdMob().requestNewInterstitial2();


        CrashReport.initCrashReport(getApplicationContext(), "5bf803957f", false);

        YouTubePlayerActivity.setDeveloperKey(YoutubeApiService.DEVOTE_KEY);

        AdModule.getInstance().getFacebookAd().loadAds("811681725685294_811682365685230");

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    public static void setSpecial() {
        SpecialVersions.setSpecial();
    }

    /**
     * 默认false
     * @return
     */
    public static boolean isSpecial() {
        return SpecialVersions.isSpecial();
    }

    private void configureRxJavaErrorHandler() {
        // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                Log.e(TAG, "RxJavaPlugins.ErrorHandler called with -> : throwable = [" + throwable.getClass().getName() + "]");

                if (throwable instanceof UndeliverableException) {
                    // As UndeliverableException is a wrapper, get the cause of it to get the "real" exception
                    throwable = throwable.getCause();
                }

                if (throwable instanceof CompositeException) {
                    for (Throwable element : ((CompositeException) throwable).getExceptions()) {
                        if (checkThrowable(element)) return;
                    }
                }

                if (checkThrowable(throwable)) return;

                // Throw uncaught exception that will trigger the report system
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), throwable);
            }

            private boolean checkThrowable(@NonNull Throwable throwable) {
                // Don't crash the application over a simple network problem
                return ExtractorHelper.hasAssignableCauseThrowable(throwable,
                        IOException.class, SocketException.class, InterruptedException.class, InterruptedIOException.class);
            }
        });
    }


//    private void initACRA() {
//        try {
//            final ACRAConfiguration acraConfig = new ConfigurationBuilder(this)
//                    .setReportSenderFactoryClasses(reportSenderFactoryClasses)
//                    .setBuildConfigClass(BuildConfig.class)
//                    .build();
//            ACRA.init(this, acraConfig);
//        } catch (ACRAConfigurationException ace) {
//            ace.printStackTrace();
//            ErrorActivity.reportError(this, ace, null, null, ErrorActivity.ErrorInfo.make(UserAction.SOMETHING_ELSE, "none",
//                    "Could not initialize ACRA crash report", R.string.app_ui_crash));
//        }
//    }

    public void initNotificationChannel() {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }

        final String id = getString(R.string.notification_channel_id);
        final CharSequence name = getString(R.string.notification_channel_name);
        final String description = getString(R.string.notification_channel_description);

        // Keep this below DEFAULT to avoid making noise on every notification update
        final int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(mChannel);
    }
}
