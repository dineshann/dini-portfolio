package com.amortree.dini;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Fires when a running timer reaches its goal (30/40 min work, 20 min focus, 5 min break). */
public class TimerAlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent in) {
        try {
            String title = in.getStringExtra("t");
            String text = in.getStringExtra("x");
            int idx = in.getIntExtra("i", 0);
            Reminders.ensureChannel(c);
            Intent open = new Intent(c, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(c, 12, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notification.Builder b = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(c, Reminders.CHANNEL)
                    : new Notification.Builder(c);
            b.setSmallIcon(R.drawable.ic_stat)
                    .setContentTitle(title == null ? "Timer" : title)
                    .setContentText(text == null ? "" : text)
                    .setColor(0xFFE0381E)
                    .setContentIntent(pi)
                    .setAutoCancel(true);
            if (Build.VERSION.SDK_INT < 26) b.setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL);
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.notify(3100 + idx, b.build());
        } catch (Exception ignored) { }
    }
}
