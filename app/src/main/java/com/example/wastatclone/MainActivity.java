package com.example.wastatclone;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root;
    LinearLayout contactList;
    LinearLayout timelineList;

    TextView contactCount;
    TextView onlineCount;
    TextView offlineCount;
    TextView lastSeenCount;

    SharedPreferences prefs;

    ArrayList<String> contacts = new ArrayList<>();
    ArrayList<String> events = new ArrayList<>();

    SimpleDateFormat timeFormat =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("data", MODE_PRIVATE);

        loadData();
        buildDashboard();

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    10
            );
        }
    }

    void buildDashboard() {

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 20, 16, 30);

        scroll.addView(root);

        TextView title = text(
                "WaStat Style",
                28,
                Color.BLACK
        );

        title.setGravity(Gravity.CENTER);
        root.addView(title);

        space(10);

        TextView subtitle = text(
                "Activity Tracker",
                16,
                Color.DKGRAY
        );

        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        space(20);

        contactCount = text("Contacts: 0", 17, Color.BLACK);
        onlineCount = text("Online: 0", 17, Color.rgb(0, 140, 60));
        offlineCount = text("Offline: 0", 17, Color.DKGRAY);
        lastSeenCount = text("Last Seen: 0",
