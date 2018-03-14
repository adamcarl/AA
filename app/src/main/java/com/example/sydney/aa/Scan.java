package com.example.sydney.aa;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

import static com.example.sydney.aa.Constants.COLUMN_LIST_CARD;
import static com.example.sydney.aa.Constants.COLUMN_LIST_CODE;
import static com.example.sydney.aa.Constants.COLUMN_LIST_DESC;
import static com.example.sydney.aa.Constants.COLUMN_LIST_DOCD;
import static com.example.sydney.aa.Constants.COLUMN_LIST_PONO;
import static com.example.sydney.aa.Constants.COLUMN_LIST_PONU;
import static com.example.sydney.aa.Constants.COLUMN_LIST_QUAN;
import static com.example.sydney.aa.Constants.TABLE_DATA;
import static com.example.sydney.aa.Constants.TABLE_LIST;
import static com.example.sydney.aa.R.layout.activity_scan;

/**
 * Created by PROGRAMMER2 on 5/2/2017.
 */
public class Scan extends AppCompatActivity {

    Button searchItem, mainSubmit, mainClear;
    EditText enterNumber, enterCode, quan;
    TextView desc, card;
    CoordinatorLayout coordinatorLayout;
    View dummyView;

    DBHelper dbhelper;

//    private TextWatcher myTextWatcher = new TextWatcher() {
//        @Override
//        public void afterTextChanged(Editable editable) {
//            String[] list;
//            try {
//                String mCode = enterNumber.getText().toString().trim();
//                list = dbhelper.searchForItem(mCode);
////                        printItem(mCode);
//                code.setText(list[0]);
//                desc.setText(list[1]);
//                enterNumber.removeTextChangedListener(myTextWatcher);
////                        enterBarcode.setText("");
//                        dummyView.requestFocus();
//
//                        enterNumber.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
////                                enterNumber.requestFocus();
////                                code.setText("");
////                                quantity.setText("");
//                            }
//                        }, 1000);
//                    enterNumber.addTextChangedListener(myTextWatcher);
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_scan);
        init();
        dbhelper = new DBHelper(this);

        searchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] list;
                try {
                    String mNumber = enterNumber.getText().toString().trim();
                    String mCode = enterCode.getText().toString().trim();
                    list = dbhelper.searchForItem(mNumber,mCode);
                    if(list == null){
                        enterNumber.setText("");
                        enterCode.setText("");
                        desc.setText("");
                        card.setText("");
                        enterNumber.setEnabled(true);
                        enterCode.setEnabled(true);
                        quan.setEnabled(false);
                        enterNumber.requestFocus();
                        Snackbar.make(coordinatorLayout, "NOT FOUND", Snackbar.LENGTH_LONG).show();
                    }

                    else {
                        desc.setText(list[0]);
                        card.setText(list[1]);
                        enterNumber.setEnabled(false);
                        enterCode.setEnabled(false);
                        searchItem.setEnabled(false);
                        quan.setEnabled(true);
                        quan.requestFocus();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        mainSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBaKamo();
            }
        });

        mainClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBaKamo();
            }
        });
    }

    private void init() {

        desc = (TextView) findViewById(R.id.txtDESC);
        card = (TextView) findViewById(R.id.txtCARD);

        enterNumber = (EditText) findViewById(R.id.etInputPONumber);
        enterCode = (EditText) findViewById(R.id.etInputItemCode);
        quan = (EditText) findViewById(R.id.etCase);

        searchItem = (Button) findViewById(R.id.btnSearchItem);
        mainSubmit = (Button) findViewById(R.id.btn_submit);
        mainClear = (Button) findViewById(R.id.btn_clear);

        dummyView = findViewById(R.id.dummyView);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch(id){
            case R.id.menu_load_list:
                importList();
                return true;
//            case R.id.menu_load_po:
//                importPO();
//                return true;
            case R.id.menu_finalize:
                finalizeBaKamo();
                return true;
            case R.id.menu_clear_master:
                clearDatabase(1);
                return true;
//            case R.id.menu_clear_po:
//                clearDatabase(2);
//                return true;
            case R.id.menu_clear_scan:
                clearDatabase(2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
    //IMPORTING FILE

    public void importList() {
        Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
        fileintent.setType("text/csv");
        try {
            startActivityForResult(fileintent, 1);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(coordinatorLayout, "No app found for importing the file", Snackbar.LENGTH_LONG).show();
        }
    }

    //    public void importPO() {
//        Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
//        fileintent.setType("text/csv");
//        try {
//            startActivityForResult(fileintent, 2);
//        } catch (ActivityNotFoundException e) {
//            Snackbar.make(coordinatorLayout, "No app found for importing the file", Snackbar.LENGTH_LONG).show();
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null){
            return;
        }
        switch (requestCode) {
            case 1:
                String filepath = data.getData().getPath();
                dbhelper.dbWriter.execSQL("delete from " + TABLE_LIST);
                try {
                    if (resultCode == RESULT_OK) {
                        try {
                            FileReader file = new FileReader(filepath);
                            BufferedReader buffer = new BufferedReader(file);
                            ContentValues contentValues = new ContentValues();
                            String line;
                            while ((line = buffer.readLine()) != null) {
                                StringTokenizer tokens = new StringTokenizer(line, ",");
                                String mPono = tokens.nextToken();
                                String mPonu = tokens.nextToken();
                                String mDocd = tokens.nextToken();
                                String mQuan = tokens.nextToken();
                                String mCode = tokens.nextToken();
                                String mDesc = tokens.nextToken();
                                String mCard = tokens.nextToken();

                                contentValues.put(COLUMN_LIST_PONO, mPono);
                                contentValues.put(COLUMN_LIST_PONU, mPonu);
                                contentValues.put(COLUMN_LIST_DOCD, mDocd);
                                contentValues.put(COLUMN_LIST_QUAN, mQuan);
                                contentValues.put(COLUMN_LIST_CODE, mCode);
                                contentValues.put(COLUMN_LIST_DESC, mDesc);
                                contentValues.put(COLUMN_LIST_CARD, mCard);

                                dbhelper.dbWriter.insert(TABLE_LIST, null, contentValues);
                            }
                            Snackbar.make(coordinatorLayout, "Import successful.", Snackbar.LENGTH_LONG).show();
                        } catch (SQLException e) {
                            Log.e("Error",e.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    Snackbar.make(coordinatorLayout, "Failed Import File", Snackbar.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
                break;
//            case 2:
//                String filePO = data.getData().getPath();
//                dbhelper.dbWriter.execSQL("delete from " + TABLE_PO);
//                try {
//                    if (resultCode == RESULT_OK) {
//                        try {
//                            FileReader file = new FileReader(filePO);
//                            BufferedReader buffer = new BufferedReader(file);
//                            ContentValues contentValues = new ContentValues();
//                            String line;
//                            while ((line = buffer.readLine()) != null) {
//                                StringTokenizer tokens = new StringTokenizer(line, ",");
//                                String mNumber = tokens.nextToken();
//                                String mCode = tokens.nextToken();
//
//                                contentValues.put(COLUMN_PO_NUMBER, mNumber);
//                                contentValues.put(COLUMN_PO_CODE, mCode);
//                                dbhelper.dbWriter.insert(TABLE_PO, null, contentValues);
//                            }
//                            Snackbar.make(coordinatorLayout, "Import successful.", Snackbar.LENGTH_LONG).show();
//                        } catch (SQLException e) {
//                            Log.e("Error",e.getMessage());
//                        }
//                    }
//                } catch (Exception ex) {
//                    Snackbar.make(coordinatorLayout, "Failed Import File", Snackbar.LENGTH_LONG).show();
//                    ex.printStackTrace();
//                }
//                break;
        }
    }

    void clearDatabase(int code){
        switch (code){
            case 1:
                new AlertDialog.Builder(this)
                        .setTitle("Clear Master List")
                        .setMessage("Do you really want to clear Master List?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dbhelper.dbWriter.execSQL("delete from " + TABLE_LIST);
                                Snackbar.make(coordinatorLayout, "Master List cleared", Snackbar.LENGTH_LONG).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                break;
//            case 2:
//                new AlertDialog.Builder(this)
//                        .setTitle("Clear PO")
//                        .setMessage("Do you really want to clear PO?")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                dbhelper.dbWriter.execSQL("delete from " + TABLE_PO);
//                                Snackbar.make(coordinatorLayout, "PO cleared", Snackbar.LENGTH_LONG).show();
//                            }})
//                        .setNegativeButton(android.R.string.no, null).show();
//                break;
            case 2:
                new AlertDialog.Builder(this)
                        .setTitle("Clear Data Scan")
                        .setMessage("Do you really want to clear Data Scan?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dbhelper.dbWriter.execSQL("delete from " + TABLE_DATA);
                                Snackbar.make(coordinatorLayout, "Data Scan cleared", Snackbar.LENGTH_LONG).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                break;
        }
    }
    void submitBaKamo(){

        //Return legends
        //1 = New item added.
        //2 = Old item updated.
        //3 = Quantity is above the limit.
        //5 = Failed to update quantity.
        //6 = Unknown Database error.
        //7 = Failed to add item.

        new AlertDialog.Builder(this)
                .setTitle("Submit Data")
                .setMessage("Do you really want to Submit?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        int mQuan;

                        if (quan.getText().toString().trim().equals(""))
                            mQuan = 0;
                        else
                            mQuan = Integer.parseInt(quan.getText().toString().trim());

                        int result = dbhelper.insertItem(enterNumber.getText().toString().trim(),
                                enterCode.getText().toString().trim(), mQuan);

                        if (result==1)
                            Snackbar.make(coordinatorLayout, "Added new item", Snackbar.LENGTH_LONG).show();
                        else if(result==2)
                            Snackbar.make(coordinatorLayout, "Updated old item", Snackbar.LENGTH_LONG).show();
                        else if(result==3)
                            Snackbar.make(coordinatorLayout, "Quantity not allowed", Snackbar.LENGTH_LONG).show();
                        else if(result==5)
                            Snackbar.make(coordinatorLayout, "Failed to update quantity", Snackbar.LENGTH_LONG).show();
                        else if(result==6)
                            Snackbar.make(coordinatorLayout, "Unknown database error", Snackbar.LENGTH_LONG).show();
                        else if(result==7)
                            Snackbar.make(coordinatorLayout, "Failed to add item", Snackbar.LENGTH_LONG).show();

                        enterNumber.setText("");
                        enterCode.setText("");
                        desc.setText("");
                        card.setText("");
                        quan.setText("");
                        enterNumber.setEnabled(true);
                        enterCode.setEnabled(true);
                        searchItem.setEnabled(true);
                        quan.setEnabled(false);
                        enterNumber.requestFocus();

                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    void finalizeBaKamo(){
        AlertDialog.Builder finalizeBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.custom_alertdialog_finalize, null);
        finalizeBuilder.setView(alertLayout);

        final AppCompatButton btnEnterAuthenticate = (AppCompatButton) alertLayout.findViewById(R.id.btnFinalizeSignature);
        final AppCompatButton btnCancel = (AppCompatButton) alertLayout.findViewById(R.id.btnFinalizeCancel);
        final AppCompatEditText etName = (AppCompatEditText) alertLayout.findViewById(R.id.etFinalizeName);

        final AlertDialog alertFinalize =  finalizeBuilder.create();
        alertFinalize.show();
        btnEnterAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mName = etName.getText().toString().trim();
                Intent myIntent = new Intent(Scan.this, SignatureActivity.class);
                myIntent.putExtra("name", mName);
                Scan.this.startActivity(myIntent);
                Scan.this.finish();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertFinalize.dismiss();
            }
        });
    }

    void clearBaKamo(){
        new AlertDialog.Builder(this)
                .setTitle("Clear")
                .setMessage("Do you really want to clear?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        enterNumber.setText("");
                        enterCode.setText("");
                        desc.setText("");
                        card.setText("");
                        quan.setText("");
                        enterNumber.setEnabled(true);
                        enterCode.setEnabled(true);
                        searchItem.setEnabled(true);
                        quan.setEnabled(false);
                        enterNumber.requestFocus();

                        Snackbar.make(coordinatorLayout, "Cleared", Snackbar.LENGTH_LONG).show();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }


}