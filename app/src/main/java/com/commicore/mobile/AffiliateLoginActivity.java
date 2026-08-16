package com.commicore.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AffiliateLoginActivity extends Activity {
    private static final String AFFILIATE_URL = "https://affiliate.shopee.co.th/";
    private WebView web;
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 0);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setPadding(Ui.dp(this,8),Ui.dp(this,6),Ui.dp(this,8),Ui.dp(this,6));

        Button back = new Button(this);
        back.setText("← ย้อนกลับ");
        back.setAllCaps(false);
        top.addView(back, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        status = new TextView(this);
        status.setText("Shopee Affiliate Login • เลื่อนหน้าเว็บได้เต็มจอ");
        status.setTextSize(14);
        status.setPadding(Ui.dp(this,10),Ui.dp(this,8),Ui.dp(this,10),Ui.dp(this,8));
        top.addView(status, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));
        root.addView(top);

        web = new WebView(this);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(false);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        web.setVerticalScrollBarEnabled(true);
        web.setHorizontalScrollBarEnabled(false);
        web.setNestedScrollingEnabled(true);
        web.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view,url);
                status.setText("Shopee Affiliate • " + (url == null ? "" : url));
            }
        });

        root.addView(web, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        Button confirm = new Button(this);
        confirm.setText("✅ Login สำเร็จแล้ว — กลับ CommiCore");
        confirm.setAllCaps(false);
        confirm.setPadding(Ui.dp(this,8),Ui.dp(this,10),Ui.dp(this,8),Ui.dp(this,10));
        root.addView(confirm, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        back.setOnClickListener(v -> {
            if(web.canGoBack()) web.goBack(); else finish();
        });

        confirm.setOnClickListener(v -> {
            String u = web.getUrl()==null ? "" : web.getUrl();
            if(!u.contains("affiliate.shopee.co.th")) {
                Toast.makeText(this,"ยังไม่ได้อยู่ใน Shopee Affiliate",Toast.LENGTH_LONG).show();
                return;
            }
            LoginPrefs.setShopeeReady(this,true);
            cm.flush();
            Toast.makeText(this,"บันทึกสถานะ Shopee Affiliate แล้ว",Toast.LENGTH_SHORT).show();
            finish();
        });

        setContentView(root);
        web.loadUrl(AFFILIATE_URL);
    }

    @Override public void onBackPressed() {
        if(web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
