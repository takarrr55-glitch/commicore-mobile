package com.commicore.mobile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginSetupActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void render() {
        ScrollView sc = new ScrollView(this);
        LinearLayout root = Ui.vertical(this);
        sc.addView(root);

        root.addView(Ui.title(this, "ตั้งค่าการ Login ก่อนเริ่มสแกน", 24));
        root.addView(Ui.body(this,
            "ทำ 2 อย่างนี้ครั้งแรก: Login Shopee และ Login Google Flow ให้เรียบร้อย แล้วกดยืนยันในแอป CommiCore. " +
            "หลังยืนยันครบทั้งสองระบบ จึงจะเข้า Shopee Scanner ได้"));

        status = Ui.body(this, "");
        root.addView(status);

        Button shopee = Ui.button(this, "① เปิด Shopee เพื่อ Login");
        shopee.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){ openShopee(); }
        });
        root.addView(shopee);

        Button confirmShopee = Ui.button(this, "ยืนยันว่า Login Shopee แล้ว");
        confirmShopee.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                LoginPrefs.setShopeeReady(LoginSetupActivity.this, true);
                refreshStatus();
                Toast.makeText(LoginSetupActivity.this, "บันทึกสถานะ Shopee แล้ว", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(confirmShopee);

        Button flow = Ui.button(this, "② เปิด Google Flow เพื่อ Login");
        flow.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://labs.google/fx/tools/flow")));
                } catch(Exception e) {
                    Toast.makeText(LoginSetupActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
        root.addView(flow);

        Button confirmFlow = Ui.button(this, "ยืนยันว่า Login Google Flow แล้ว");
        confirmFlow.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                LoginPrefs.setFlowReady(LoginSetupActivity.this, true);
                refreshStatus();
                Toast.makeText(LoginSetupActivity.this, "บันทึกสถานะ Google Flow แล้ว", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(confirmFlow);

        Button scanner = Ui.button(this, "③ เข้า Shopee Scanner");
        scanner.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                if(!LoginPrefs.ready(LoginSetupActivity.this)) {
                    Toast.makeText(LoginSetupActivity.this,
                        "ต้องยืนยัน Login Shopee และ Google Flow ก่อน", Toast.LENGTH_LONG).show();
                    return;
                }
                startActivity(new Intent(LoginSetupActivity.this, ProductScannerActivity.class));
            }
        });
        root.addView(scanner);

        Button reset = Ui.button(this, "ล้างสถานะ Login ที่ยืนยันไว้");
        reset.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                LoginPrefs.reset(LoginSetupActivity.this);
                refreshStatus();
            }
        });
        root.addView(reset);

        root.addView(Ui.body(this,
            "CommiCore ไม่เก็บรหัสผ่านของ Shopee หรือ Google. การ Login เกิดในแอป/Browser ของผู้ใช้เองเท่านั้น"));

        setContentView(sc);
        refreshStatus();
    }

    private void refreshStatus() {
        if(status == null) return;
        String s = "Shopee: " + (LoginPrefs.shopeeReady(this) ? "✅ ยืนยันแล้ว" : "⚠️ ยังไม่ยืนยัน") +
            "\nGoogle Flow: " + (LoginPrefs.flowReady(this) ? "✅ ยืนยันแล้ว" : "⚠️ ยังไม่ยืนยัน") +
            "\nScanner: " + (LoginPrefs.ready(this) ? "✅ พร้อมใช้งาน" : "🔒 ล็อกอยู่");
        status.setText(s);
    }

    private void openShopee() {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.shopee.th");
            if(i == null) throw new ActivityNotFoundException();
            startActivity(i);
        } catch(Exception e) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://shopee.co.th/"))); }
            catch(Exception ex){ Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show(); }
        }
    }
}
