package com.commicore.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;

public class ProductScannerActivity extends Activity {
    private static final String AFFILIATE_URL="https://affiliate.shopee.co.th/";
    private WebView web; private TextView status; private DbHelper db;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if(!LoginPrefs.shopeeReady(this) || !LoginPrefs.flowReady(this)) {
            Toast.makeText(this,"กรุณา Login Shopee Affiliate และ Google Flow ก่อน",Toast.LENGTH_LONG).show(); finish(); return;
        }
        db=new DbHelper(this);
        LinearLayout root=Ui.vertical(this);
        root.addView(Ui.title(this,"Affiliate Product Analyzer",24));
        root.addView(Ui.body(this,"ใช้หน้า Shopee Affiliate ที่ Login อยู่ เลือก ‘ขายดี’ และ/หรือ ‘ค่าคอมพิเศษ’ แล้วสแกน ระบบจะจัดอันดับจากยอดขาย + Commission + Extra Comm + ช่วงราคา"));
        Button open=Ui.button(this,"เปิดหน้า Affiliate Products"); root.addView(open);
        Button best=Ui.button(this,"เลือกตัวกรอง: ขายดี"); root.addView(best);
        Button extra=Ui.button(this,"เลือกตัวกรอง: ค่าคอมพิเศษ / Extra Comm"); root.addView(extra);
        Button scan=Ui.button(this,"🔎 สแกน + วิเคราะห์สินค้าที่เห็น"); root.addView(scan);
        Button top=Ui.button(this,"ดู Top Products ที่วิเคราะห์แล้ว"); root.addView(top);
        status=Ui.body(this,"พร้อมสแกน"); root.addView(status);

        web=new WebView(this);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1f); web.setLayoutParams(lp);
        WebSettings ws=web.getSettings(); ws.setJavaScriptEnabled(true); ws.setDomStorageEnabled(true); ws.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
        web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient()); web.addJavascriptInterface(new Bridge(),"CommiBridge"); root.addView(web);

        open.setOnClickListener(v -> web.loadUrl(AFFILIATE_URL));
        best.setOnClickListener(v -> clickText(new String[]{"ขายดี","Best Seller","สินค้าขายดี"}));
        extra.setOnClickListener(v -> clickText(new String[]{"ค่าคอมพิเศษ","Extra Comm","Extra Commission"}));
        scan.setOnClickListener(v -> injectScanner());
        top.setOnClickListener(v -> startActivity(new android.content.Intent(this,ProductListActivity.class)));
        web.loadUrl(AFFILIATE_URL); setContentView(root);
    }

    private void clickText(String[] labels) {
        StringBuilder arr=new StringBuilder("["); for(int i=0;i<labels.length;i++){ if(i>0)arr.append(','); arr.append('"').append(labels[i].replace("\"","\\\"")).append('"'); } arr.append(']');
        String js="(function(){var ls="+arr+";var es=document.querySelectorAll('button,label,[role=tab],[role=button],span,div');for(var j=0;j<ls.length;j++){for(var i=0;i<es.length;i++){var t=(es[i].innerText||es[i].textContent||'').trim();if(t===ls[j]||t.indexOf(ls[j])>=0){var e=es[i];while(e&&e!==document.body&&!((e.tagName==='BUTTON')||e.getAttribute('role')==='button'||e.getAttribute('role')==='tab'||e.tagName==='LABEL'))e=e.parentElement;(e||es[i]).click();return 'clicked:'+ls[j];}}}return 'not-found';})()";
        web.evaluateJavascript(js,value -> status.setText("Filter: "+value));
    }

    private void injectScanner() {
        status.setText("กำลังอ่านสินค้าที่แสดงบนหน้า Affiliate...");
        String js="(function(){function hrefOf(c){var a=c.querySelector('a[href]');return a?a.href:'';}function titleOf(c){var sels=['[class*=name]','[class*=title]','h3','h4','a'];for(var i=0;i<sels.length;i++){var e=c.querySelector(sels[i]);var t=e&&(e.innerText||e.textContent||'').trim();if(t&&t.length>=6&&t.length<240)return t;}return (c.innerText||'').trim().split(/\\n/)[0]||'';}var cards=[].slice.call(document.querySelectorAll('.product-offer-item,[class*=product-offer-item],[class*=AffiliateItemCard],[class*=product-card]'));if(!cards.length){var aa=[].slice.call(document.querySelectorAll('a[href*=/offer/],a[href*=shopee]'));for(var ai=0;ai<aa.length;ai++){var p=aa[ai];for(var up=0;up<5&&p&&p.parentElement;up++){p=p.parentElement;if((p.innerText||'').length>40&&(p.innerText||'').length<1600){cards.push(p);break;}}}}var out=[],seen={};for(var i=0;i<cards.length&&out.length<80;i++){var c=cards[i],text=(c.innerText||c.textContent||'').trim();if(text.length<20)continue;var url=hrefOf(c);if(!url||seen[url])continue;seen[url]=1;var title=titleOf(c);if(title.length<4)continue;var pm=text.match(/฿\\s*[0-9,.]+/);var sm=text.match(/(?:ขายแล้ว|ยอดขาย|sold)\\s*[:：]?\\s*([0-9,.]+)\\s*(พัน|หมื่น|แสน|ล้าน|k|m)?/i);var per=[].slice.call(text.matchAll(/([0-9]+(?:\\.[0-9]+)?)\\s*%/g)).map(function(x){return parseFloat(x[1])}).filter(function(x){return isFinite(x)});var maxp=per.length?Math.max.apply(null,per):0;var hasExtra=/extra\\s*comm|ค่าคอมพิเศษ|คอมพิเศษ/i.test(text);var extra=hasExtra?maxp:0;var comm=0;for(var pi=0;pi<per.length;pi++){if(!hasExtra||per[pi]!==maxp)comm=Math.max(comm,per[pi]);}if(comm===0&&per.length)comm=per[0];var img=c.querySelector('img');out.push({title:title,url:url,price:pm?pm[0]:'',sold:sm?sm[0]:'',image:img?(img.currentSrc||img.src||''):'',commission:comm,extraCommission:extra,affiliateLink:''});}CommiBridge.onProducts(JSON.stringify(out));return out.length;})()";
        web.evaluateJavascript(js,null);
    }

    private class Bridge {
        @JavascriptInterface public void onProducts(final String json) {
            runOnUiThread(() -> { try { JSONArray arr=new JSONArray(json); db.importProducts(arr); status.setText("พบ "+arr.length()+" รายการ • เรียงตาม Affiliate Score"); Toast.makeText(ProductScannerActivity.this,"วิเคราะห์ "+arr.length()+" สินค้าแล้ว",Toast.LENGTH_LONG).show(); } catch(Exception e){ status.setText("สแกนไม่สำเร็จ: "+e.getMessage()); } });
        }
    }
}
