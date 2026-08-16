package com.commicore.mobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginSetupActivity extends Activity {
    private static final String FLOW_URL = "https://labs.google/fx/tools/flow";
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.vertical(this);
        root.addView(Ui.title(this, "ตั้งค่าก่อนเริ่มทำงาน", 24));
        root.addView(Ui.body(this,
            "ลำดับ: Login Shopee Affiliate → Login Google Flow → ตั้งค่า Flow → เข้า Affiliate Product Analyzer. " +
            "หน้า Shopee Affiliate จะเปิดเต็มจอแยกต่างหากเพื่อให้เลื่อนและ Login ได้สะดวก"));

        status = Ui.body(this, "");
        root.addView(status);

        Button affiliate = Ui.button(this, "① เปิด Shopee Affiliate Login แบบเต็มจอ");
        root.addView(affiliate);

        Button flow = Ui.button(this, "② เปิด Google Flow AI เพื่อ Login");
        root.addView(flow);

        Button confirmFlow = Ui.button(this, "✅ ยืนยันว่า Login Google Flow แล้ว");
        root.addView(confirmFlow);

        Button flowSettings = Ui.button(this, "③ ตั้งค่า Flow ทั้งหมด");
        root.addView(flowSettings);

        Button analyze = Ui.button(this, "④ ไปหน้า Affiliate Product Analyzer");
        root.addView(analyze);

        Button reset = Ui.button(this, "ล้างสถานะ Login / Flow Setup");
        root.addView(reset);

        affiliate.setOnClickListener(v ->
            startActivity(new Intent(this, AffiliateLoginActivity.class)));

        flow.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FLOW_URL)));
            } catch(Exception e) {
                Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
            }
        });

        confirmFlow.setOnClickListener(v -> {
            LoginPrefs.setFlowReady(this,true);
            refreshStatus();
            Toast.makeText(this,"บันทึกสถานะ Google Flow แล้ว",Toast.LENGTH_SHORT).show();
        });

        flowSettings.setOnClickListener(v -> {
            if(!LoginPrefs.flowReady(this)) {
                Toast.makeText(this,"กรุณา Login Google Flow และกดยืนยันก่อน",Toast.LENGTH_LONG).show();
                return;
            }
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
