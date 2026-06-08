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

public class HistoryListActivity extends AppCompatActivity {

    private ListView listViewHistory;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_list);

        listViewHistory = findViewById(R.id.listViewHistory);
        dbHelper = new DBHelper(this);

        displayHistory();

        listViewHistory.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MONTH));
            Intent intent = new Intent(HistoryListActivity.this, MonthlyCalculationsActivity.class);
            intent.putExtra("MONTH", month);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayHistory();
    }

    private void displayHistory() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Group by month and sum the final costs
        String query = "SELECT " + DBHelper.COLUMN_ID + ", " + DBHelper.COLUMN_MONTH + ", SUM(" + DBHelper.COLUMN_FINAL_COST + ") as total_month_cost " +
                "FROM " + DBHelper.TABLE_NAME + " " +
                "GROUP BY " + DBHelper.COLUMN_MONTH;
        
        Cursor cursor = db.rawQuery(query, null);

        String[] from = new String[]{DBHelper.COLUMN_MONTH, "total_month_cost"};
        int[] to = new int[]{R.id.tvListMonth, R.id.tvListFinalCost};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item_bill, cursor, from, to, 0);

        adapter.setViewBinder((view, cursor1, columnIndex) -> {
            if (view.getId() == R.id.tvListFinalCost) {
                double cost = cursor1.getDouble(columnIndex);
                ((TextView) view).setText(String.format(Locale.getDefault(), "Total: RM %.2f", cost));
                return true;
            }
            return false;
        });

        listViewHistory.setAdapter(adapter);
    }
}
