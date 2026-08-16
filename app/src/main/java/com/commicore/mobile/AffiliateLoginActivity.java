package com.commicore.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

        LinearLayout primary = new LinearLayout(this);
        primary.setOrientation(LinearLayout.HORIZONTAL);
        primary.setPadding(Ui.dp(this,4),Ui.dp(this,2),Ui.dp(this,4),Ui.dp(this,2));
        Button back = mini("←");
        Button verify = mini("◎ ยืนยันตัวตน");
        Button done = mini("✓ เสร็จ");
        primary.addView(back,new LinearLayout.LayoutParams(0,Ui.dp(this,40),1f));
        primary.addView(verify,new LinearLayout.LayoutParams(0,Ui.dp(this,40),2f));
        primary.addView(done,new LinearLayout.LayoutParams(0,Ui.dp(this,40),1.2f));
        root.addView(primary);

        LinearLayout tools = new LinearLayout(this);
        tools.setOrientation(LinearLayout.HORIZONTAL);
        tools.setPadding(Ui.dp(this,4),0,Ui.dp(this,4),0);
        Button top = mini("⇈");
        Button up = mini("▲");
        Button down = mini("▼");
        Button bottom = mini("⇊");
        Button minus = mini("−");
        Button plus = mini("+");
        Button reload = mini("↻");
        tools.addView(top,new LinearLayout.LayoutParams(0,Ui.dp(this,34),1f));
        tools.addView(up,new LinearLayout.LayoutParams(0,Ui.dp(this,34),1f));
        tools.addView(down,new LinearLayout.LayoutParams(0,Ui.dp(this,34),1f));
        tools.addView(bottom,new LinearLayout.LayoutParams(0,Ui.dp(this,34),1f));
        tools.addView(minus,new LinearLayout.LayoutParams(0,Ui.dp(this,34),1f));
        tools.addView(plus,new LinearLayout.LayoutParams(0,Ui.dp(this,34),1f));
        tools.addView(reload,new LinearLayout.LayoutParams(0,Ui.dp(this,34),1f));
        root.addView(tools);

        status = new TextView(this);
        status.setText("Shopee Affiliate");
        status.setTextSize(12);
        status.setSingleLine(true);
        status.setEllipsize(TextUtils.TruncateAt.END);
        status.setPadding(Ui.dp(this,8),Ui.dp(this,2),Ui.dp(this,8),Ui.dp(this,3));
        root.addView(status,new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,Ui.dp(this,26)));

        web = new WebView(this);
        WebViewPageTools.configure(web);

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
                status.setText("Shopee Affiliate • " + WebViewPageTools.shortLocation(url));
                if(url != null && (url.contains("/verify/") || url.contains("traffic") || url.contains("captcha"))) {
                    view.postDelayed(() -> WebViewPageTools.focusVerification(view), 700);
                }
            }
        });

        root.addView(web,new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,0,1f));

        back.setOnClickListener(v -> { if(web.canGoBack()) web.goBack(); else finish(); });
        verify.setOnClickListener(v -> WebViewPageTools.focusVerification(web));
        top.setOnClickListener(v -> WebViewPageTools.toTop(web));
        up.setOnClickListener(v -> WebViewPageTools.scroll(web,-1));
        down.setOnClickListener(v -> WebViewPageTools.scroll(web,1));
        bottom.setOnClickListener(v -> WebViewPageTools.toBottom(web));
        minus.setOnClickListener(v -> WebViewPageTools.zoomOut(web));
        plus.setOnClickListener(v -> WebViewPageTools.zoomIn(web));
        reload.setOnClickListener(v -> web.reload());

        done.setOnClickListener(v -> {
            String u = web.getUrl()==null?"":web.getUrl();
            if(!u.contains("affiliate.shopee.co.th") && !u.contains("shopee.co.th")) {
                Toast.makeText(this,"ยังไม่ได้อยู่ในหน้า Shopee/Affiliate",Toast.LENGTH_LONG).show();
                return;
            }
            if(u.contains("/verify/") || u.contains("traffic") || u.contains("captcha")) {
                Toast.makeText(this,"กรุณายืนยันตัวตนให้ผ่านก่อน แล้วค่อยกด ✓ เสร็จ",Toast.LENGTH_LONG).show();
                WebViewPageTools.focusVerification(web);
                return;
            }
            LoginPrefs.setShopeeReady(this,true);
            cm.flush();
            Toast.makeText(this,"Shopee Affiliate พร้อมใช้งาน",Toast.LENGTH_SHORT).show();
            finish();
        });

        setContentView(root);
        web.loadUrl(AFFILIATE_URL);
    }

    private Button mini(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setPadding(Ui.dp(this,2),0,Ui.dp(this,2),0);
        return b;
    }

    @Override public void onBackPressed() {
        if(web!=null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
