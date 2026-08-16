package com.commicore.mobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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

public class LoginSetupActivity extends Activity {
    private static final String AFFILIATE_URL = "https://affiliate.shopee.co.th/";
    private static final String FLOW_URL = "https://labs.google/fx/tools/flow";
    private TextView status;
    private WebView web;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.vertical(this);
        root.addView(Ui.title(this, "Login ก่อนวิเคราะห์สินค้า", 24));
        root.addView(Ui.body(this,
            "1) Login Shopee Affiliate ในหน้าด้านล่าง  2) Login Google Flow ใน Browser แล้วกลับมายืนยัน  " +
            "CommiCore ไม่ดักรหัสผ่าน, OTP, Bearer token หรือ cookie ส่งออกจากเครื่อง"));
        status = Ui.body(this, ""); root.addView(status);
        Button affiliate = Ui.button(this, "① เปิด Shopee Affiliate Login"); root.addView(affiliate);
        Button confirmAffiliate = Ui.button(this, "✅ ยืนยันว่าเข้า Shopee Affiliate ได้แล้ว"); root.addView(confirmAffiliate);
        Button flow = Ui.button(this, "② เปิด Google Flow AI เพื่อ Login"); root.addView(flow);
        Button confirmFlow = Ui.button(this, "✅ ยืนยันว่าเข้า Google Flow ได้แล้ว"); root.addView(confirmFlow);
        Button analyze = Ui.button(this, "③ ไปหน้า Affiliate Product Analyzer"); root.addView(analyze);
        Button reset = Ui.button(this, "ล้างสถานะ Login ที่ยืนยันไว้"); root.addView(reset);

        web = new WebView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        web.setLayoutParams(lp);
        WebSettings ws = web.getSettings(); ws.setJavaScriptEnabled(true); ws.setDomStorageEnabled(true); ws.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        web.setWebChromeClient(new WebChromeClient()); web.setWebViewClient(new WebViewClient()); root.addView(web);

        affiliate.setOnClickListener(v -> web.loadUrl(AFFILIATE_URL));
        confirmAffiliate.setOnClickListener(v -> {
            String u = web.getUrl()==null?"":web.getUrl();
            if(!u.contains("affiliate.shopee.co.th")) { Toast.makeText(this,"กรุณาเปิดและ Login Shopee Affiliate ก่อน",Toast.LENGTH_LONG).show(); return; }
            LoginPrefs.setShopeeReady(this,true); CookieManager.getInstance().flush(); refreshStatus();
        });
        flow.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FLOW_URL))));
        confirmFlow.setOnClickListener(v -> { LoginPrefs.setFlowReady(this,true); refreshStatus(); });
        analyze.setOnClickListener(v -> {
            if(!LoginPrefs.ready(this)) { Toast.makeText(this,"ต้อง Login Shopee Affiliate และ Google Flow ก่อน",Toast.LENGTH_LONG).show(); return; }
            startActivity(new Intent(this, ProductScannerActivity.class));
        });
        reset.setOnClickListener(v -> { LoginPrefs.reset(this); refreshStatus(); });
        web.loadUrl(AFFILIATE_URL); setContentView(root); refreshStatus();
    }

    @Override protected void onResume() { super.onResume(); refreshStatus(); }
    private void refreshStatus() {
        if(status==null) return;
        status.setText("Shopee Affiliate: " + (LoginPrefs.shopeeReady(this)?"✅ พร้อม":"⚠️ ยังไม่ยืนยัน") +
            "\nGoogle Flow: " + (LoginPrefs.flowReady(this)?"✅ พร้อม":"⚠️ ยังไม่ยืนยัน") +
            "\nAnalyzer: " + (LoginPrefs.ready(this)?"✅ เข้าใช้งานได้":"🔒 ล็อกอยู่"));
    }
}
