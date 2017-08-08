package com.example.sydney.aa;

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

    static final String DATABASE_NAME = "dbscanner.db";
    static final int DATABASE_VERSION = 1;

    SQLiteDatabase dbWriter = this.getWritableDatabase();
    SQLiteDatabase dbReader = this.getReadableDatabase();

    static final String TABLE_ITEM = "item";
    static final String TABLE_ITEM_IMPORT = "itemImport";

    static final String COLUMN_ID = "id";
    static final String COLUMN_BARCODE = "barcode";

    static final String COLUMN_ID_IMPORT = "idImport";
    static final String COLUMN_BARCODE_IMPORT = "barcodeImport";
    static final String COLUMN_DESCRIPTION_IMPORT = "descriptionImport";
//    private static final String COLUMN_QUANTITY= "quantity";

    private static final String TABLE_ITEM_CREATE = "create table if not exists "+TABLE_ITEM+" ("
            +COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            +COLUMN_BARCODE+" TEXT)";
    private static final String TABLE_ITEM_IMPORT_CREATE = "create table if not exists "+TABLE_ITEM_IMPORT+" ("
            +COLUMN_ID_IMPORT+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            +COLUMN_BARCODE_IMPORT+" TEXT, "
            +COLUMN_DESCRIPTION_IMPORT+" TEXT) ";
//            +COLUMN_QUANTITY+" INTEGER)";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //CREATE TABLE
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_ITEM_CREATE);
        sqLiteDatabase.execSQL(TABLE_ITEM_IMPORT_CREATE);
    }

    public Cursor queryDataWrite(String sql){
        return dbWriter.rawQuery(sql,null);
    }

    Cursor queryDataRead(String sql){
        return dbReader.rawQuery(sql,null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM_IMPORT  );
        onCreate(sqLiteDatabase);
    }

    //ADD ITEM
    void insertItem(String bcode){
        ContentValues values = new ContentValues();

        values.put(COLUMN_BARCODE, bcode);
//        values.put(COLUMN_DESCRIPTION, desc);
//        values.put(COLUMN_QUANTITY, item.getQuantity());

        dbWriter.insert(TABLE_ITEM , null , values);
    }

    //SEARCH AFTER SCAN ITEM
    public int searchForItem(String bcode) {
        String[] selectionArgs = new String[]{ bcode };
        try {
            int i = 0;
            Cursor cursor = null;
            cursor = dbReader.rawQuery("select * from " + TABLE_ITEM + " where " + COLUMN_BARCODE + "=?", selectionArgs);
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
        String[] selectionArgs = new String[]{ bcode };
        try {
            int i = 0;
            Cursor cursor = null;
            cursor = dbReader.rawQuery("select * from " + TABLE_ITEM + " where " + COLUMN_BARCODE + "=?", selectionArgs);
            cursor.moveToFirst();
            i = cursor.getCount();
            cursor.close();
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    void deleteAll(){
        dbWriter.delete(TABLE_ITEM,null,null);
        dbWriter.delete(TABLE_ITEM_IMPORT,null,null);
    }

//    //UPDATE ITEM QUANTITY
//    public void updateQuantity(int id,int newQuantity){
//        this.getWritableDatabase().execSQL("UPDATE "+TABLE_ITEM+" SET "+ COLUMN_QUANTITY+"='" + newQuantity + "' WHERE id='" + id + "'");
//    }
    //GETTING VALUES IN EXCEL
    public ArrayList<HashMap<String, String>> getAllProducts() {
        ArrayList<HashMap<String, String>> proList;
        proList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM item";
        Cursor cursor = dbReader.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                //id, barcode, description, quantity
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(COLUMN_ID, cursor.getString(0));
                map.put(COLUMN_BARCODE, cursor.getString(1));
//                map.put(COLUMN_QUANTITY, cursor.getString(3));
                proList.add(map);
            } while (cursor.moveToNext());
        }
        return proList;
    }
    Cursor getAllItems(){
        String rawBaKamo = "SELECT " + TABLE_ITEM_IMPORT + "." + COLUMN_BARCODE_IMPORT + " , " + TABLE_ITEM_IMPORT + "." + COLUMN_DESCRIPTION_IMPORT + " , "
                + TABLE_ITEM + "." + COLUMN_BARCODE + " FROM " + TABLE_ITEM_IMPORT + " LEFT JOIN " + TABLE_ITEM + " ON "
                + TABLE_ITEM_IMPORT + "." + COLUMN_BARCODE_IMPORT + " = " + TABLE_ITEM + "." + COLUMN_BARCODE + " GROUP BY " + COLUMN_BARCODE_IMPORT;
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
