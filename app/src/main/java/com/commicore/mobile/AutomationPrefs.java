package com.commicore.mobile;

import android.content.Context;
import android.content.SharedPreferences;

public final class AutomationPrefs {
    public static final String PREF="automation";
    public static final String MODE_NONE="NONE";
    public static final String MODE_FLOW="FLOW_FILL";
    public static final String MODE_SHOPEE="SHOPEE_CAPTION";

    private AutomationPrefs(){}

    public static void setFlow(Context c,String prompt) {
        c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit()
            .putString("mode",MODE_FLOW)
            .putString("payload",prompt)
            .putLong("started",System.currentTimeMillis())
            .apply();
    }

    public static void setShopee(Context c,String caption) {
        c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit()
            .putString("mode",MODE_SHOPEE)
            .putString("payload",caption)
            .putLong("started",System.currentTimeMillis())
            .apply();
    }

    public static SharedPreferences get(Context c) {
        return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
    }

    public static void clear(Context c) {
        get(c).edit().putString("mode",MODE_NONE).remove("payload").apply();
    }
}
