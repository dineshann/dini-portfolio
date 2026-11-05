package com.amortree.dini;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent in) {
        try {
            Reminders.ensureChannel(c);
            Reminders.scheduleAll(c);
        } catch (Exception ignored) { }
    }
}
