package org.schabi.newpipe.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import us.shandian.giga.util.SpecialVersions;

/**
 * Created by liyanju on 2017/12/8.
 */

public class InstallReferrerReceiver extends BroadcastReceiver {

    private static final String TARGET = "&referrer=";

    private SpecialVersions.InstallReferrerReceiverHandler receiverHandler =
            SpecialVersions.createInstallReferrerReceiverHandler();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            receiverHandler.onHandleIntent(context, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
