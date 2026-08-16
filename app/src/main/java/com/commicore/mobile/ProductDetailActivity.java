package com.commicore.mobile;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class ProductDetailActivity extends Activity {
    private DbHelper.Product p;
    private String caption,imagePrompt,videoPrompt;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        long id=getIntent().getLongExtra("id",-1);
        p=new DbHelper(this).get(id);
        if(p==null){ finish(); return; }

        caption=PromptBuilder.caption(p);
        imagePrompt=PromptBuilder.imagePrompt(p);
        videoPrompt=PromptBuilder.videoPrompt(p);

        ScrollView sc=new ScrollView(this);
        LinearLayout root=Ui.vertical(this);
        sc.addView(root);

        root.addView(Ui.title(this,p.title,22));
        root.addView(Ui.body(this,
            "Score "+Math.round(p.score)+" • "+safe(p.price)+" • "+safe(p.sold)));

        root.addView(Ui.title(this,"Caption",18));
        root.addView(Ui.body(this,caption));
        Button copyCaption=Ui.button(this,"Copy Caption");
        copyCaption.setOnClickListener(v->copy("Caption",caption));
        root.addView(copyCaption);

        root.addView(Ui.title(this,"Google Flow",18));
        Button img=Ui.button(this,"เปิด Flow + ใส่ Prompt สร้างภาพ");
        img.setOnClickListener(v->startFlow(imagePrompt));
        root.addView(img);

        Button vid=Ui.button(this,"เปิด Flow + ใส่ Prompt สร้างวิดีโอ");
        vid.setOnClickListener(v->startFlow(videoPrompt));
        root.addView(vid);

        Button copyVideo=Ui.button(this,"Copy Video Prompt");
        copyVideo.setOnClickListener(v->copy("Video Prompt",videoPrompt));
        root.addView(copyVideo);

        root.addView(Ui.title(this,"Shopee Video",18));
        Button share=Ui.button(this,"ส่งวิดีโอล่าสุดไป Shopee + เตรียม Caption");
        share.setOnClickListener(v->sendLatestToShopee());
        root.addView(share);

        root.addView(Ui.body(this,
            "รุ่นนี้จะช่วยส่งไฟล์/กรอก Caption แต่ไม่กดปุ่ม “โพสต์” ขั้นสุดท้ายเอง " +
            "เพื่อป้องกันการโพสต์ผิดสินค้า หาก UI ของ Shopee เปลี่ยน"));

        Button product=Ui.button(this,"เปิดหน้าสินค้า");
        product.setOnClickListener(v->{
            try{ startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(p.url))); }
            catch(Exception ignored){}
        });
        root.addView(product);

        setContentView(sc);
    }

    private void startFlow(String prompt) {
        AutomationPrefs.setFlow(this,prompt);
        try{
            Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://labs.google/fx/tools/flow"));
            startActivity(i);
            Toast.makeText(this,"กำลังรอหน้า Flow เพื่อใส่ Prompt",Toast.LENGTH_LONG).show();
        } catch(Exception e){ Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show(); }
    }

    private void sendLatestToShopee() {
        Uri uri=MediaHelper.latestVideo(this);
        if(uri==null){
            Toast.makeText(this,"ยังไม่พบวิดีโอในเครื่อง",Toast.LENGTH_LONG).show();
            return;
        }
        AutomationPrefs.setShopee(this,caption);
        Intent i=new Intent(Intent.ACTION_SEND);
        i.setType("video/*");
        i.putExtra(Intent.EXTRA_STREAM,uri);
        i.putExtra(Intent.EXTRA_TEXT,caption);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.setPackage("com.shopee.th");
        try {
            startActivity(i);
        } catch(ActivityNotFoundException e) {
            Intent launch=getPackageManager().getLaunchIntentForPackage("com.shopee.th");
            if(launch!=null) startActivity(launch);
            else Toast.makeText(this,"ไม่พบ Shopee",Toast.LENGTH_LONG).show();
        }
    }

    private void copy(String label,String text) {
        ClipboardManager c=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        c.setPrimaryClip(ClipData.newPlainText(label,text));
        Toast.makeText(this,"Copy แล้ว",Toast.LENGTH_SHORT).show();
    }

    private String safe(String s){ return s==null?"":s; }
}
