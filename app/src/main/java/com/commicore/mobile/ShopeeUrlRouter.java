package com.commicore.mobile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;

public final class ShopeeUrlRouter {
    private ShopeeUrlRouter() {}

    public static boolean handle(Context context, WebView view, String rawUrl) {
        if(rawUrl == null || rawUrl.trim().isEmpty()) return false;
        String url = rawUrl.trim();
        Uri uri;
        try { uri = Uri.parse(url); }
        catch(Exception e) { return false; }

        String scheme = uri.getScheme();
        if(scheme == null) return false;
        scheme = scheme.toLowerCase();
        if("http".equals(scheme) || "https".equals(scheme)) return false;

        if("shopeeth".equals(scheme) || "shopee".equals(scheme)) {
            String target = safeDecode(uri.getQueryParameter("navigate_url"));
            if(isAffiliateHttps(target)) {
                view.loadUrl(target);
                return true;
            }

            // Some Shopee redirects double-encode the destination URL.
            String decoded = safeDecode(safeDecode(url));
            int i = decoded.indexOf("https://affiliate.shopee.co.th");
            if(i >= 0) {
                String affiliate = decoded.substring(i);
                int cut = affiliate.indexOf("&stm_");
                if(cut > 0) affiliate = affiliate.substring(0, cut);
                cut = affiliate.indexOf("&uls_trackid");
                if(cut > 0) affiliate = affiliate.substring(0, cut);
                if(isAffiliateHttps(affiliate)) {
                    view.loadUrl(affiliate);
                    return true;
                }
            }

            // Keep the login inside the WebView instead of showing ERR_UNKNOWN_URL_SCHEME.
            view.loadUrl("https://affiliate.shopee.co.th/");
            return true;
        }

        // Unknown non-web scheme: try the owning app. If none exists, consume it
        // so WebView does not show an ERR_UNKNOWN_URL_SCHEME error page.
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(i);
        } catch(Exception ignored) {}
        return true;
    }

    private static String safeDecode(String s) {
        if(s == null) return null;
        try { return Uri.decode(s); }
        catch(Exception e) { return s; }
    }

    private static boolean isAffiliateHttps(String s) {
        if(s == null) return false;
        try {
            Uri u = Uri.parse(s);
            String host = u.getHost();
            return "https".equalsIgnoreCase(u.getScheme()) &&
                   host != null && host.toLowerCase().endsWith("affiliate.shopee.co.th");
        } catch(Exception e) { return false; }
    }
}
