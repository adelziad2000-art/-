package com.example.tasbeehapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String PREFS_NAME = "tasbeeh_prefs";

    private static final String KEY_SELECTED_DHIKR = "selected_dhikr";
    private static final String KEY_ADHKAR_LIST = "adhkar_list";

    // مفاتيح النسخة القديمة عشان ننقل منها البيانات لو كانت موجودة
    private static final String KEY_LEGACY_COUNT = "count";
    private static final String KEY_LEGACY_DHIKR = "dhikr";
    private static final String KEY_MIGRATION_DONE = "legacy_migration_done";

    private int count;
    private String selectedDhikr;

    private TextView countText;
    private TextView dhikrText;
    private TextView dateText;
    private Spinner spinner;
    private TableLayout tableLayout;

    private SharedPreferences prefs;
    private ArrayList<String> adhkarList;
    private ArrayAdapter<String> adapter;

    private final String[] defaultAdhkar = new String[]{
            "سبحان الله",
            "الحمد لله",
            "الله أكبر",
            "لا إله إلا الله",
            "أستغفر الله",
            "لا حول ولا قوة إلا بالله"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.parseColor("#F5F1E8"));

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        loadAdhkarList();

        // تنقل العدد القديم للجدول اليومي مرة واحدة فقط
        migrateOldCountIfNeeded();

        selectedDhikr = prefs.getString(KEY_SELECTED_DHIKR, adhkarList.get(0));

        if (!adhkarList.contains(selectedDhikr)) {
            selectedDhikr = adhkarList.get(0);
        }

        count = getTodayCount(selectedDhikr);

        buildUi();
        updateUi();
        refreshDailyTable();
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor("#F5F1E8"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundColor(Color.parseColor("#F5F1E8"));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        scrollView.addView(root);

        TextView title = new TextView(this);
        title.setText("سبحة إلكترونية");
        title.setTextSize(32);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#234236"));
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        dateText = new TextView(this);
        dateText.setText("إحصائيات اليوم: " + getToday());
        dateText.setTextSize(16);
        dateText.setTextColor(Color.parseColor("#5F6F67"));
        dateText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(-1, -2);
        dateParams.setMargins(0, dp(8), 0, dp(20));
        root.addView(dateText, dateParams);

        spinner = new Spinner(this);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                adhkarList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int selectedIndex = adhkarList.indexOf(selectedDhikr);
        if (selectedIndex >= 0) {
            spinner.setSelection(selectedIndex);
        }

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(-1, dp(56));
        spinnerParams.setMargins(0, 0, 0, dp(12));
        root.addView(spinner, spinnerParams);

        Button addDhikrButton = new Button(this);
        addDhikrButton.setText("إضافة ذكر جديد");
        addDhikrButton.setTextSize(18);
        addDhikrButton.setTextColor(Color.WHITE);
        addDhikrButton.setBackgroundColor(Color.parseColor("#3F5F4B"));

        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(-1, dp(52));
        addParams.setMargins(0, 0, 0, dp(32));
        root.addView(addDhikrButton, addParams);

        dhikrText = new TextView(this);
        dhikrText.setTextSize(28);
        dhikrText.setTypeface(Typeface.DEFAULT_BOLD);
        dhikrText.setTextColor(Color.parseColor("#2F5D46"));
        dhikrText.setGravity(Gravity.CENTER);
        root.addView(dhikrText, new LinearLayout.LayoutParams(-1, -2));

        countText = new TextView(this);
        countText.setTextSize(86);
        countText.setTypeface(Typeface.DEFAULT_BOLD);
        countText.setTextColor(Color.parseColor("#1C3328"));
        countText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(-1, -2);
        countParams.setMargins(0, dp(12), 0, dp(20));
        root.addView(countText, countParams);

        Button tapButton = new Button(this);
        tapButton.setText("سبّح");
        tapButton.setTextSize(30);
        tapButton.setTypeface(Typeface.DEFAULT_BOLD);
        tapButton.setTextColor(Color.WHITE);
        tapButton.setBackgroundColor(Color.parseColor("#2E7D5B"));

        LinearLayout.LayoutParams tapParams = new LinearLayout.LayoutParams(dp(220), dp(120));
        tapParams.setMargins(0, 0, 0, dp(22));
        root.addView(tapButton, tapParams);

        Button resetButton = new Button(this);
        resetButton.setText("تصفير الذكر الحالي اليوم");
        resetButton.setTextSize(18);
        resetButton.setTextColor(Color.WHITE);
        resetButton.setBackgroundColor(Color.parseColor("#8B3A3A"));

        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(-1, dp(56));
        resetParams.setMargins(0, 0, 0, dp(28));
        root.addView(resetButton, resetParams);

        TextView tableTitle = new TextView(this);
        tableTitle.setText("جدول تسبيحات اليوم");
        tableTitle.setTextSize(24);
        tableTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tableTitle.setTextColor(Color.parseColor("#234236"));
        tableTitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams tableTitleParams = new LinearLayout.LayoutParams(-1, -2);
        tableTitleParams.setMargins(0, dp(4), 0, dp(12));
        root.addView(tableTitle, tableTitleParams);

        tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(true);
        tableLayout.setShrinkAllColumns(true);

        root.addView(tableLayout, new LinearLayout.LayoutParams(-1, -2));

        setContentView(scrollView);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean firstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstSelection) {
                    firstSelection = false;
                    return;
                }

                selectedDhikr = adhkarList.get(position);
                count = getTodayCount(selectedDhikr);
                saveSelectedDhikr();
                updateUi();
                refreshDailyTable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        addDhikrButton.setOnClickListener(v -> showAddDhikrDialog());

        tapButton.setOnClickListener(v -> {
            count++;
            saveTodayCount(selectedDhikr, count);
            saveSelectedDhikr();
            updateUi();
            refreshDailyTable();
            vibrate();
        });

        resetButton.setOnClickListener(v -> showResetConfirmationDialog());
    }

    private void showAddDhikrDialog() {
        EditText input = new EditText(this);
        input.setHint("اكتب الذكر هنا");
        input.setTextDirection(View.TEXT_DIRECTION_RTL);
        input.setSingleLine(false);
        input.setMinLines(1);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("إضافة ذكر جديد")
                .setMessage("اكتب اسم الذكر الذي تريد إضافته")
                .setView(input)
                .setPositiveButton("إضافة", null)
                .setNegativeButton("إلغاء", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(v -> {
                String newDhikr = normalizeDhikrText(input.getText().toString());

                if (newDhikr.isEmpty()) {
                    Toast.makeText(this, "من فضلك اكتب اسم الذكر", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (containsDhikrNormalized(newDhikr)) {
                    Toast.makeText(this, "هذا الذكر موجود بالفعل", Toast.LENGTH_SHORT).show();
                    return;
                }

                adhkarList.add(newDhikr);
                saveAdhkarList();

                adapter.notifyDataSetChanged();

                selectedDhikr = newDhikr;
                count = getTodayCount(selectedDhikr);
                saveSelectedDhikr();

                spinner.setSelection(adhkarList.indexOf(newDhikr));

                updateUi();
                refreshDailyTable();

                Toast.makeText(this, "تمت إضافة الذكر", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("تأكيد التصفير")
                .setMessage("هل تريد تصفير عدد هذا الذكر لليوم؟")
                .setPositiveButton("نعم، صفّر", (dialog, which) -> {
                    count = 0;
                    saveTodayCount(selectedDhikr, count);
                    updateUi();
                    refreshDailyTable();
                    Toast.makeText(this, "تم تصفير الذكر الحالي", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void updateUi() {
        dhikrText.setText(selectedDhikr);
        countText.setText(String.valueOf(count));
        dateText.setText("إحصائيات اليوم: " + getToday());
    }

    private void refreshDailyTable() {
        tableLayout.removeAllViews();

        addTableRow("الذكر", "عدد اليوم", true);

        int total = 0;
        boolean hasAnyCount = false;

        for (String dhikr : adhkarList) {
            int value = getTodayCount(dhikr);

            if (value > 0) {
                addTableRow(dhikr, String.valueOf(value), false);
                total += value;
                hasAnyCount = true;
            }
        }

        if (!hasAnyCount) {
            addTableRow("لا توجد تسبيحات اليوم", "0", false);
        } else {
            addTableRow("الإجمالي", String.valueOf(total), true);
        }
    }

    private void addTableRow(String firstText, String secondText, boolean isHeader) {
        TableRow row = new TableRow(this);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, dp(2), 0, dp(2));

        TextView firstCell = createCell(firstText, isHeader);
        TextView secondCell = createCell(secondText, isHeader);

        row.addView(firstCell);
        row.addView(secondCell);

        tableLayout.addView(row, new TableLayout.LayoutParams(-1, -2));
    }

    private TextView createCell(String text, boolean isHeader) {
        TextView cell = new TextView(this);

        cell.setText(text);
        cell.setTextSize(isHeader ? 17 : 16);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(8), dp(10), dp(8), dp(10));
        cell.setTextColor(Color.parseColor("#1C3328"));

        if (isHeader) {
            cell.setTypeface(Typeface.DEFAULT_BOLD);
            cell.setBackgroundColor(Color.parseColor("#DDD4C4"));
        } else {
            cell.setBackgroundColor(Color.parseColor("#EFE8DA"));
        }

        return cell;
    }

    private void loadAdhkarList() {
        adhkarList = new ArrayList<>();

        String savedJson = prefs.getString(KEY_ADHKAR_LIST, null);

        if (savedJson != null) {
            try {
                JSONArray array = new JSONArray(savedJson);

                for (int i = 0; i < array.length(); i++) {
                    String value = normalizeDhikrText(array.getString(i));

                    if (!value.isEmpty() && !containsDhikrNormalized(value)) {
                        adhkarList.add(value);
                    }
                }
            } catch (Exception ignored) { }
        }

        if (adhkarList.isEmpty()) {
            for (String dhikr : defaultAdhkar) {
                adhkarList.add(normalizeDhikrText(dhikr));
            }

            saveAdhkarList();
        }
    }

    private void saveAdhkarList() {
        JSONArray array = new JSONArray();

        for (String dhikr : adhkarList) {
            array.put(normalizeDhikrText(dhikr));
        }

        prefs.edit()
                .putString(KEY_ADHKAR_LIST, array.toString())
                .apply();
    }

    private void migrateOldCountIfNeeded() {
        boolean migrationDone = prefs.getBoolean(KEY_MIGRATION_DONE, false);

        if (migrationDone) {
            return;
        }

        int oldCount = prefs.getInt(KEY_LEGACY_COUNT, 0);
        String oldDhikr = normalizeDhikrText(prefs.getString(KEY_LEGACY_DHIKR, ""));

        if (oldCount > 0 && !oldDhikr.isEmpty()) {
            if (!containsDhikrNormalized(oldDhikr)) {
                adhkarList.add(oldDhikr);
                saveAdhkarList();
            }

            int todayCurrentCount = getTodayCount(oldDhikr);
            saveTodayCount(oldDhikr, todayCurrentCount + oldCount);

            prefs.edit()
                    .putString(KEY_SELECTED_DHIKR, oldDhikr)
                    .putBoolean(KEY_MIGRATION_DONE, true)
                    .apply();
        } else {
            prefs.edit()
                    .putBoolean(KEY_MIGRATION_DONE, true)
                    .apply();
        }
    }

    private void saveSelectedDhikr() {
        prefs.edit()
                .putString(KEY_SELECTED_DHIKR, selectedDhikr)
                .apply();
    }

    private int getTodayCount(String dhikr) {
        return prefs.getInt(getTodayCountKey(dhikr), 0);
    }

    private void saveTodayCount(String dhikr, int value) {
        prefs.edit()
                .putInt(getTodayCountKey(dhikr), value)
                .apply();
    }

    private String getTodayCountKey(String dhikr) {
        String cleanDhikr = normalizeDhikrText(dhikr);

        String encodedDhikr = Base64.encodeToString(
                cleanDhikr.getBytes(StandardCharsets.UTF_8),
                Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING
        );

        return "daily_count_" + getToday() + "_" + encodedDhikr;
    }

    private String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private String normalizeDhikrText(String text) {
        if (text == null) {
            return "";
        }

        return text.trim().replaceAll("\\s+", " ");
    }

    private boolean containsDhikrNormalized(String dhikr) {
        String cleanDhikr = normalizeDhikrText(dhikr);

        for (String existingDhikr : adhkarList) {
            if (normalizeDhikrText(existingDhikr).equals(cleanDhikr)) {
                return true;
            }
        }

        return false;
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            35,
                            VibrationEffect.DEFAULT_AMPLITUDE
                    )
            );
        } else {
            vibrator.vibrate(35);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
