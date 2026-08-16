package com.commicore.mobile;

import android.content.Context;
import android.content.SharedPreferences;

public final class LoginPrefs {
    private static final String PREF = "login_setup";
    private static final String SHOPEE = "shopee_confirmed";
    private static final String FLOW = "flow_confirmed";

    private LoginPrefs() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static boolean shopeeReady(Context c) {
        return prefs(c).getBoolean(SHOPEE, false);
    }

    public static boolean flowReady(Context c) {
        return prefs(c).getBoolean(FLOW, false);
    }

    public static boolean ready(Context c) {
        return shopeeReady(c) && flowReady(c);
    }

    public static void setShopeeReady(Context c, boolean ready) {
        prefs(c).edit().putBoolean(SHOPEE, ready).apply();
    }

    public static void setFlowReady(Context c, boolean ready) {
        prefs(c).edit().putBoolean(FLOW, ready).apply();
    }

    public static void reset(Context c) {
        prefs(c).edit().clear().apply();
    }
}
