package com.commicore.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        requestStorageIfNeeded();

        ScrollView sc = new ScrollView(this);
        LinearLayout l = Ui.vertical(this);
        sc.addView(l);

        l.addView(Ui.title(this,"CommiCore Mobile",28));
        l.addView(Ui.body(this,
            "Clean-room Android 10+ • ไม่มี API key • ไม่มี CommiCore server • " +
            "ไม่ดัก Google token • ข้อมูลสินค้าและ Prompt เก็บในเครื่อง"));

        status = Ui.body(this,"");
        l.addView(status);

        Button access = Ui.button(this,"① เปิด Accessibility สำหรับ CommiCore");
        access.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        l.addView(access);

        Button login = Ui.button(this,"② Login Shopee + Google Flow");
        login.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                startActivity(new Intent(MainActivity.this,LoginSetupActivity.class));
            }
        });
        l.addView(login);

        Button scan = Ui.button(this,"③ สแกนสินค้า Shopee");
        scan.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                if(!LoginPrefs.ready(MainActivity.this)) {
                    Toast.makeText(MainActivity.this,
                        "กรุณา Login Shopee และ Google Flow ก่อนเข้า Scanner",
                        Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this,LoginSetupActivity.class));
                    return;
                }
                startActivity(new Intent(MainActivity.this,ProductScannerActivity.class));
            }
        });
        l.addView(scan);

        Button products = Ui.button(this,"④ รายการสินค้าที่สแกน");
        products.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                startActivity(new Intent(MainActivity.this,ProductListActivity.class));
            }
        });
        l.addView(products);

        Button flow = Ui.button(this,"เปิด Google Flow");
        flow.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){ openUrl("https://labs.google/fx/tools/flow"); }
        });
        l.addView(flow);

        Button shopee = Ui.button(this,"เปิด Shopee");
        shopee.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){ openShopee(); }
        });
        l.addView(shopee);

        Button stop = Ui.button(this,"หยุด Automation ทันที");
        stop.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                AutomationPrefs.clear(MainActivity.this);
                Toast.makeText(MainActivity.this,"หยุด Automation แล้ว",Toast.LENGTH_SHORT).show();
            }
        });
        l.addView(stop);

        l.addView(Ui.title(this,"ความเป็นส่วนตัว",19));
        l.addView(Ui.body(this,
            "Accessibility จำกัดเฉพาะ Chrome, Huawei Browser และ Shopee ตามไฟล์ config. " +
            "แอปนี้ไม่มี endpoint สำหรับส่ง diagnostics, token, cookie, SMS, contact, microphone หรือ location. " +
            "ปุ่มโพสต์สุดท้ายยังให้ผู้ใช้ยืนยันเองในรุ่นนี้เพื่อลดความเสี่ยงกดผิดจาก UI Shopee ที่เปลี่ยนได้."));

        setContentView(sc);
    }

    @Override protected void onResume() {
        super.onResume();
        if(status != null) {
            status.setText(
                "Accessibility: " + (isAccessibilityEnabled() ? "✅ เปิดแล้ว" : "⚠️ ยังไม่เปิด") +
                "\nShopee Login: " + (LoginPrefs.shopeeReady(this) ? "✅ ยืนยันแล้ว" : "⚠️ ยังไม่ยืนยัน") +
                "\nGoogle Flow Login: " + (LoginPrefs.flowReady(this) ? "✅ ยืนยันแล้ว" : "⚠️ ยังไม่ยืนยัน") +
                "\nScanner: " + (LoginPrefs.ready(this) ? "✅ พร้อม" : "🔒 รอ Login")
            );
        }
    }

    private void requestStorageIfNeeded() {
        if(android.os.Build.VERSION.SDK_INT <= 32 &&
           checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
        } else if(android.os.Build.VERSION.SDK_INT >= 33 &&
           checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, 11);
        }
    }

    private boolean isAccessibilityEnabled() {
        String s = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return s != null && s.toLowerCase().contains(getPackageName().toLowerCase());
    }

    private void openUrl(String u) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u))); }
        catch(Exception e){ Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show(); }
    }

    private void openShopee() {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.shopee.th");
            if(i == null) throw new ActivityNotFoundException();
            startActivity(i);
        } catch(Exception e) {
            openUrl("https://shopee.co.th/");
        }
    }
}
