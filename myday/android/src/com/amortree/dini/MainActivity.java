package com.amortree.dini;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;
    /** Time the user tapped "Stop" on the timer notification (0 = none pending). */
    private volatile long pendingStop = 0;

    private boolean isDark() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    /** Status/nav bar colour + icon contrast always follow the real system theme. */
    private void applyBars() {
        boolean dark = isDark();
        int bg = dark ? Color.parseColor("#0B0B0B") : Color.parseColor("#EDEDEB");
        Window w = getWindow();
        w.setStatusBarColor(bg);
        w.setNavigationBarColor(bg);
        View decor = w.getDecorView();
        int f = decor.getSystemUiVisibility();
        if (dark) {
            f &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) f &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            f |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) f |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(f);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        applyBars();
        Reminders.ensureChannel(this);
        TimerNotif.ensureChannel(this);
        noteStopIntent(getIntent());
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        web = new WebView(this);
        web.setBackgroundColor(Color.TRANSPARENT);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return handle(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return handle(r.getUrl().toString());
            }

            private boolean handle(String url) {
                if (url.startsWith("file:///android_asset/")) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) { }
                return true;
            }
        });
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    /** Remember a Stop tap from the notification (timestamp = the tap, not when JS gets to it). */
    private void noteStopIntent(Intent in) {
        if (in != null && in.getBooleanExtra("stop", false)) {
            pendingStop = System.currentTimeMillis();
            in.removeExtra("stop");
        }
    }

    @Override
    protected void onNewIntent(Intent in) {
        super.onNewIntent(in);
        setIntent(in);
        noteStopIntent(in);
        if (web != null) web.evaluateJavascript("window.__nativeStop&&window.__nativeStop()", null);
    }

    @Override
    public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        applyBars();
        if (web != null) web.evaluateJavascript("window.onNativeTheme&&window.onNativeTheme(" + isDark() + ")", null);
    }

    @Override
    public void onBackPressed() {
        if (web == null) { super.onBackPressed(); return; }
        web.evaluateJavascript("(window.__back?window.__back():false)", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String v) {
                if (!"true".equals(v)) moveTaskToBack(true);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
    }

    private class Bridge {
        @JavascriptInterface
        public boolean isDark() {
            return MainActivity.this.isDark();
        }

        @JavascriptInterface
        public void showTimer(String client, String desc, long startMs) {
            try { TimerNotif.show(MainActivity.this, client, desc, startMs); } catch (Exception ignored) { }
        }

        @JavascriptInterface
        public void setTimerAlerts(String json) {
            try { TimerNotif.setAlerts(MainActivity.this, json); } catch (Exception ignored) { }
        }

        @JavascriptInterface
        public void clearTimer() {
            try { TimerNotif.clear(MainActivity.this); } catch (Exception ignored) { }
        }

        /** Returns (and clears) the time the user hit Stop on the notification, or 0. */
        @JavascriptInterface
        public long pendingStop() {
            long t = pendingStop;
            pendingStop = 0;
            return t;
        }

        @JavascriptInterface
        public void setReminders(String json) {
            try {
                Reminders.save(MainActivity.this, json);
                Reminders.scheduleAll(MainActivity.this);
            } catch (Exception ignored) { }
        }
    }
}
