package com.example.sydney.aa;

import android.provider.BaseColumns;

/**
 * Created by PROGRAMMER2 on 2/23/2018.
 * ABZTRAK INC.
 */

interface Constants extends BaseColumns {
    String TABLE_PO = "tbl_po";
    String COLUMN_PO_NUMBER = "Number";
    String COLUMN_PO_CODE= "Code";
    String CREATE_TABLE_PO = "CREATE TABLE " + TABLE_PO + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PO_NUMBER + " TEXT NOT NULL, "
            + COLUMN_PO_CODE + " TEXT NOT NULL);";

    String TABLE_LIST = "tbl_list";
    String COLUMN_LIST_CODE = "Code";
    String COLUMN_LIST_DESC = "Desc";
    String COLUMN_LIST_CASE = "QCase";
    String COLUMN_LIST_PIECE = "QPiece";
    String CREATE_TABLE_LIST = "CREATE TABLE " + TABLE_LIST + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_LIST_CODE + " TEXT NOT NULL, "
            + COLUMN_LIST_DESC + " TEXT NOT NULL, "
            + COLUMN_LIST_CASE + " INTEGER NOT NULL, "
            + COLUMN_LIST_PIECE + " INTEGER NOT NULL);";

    String TABLE_DATA = "tbl_data";
    String COLUMN_DATA_NUMBER = "Number";
    String COLUMN_DATA_CODE = "Code";
    String COLUMN_DATA_CASE = "QCase";
    String COLUMN_DATA_PIECE = "QPiece";
    String CREATE_TABLE_DATA = "CREATE TABLE " + TABLE_DATA + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_DATA_NUMBER + " TEXT NOT NULL, "
            + COLUMN_DATA_CODE + " TEXT NOT NULL, "
            + COLUMN_DATA_CASE + " INTEGER NOT NULL, "
            + COLUMN_DATA_PIECE + " INTEGER NOT NULL);";

}
