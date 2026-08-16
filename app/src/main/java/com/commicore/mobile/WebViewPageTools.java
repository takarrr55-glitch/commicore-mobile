package com.commicore.mobile;

import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public final class WebViewPageTools {
    private WebViewPageTools() {}

    public static void configure(WebView web) {
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setTextZoom(82);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setSupportMultipleWindows(false);

        web.setInitialScale(0);
        web.setVerticalScrollBarEnabled(true);
        web.setHorizontalScrollBarEnabled(false);
        web.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        web.setNestedScrollingEnabled(true);
        web.setOnTouchListener((v,event) -> {
            if(v.getParent()!=null) v.getParent().requestDisallowInterceptTouchEvent(true);
            if(event.getAction()== MotionEvent.ACTION_DOWN) v.requestFocus();
            return false;
        });
    }

    public static String shortLocation(String raw) {
        if(raw==null || raw.trim().isEmpty()) return "";
        try {
            Uri u = Uri.parse(raw);
            String host = u.getHost();
            String path = u.getPath();
            if(host==null) return raw.length()>48 ? raw.substring(0,48)+"…" : raw;
            String s = host + (path==null?"":path);
            return s.length()>58 ? s.substring(0,58)+"…" : s;
        } catch(Exception e) {
            return raw.length()>48 ? raw.substring(0,48)+"…" : raw;
        }
    }

    public static void scroll(WebView web, int direction) {
        if(web==null) return;
        String dir = direction > 0 ? "1" : "-1";
        String js = "(function(){"+
            "var d=Math.max(window.innerHeight*0.72,420)*"+dir+";"+
            "var moved=false;"+
            "var all=[document.scrollingElement,document.documentElement,document.body].concat(Array.prototype.slice.call(document.querySelectorAll('*')));"+
            "var best=null,bestRange=0;"+
            "for(var i=0;i<all.length;i++){var e=all[i];if(!e)continue;var r=(e.scrollHeight||0)-(e.clientHeight||0);"+
            "if(r>80){var st=window.getComputedStyle?window.getComputedStyle(e):null;var oy=st?st.overflowY:'';"+
            "if(e===document.scrollingElement||e===document.documentElement||e===document.body||oy==='auto'||oy==='scroll'){"+
            "var rc=e.getBoundingClientRect?e.getBoundingClientRect():null;var visible=!rc||(rc.bottom>0&&rc.top<window.innerHeight);"+
            "if(visible&&r>bestRange){best=e;bestRange=r;}}}}"+
            "if(best){try{best.scrollBy({top:d,left:0,behavior:'smooth'});}catch(x){best.scrollTop+=d;}moved=true;}"+
            "if(!moved){try{window.scrollBy({top:d,left:0,behavior:'smooth'});}catch(x){window.scrollBy(0,d);}}"+
            "return moved;})()";
        web.evaluateJavascript(js,null);
        if(direction>0) web.pageDown(false); else web.pageUp(false);
    }

    public static void toTop(WebView web) {
        if(web==null) return;
        web.evaluateJavascript("(function(){var e=document.scrollingElement||document.documentElement||document.body;if(e)e.scrollTop=0;window.scrollTo(0,0);return true;})()",null);
        web.scrollTo(0,0);
    }

    public static void toBottom(WebView web) {
        if(web==null) return;
        web.evaluateJavascript("(function(){var all=[document.scrollingElement,document.documentElement,document.body].concat(Array.prototype.slice.call(document.querySelectorAll('*')));for(var i=0;i<all.length;i++){var e=all[i];if(e&&(e.scrollHeight||0)>(e.clientHeight||0)+80){try{e.scrollTop=e.scrollHeight;}catch(x){}}}window.scrollTo(0,Math.max(document.body?document.body.scrollHeight:0,document.documentElement?document.documentElement.scrollHeight:0));return true;})()",null);
        web.pageDown(true);
    }

    public static void focusVerification(WebView web) {
        if(web==null) return;
        String js = "(function(){"+
            "var keys=['ยืนยันตัวตน','ยืนยัน','verify','verification','captcha','robot','security check'];"+
            "var els=Array.prototype.slice.call(document.querySelectorAll('button,a,h1,h2,h3,h4,label,span,div,iframe'));"+
            "var best=null,bestLen=999999;"+
            "for(var i=0;i<els.length;i++){var e=els[i];var t=((e.innerText||e.textContent||e.getAttribute('title')||e.getAttribute('aria-label')||'')+'').trim().toLowerCase();"+
            "if(e.tagName==='IFRAME'){var src=(e.src||'').toLowerCase();if(src.indexOf('captcha')>=0||src.indexOf('verify')>=0||src.indexOf('challenge')>=0){best=e;break;}}"+
            "for(var k=0;k<keys.length;k++){if(t.indexOf(keys[k].toLowerCase())>=0&&t.length<bestLen){best=e;bestLen=t.length;}}}"+
            "if(best){try{best.scrollIntoView({behavior:'smooth',block:'center',inline:'nearest'});}catch(x){best.scrollIntoView(true);}return 'found';}"+
            "window.scrollTo(0,Math.max(document.body?document.body.scrollHeight:0,document.documentElement?document.documentElement.scrollHeight:0));return 'bottom';})()";
        web.evaluateJavascript(js,null);
    }

    public static void zoomOut(WebView web) { if(web!=null) web.zoomOut(); }
    public static void zoomIn(WebView web) { if(web!=null) web.zoomIn(); }
}
