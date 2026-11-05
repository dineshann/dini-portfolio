package com.amortree.dini;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

/** Ongoing "timer is running" notification: live system chronometer + a Stop action. */
final class TimerNotif {
    static final String CHANNEL = "dini_timer";
    private static final int ID = 3000;

    private TimerNotif() { }

    static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null || nm.getNotificationChannel(CHANNEL) != null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL, "Running timer", NotificationManager.IMPORTANCE_LOW);
        ch.setDescription("Shows the task you are tracking, with a Stop button");
        ch.setShowBadge(false);
        nm.createNotificationChannel(ch);
    }

    static void show(Context c, String client, String desc, long startMs) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        ensureChannel(c);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        Intent open = new Intent(c, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(c, 10, open, flags);

        Intent stop = new Intent(c, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra("stop", true);
        PendingIntent stopPi = PendingIntent.getActivity(c, 11, stop, flags);

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(c, CHANNEL)
                : new Notification.Builder(c);
        b.setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(client == null || client.length() == 0 ? "Tracking time" : client)
                .setContentText(desc == null || desc.length() == 0 ? "Timer running" : desc)
                .setWhen(startMs)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(0xFFE0381E)
                .setContentIntent(openPi)
                .addAction(R.drawable.ic_stat, "Stop", stopPi);
        if (Build.VERSION.SDK_INT < 26) b.setPriority(Notification.PRIORITY_LOW);
        nm.notify(ID, b.build());
    }

    static void clear(Context c) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(ID);
        cancelAlerts(c);
    }

    private static PendingIntent alertPi(Context c, int i, String title, String text) {
        Intent in = new Intent(c, TimerAlertReceiver.class);
        in.setAction("com.amortree.dini.TIMER_ALERT");
        in.putExtra("i", i);
        in.putExtra("t", title);
        in.putExtra("x", text);
        return PendingIntent.getBroadcast(c, 4000 + i, in, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void cancelAlerts(Context c) {
        SharedPreferences p = c.getSharedPreferences("dini", Context.MODE_PRIVATE);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        int old = p.getInt("talerts", 0);
        if (am != null) for (int i = 0; i < old; i++) am.cancel(alertPi(c, i, "", ""));
        p.edit().putInt("talerts", 0).apply();
    }

    /** json: [{"at":epochMs,"t":"title","x":"text"}, ...] — replaces any alerts set before. */
    static void setAlerts(Context c, String json) {
        cancelAlerts(c);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        int n = 0;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length() && i < 6; i++) {
                JSONObject o = arr.getJSONObject(i);
                long at = o.getLong("at");
                if (at <= System.currentTimeMillis()) continue;
                PendingIntent pi = alertPi(c, n, o.optString("t", "Timer"), o.optString("x", ""));
                boolean exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms();
                if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
                else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
                n++;
            }
        } catch (Exception ignored) { }
        c.getSharedPreferences("dini", Context.MODE_PRIVATE).edit().putInt("talerts", n).apply();
    }
}
