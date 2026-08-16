package com.commicore.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.List;

public class ProductListActivity extends Activity {
    private List<DbHelper.Product> items;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root=Ui.vertical(this);
        root.addView(Ui.title(this,"สินค้าที่สแกน",24));

        final ListView list=new ListView(this);
        items=new DbHelper(this).all();
        ArrayAdapter<DbHelper.Product> ad=new ArrayAdapter<>(
            this, android.R.layout.simple_list_item_1, items);
        list.setAdapter(ad);
        list.setOnItemClickListener((parent,view,pos,id)->{
            Intent i=new Intent(ProductListActivity.this,ProductDetailActivity.class);
            i.putExtra("id",items.get(pos).id);
            startActivity(i);
        });
        root.addView(list,new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,0,1f));
        setContentView(root);
    }
}
