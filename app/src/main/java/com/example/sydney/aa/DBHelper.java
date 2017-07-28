package com.programmer2.mybarcodescanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by PROGRAMMER2 on 5/2/2017.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dbscanner.db";
    private static final String TABLE_ITEM = "item";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_BARCODE = "barcode";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_QUANTITY= "quantity";

    private static final String TABLE_ITEM_CREATE = "create table if not exists "+TABLE_ITEM+" ("
            +COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            +COLUMN_BARCODE+" TEXT, "
            +COLUMN_DESCRIPTION+" TEXT, "
            +COLUMN_QUANTITY+" INTEGER)";



    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //CREATE TABLE
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_ITEM_CREATE);
    }

    public Cursor queryDataWrite(String sql){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery(sql,null);
    }

    public Cursor queryDataRead(String sql){
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery(sql,null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
        onCreate(sqLiteDatabase);
    }


    //ADD ITEM
    public void insertItem(Item item){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_BARCODE, item.getBarcode());
        values.put(COLUMN_DESCRIPTION, item.getDescription());
        values.put(COLUMN_QUANTITY, item.getQuantity());

        database.insert(TABLE_ITEM , null , values);
        database.close();
    }

    //SEARCH AFTER SCAN ITEM
    public int searchForItem(String bcode) {
        SQLiteDatabase database = this.getReadableDatabase();
        String[] selectionArgs = new String[]{ bcode };
        try {
            int i = 0;
            Cursor cursor = null;
            cursor = database.rawQuery("select * from " + TABLE_ITEM + " where " + COLUMN_BARCODE + "=?", selectionArgs);
            cursor.moveToFirst();
            i = cursor.getCount();
            cursor.close();
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //SEARCH FOR DUPLICATE
    public int searchForDuplicate(String bcode) {
        SQLiteDatabase database = this.getReadableDatabase();
        String[] selectionArgs = new String[]{ bcode };
        try {
            int i = 0;
            Cursor cursor = null;
            cursor = database.rawQuery("select * from " + TABLE_ITEM + " where " + COLUMN_BARCODE + "=?", selectionArgs);
            cursor.moveToFirst();
            i = cursor.getCount();
            cursor.close();
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //UPDATE ITEM QUANTITY
    public void updateQuantity(int id,int newQuantity){
        this.getWritableDatabase().execSQL("UPDATE "+TABLE_ITEM+" SET "+ COLUMN_QUANTITY+"='" + newQuantity + "' WHERE id='" + id + "'");
    }

    //GETTING VALUES IN EXCEL
    public ArrayList<HashMap<String, String>> getAllProducts() {
        ArrayList<HashMap<String, String>> proList;
        proList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM item";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                //id, barcode, description, quantity
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(COLUMN_ID, cursor.getString(0));
                map.put(COLUMN_BARCODE, cursor.getString(1));
                map.put(COLUMN_DESCRIPTION, cursor.getString(2));
                map.put(COLUMN_QUANTITY, cursor.getString(3));
                proList.add(map);
            } while (cursor.moveToNext());
        }
        return proList;
    }

//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        android.util.Log.w("Constants", "Upgrading database, which will destroy all old data");
//        db.execSQL("DROP TABLE IF EXISTS constants");
//        onCreate(db);
//    }
}
