package com.armandasalmd.weeklyroutine.notification;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.armandasalmd.weeklyroutine.MainActivity;
import com.armandasalmd.weeklyroutine.R;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.EventProp;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.google.gson.Gson;

import java.util.Arrays;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class MyIntentService extends IntentService {

    private Event event;

    private final static String chanellId = "994456";
    private String title, message;

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            loadIntentInfo(intent.getExtras());
            if (event == null) {
                Log.i("Armandas", "onHandleIntent: event is null");
                return;
            }

            int use_url;
            if (event.isSpecial)
                use_url = R.string.key_use_notif_special;
            else
                use_url = R.string.key_use_notif_plans;
            boolean useNotif = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean(getString(use_url), true);

            if (useNotif) {
                calcMessage();
                Intent notifyIntent = new Intent(this, MainActivity.class);
                notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, event.getNotificationId(),
                        notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                boolean vibrate = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getBoolean(getString(R.string.key_vibrate), false);

                if (!vibrate) {
                    PugNotification.with(getApplicationContext())
                            .load().autoCancel(true)
                            .title(title).click(pendingIntent)
                            .when(System.currentTimeMillis())
                            .message(message)
                            .smallIcon(R.drawable.ic_notif)
                            .flags(Notification.DEFAULT_ALL)
                            .vibrate(new long[] {})
                            .simple().build();
                } else {
                    PugNotification.with(getApplicationContext())
                            .load().autoCancel(true)
                            .title(title).click(pendingIntent)
                            .when(System.currentTimeMillis())
                            .message(message)
                            .smallIcon(R.drawable.ic_notif)
                            .flags(Notification.DEFAULT_ALL)
                            .vibrate(new long[] {400, 250, 400})
                            .simple().build();
                }
            }
        } else
            Log.i("Armandas", "failed to load intent");
    }

    private void calcMessage() {
        if (!event.getDescription().isEmpty()) {
            title = (event.getTitle() + " (" + event.getTimeFrom() + ")");
            message = (event.getDescription());
        } else {
            title = (event.getTitle());
            if (event.useTimeTo)
                message = (event.getTimeFrom() + " - " + event.getTimeTo() + "(" +
                        event.getDuration().toString(getApplicationContext()) + ")");
            else
                message = (event.getTimeFrom());
        }
    }

    private void loadIntentInfo(Bundle bundle) {
        Gson gson = new Gson();
        event = gson.fromJson(bundle.getString(NotifCenter.TAG_EVENT), Event.class);
    }
}
