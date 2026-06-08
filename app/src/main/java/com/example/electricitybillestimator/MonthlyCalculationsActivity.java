package com.example.electricitybillestimator;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MonthlyCalculationsActivity extends AppCompatActivity {

    private ListView listViewCalculations;
    private TextView tvMonthlyHeader;
    private DBHelper dbHelper;
    private String month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_calculations);

        month = getIntent().getStringExtra("MONTH");
        tvMonthlyHeader = findViewById(R.id.tvMonthlyHeader);
        tvMonthlyHeader.setText(String.format("Calculations for %s", month));

        listViewCalculations = findViewById(R.id.listViewCalculations);
        dbHelper = new DBHelper(this);

        displayCalculations();

        listViewCalculations.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MonthlyCalculationsActivity.this, DetailActivity.class);
            intent.putExtra("BILL_ID", (int) id);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayCalculations();
    }

    private void displayCalculations() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_NAME, null, DBHelper.COLUMN_MONTH + " = ?",
                new String[]{month}, null, null, null);

        String[] from = new String[]{DBHelper.COLUMN_UNITS, DBHelper.COLUMN_FINAL_COST};
        int[] to = new int[]{R.id.tvCalcSummary, R.id.tvCalcCost};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item_calculation, cursor, from, to, 0);

        adapter.setViewBinder((view, cursor1, columnIndex) -> {
            if (view.getId() == R.id.tvCalcSummary) {
                double units = cursor1.getDouble(cursor1.getColumnIndexOrThrow(DBHelper.COLUMN_UNITS));
                ((TextView) view).setText(String.format(Locale.getDefault(), "Units: %.2f kWh", units));
                return true;
            } else if (view.getId() == R.id.tvCalcCost) {
                double cost = cursor1.getDouble(cursor1.getColumnIndexOrThrow(DBHelper.COLUMN_FINAL_COST));
                ((TextView) view).setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", cost));
                return true;
            }
            return false;
        });

        listViewCalculations.setAdapter(adapter);
    }
}
