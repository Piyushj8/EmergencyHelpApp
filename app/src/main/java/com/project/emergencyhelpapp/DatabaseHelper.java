package com.project.emergencyhelpapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "my_list.db";
    public static final String TABLE_NAME = "mylist_data";
    public static final String COL1 = "ID";
    public static final String COL2 = "ITEM1";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " ITEM1 TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String s = "DROP IF TABLE EXISTS ";
        db.execSQL(s + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String item1) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item1);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
    public Cursor getListContents(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }
    public ArrayList<String> getContactNumbers() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> contactNumbers = new ArrayList<>();

        // Query the database to retrieve contact numbers
        Cursor cursor = db.rawQuery("SELECT " + COL2 + " FROM " + TABLE_NAME, null);
        Log.d("Called getContactNumbers", "printing cursor of Contact number " +cursor.getColumnIndex(COL2));
        // Check if the cursor is not null and contains data
        if (cursor != null && cursor.moveToFirst()) {

            // Iterate over the cursor to extract contact numbers
            do {
               @SuppressLint("Range") String contactNumber = cursor.getString(cursor.getColumnIndex(COL2));
                Log.d("Called getContactNumbers", "printing  Contact number " +contactNumber);
                contactNumbers.add(contactNumber);
            } while (cursor.moveToNext());

            // Close the cursor after use
            cursor.close();
        }

        // Close the database connection
        db.close();

        return contactNumbers;
    }
}