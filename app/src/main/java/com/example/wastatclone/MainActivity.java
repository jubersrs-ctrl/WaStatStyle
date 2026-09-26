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
        lastSeenCount = text("Last Seen: 0", 17, Color.rgb(30, 90, 180));

        root.addView(contactCount);
        root.addView(onlineCount);
        root.addView(offlineCount);
        root.addView(lastSeenCount);

        space(15);

        Button add = new Button(this);
        add.setText("＋ Add Contact");
        add.setOnClickListener(v -> addContact());
        root.addView(add);

        space(15);

        TextView statTitle = text(
                "30 Days Statistics",
                21,
                Color.BLACK
        );

        root.addView(statTitle);

        space(8);

        LinearLayout chart = createChart();
        root.addView(chart);

        space(20);

        TextView contactsTitle = text(
                "Tracked Contacts",
                21,
                Color.BLACK
        );

        root.addView(contactsTitle);

        space(8);

        contactList = new LinearLayout(this);
        contactList.setOrientation(LinearLayout.VERTICAL);

        root.addView(contactList);

        space(22);

        TextView timelineTitle = text(
                "Activity Timeline",
                21,
                Color.BLACK
        );

        root.addView(timelineTitle);

        space(8);

        timelineList = new LinearLayout(this);
        timelineList.setOrientation(LinearLayout.VERTICAL);

        root.addView(timelineList);

        space(20);

        Button clear = new Button(this);
        clear.setText("🗑 Clear History");
        clear.setOnClickListener(v -> clearHistory());

        root.addView(clear);

        setContentView(scroll);

        render();
    }
