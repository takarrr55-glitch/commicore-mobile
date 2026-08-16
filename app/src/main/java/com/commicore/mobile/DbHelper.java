package com.commicore.mobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DB = "commicore.db";

    public DbHelper(Context c) { super(c, DB, null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT NOT NULL," +
            "url TEXT UNIQUE," +
            "price TEXT," +
            "sold TEXT," +
            "image_url TEXT," +
            "score REAL DEFAULT 0," +
            "created_at INTEGER DEFAULT (strftime('%s','now')))" );
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {}

    public int importProducts(JSONArray arr) {
        SQLiteDatabase db = getWritableDatabase();
        int count = 0;
        db.beginTransaction();
        try {
            for (int i=0; i<arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String title = o.optString("title","").trim();
                String url = o.optString("url","").trim();
                if (title.length() < 4 || url.length() < 8) continue;
                String price = o.optString("price","");
                String sold = o.optString("sold","");
                String image = o.optString("image","");
                double score = score(sold, price);
                android.content.ContentValues v = new android.content.ContentValues();
                v.put("title", title); v.put("url", url); v.put("price", price);
                v.put("sold", sold); v.put("image_url", image); v.put("score", score);
                long id = db.insertWithOnConflict("products", null, v, SQLiteDatabase.CONFLICT_IGNORE);
                if (id != -1) count++;
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
        return count;
    }

    private double score(String sold, String price) {
        String n = sold == null ? "" : sold.replaceAll("[^0-9.]", "");
        double s = 0;
        try { if (!n.isEmpty()) s = Double.parseDouble(n); } catch(Exception ignored) {}
        if (sold != null && sold.contains("พัน")) s *= 1000;
        if (sold != null && sold.contains("หมื่น")) s *= 10000;
        return Math.min(100.0, 20.0 + Math.pow(Math.max(s,0),0.25)*8.0);
    }

    public List<Product> all() {
        ArrayList<Product> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT id,title,url,price,sold,image_url,score FROM products ORDER BY score DESC,id DESC", null);
        try {
            while(c.moveToNext()) {
                out.add(new Product(c.getLong(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getString(5), c.getDouble(6)));
            }
        } finally { c.close(); }
        return out;
    }

    public Product get(long id) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT id,title,url,price,sold,image_url,score FROM products WHERE id=?",
            new String[]{String.valueOf(id)});
        try {
            if(c.moveToFirst()) return new Product(c.getLong(0), c.getString(1), c.getString(2),
                c.getString(3), c.getString(4), c.getString(5), c.getDouble(6));
        } finally { c.close(); }
        return null;
    }

    public static class Product {
        public final long id; public final String title,url,price,sold,image; public final double score;
        public Product(long id,String title,String url,String price,String sold,String image,double score) {
            this.id=id; this.title=title; this.url=url; this.price=price;
            this.sold=sold; this.image=image; this.score=score;
        }
        @Override public String toString() {
            String p = price==null?"":price; String s = sold==null?"":sold;
            return String.format("⭐ %.0f  %s\n%s  %s", score, title, p, s);
        }
    }
}
