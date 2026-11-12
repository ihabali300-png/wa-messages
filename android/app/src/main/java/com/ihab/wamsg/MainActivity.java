package com.ihab.wamsg;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Full-screen WebView around the customer-messages tool (served from GitHub Pages, so it updates by itself). */
public class MainActivity extends Activity {
    private static final String HOME = "https://ihabali300-png.github.io/wa-messages/";
    private WebView web;

    /** Copy from the page: navigator.clipboard is unreliable inside WebView. */
    public class Bridge {
        @JavascriptInterface public void copy(String text) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("message", text));
        }
    }

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " WaMsgApk");
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String host = u.getHost() == null ? "" : u.getHost();
                if ("ihabali300-png.github.io".equals(host)) return false;
                if (host.endsWith("whatsapp.com") || "wa.me".equals(host)) {   // open WhatsApp app directly, text stays UTF-8 (emoji safe)
                    Uri.Builder b = new Uri.Builder().scheme("whatsapp").authority("send");
                    String phone = u.getQueryParameter("phone"), text = u.getQueryParameter("text");
                    if (phone != null && !phone.isEmpty()) b.appendQueryParameter("phone", phone);
                    if (text != null) b.appendQueryParameter("text", text);
                    try { startActivity(new Intent(Intent.ACTION_VIEW, b.build())); return true; }
                    catch (ActivityNotFoundException e) { /* no WhatsApp: fall through to browser */ }
                }
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (ActivityNotFoundException e) { }
                return true;
            }
        });
        if (saved != null) web.restoreState(saved); else web.loadUrl(HOME + "?apk=1");
    }

    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); web.saveState(out); }

    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
