package com.example.electricitybillestimator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private Spinner spinnerMonthDetail;
    private EditText etUnitsDetail;
    private SeekBar seekBarRebateDetail;
    private TextView tvRebateLabelDetail, tvTotalChargesDetail, tvFinalCostDetail;
    private Button btnCalculateDetail, btnUpdate, btnDelete;

    private DBHelper dbHelper;
    private int billId;
    private int rebatePercentDetail = 0;
    private double totalChargesDetail = 0.0;
    private double finalCostDetail = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dbHelper = new DBHelper(this);
        billId = getIntent().getIntExtra("BILL_ID", -1);

        spinnerMonthDetail = findViewById(R.id.spinnerMonthDetail);
        etUnitsDetail = findViewById(R.id.etUnitsDetail);
        seekBarRebateDetail = findViewById(R.id.seekBarRebateDetail);
        tvRebateLabelDetail = findViewById(R.id.tvRebateLabelDetail);
        tvTotalChargesDetail = findViewById(R.id.tvTotalChargesDetail);
        tvFinalCostDetail = findViewById(R.id.tvFinalCostDetail);
        btnCalculateDetail = findViewById(R.id.btnCalculateDetail);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        loadRecord();

        seekBarRebateDetail.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebatePercentDetail = progress;
                tvRebateLabelDetail.setText(String.format(Locale.getDefault(), "Rebate Percentage: %d%%", rebatePercentDetail));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnUpdate.setOnClickListener(v -> updateRecord());
        btnDelete.setOnClickListener(v -> deleteRecord());
        btnCalculateDetail.setOnClickListener(v -> calculateBillDetail());
    }

    private void calculateBillDetail() {
        String unitsStr = etUnitsDetail.getText().toString();
        if (unitsStr.isEmpty()) {
            Toast.makeText(this, "Please enter units used", Toast.LENGTH_SHORT).show();
            return;
        }

        double units = Double.parseDouble(unitsStr);
        if (units < 1 || units > 1000) {
            Toast.makeText(this, "Please enter units between 1-1000", Toast.LENGTH_SHORT).show();
            return;
        }

        totalChargesDetail = 0.0;
        if (units <= 200) {
            totalChargesDetail = units * 0.218;
        } else if (units <= 300) {
            totalChargesDetail = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            totalChargesDetail = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else {
            totalChargesDetail = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }

        finalCostDetail = totalChargesDetail - (totalChargesDetail * (rebatePercentDetail / 100.0));

        tvTotalChargesDetail.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalChargesDetail));
        tvFinalCostDetail.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCostDetail));
    }

    private void loadRecord() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_NAME, null, DBHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(billId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MONTH));
            double units = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_UNITS));
            rebatePercentDetail = (int) cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_REBATE));
            double totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TOTAL_CHARGES));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FINAL_COST));

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.months_array, android.R.layout.simple_spinner_item);
            spinnerMonthDetail.setSelection(adapter.getPosition(month));

            etUnitsDetail.setText(String.valueOf(units));
            seekBarRebateDetail.setProgress(rebatePercentDetail);
            tvRebateLabelDetail.setText(String.format(Locale.getDefault(), "Rebate Percentage: %d%%", rebatePercentDetail));
            
            totalChargesDetail = totalCharges;
            finalCostDetail = finalCost;
            
            tvTotalChargesDetail.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalCharges));
            tvFinalCostDetail.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCost));
            cursor.close();
        }
    }

    private void updateRecord() {
        if (totalChargesDetail == 0.0) {
            Toast.makeText(this, "Please calculate the bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        String unitsStr = etUnitsDetail.getText().toString();
        double units = Double.parseDouble(unitsStr);
        String month = spinnerMonthDetail.getSelectedItem().toString();

        int rows = dbHelper.updateBill(billId, month, units, (double) rebatePercentDetail, totalChargesDetail, finalCostDetail);
        if (rows > 0) {
            Toast.makeText(this, "Record updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update record.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRecord() {
        int rows = dbHelper.deleteBill(billId);
        if (rows > 0) {
            Toast.makeText(this, "Record deleted successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete record.", Toast.LENGTH_SHORT).show();
        }
    }
}
