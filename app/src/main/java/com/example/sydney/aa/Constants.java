package com.example.sydney.aa;

import android.provider.BaseColumns;

/**
 * Created by PROGRAMMER2 on 2/23/2018.
 * ABZTRAK INC.
 */

interface Constants extends BaseColumns {
//    String TABLE_PO = "tbl_po";
//    String COLUMN_PO_NUMBER = "Number";
//    String COLUMN_PO_CODE= "Code";
//    String CREATE_TABLE_PO = "CREATE TABLE " + TABLE_PO + " ("
//            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//            + COLUMN_PO_NUMBER + " TEXT NOT NULL, "
//            + COLUMN_PO_CODE + " TEXT NOT NULL);";

    String TABLE_LIST = "tbl_list";
    String COLUMN_LIST_PONO = "PONO";
    String COLUMN_LIST_PONU = "POnum";
    String COLUMN_LIST_DOCD = "DocDate";
    String COLUMN_LIST_QUAN = "Quantity";
    String COLUMN_LIST_CODE = "ItemCode";
    String COLUMN_LIST_DESC = "Description";
    String COLUMN_LIST_CARD = "CardName";
    String CREATE_TABLE_LIST = "CREATE TABLE " + TABLE_LIST + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_LIST_PONO + " TEXT NOT NULL, "
            + COLUMN_LIST_PONU + " TEXT NOT NULL, "
            + COLUMN_LIST_DOCD + " TEXT NOT NULL, "
            + COLUMN_LIST_QUAN + " INTEGER NOT NULL, "
            + COLUMN_LIST_CODE + " TEXT NOT NULL, "
            + COLUMN_LIST_DESC + " TEXT NOT NULL, "
            + COLUMN_LIST_CARD + " TEXT NOT NULL);";

    String TABLE_DATA = "tbl_data";
    String COLUMN_DATA_PONU = "POnum";
    String COLUMN_DATA_CODE = "ItemCode";
    String COLUMN_DATA_QUAN = "Quantity";
    String CREATE_TABLE_DATA = "CREATE TABLE " + TABLE_DATA + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_DATA_PONU + " TEXT NOT NULL, "
            + COLUMN_DATA_CODE + " TEXT NOT NULL, "
            + COLUMN_DATA_QUAN + " INTEGER NOT NULL);";

    //public static String SERVER_IP = "192.168.137.142"; //10.0.0.112 Abztrak //Ahuehuehue 192.168.137.5 //WagKumonnect 192.168.137.142 //OCI_ap_5 192.168.137.2
    int SERVER_PORT= 8998;
}
