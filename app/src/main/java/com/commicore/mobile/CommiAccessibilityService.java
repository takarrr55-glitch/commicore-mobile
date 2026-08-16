package com.commicore.mobile;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.SharedPreferences;
import java.util.List;

public class CommiAccessibilityService extends AccessibilityService {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private long lastAttempt=0;

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences p=AutomationPrefs.get(this);
        String mode=p.getString("mode",AutomationPrefs.MODE_NONE);
        if(AutomationPrefs.MODE_NONE.equals(mode)) return;

        long now=System.currentTimeMillis();
        if(now-lastAttempt<1200) return;
        lastAttempt=now;

        final String payload=p.getString("payload","");
        final CharSequence pkg=event.getPackageName();
        handler.postDelayed(new Runnable(){
            @Override public void run(){
                AccessibilityNodeInfo root=getRootInActiveWindow();
                if(root==null) return;
                try {
                    if(AutomationPrefs.MODE_FLOW.equals(mode)) {
                        handleFlow(root,payload,pkg==null?"":pkg.toString());
                    } else if(AutomationPrefs.MODE_SHOPEE.equals(mode)) {
                        handleShopee(root,payload,pkg==null?"":pkg.toString());
                    }
                } finally {
                    root.recycle();
                }
            }
        },700);
    }

    private void handleFlow(AccessibilityNodeInfo root,String prompt,String pkg) {
        if(!(pkg.contains("chrome") || pkg.contains("huawei"))) return;
        AccessibilityNodeInfo editable=findEditable(root);
        if(editable==null) return;

        Bundle args=new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,prompt);
        boolean ok=editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args);
        editable.recycle();
        if(!ok) return;

        String[] labels={"Generate","Create","สร้าง","สร้างวิดีโอ","Generate video","Generate image","สร้างรูปภาพ"};
        AccessibilityNodeInfo b=findClickableByText(root,labels);
        if(b!=null) {
            b.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            b.recycle();
        }
        AutomationPrefs.clear(this);
    }

    private void handleShopee(AccessibilityNodeInfo root,String caption,String pkg) {
        if(!pkg.contains("shopee")) return;
        AccessibilityNodeInfo editable=findEditable(root);
        if(editable!=null) {
            Bundle args=new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,caption);
            if(editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)) {
                AutomationPrefs.clear(this);
            }
            editable.recycle();
        }
    }

    private AccessibilityNodeInfo findEditable(AccessibilityNodeInfo n) {
        if(n==null) return null;
        if(n.isEditable() && n.isVisibleToUser()) return AccessibilityNodeInfo.obtain(n);
        for(int i=0;i<n.getChildCount();i++) {
            AccessibilityNodeInfo c=n.getChild(i);
            if(c==null) continue;
            AccessibilityNodeInfo r=findEditable(c);
            c.recycle();
            if(r!=null) return r;
        }
        return null;
    }

    private AccessibilityNodeInfo findClickableByText(AccessibilityNodeInfo root,String[] labels) {
        for(String s:labels) {
            List<AccessibilityNodeInfo> list=root.findAccessibilityNodeInfosByText(s);
            if(list==null) continue;
            for(AccessibilityNodeInfo n:list) {
                if(n!=null && n.isVisibleToUser()) {
                    AccessibilityNodeInfo x=n;
                    for(int k=0;k<4 && x!=null;k++) {
                        if(x.isClickable()) return AccessibilityNodeInfo.obtain(x);
                        AccessibilityNodeInfo parent=x.getParent();
                        if(x!=n) x.recycle();
                        x=parent;
                    }
                    if(x!=null && x!=n) x.recycle();
                }
            }
            for(AccessibilityNodeInfo n:list) if(n!=null) n.recycle();
        }
        return null;
    }

    @Override public void onInterrupt() {}
}
