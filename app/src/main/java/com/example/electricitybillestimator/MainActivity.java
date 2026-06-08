package com.example.electricitybillestimator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText etUnits;
    private SeekBar seekBarRebate;
    private TextView tvRebateLabel, tvTotalCharges, tvFinalCost;
    private Button btnCalculate, btnSave, btnHistory, btnAbout;

    private DBHelper dbHelper;
    private double totalCharges = 0.0;
    private double finalCost = 0.0;
    private int rebatePercent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        etUnits = findViewById(R.id.etUnits);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        tvRebateLabel = findViewById(R.id.tvRebateLabel);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost = findViewById(R.id.tvFinalCost);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);
        btnHistory = findViewById(R.id.btnHistory);
        btnAbout = findViewById(R.id.btnAbout);

        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebatePercent = progress;
                tvRebateLabel.setText(String.format(Locale.getDefault(), "Rebate Percentage: %d%%", rebatePercent));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnCalculate.setOnClickListener(v -> calculateBill());

        btnSave.setOnClickListener(v -> saveBill());

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryListActivity.class);
            startActivity(intent);
        });

        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            toggleTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        recreate();
    }

    private void calculateBill() {
        String unitsStr = etUnits.getText().toString();
        if (unitsStr.isEmpty()) {
            Toast.makeText(this, "Please enter units used", Toast.LENGTH_SHORT).show();
            return;
        }

        double units = Double.parseDouble(unitsStr);

        if (units < 1 || units > 1000) {
            Toast.makeText(this, "Please enter units between 1-1000", Toast.LENGTH_SHORT).show();
            return;
        }

        totalCharges = 0.0;

        if (units <= 200) {
            totalCharges = units * 0.218;
        } else if (units <= 300) {
            totalCharges = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            totalCharges = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else {
            totalCharges = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }

        finalCost = totalCharges - (totalCharges * (rebatePercent / 100.0));

        tvTotalCharges.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalCharges));
        tvFinalCost.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCost));
    }

    private void saveBill() {
        if (totalCharges == 0.0) {
            Toast.makeText(this, "Please calculate the bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        String month = spinnerMonth.getSelectedItem().toString();
        double units = Double.parseDouble(etUnits.getText().toString());

        long id = dbHelper.insertBill(month, units, (double) rebatePercent, totalCharges, finalCost);
        if (id != -1) {
            Toast.makeText(this, "Record saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save record.", Toast.LENGTH_SHORT).show();
        }
    }
}
