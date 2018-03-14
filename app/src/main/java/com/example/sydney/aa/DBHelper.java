package com.example.sydney.aa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.sydney.aa.Constants.COLUMN_DATA_CODE;
import static com.example.sydney.aa.Constants.COLUMN_DATA_PONU;
import static com.example.sydney.aa.Constants.COLUMN_DATA_QUAN;
import static com.example.sydney.aa.Constants.COLUMN_LIST_CODE;
import static com.example.sydney.aa.Constants.COLUMN_LIST_PONU;
import static com.example.sydney.aa.Constants.COLUMN_LIST_QUAN;
import static com.example.sydney.aa.Constants.CREATE_TABLE_DATA;
import static com.example.sydney.aa.Constants.CREATE_TABLE_LIST;
import static com.example.sydney.aa.Constants.TABLE_DATA;
import static com.example.sydney.aa.Constants.TABLE_LIST;

/**
 * Created by PROGRAMMER2 on 5/2/2017.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "scan_db.db";
    private static final int DATABASE_VERSION = 1;

    SQLiteDatabase dbWriter = this.getWritableDatabase();
    SQLiteDatabase dbReader = this.getReadableDatabase();

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
//        arg0.execSQL(CREATE_TABLE_PO);
        arg0.execSQL(CREATE_TABLE_LIST);
        arg0.execSQL(CREATE_TABLE_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int i, int i1) {
//        arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_PO);
//        onCreate(arg0);
        arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
        onCreate(arg0);
        arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        onCreate(arg0);
    }

    public Cursor queryDataWrite(String sql){
        return dbWriter.rawQuery(sql,null);
    }

    Cursor queryDataRead(String sql){
        return dbReader.rawQuery(sql,null);
    }

    //ADD ITEM
    int insertItem(String pPO, String pCode, int pQuan) {

        //Return legends
        //1 = New item added.
        //2 = Old item updated.
        //3 = Quantity is above the limit.
        //5 = Failed to update quantity.
        //6 = Unknown Database error.
        //7 = Failed to add item.
        String[] selectionArgs = new String[]{pPO, pCode};
        try {
            Cursor cursor = dbReader.rawQuery("SELECT COUNT(*) FROM " + TABLE_DATA+ " WHERE " +
                    COLUMN_DATA_PONU + "=? AND " + COLUMN_DATA_CODE + "=?", selectionArgs);
            cursor.moveToFirst();
            int mCount = cursor.getInt(0);
            cursor.close();

            if (mCount!=0){
                Cursor cursor1 = dbReader.rawQuery("SELECT " + COLUMN_DATA_QUAN + " FROM " +
                        TABLE_DATA + " WHERE " + COLUMN_DATA_PONU +
                        "=? AND " + COLUMN_DATA_CODE + "=?", selectionArgs);
                cursor1.moveToFirst();
                int mSum = cursor1.getInt(0) + pQuan;
                cursor1.close();

                Cursor cursor2 = dbReader.rawQuery("SELECT " + COLUMN_LIST_QUAN + " FROM " +
                        TABLE_LIST + " WHERE " + COLUMN_LIST_PONU +
                        "=? AND " + COLUMN_LIST_CODE + "=?", selectionArgs);
                cursor2.moveToFirst();
                int mQuan = cursor2.getInt(0);
                cursor2.close();

                if (mQuan >= mSum) {
                    try{
                        String mWHERE = COLUMN_DATA_PONU + " = ? AND " + COLUMN_DATA_CODE + "=?";
                        String[] mWHERE_ARGS = new String[]{pPO, pCode};
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_DATA_QUAN, mSum);
                        dbWriter.update(TABLE_DATA, values, mWHERE, mWHERE_ARGS);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        return 5;
                    }
                }
                else{
                    return 3;
                }
                return 2;
            }
            else {
                try{
                    Cursor cursor1 = dbReader.rawQuery("SELECT " + COLUMN_LIST_QUAN + " FROM " +
                            TABLE_LIST + " WHERE " + COLUMN_LIST_PONU + "=? AND " +
                            COLUMN_LIST_CODE + "=?", selectionArgs);
                    cursor1.moveToFirst();
                    int mQuan = cursor1.getInt(0);
                    cursor1.close();

                    if (mQuan >= pQuan) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_DATA_PONU, pPO);
                        values.put(COLUMN_DATA_CODE, pCode);
                        values.put(COLUMN_DATA_QUAN, pQuan);
                        dbWriter.insert(TABLE_DATA, null, values);
                        return 1;
                    }
                    else {
                        return 3;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return 7;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 6;
        }
    }

    //SEARCH AFTER SCAN ITEM
    String[] searchForItem(String pPO, String pCode) {
        String[] selectionArgs = new String[]{pPO, pCode};
        try {
            Cursor cursor = dbReader.rawQuery("SELECT COUNT(*) FROM " + TABLE_LIST + " WHERE " +
                    COLUMN_LIST_PONU + "=? AND " + COLUMN_LIST_CODE + "=?", selectionArgs);
            cursor.moveToFirst();
            int mCount = cursor.getInt(0);
            cursor.close();

            if (mCount!=0){
                Cursor cursor1 = dbReader.rawQuery("SELECT * FROM " + TABLE_LIST + " WHERE " +
                        COLUMN_LIST_PONU + "=? AND " + COLUMN_LIST_CODE + "=?", selectionArgs);
                cursor1.moveToFirst();
                String[] list = {cursor1.getString(6), cursor1.getString(7)};
                cursor1.close();
                return list;
            }
            else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //SEARCH FOR DUPLICATE
//    int searchForDuplicate(String bcode) {
//        String[] selectionArgs = new String[]{ bcode };
//        try {
//            int i = 0;
//            Cursor cursor = null;
//            cursor = dbReader.rawQuery("select * from " + TABLE_ITEM + " where " + COLUMN_BARCODE +
//                    "=?", selectionArgs);
//            cursor.moveToFirst();
//            i = cursor.getCount();
//            cursor.close();
//            return i;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }


    Cursor exportAllItems() {
        String rawBaKamo = "SELECT " + COLUMN_DATA_PONU + "," + COLUMN_DATA_CODE + "," +
                COLUMN_DATA_QUAN + " FROM " + TABLE_DATA;
        return queryDataRead(rawBaKamo);
    }

//    Cursor getAllItems(){
//    String rawBaKamo = "SELECT * FROM "+TABLE_ITEM_IMPORT;
//    return dbReader.rawQuery(rawBaKamo,null);
//    }
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        android.util.Log.w("Constants", "Upgrading database, which will destroy all old data");
//        db.execSQL("DROP TABLE IF EXISTS constants");
//        onCreate(db);
//    }
}