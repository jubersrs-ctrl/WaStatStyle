package com.example.wastatclone;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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

    LinearLayout root, contactList, historyList;
    TextView contactCount, onlineCount, offlineCount, lastSeenCount;
    ArrayList<String> contacts = new ArrayList<>();
    ArrayList<String> events = new ArrayList<>();

    android.content.SharedPreferences prefs;

    final SimpleDateFormat fmt =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("data", MODE_PRIVATE);
        load();

        buildDashboard();

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10
            );
        }
    }

    void buildDashboard() {

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 18, 18, 30);
        root.setBackgroundColor(Color.rgb(245, 247, 250));

        scroll.addView(root);

        TextView title = text("WaStat Style", 28, Color.rgb(20, 20, 20));
        title.setTypeface(null, 1);
        root.addView(title);

        TextView subtitle =
                text("Activity Dashboard", 15, Color.DKGRAY);
        root.addView(subtitle);

        addSpace(18);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);

        contactCount = statCard(stats, "Contacts", "0");
        onlineCount = statCard(stats, "Online", "0");
        offlineCount = statCard(stats, "Offline", "0");

        root.addView(stats);

        addSpace(10);

        LinearLayout secondStats = new LinearLayout(this);
        secondStats.setOrientation(LinearLayout.HORIZONTAL);

        lastSeenCount = statCard(secondStats, "Last Seen", "0");

        root.addView(secondStats);

        addSpace(22);

        Button add = new Button(this);
        add.setText("＋  Add Contact");
        add.setTextSize(16);
        add.setOnClickListener(v -> addContact());
        root.addView(add);

        addSpace(10);

        TextView contactsTitle =
                text("Your Contacts", 20, Color.rgb(25, 25, 25));
        contactsTitle.setTypeface(null, 1);
        root.addView(contactsTitle);

        contactList = new LinearLayout(this);
        contactList.setOrientation(LinearLayout.VERTICAL);
        root.addView(contactList);

        addSpace(20);

        TextView historyTitle =
                text("Activity History", 20, Color.rgb(25, 25, 25));
        historyTitle.setTypeface(null, 1);
        root.addView(historyTitle);

        historyList = new LinearLayout(this);
        historyList.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyList);

        addSpace(12);

        Button clear = new Button(this);
        clear.setText("🗑  Clear History");
        clear.setOnClickListener(v -> clearHistory());
        root.addView(clear);

        setContentView(scroll);

        render();
    }

    TextView statCard(
            LinearLayout parent,
            String label,
            String value) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(14, 14, 14, 14);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(24);
        card.setBackground(bg);

        TextView number = text(value, 23, Color.BLACK);
        number.setTypeface(null, 1);

        TextView name = text(label, 13, Color.DKGRAY);

        card.addView(number);
        card.addView(name);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, 105, 1);
        p.setMargins(4, 4, 4, 4);

        parent.addView(card, p);

        return number;
    }

    void addContact() {

        final EditText input = new EditText(this);
        input.setHint("Name or WhatsApp number");
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("Add Contact")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (d, w) -> {

                    String s = input.getText()
                            .toString()
                            .trim();

                    if (s.isEmpty()) return;

                    if (contacts.contains(s)) {
                        Toast.makeText(
                                this,
                                "Contact already added",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    contacts.add(s);
                    save();
                    render();
                })
                .show();
    }

    void addCard(String name) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(14, 12, 14, 12);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(20);
        box.setBackground(bg);

        TextView title = text(name, 18, Color.BLACK);
        title.setTypeface(null, 1);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);

        Button online = new Button(this);
        online.setText("Online");

        Button offline = new Button(this);
        offline.setText("Offline");

        Button seen = new Button(this);
        seen.setText("Last Seen");

        buttons.addView(online,
                new LinearLayout.LayoutParams(0, 55, 1));

        buttons.addView(offline,
                new LinearLayout.LayoutParams(0, 55, 1));

        buttons.addView(seen,
                new LinearLayout.LayoutParams(0, 55, 1));

        online.setOnClickListener(
                v -> record(name, "ONLINE"));

        offline.setOnClickListener(
                v -> record(name, "OFFLINE"));

        seen.setOnClickListener(
                v -> record(name, "LAST SEEN"));

        box.addView(title);
        box.addView(buttons);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        p.setMargins(0, 6, 0, 6);

        contactList.addView(box, p);
    }

    void record(String name, String type) {

        String event =
                fmt.format(new Date()) +
                "  •  " +
                name +
                "  •  " +
                type;

        events.add(0, event);

        if (events.size() > 50)
            events.remove(events.size() - 1);

        save();
        render();

        Toast.makeText(
                this,
                type + " recorded",
                Toast.LENGTH_SHORT
        ).show();
    }

    void clearHistory() {

        if (events.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Clear activity?")
                .setMessage(
                        "All locally recorded activity will be removed."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (d, w) -> {

                    events.clear();
                    save();
                    render();
                })
                .show();
    }

    void render() {

        if (contactList == null) return;

        contactList.removeAllViews();
        historyList.removeAllViews();

        int online = 0;
        int offline = 0;
        int seen = 0;

        for (String e : events) {

            if (e.contains("ONLINE"))
                online++;

            else if (e.contains("OFFLINE"))
                offline++;

            else if (e.contains("LAST SEEN"))
                seen++;
        }

        contactCount.setText(String.valueOf(contacts.size()));
        onlineCount.setText(String.valueOf(online));
        offlineCount.setText(String.valueOf(offline));
        lastSeenCount.setText(String.valueOf(seen));

        for (String c : contacts)
            addCard(c);

        if (events.isEmpty()) {

            TextView empty =
                    text("No activity recorded yet.", 15, Color.GRAY);

            empty.setPadding(8, 12, 8, 12);

            historyList.addView(empty);

        } else {

            int count = Math.min(events.size(), 50);

            for (int i = 0; i < count; i++) {

                TextView item =
                        text("• " + events.get(i), 14, Color.DKGRAY);

                item.setPadding(8, 8, 8, 8);

                historyList.addView(item);
            }
        }
    }

    TextView text(
            String value,
            float size,
            int color) {

        TextView t = new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        return t;
    }

    void addSpace(int h) {

        Space s = new Space(this);

        root.addView(
                s,
                new LinearLayout.LayoutParams(1, h)
        );
    }

    void load() {

        String c =
                prefs.getString("contacts", "");

        if (!c.isEmpty())
            contacts.addAll(
                    Arrays.asList(c.split("\\|"))
            );

        String e =
                prefs.getString("events", "");

        if (!e.isEmpty())
            events.addAll(
                    Arrays.asList(e.split("\n"))
            );
    }

    void save() {

        StringBuilder c = new StringBuilder();

        for (String s : contacts) {

            if (c.length() > 0)
                c.append("|");

            c.append(s.replace("|", ""));
        }

        StringBuilder e = new StringBuilder();

        for (String s : events) {

            if (e.length() > 0)
                e.append("\n");

            e.append(s.replace("\n", " "));
        }

        prefs.edit()
                .putString("contacts", c.toString())
                .putString("events", e.toString())
                .apply();
    }
    }
