package com.commicore.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;

public class ProductScannerActivity extends Activity {
    private WebView web;
    private EditText query;
    private TextView status;
    private DbHelper db;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DbHelper(this);

        LinearLayout root = Ui.vertical(this);
        root.addView(Ui.title(this,"Shopee Product Scanner",24));
        root.addView(Ui.body(this,
            "ค้นหาบนหน้าเว็บ Shopee แล้วกด “สแกนสินค้าที่เห็น” ระบบจะเก็บเฉพาะข้อมูลที่แสดงบนหน้าเว็บของคุณ"));

        query = new EditText(this);
        query.setHint("เช่น ของใช้ในบ้าน / อุปกรณ์สัตว์เลี้ยง");
        root.addView(query);

        Button search = Ui.button(this,"ค้นหาใน Shopee");
        root.addView(search);
        Button scan = Ui.button(this,"สแกนสินค้าที่เห็นบนหน้าจอ");
        root.addView(scan);
        status = Ui.body(this,"ยังไม่ได้สแกน");
        root.addView(status);

        web = new WebView(this);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,0,1f);
        web.setLayoutParams(wlp);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setUserAgentString(ws.getUserAgentString() + " CommiCoreMobile/0.3");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Bridge(),"AndroidBridge");
        root.addView(web);

        search.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                String q=query.getText().toString().trim();
                if(q.isEmpty()) return;
                String url="https://shopee.co.th/search?keyword="+Uri.encode(q);
                status.setText("กำลังเปิด "+q);
                web.loadUrl(url);
            }
        });

        scan.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){ injectScanner(); }
        });

        setContentView(root);
    }

    private void injectScanner() {
        String js =
        "(function(){"+
        "var out=[],seen={};var as=document.querySelectorAll('a[href]');"+
        "for(var i=0;i<as.length && out.length<60;i++){"+
        " var a=as[i],h=a.href||'',txt=(a.innerText||'').trim();"+
        " if(!h || txt.length<6) continue;"+
        " if(!(h.indexOf('/product/')>=0 || /-i\\.\\d+\\.\\d+/.test(h))) continue;"+
        " h=h.split('?')[0]; if(seen[h]) continue; seen[h]=1;"+
        " var lines=txt.split(/\\n+/).map(function(x){return x.trim();}).filter(Boolean);"+
        " var title=''; for(var j=0;j<lines.length;j++){if(lines[j].length>title.length && lines[j].length<220)title=lines[j];}"+
        " var pm=txt.match(/฿\\s?[0-9,.]+/);"+
        " var sm=txt.match(/(?:ขายแล้ว|sold)\\s*[^\\n]*/i);"+
        " var img=a.querySelector('img');"+
        " out.push({title:title||txt.slice(0,180),url:h,price:pm?pm[0]:'',sold:sm?sm[0]:'',image:img?(img.currentSrc||img.src||''):''});"+
        "}"+
        "AndroidBridge.onProducts(JSON.stringify(out));"+
        "})();";
        web.evaluateJavascript(js,null);
    }

    private class Bridge {
        @JavascriptInterface public void onProducts(final String json) {
            runOnUiThread(new Runnable(){
                @Override public void run(){
                    try {
                        JSONArray arr=new JSONArray(json);
                        int n=db.importProducts(arr);
                        status.setText("พบ "+arr.length()+" รายการ • เพิ่มใหม่ "+n+" รายการ");
                        Toast.makeText(ProductScannerActivity.this,
                            "เพิ่มสินค้า "+n+" รายการ",Toast.LENGTH_LONG).show();
                    } catch(Exception e) {
                        status.setText("สแกนไม่สำเร็จ: "+e.getMessage());
                    }
                }
            });
        }
    }
}
