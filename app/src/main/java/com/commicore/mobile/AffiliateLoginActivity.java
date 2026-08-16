package com.commicore.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.MotionEvent;
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
        root.setPadding(0,0,0,0);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(Ui.dp(this,6),Ui.dp(this,4),Ui.dp(this,6),Ui.dp(this,4));

        Button back = mini("←");
        Button up = mini("▲");
        Button down = mini("▼");
        Button reload = mini("↻");
        nav.addView(back,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        nav.addView(up,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        nav.addView(down,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        nav.addView(reload,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        root.addView(nav);

        status = new TextView(this);
        status.setText("Shopee Affiliate Login • ลากหน้าเว็บ หรือใช้ ▲ ▼ เพื่อเลื่อน");
        status.setTextSize(13);
        status.setPadding(Ui.dp(this,10),Ui.dp(this,4),Ui.dp(this,10),Ui.dp(this,6));
        root.addView(status);

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
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setSupportMultipleWindows(false);

        web.setVerticalScrollBarEnabled(true);
        web.setHorizontalScrollBarEnabled(false);
        web.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        web.setOnTouchListener((v,event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if(event.getAction()==MotionEvent.ACTION_DOWN) v.requestFocus();
            return false;
        });

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web,true);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return ShopeeUrlRouter.handle(AffiliateLoginActivity.this, view, url);
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request == null || request.getUrl() == null ? null : request.getUrl().toString();
                return ShopeeUrlRouter.handle(AffiliateLoginActivity.this, view, url);
            }

            @Override public void onPageFinished(WebView view,String url) {
                super.onPageFinished(view,url);
                status.setText("Shopee Affiliate • ลากขึ้น/ลงได้ • " + (url==null?"":url));
            }
        });

        root.addView(web,new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,0,1f));

        Button confirm = new Button(this);
        confirm.setText("✅ Login สำเร็จแล้ว — กลับ CommiCore");
        confirm.setAllCaps(false);
        confirm.setPadding(Ui.dp(this,8),Ui.dp(this,10),Ui.dp(this,8),Ui.dp(this,10));
        root.addView(confirm,new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

        back.setOnClickListener(v -> { if(web.canGoBack()) web.goBack(); else finish(); });
        up.setOnClickListener(v -> scrollBy(-1));
        down.setOnClickListener(v -> scrollBy(1));
        reload.setOnClickListener(v -> web.reload());

        confirm.setOnClickListener(v -> {
            String u = web.getUrl()==null?"":web.getUrl();
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

    private Button mini(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setPadding(Ui.dp(this,4),Ui.dp(this,4),Ui.dp(this,4),Ui.dp(this,4));
        return b;
    }

    private void scrollBy(int direction) {
        if(web==null) return;
        String js = "(function(){var e=document.scrollingElement||document.documentElement||document.body;" +
            "var d=Math.max(window.innerHeight*0.78,500)*" + direction + ";" +
            "if(e&&e.scrollBy){e.scrollBy({top:d,left:0,behavior:'smooth'});}else{window.scrollBy(0,d);} return true;})()";
        web.evaluateJavascript(js,null);
        if(direction>0) web.pageDown(false); else web.pageUp(false);
    }

    @Override public void onBackPressed() {
        if(web!=null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
