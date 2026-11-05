package com.amortree.dini;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/** Daily repeating "block is starting" reminders, driven by the schedule the web app hands over. */
final class Reminders {
    static final String CHANNEL = "dini_blocks";
    private static final String PREFS = "dini";

    private Reminders() { }

    static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null || nm.getNotificationChannel(CHANNEL) != null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL, "Schedule reminders", NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription("A nudge when each part of your day starts");
        nm.createNotificationChannel(ch);
    }

    static void save(Context c, String json) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("reminders", json).apply();
    }

    static void scheduleAll(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        int old = p.getInt("count", 0);
        for (int i = 0; i < old; i++) am.cancel(pending(c, i, 0, "", ""));
        int n = 0;
        try {
            JSONArray arr = new JSONArray(p.getString("reminders", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                scheduleOne(c, i, o.getInt("m"), o.optString("t", "Dini"), o.optString("x", ""));
                n++;
            }
        } catch (Exception ignored) { }
        p.edit().putInt("count", n).apply();
    }

    static void scheduleOne(Context c, int idx, int minuteOfDay, String title, String text) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, (minuteOfDay / 60) % 24);
        cal.set(Calendar.MINUTE, minuteOfDay % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis() + 1000) cal.add(Calendar.DAY_OF_YEAR, 1);
        PendingIntent pi = pending(c, idx, minuteOfDay, title, text);
        // Inexact-while-idle: needs no special permission and still fires in Doze.
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }

    private static PendingIntent pending(Context c, int idx, int m, String title, String text) {
        Intent in = new Intent(c, ReminderReceiver.class);
        in.setAction("com.amortree.dini.REMIND");
        in.putExtra("i", idx);
        in.putExtra("m", m);
        in.putExtra("t", title);
        in.putExtra("x", text);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(c, 1000 + idx, in, flags);
    }
}
