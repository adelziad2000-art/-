package com.example.tasbeehapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int count;
    private String selectedDhikr;
    private TextView countText;
    private TextView dhikrText;
    private SharedPreferences prefs;

    private final String[] adhkar = new String[]{
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

        prefs = getSharedPreferences("tasbeeh_prefs", MODE_PRIVATE);
        count = prefs.getInt("count", 0);
        selectedDhikr = prefs.getString("dhikr", adhkar[0]);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.parseColor("#F5F1E8"));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("سبحة إلكترونية");
        title.setTextSize(32);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#234236"));
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, adhkar);
        spinner.setAdapter(adapter);
        int index = 0;
        for (int i = 0; i < adhkar.length; i++) {
            if (adhkar[i].equals(selectedDhikr)) index = i;
        }
        spinner.setSelection(index);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(-1, dp(56));
        spinnerParams.setMargins(0, dp(24), 0, dp(48));
        root.addView(spinner, spinnerParams);

        dhikrText = new TextView(this);
        dhikrText.setText(selectedDhikr);
        dhikrText.setTextSize(28);
        dhikrText.setTypeface(Typeface.DEFAULT_BOLD);
        dhikrText.setTextColor(Color.parseColor("#2F5D46"));
        dhikrText.setGravity(Gravity.CENTER);
        root.addView(dhikrText, new LinearLayout.LayoutParams(-1, -2));

        countText = new TextView(this);
        countText.setText(String.valueOf(count));
        countText.setTextSize(86);
        countText.setTypeface(Typeface.DEFAULT_BOLD);
        countText.setTextColor(Color.parseColor("#1C3328"));
        countText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(-1, -2);
        countParams.setMargins(0, dp(16), 0, dp(24));
        root.addView(countText, countParams);

        Button tapButton = new Button(this);
        tapButton.setText("سبّح");
        tapButton.setTextSize(30);
        tapButton.setTypeface(Typeface.DEFAULT_BOLD);
        tapButton.setTextColor(Color.WHITE);
        tapButton.setBackgroundColor(Color.parseColor("#2E7D5B"));
        LinearLayout.LayoutParams tapParams = new LinearLayout.LayoutParams(dp(210), dp(120));
        tapParams.setMargins(0, 0, 0, dp(36));
        root.addView(tapButton, tapParams);

        Button resetButton = new Button(this);
        resetButton.setText("إعادة العد");
        resetButton.setTextSize(20);
        resetButton.setTextColor(Color.WHITE);
        resetButton.setBackgroundColor(Color.parseColor("#8B3A3A"));
        root.addView(resetButton, new LinearLayout.LayoutParams(-1, dp(56)));

        setContentView(root);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newDhikr = adhkar[position];
                if (!newDhikr.equals(selectedDhikr)) {
                    selectedDhikr = newDhikr;
                    count = 0;
                    updateUi();
                    saveData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        tapButton.setOnClickListener(v -> {
            count++;
            updateUi();
            saveData();
            vibrate();
        });

        resetButton.setOnClickListener(v -> {
            count = 0;
            updateUi();
            saveData();
        });
    }

    private void updateUi() {
        countText.setText(String.valueOf(count));
        dhikrText.setText(selectedDhikr);
    }

    private void saveData() {
        prefs.edit().putInt("count", count).putString("dhikr", selectedDhikr).apply();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(35);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