String getDateLabel(int index) {
    Calendar c = Calendar.getInstance();

    c.add(Calendar.DAY_OF_YEAR, -(29 - index));

    return new SimpleDateFormat(
            "dd/MM",
            Locale.getDefault()
    ).format(c.getTime());
}
    LinearLayout createChart() {

        LinearLayout chart = new LinearLayout(this);
        chart.setOrientation(LinearLayout.VERTICAL);
        chart.setPadding(10, 15, 10, 15);

        int[] daily = getLast30Days();

        for (int i = 0; i < 30; i++) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView date = text(
                    getDateLabel(i),
                    11,
                    Color.DKGRAY
            );

            row.addView(
                    date,
                    new LinearLayout.LayoutParams(80, 45)
            );

            int value = daily[i];

            TextView bar = new TextView(this);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.rgb(76, 175, 80));
            bg.setCornerRadius(10);

            bar.setBackground(bg);

            int width = Math.min(
                    400,
                    Math.max(8, value * 35)
            );

            row.addView(
                    bar,
                    new LinearLayout.LayoutParams(width, 22)
            );

            TextView number = text(
                    "  " + value,
                    12,
                    Color.DKGRAY
            );

            row.addView(number);

            chart.addView(row);
        }

        return chart;
    }

    String getDateLabel(int index) {

        Calendar c = Calendar.getInstance();

        c.add(Calendar.DAY_OF_YEAR, -(29 - index));

        return new SimpleDateFormat(
                "dd/MM",
                Locale.getDefault()
        ).format(c.getTime());
    }

    int[] getLast30Days() {

        int[] result = new int[30];

        Calendar today = Calendar.getInstance();

        for (String event : events) {

            Long time = getEventTime(event);

            if (time == null)
                continue;

            Calendar eventDay = Calendar.getInstance();
            eventDay.setTimeInMillis(time);

            long diff =
                    getDayNumber(today)
                            - getDayNumber(eventDay);

            if (diff >= 0 && diff < 30) {

                int index = 29 - (int) diff;

                if (index >= 0 && index < 30)
                    result[index]++;
            }
        }

        return result;
    }

    Long getEventTime(String event) {

        try {

            int p = event.indexOf("|");

            if (p > 0) {

                String first = event.substring(0, p);

                return Long.parseLong(first);
            }

        } catch (Exception ignored) {
        }

        return null;
    }

    long getDayNumber(Calendar c) {

        Calendar copy = (Calendar) c.clone();

        copy.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        copy.set(
                Calendar.MINUTE,
                0
        );

        copy.set(
                Calendar.SECOND,
                0
        );

        copy.set(
                Calendar.MILLISECOND,
                0
        );

        return copy.getTimeInMillis() /
                (24L * 60L * 60L * 1000L);
    }

    void addContact() {

        final EditText input = new EditText(this);

        input.setHint("Name or WhatsApp number");
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("Add Contact")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> {

                    String name =
                            input.getText()
                                    .toString()
                                    .trim();

                    if (name.isEmpty())
                        return;

                    if (contacts.contains(name)) {

                        Toast.makeText(
                                this,
                                "Contact already added",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    contacts.add(name);

                    record(
                            name,
                            "ADDED"
                    );

                    save();
                    render();
                })
                .show();
    }

    void removeContact(String name) {

        new AlertDialog.Builder(this)
                .setTitle("Remove Contact?")
                .setMessage(name)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {

                    contacts.remove(name);

                    record(
                            name,
                            "REMOVED"
                    );

                    save();
                    render();
                })
                .show();
    }

    void showStatusMenu(String name) {

        String[] options = {
                "ONLINE",
                "OFFLINE",
                "LAST SEEN"
        };

        new AlertDialog.Builder(this)
                .setTitle(name)
                .setItems(options, (dialog, which) -> {

                    record(
                            name,
                            options[which]
                    );

                    save();
                    render();
                })
                .show();
    }

    void record(
            String name,
            String type
    ) {

        String event =
                System.currentTimeMillis()
                        + "|"
                        + name
                        + "|"
                        + type;

        events.add(0, event);

        if (events.size() > 100)
            events.remove(events.size() - 1);
    }

    void render() {

        if (contactList == null ||
                timelineList == null)
            return;

        contactList.removeAllViews();
        timelineList.removeAllViews();

        int online = 0;
        int offline = 0;
        int seen = 0;

        for (String event : events) {

            String[] p = event.split("\\|");

            if (p.length >= 3) {

                if (p[2].equals("ONLINE"))
                    online++;

                else if (p[2].equals("OFFLINE"))
                    offline++;

                else if (p[2].equals("LAST SEEN"))
                    seen++;
            }
        }

        contactCount.setText(
                "Contacts: " + contacts.size()
        );

        onlineCount.setText(
                "Online: " + online
        );

        offlineCount.setText(
                "Offline: " + offline
        );

        lastSeenCount.setText(
                "Last Seen: " + seen
        );

        for (String name : contacts)
            addContactCard(name);

        int limit =
                Math.min(events.size(), 50);

        for (int i = 0; i < limit; i++)
            addTimelineItem(events.get(i));
    }

    void addContactCard(String name) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                14,
                12,
                14,
                12
        );

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(Color.rgb(
                245,
                245,
                245
        ));

        bg.setCornerRadius(18);

        box.setBackground(bg);

        TextView title =
                text(
                        name,
                        18,
                        Color.BLACK
                );

        box.addView(title);

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button status =
                new Button(this);

        status.setText("Status");

        status.setOnClickListener(
                v -> showStatusMenu(name)
        );

        Button remove =
                new Button(this);

        remove.setText("Remove");

        remove.setOnClickListener(
                v -> removeContact(name)
        );

        buttons.addView(
                status,
                new LinearLayout.LayoutParams(
                        0,
                        50,
                        1
                )
        );

        buttons.addView(
                remove,
                new LinearLayout.LayoutParams(
                        0,
                        50,
                        1
                )
        );

        box.addView(buttons);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.setMargins(
                0,
                0,
                0,
                10
        );

        contactList.addView(
                box,
                params
        );
    }

    void addTimelineItem(String event) {

        String[] p =
                event.split("\\|");

        if (p.length < 3)
            return;

        long time;

        try {

            time =
                    Long.parseLong(p[0]);

        } catch (Exception e) {

            return;
        }

        String name = p[1];
        String type = p[2];

        String date =
                timeFormat.format(
                        new Date(time)
                );

        TextView item =
                text(
                        "• " + date
                                + "\n  "
                                + name
                                + " — "
                                + type,
                        15,
                        Color.DKGRAY
                );

        item.setPadding(
                12,
                12,
                12,
                12
        );

        timelineList.addView(item);
    }

    void clearHistory() {

        if (events.isEmpty())
            return;

        new AlertDialog.Builder(this)
                .setTitle("Clear activity?")
                .setMessage(
                        "All locally recorded activity will be removed."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Clear",
                        (dialog, which) -> {

                            events.clear();

                            save();
                            render();
                        }
                )
                .show();
    }

    TextView text(
            String value,
            float size,
            int color
    ) {

        TextView t =
                new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        return t;
    }

    void space(int height) {

        Space s = new Space(this);

        root.addView(
                s,
                new LinearLayout.LayoutParams(
                        1,
                        height
                )
        );
    }

    void loadData() {

        String cs =
                prefs.getString(
                        "contacts",
                        ""
                );

        if (!cs.isEmpty()) {

            contacts.addAll(
                    Arrays.asList(
                            cs.split("\\|")
                    )
            );
        }

        String es =
                prefs.getString(
                        "events",
                        ""
                );

        if (!es.isEmpty()) {

            events.addAll(
                    Arrays.asList(
                            es.split("\\n")
                    )
            );
        }
    }

    void save() {

        StringBuilder c =
                new StringBuilder();

        for (String s : contacts) {

            if (c.length() > 0)
                c.append("|");

            c.append(
                    s.replace("|", "")
            );
        }

        StringBuilder e =
                new StringBuilder();

        for (String s : events) {

            if (e.length() > 0)
                e.append("\n");

            e.append(
                    s.replace("\n", "")
            );
        }

        prefs.edit()
                .putString(
                        "contacts",
                        c.toString()
                )
                .putString(
                        "events",
                        e.toString()
                )
                .apply();
}
}
