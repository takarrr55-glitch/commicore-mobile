package com.commicore.mobile;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    private Ui() {}

    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static TextView title(Context c, String s, int sp) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(Color.rgb(25,25,25));
        t.setPadding(0, dp(c,8),0,dp(c,8));
        return t;
    }

    public static TextView body(Context c, String s) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(Color.rgb(65,65,65));
        t.setLineSpacing(0,1.15f);
        t.setPadding(0, dp(c,5),0,dp(c,5));
        return t;
    }

    public static Button button(Context c, String s) {
        Button b = new Button(c);
        b.setText(s);
        b.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,dp(c,5),0,dp(c,5));
        b.setLayoutParams(lp);
        return b;
    }

    public static LinearLayout vertical(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(c,18),dp(c,18),dp(c,18),dp(c,28));
        l.setBackgroundColor(Color.rgb(248,248,248));
        return l;
    }
}
