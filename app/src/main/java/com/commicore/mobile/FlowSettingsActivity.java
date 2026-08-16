package com.commicore.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FlowSettingsActivity extends Activity {
    private Spinner videoType, imageModel, imageAspect, imageScale, videoModel, videoAspect, videoScale, duration, quality;
    private EditText sceneCount;
    private CheckBox createNewProject, autoAnimate, reuseProduct, usePresenterReference;
    private TextView summary;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        LinearLayout root = Ui.vertical(this);
        sc.addView(root);

        root.addView(Ui.title(this,"ตั้งค่า Google Flow",24));
        root.addView(Ui.body(this,
            "ตั้งค่ากระบวนการสร้างภาพ/วิดีโอของ CommiCore ให้ตรงกับตัวเลือกที่ต้องการใช้ใน Flow. " +
            "ตัวเลือกชื่อโมเดลเป็นค่าที่ CommiCore เก็บไว้สำหรับ workflow ของเรา และอาจไม่ตรงกับชื่อที่ Google แสดงทุกบัญชี/ทุกช่วงเวลา"));

        videoType = addSpinner(root,"โหมดวิดีโอ", new String[]{"Frames","Ingredients","Text to Video"}, FlowSettingsPrefs.get(this,"videoType","Frames"));
        imageModel = addSpinner(root,"โมเดลภาพ", new String[]{"Nano Banana 2","Imagen 4","Default"}, FlowSettingsPrefs.get(this,"imageModel","Nano Banana 2"));
        imageAspect = addSpinner(root,"สัดส่วนภาพ", new String[]{"9:16","16:9","1:1","4:5"}, FlowSettingsPrefs.get(this,"imageAspect","9:16"));
        imageScale = addSpinner(root,"จำนวน/Scale ภาพ", new String[]{"1x","2x","4x"}, FlowSettingsPrefs.get(this,"imageScale","1x"));
        videoModel = addSpinner(root,"โมเดลวิดีโอ", new String[]{"Veo 3.1 Lite","Veo 3.1 - Quality","Veo 3.1 - Fast","Veo 3.1","Default"}, FlowSettingsPrefs.get(this,"videoModel","Veo 3.1 Lite"));
        videoAspect = addSpinner(root,"สัดส่วนวิดีโอ", new String[]{"9:16","16:9"}, FlowSettingsPrefs.get(this,"videoAspect","9:16"));
        videoScale = addSpinner(root,"จำนวน/Scale วิดีโอ", new String[]{"1x","2x","4x"}, FlowSettingsPrefs.get(this,"videoScale","1x"));
        duration = addSpinner(root,"ความยาววิดีโอ", new String[]{"8s","6s","4s"}, FlowSettingsPrefs.get(this,"duration","8s"));
        quality = addSpinner(root,"คุณภาพดาวน์โหลด", new String[]{"1080p Upscaled","Original","720p"}, FlowSettingsPrefs.get(this,"quality","1080p Upscaled"));

        root.addView(Ui.body(this,"จำนวน Scene"));
        sceneCount = new EditText(this);
        sceneCount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        sceneCount.setText(String.valueOf(FlowSettingsPrefs.getInt(this,"sceneCount",1)));
        root.addView(sceneCount);

        createNewProject = addCheck(root,"สร้าง Project ใหม่ต่อสินค้า", FlowSettingsPrefs.getBool(this,"createNewProject",true));
        autoAnimate = addCheck(root,"หลังได้ภาพให้ไปขั้น Animate ต่อ", FlowSettingsPrefs.getBool(this,"autoAnimate",true));
        reuseProduct = addCheck(root,"ใช้ภาพสินค้าเดิมเป็น Reference ทุก Scene", FlowSettingsPrefs.getBool(this,"reuseProduct",true));
        usePresenterReference = addCheck(root,"ใช้ Presenter Reference ถ้ามี", FlowSettingsPrefs.getBool(this,"usePresenterReference",false));

        Button save = Ui.button(this,"บันทึกการตั้งค่า Flow");
        save.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){ save(); }
        });
        root.addView(save);

        summary = Ui.body(this,"");
        root.addView(summary);
        refreshSummary();

        setContentView(sc);
    }

    private Spinner addSpinner(LinearLayout root,String label,String[] items,String selected) {
        root.addView(Ui.body(this,label));
        Spinner s = new Spinner(this);
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
        int idx=0;
        for(int i=0;i<items.length;i++) if(items[i].equals(selected)) idx=i;
        s.setSelection(idx);
        root.addView(s);
        return s;
    }

    private CheckBox addCheck(LinearLayout root,String label,boolean checked) {
        CheckBox c = new CheckBox(this);
        c.setText(label);
        c.setChecked(checked);
        root.addView(c);
        return c;
    }

    private String val(Spinner s){ return String.valueOf(s.getSelectedItem()); }

    private void save() {
        int scenes=1;
        try { scenes=Math.max(1,Integer.parseInt(sceneCount.getText().toString().trim())); }
        catch(Exception ignored) {}
        FlowSettingsPrefs.save(this,
            val(videoType), val(imageModel), val(imageAspect), val(imageScale),
            val(videoModel), val(videoAspect), val(videoScale), val(duration), val(quality), scenes,
            createNewProject.isChecked(), autoAnimate.isChecked(), reuseProduct.isChecked(), usePresenterReference.isChecked());
        LoginPrefs.setFlowReady(this,true);
        Toast.makeText(this,"บันทึก Flow settings แล้ว",Toast.LENGTH_SHORT).show();
        refreshSummary();
    }

    private void refreshSummary(){ if(summary!=null) summary.setText(FlowSettingsPrefs.summary(this)); }
}
