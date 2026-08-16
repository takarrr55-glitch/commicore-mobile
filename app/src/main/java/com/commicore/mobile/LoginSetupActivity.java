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
        root.addView(Ui.title(this, "ตั้งค่าก่อนเริ่มทำงาน", 24));
        root.addView(Ui.body(this,
            "ลำดับ: Login Shopee Affiliate → Login Google Flow → ตั้งค่า Flow → เข้า Affiliate Product Analyzer. " +
            "CommiCore ไม่เก็บรหัสผ่าน, OTP, Bearer token หรือส่ง cookie ออกจากเครื่อง"));

        status = Ui.body(this, "");
        root.addView(status);

        Button affiliate = Ui.button(this, "① เปิด Shopee Affiliate Login");
        root.addView(affiliate);

        Button confirmAffiliate = Ui.button(this, "✅ ยืนยันว่าเข้า Shopee Affiliate ได้แล้ว");
        root.addView(confirmAffiliate);

        Button flow = Ui.button(this, "② เปิด Google Flow AI เพื่อ Login");
        root.addView(flow);

        Button flowSettings = Ui.button(this, "③ ตั้งค่า Flow ทั้งหมด");
        root.addView(flowSettings);

        Button analyze = Ui.button(this, "④ ไปหน้า Affiliate Product Analyzer");
        root.addView(analyze);

        Button reset = Ui.button(this, "ล้างสถานะ Login / Flow Setup");
        root.addView(reset);

        web = new WebView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        web.setLayoutParams(lp);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient());
        root.addView(web);

        affiliate.setOnClickListener(v -> web.loadUrl(AFFILIATE_URL));

        confirmAffiliate.setOnClickListener(v -> {
            String u = web.getUrl() == null ? "" : web.getUrl();
            if(!u.contains("affiliate.shopee.co.th")) {
                Toast.makeText(this,
                    "กรุณาเปิดและ Login Shopee Affiliate ก่อน",
                    Toast.LENGTH_LONG).show();
                return;
            }
            LoginPrefs.setShopeeReady(this, true);
            CookieManager.getInstance().flush();
            refreshStatus();
        });

        flow.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FLOW_URL)));
            } catch(Exception e) {
                Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
            }
        });

        flowSettings.setOnClickListener(v -> {
            LoginPrefs.setFlowReady(this, true);
            startActivity(new Intent(this, FlowSettingsActivity.class));
        });

        analyze.setOnClickListener(v -> {
            if(!LoginPrefs.shopeeReady(this) || !LoginPrefs.flowReady(this) || !FlowSettingsPrefs.configured(this)) {
                Toast.makeText(this,
                    "ต้อง Login Shopee Affiliate, Login Flow และบันทึก Flow Settings ก่อน",
                    Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(this, ProductScannerActivity.class));
        });

        reset.setOnClickListener(v -> {
            LoginPrefs.reset(this);
            getSharedPreferences("flow_settings", MODE_PRIVATE).edit().clear().apply();
            refreshStatus();
        });

        web.loadUrl(AFFILIATE_URL);
        setContentView(root);
        refreshStatus();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        if(status==null) return;
        status.setText(
            "Shopee Affiliate: " + (LoginPrefs.shopeeReady(this)?"✅ พร้อม":"⚠️ ยังไม่ยืนยัน") +
            "\nGoogle Flow: " + (LoginPrefs.flowReady(this)?"✅ Login แล้ว":"⚠️ ยังไม่ยืนยัน") +
            "\nFlow Settings: " + (FlowSettingsPrefs.configured(this)?"✅ ตั้งค่าแล้ว":"⚠️ ยังไม่ตั้งค่า") +
            "\nAnalyzer: " + ((LoginPrefs.shopeeReady(this) && LoginPrefs.flowReady(this) && FlowSettingsPrefs.configured(this))?"✅ เข้าใช้งานได้":"🔒 ล็อกอยู่")
        );
    }
}
