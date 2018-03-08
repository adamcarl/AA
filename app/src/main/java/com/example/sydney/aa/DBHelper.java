package com.example.sydney.aa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

import static android.provider.BaseColumns._ID;
import static com.example.sydney.aa.Constants.COLUMN_DATA_CASE;
import static com.example.sydney.aa.Constants.COLUMN_DATA_CODE;
import static com.example.sydney.aa.Constants.COLUMN_DATA_NUMBER;
import static com.example.sydney.aa.Constants.COLUMN_DATA_PIECE;
import static com.example.sydney.aa.Constants.COLUMN_LIST_CASE;
import static com.example.sydney.aa.Constants.COLUMN_LIST_CODE;
import static com.example.sydney.aa.Constants.COLUMN_LIST_PIECE;
import static com.example.sydney.aa.Constants.COLUMN_PO_CODE;
import static com.example.sydney.aa.Constants.COLUMN_PO_NUMBER;
import static com.example.sydney.aa.Constants.CREATE_TABLE_DATA;
import static com.example.sydney.aa.Constants.CREATE_TABLE_LIST;
import static com.example.sydney.aa.Constants.CREATE_TABLE_PO;
import static com.example.sydney.aa.Constants.TABLE_DATA;
import static com.example.sydney.aa.Constants.TABLE_LIST;
import static com.example.sydney.aa.Constants.TABLE_PO;

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
        arg0.execSQL(CREATE_TABLE_PO);
        arg0.execSQL(CREATE_TABLE_LIST);
        arg0.execSQL(CREATE_TABLE_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int i, int i1) {
        arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_PO);
        onCreate(arg0);
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
    int insertItem(String mPO, String mCode,int mQuanCase,int mQuanPiece){

        //Return legends
        //1 = New item added.
        //2 = Old item updated.
        //3 = Quantity is above the limit.
        //5 = Failed to update quantity.
        //6 = Unknown Database error.
        //7 = Failed to add item.
        String[] selectionArgs = new String[]{mPO, mCode};
        try {
            Cursor cursor = dbReader.rawQuery("SELECT COUNT(*) FROM " + TABLE_DATA+ " WHERE " +
                    COLUMN_DATA_NUMBER + "=? AND " + COLUMN_DATA_CODE + "=?", selectionArgs);
            cursor.moveToFirst();
            int mCount = cursor.getInt(0);
            cursor.close();

            if (mCount!=0){
                Cursor cursor1 = dbReader.rawQuery("SELECT SUM(" + COLUMN_DATA_CASE + "), SUM(" +
                        COLUMN_DATA_PIECE + ") FROM " + TABLE_DATA + " WHERE " + COLUMN_DATA_CODE +
                        "=?",new String[]{mCode});
                cursor1.moveToFirst();
                int[] list = {cursor1.getInt(0),cursor1.getInt(1)};
                cursor1.close();
                int mCase = mQuanCase + list[0];
                int mPiece = mQuanPiece + list[1];

                Cursor cursor2 = dbReader.rawQuery("SELECT " + COLUMN_LIST_CASE +
                        "," + COLUMN_LIST_PIECE + " FROM " + TABLE_LIST + " WHERE " +
                        COLUMN_LIST_CODE + "=?",new String[]{mCode});
                cursor2.moveToFirst();
                int[] mSum = {cursor2.getInt(0),cursor2.getInt(1)};
                cursor2.close();

                if(mCase<=mSum[0] && mPiece<=mSum[1]){
                    try{
                        String mWHERE = COLUMN_DATA_NUMBER + " = ? AND " + COLUMN_DATA_CODE +
                                " = ?";
                        String[] mWHERE_ARGS = new String[]{mPO,mCode};
                        ContentValues values = new ContentValues();

                        values.put(COLUMN_DATA_CASE, mCase);
                        values.put(COLUMN_DATA_PIECE, mPiece);

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
                    Cursor cursor1 = dbReader.rawQuery("SELECT SUM(" + COLUMN_LIST_CASE +
                            "), SUM(" + COLUMN_LIST_PIECE + ") FROM " + TABLE_LIST + " WHERE " +
                            COLUMN_LIST_CODE + "=?",new String[]{mCode});
                    cursor1.moveToFirst();
                    int[] list = {cursor1.getInt(0),cursor1.getInt(1)};
                    cursor1.close();

                    if(mQuanCase<=list[0] && mQuanCase<=list[1]){
                        ContentValues values = new ContentValues();

                        values.put(COLUMN_DATA_NUMBER, mPO);
                        values.put(COLUMN_DATA_CODE, mCode);
                        values.put(COLUMN_DATA_CASE, mQuanCase);
                        values.put(COLUMN_DATA_PIECE, mQuanPiece);

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
    String[] searchForItem(String mNumber, String mCode) {
        String[] selectionArgs = new String[]{ mNumber, mCode };
        try {
            Cursor cursor = dbReader.rawQuery("SELECT COUNT(*) FROM " + TABLE_PO + " WHERE " +
                    COLUMN_PO_NUMBER + "=? AND " + COLUMN_PO_CODE + "=?", selectionArgs);
            cursor.moveToFirst();
            int mCount = cursor.getInt(0);
            cursor.close();

            if (mCount!=0){
                Cursor cursor1 = dbReader.rawQuery("SELECT * FROM " + TABLE_LIST + " WHERE " +
                        COLUMN_LIST_CODE + "=?", new String[]{mCode});
                cursor1.moveToFirst();
                String[] list = {cursor1.getString(1),cursor1.getString(2),cursor1.getString(3),
                        cursor1.getString(4)};
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
        String rawBaKamo = "SELECT " + COLUMN_DATA_NUMBER + ","+ COLUMN_DATA_CODE+ ","+
                COLUMN_DATA_CASE+ ","+ COLUMN_DATA_PIECE+ " FROM " + TABLE_DATA;
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