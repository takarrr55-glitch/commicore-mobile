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
    public DbHelper(Context c) { super(c, DB, null, 2); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT NOT NULL,url TEXT UNIQUE,price TEXT,sold TEXT,image_url TEXT," +
            "commission REAL DEFAULT 0,extra_commission REAL DEFAULT 0,affiliate_link TEXT," +
            "score REAL DEFAULT 0,created_at INTEGER DEFAULT (strftime('%s','now')))" );
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if(oldV < 2) {
            try { db.execSQL("ALTER TABLE products ADD COLUMN commission REAL DEFAULT 0"); } catch(Exception ignored) {}
            try { db.execSQL("ALTER TABLE products ADD COLUMN extra_commission REAL DEFAULT 0"); } catch(Exception ignored) {}
            try { db.execSQL("ALTER TABLE products ADD COLUMN affiliate_link TEXT"); } catch(Exception ignored) {}
        }
    }

    public int importProducts(JSONArray arr) {
        SQLiteDatabase db = getWritableDatabase(); int count = 0; db.beginTransaction();
        try {
            for(int i=0;i<arr.length();i++) {
                JSONObject o=arr.optJSONObject(i); if(o==null) continue;
                String title=o.optString("title","").trim(), url=o.optString("url","").trim();
                if(title.length()<4 || url.length()<8) continue;
                String price=o.optString("price",""), sold=o.optString("sold",""), image=o.optString("image","");
                double commission=o.optDouble("commission",0), extra=o.optDouble("extraCommission",0);
                String affiliate=o.optString("affiliateLink",""); double score=score(sold,price,commission,extra);
                android.content.ContentValues v=new android.content.ContentValues();
                v.put("title",title); v.put("url",url); v.put("price",price); v.put("sold",sold); v.put("image_url",image);
                v.put("commission",commission); v.put("extra_commission",extra); v.put("affiliate_link",affiliate); v.put("score",score);
                long id=db.insertWithOnConflict("products",null,v,SQLiteDatabase.CONFLICT_IGNORE);
                if(id==-1) db.update("products",v,"url=?",new String[]{url}); else count++;
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
        return count;
    }

    private double parseCount(String s) {
        if(s==null) return 0; String n=s.replaceAll("[^0-9.]",""); double x=0;
        try { if(!n.isEmpty()) x=Double.parseDouble(n); } catch(Exception ignored) {}
        String lower=s.toLowerCase();
        if(s.contains("พัน") || lower.contains("k")) x*=1000;
        else if(s.contains("หมื่น")) x*=10000;
        else if(s.contains("แสน")) x*=100000;
        else if(s.contains("ล้าน") || lower.contains("m")) x*=1000000;
        return x;
    }

    private double parsePrice(String s) {
        if(s==null) return 0; String n=s.replaceAll("[^0-9.]","");
        try { return n.isEmpty()?0:Double.parseDouble(n); } catch(Exception e){ return 0; }
    }

    private double score(String sold,String price,double commission,double extra) {
        double sales=parseCount(sold);
        double salesScore=Math.min(30.0,Math.log10(Math.max(1,sales)+1)*7.0);
        double commScore=Math.min(30.0,Math.max(0,commission)*2.0);
        double extraScore=Math.min(25.0,Math.max(0,extra)*2.5);
        double p=parsePrice(price), priceScore=(p>=69&&p<=999)?15:(p>999&&p<=3000)?10:(p>0)?5:0;
        return Math.round((salesScore+commScore+extraScore+priceScore)*100.0)/100.0;
    }

    public List<Product> all() {
        ArrayList<Product> out=new ArrayList<>();
        Cursor c=getReadableDatabase().rawQuery("SELECT id,title,url,price,sold,image_url,commission,extra_commission,affiliate_link,score FROM products ORDER BY score DESC,id DESC",null);
        try { while(c.moveToNext()) out.add(new Product(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getDouble(6),c.getDouble(7),c.getString(8),c.getDouble(9))); }
        finally { c.close(); } return out;
    }

    public Product get(long id) {
        Cursor c=getReadableDatabase().rawQuery("SELECT id,title,url,price,sold,image_url,commission,extra_commission,affiliate_link,score FROM products WHERE id=?",new String[]{String.valueOf(id)});
        try { if(c.moveToFirst()) return new Product(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getDouble(6),c.getDouble(7),c.getString(8),c.getDouble(9)); }
        finally { c.close(); } return null;
    }

    public static class Product {
        public final long id; public final String title,url,price,sold,image,affiliateLink; public final double commission,extraCommission,score;
        public Product(long id,String title,String url,String price,String sold,String image,double commission,double extra,String affiliateLink,double score) {
            this.id=id; this.title=title; this.url=url; this.price=price; this.sold=sold; this.image=image; this.commission=commission; this.extraCommission=extra; this.affiliateLink=affiliateLink; this.score=score;
        }
        @Override public String toString() { return String.format("🏆 %.0f  %s\nคอม %.1f%% + Extra %.1f%%  %s  %s",score,title,commission,extraCommission,price==null?"":price,sold==null?"":sold); }
    }
}
