package com.example.wastatclone;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout contactList;
    TextView summary, stats, history, empty;
    SharedPreferences prefs;
    ArrayList<String> contacts = new ArrayList<>();
    ArrayList<String> events = new ArrayList<>();
    final SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("data", MODE_PRIVATE);
        load();
        contactList=findViewById(R.id.contactList);
        summary=findViewById(R.id.summary);
        stats=findViewById(R.id.stats);
        history=findViewById(R.id.history);
        empty=findViewById(R.id.empty);
        findViewById(R.id.addContact).setOnClickListener(v -> addContact());
        findViewById(R.id.clearHistory).setOnClickListener(v -> clearHistory());
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 10);
        render();
    }

    void addContact(){
        final EditText input=new EditText(this);
        input.setHint("Name or WhatsApp number"); input.setSingleLine();
        new AlertDialog.Builder(this).setTitle("Add contact").setView(input)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Add",(d,w)->{
                String s=input.getText().toString().trim();
                if(s.isEmpty()) return;
                if(contacts.contains(s)){ Toast.makeText(this,"Contact already added",Toast.LENGTH_SHORT).show(); return; }
                contacts.add(s); save(); render();
            }).show();
    }

    void clearHistory(){
        if(events.isEmpty()) return;
        new AlertDialog.Builder(this).setTitle("Clear activity?")
            .setMessage("All locally recorded activity will be removed.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Clear",(d,w)->{events.clear(); save(); render();}).show();
    }

    void render(){
        contactList.removeAllViews();
        summary.setText(contacts.size()+" tracked contact"+(contacts.size()==1?"":"s"));
        int online=0, offline=0, seen=0;
        for(String e: events){
            if(e.contains(" — ONLINE")) online++;
            else if(e.contains(" — OFFLINE")) offline++;
            else if(e.contains(" — LAST SEEN")) seen++;
        }
        stats.setText("Online  "+online+"     Offline  "+offline+"     Last Seen  "+seen);
        empty.setVisibility(contacts.isEmpty()?View.VISIBLE:View.GONE);
        for(String c: new ArrayList<>(contacts)) addCard(c);
        if(events.isEmpty()) history.setText("No activity recorded yet.");
        else {
            StringBuilder sb=new StringBuilder();
            int count=Math.min(events.size(),50);
            for(int i=0;i<count;i++) { sb.append(events.get(i)); if(i<count-1) sb.append("\n\n"); }
            history.setText(sb.toString());
        }
    }

    void addCard(String name){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(18,16,18,16);
        GradientDrawable bg=new GradientDrawable(); bg.setColor(Color.WHITE); bg.setCornerRadius(24); box.setBackground(bg);
        TextView title=new TextView(this); title.setText("●  "+name); title.setTextSize(18); title.setTextColor(Color.rgb(25,25,25)); title.setTypeface(null,1); box.addView(title);
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(0,8,0,0);
        Button on=new Button(this); on.setText("Online"); Button off=new Button(this); off.setText("Offline"); Button seen=new Button(this); seen.setText("Last Seen");
        row.addView(on,new LinearLayout.LayoutParams(0,-2,1)); row.addView(off,new LinearLayout.LayoutParams(0,-2,1)); row.addView(seen,new LinearLayout.LayoutParams(0,-2,1)); box.addView(row);
        Button remove=new Button(this); remove.setText("Remove"); box.addView(remove);
        on.setOnClickListener(v->record(name,"ONLINE")); off.setOnClickListener(v->record(name,"OFFLINE")); seen.setOnClickListener(v->record(name,"LAST SEEN"));
        remove.setOnClickListener(v->{contacts.remove(name); save(); render();});
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,12,0,0); contactList.addView(box,lp);
    }

    void record(String name,String type){
        String e=fmt.format(new Date())+" — "+name+" — "+type;
        events.add(0,e); if(events.size()>50) events.remove(events.size()-1);
        save(); render(); Toast.makeText(this,type+" recorded",Toast.LENGTH_SHORT).show();
    }

    void load(){
        String cs=prefs.getString("contacts",""); if(!cs.isEmpty()) contacts.addAll(Arrays.asList(cs.split("\\|",-1)));
        String es=prefs.getString("events",""); if(!es.isEmpty()) events.addAll(Arrays.asList(es.split("\\n",-1)));
    }

    void save(){
        StringBuilder c=new StringBuilder();
        for(String s:contacts){if(c.length()>0)c.append('|'); c.append(s.replace("|"," "));}
        StringBuilder e=new StringBuilder();
        for(String s:events){if(e.length()>0)e.append('\\n'); e.append(s.replace("\n"," "));}
        prefs.edit().putString("contacts",c.toString()).putString("events",e.toString()).apply();
    }
}
