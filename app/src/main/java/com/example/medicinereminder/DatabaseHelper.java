package com.example.medicinereminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MedicineRecords.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "medicines";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_DOSAGE = "dosage";
    private static final String COL_TIME = "scheduled_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_DOSAGE + " TEXT NOT NULL, " +
                COL_TIME + " TEXT NOT NULL)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertMedicine(String name, String dosage, String time) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DOSAGE, dosage);
        values.put(COL_TIME, time);

        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_NAME, null, values) != -1;
    }

    public Cursor getAllMedicines() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );
    }

    public boolean updateMedicine(int id, String name, String dosage, String time) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DOSAGE, dosage);
        values.put(COL_TIME, time);

        SQLiteDatabase db = getWritableDatabase();
        return db.update(
                TABLE_NAME,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    public boolean deleteMedicine(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(
                TABLE_NAME,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }
}
