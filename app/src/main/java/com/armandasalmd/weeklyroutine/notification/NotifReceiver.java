package com.armandasalmd.weeklyroutine.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.OpenData;

import java.io.Serializable;


public class NotifReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MyIntentService.class);
        intent1.putExtras(intent.getExtras());
        context.startService(intent1);
    }
}
