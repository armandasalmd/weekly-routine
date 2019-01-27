package com.armandasalmd.weeklyroutine.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Armandas on 2017-10-12.
 */

public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent pushIntent = new Intent(context, RebootService.class);
        context.startService(pushIntent);
    }
}
