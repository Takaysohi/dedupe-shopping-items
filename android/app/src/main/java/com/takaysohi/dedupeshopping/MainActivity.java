package com.takaysohi.dedupeshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** 楽天市場を表示し、同じ商品の重複を隠すスクリプトを各ページに差し込む。 */
public class MainActivity extends Activity {

    private static final String HOME = "https://www.rakuten.co.jp/";
    private static final int COLOR_TRUSTED = Color.parseColor("#1F7A4D");
    private static final int COLOR_WARNING = Color.parseColor("#B45309");

    private WebView web;
    private TextView bar;
    private String script = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        script = readAsset("dedupe.js");

        // 上部に、いま開いているサイトのドメインを常に表示する
        float d = getResources().getDisplayMetrics().density;
        int pad = (int) (8 * d);
        bar = new TextView(this);
        bar.setTextSize(13);
        bar.setTextColor(Color.WHITE);
        bar.setPadding(pad * 2, pad, pad * 2, pad);
        bar.setSingleLine(true);
        bar.setEllipsize(TextUtils.TruncateAt.MIDDLE);

        web = new WebView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(bar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(web, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        updateBar(HOME);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    return false; // ログインや決済も含め、アプリ内で開く
                }
                if (!"intent".equals(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (ActivityNotFoundException ignored) {
                        // 開けるアプリがなければ何もしない
                    }
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                updateBar(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                updateBar(url);
                view.evaluateJavascript(script, null);
            }
        });

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(HOME);
        }
    }

    private void updateBar(String url) {
        String host = url == null ? null : Uri.parse(url).getHost();
        if (host == null) {
            return;
        }
        if (isRakuten(host)) {
            bar.setBackgroundColor(COLOR_TRUSTED);
            bar.setText(host);
        } else {
            bar.setBackgroundColor(COLOR_WARNING);
            bar.setText("楽天以外のサイトです: " + host);
        }
    }

    private static boolean isRakuten(String host) {
        return host.equals("rakuten.co.jp") || host.endsWith(".rakuten.co.jp")
                || host.equals("rakuten.ne.jp") || host.endsWith(".rakuten.ne.jp")
                || host.equals("rakuten.com") || host.endsWith(".rakuten.com");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private String readAsset(String name) {
        try (InputStream in = getAssets().open(name)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return out.toString("UTF-8");
        } catch (IOException e) {
            return "";
        }
    }
}
