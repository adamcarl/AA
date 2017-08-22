package com.example.sydney.aa;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import static com.example.sydney.aa.DBHelper.COLUMN_BARCODE_IMPORT;
import static com.example.sydney.aa.DBHelper.COLUMN_DESCRIPTION_IMPORT;
import static com.example.sydney.aa.DBHelper.TABLE_ITEM_IMPORT;

/**
 * Created by PROGRAMMER2 on 5/2/2017.
 */
public class Scan extends AppCompatActivity {

    public static final int requestcode = 1;
    EditText enterBarcode;
    TextView code;
    CoordinatorLayout coordinatorLayout;
//    TextView quantity;
    View dummyView;
    CSVWriter csvWrite;

    DBHelper dbhelper;
    private TextWatcher myTextWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable editable) {
            try {
                String mCode = enterBarcode.getText().toString().trim();
                try {
                    dbhelper.insertItem(mCode);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
//                        printItem(mCode);
                code.setText(mCode);
                        enterBarcode.removeTextChangedListener(myTextWatcher);
                        enterBarcode.setText("");
                        dummyView.requestFocus();

                        enterBarcode.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                enterBarcode.requestFocus();
                                code.setText("");
//                                quantity.setText("");
                            }
                        }, 1000);
                    enterBarcode.addTextChangedListener(myTextWatcher);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        //CASTING VIEWS
        init();
        dbhelper = new DBHelper(this);
        //CREATE DIALOG FOR ADMIN LOGIN
        automaticScan();
        dbhelper.deleteAll();
    }

    private void init() {
        enterBarcode = (EditText) findViewById(R.id.etInputBarCode);
        code = (TextView) findViewById(R.id.txtCode);
//        quantity = (TextView) findViewById(R.id.txtQuantity);
        dummyView = findViewById(R.id.dummyView);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    }


//    public void mUpdate(int id,int newCount){
//        dbhelper.updateQuantity(id,newCount);
//        dbhelper.close();
//    }

    private void automaticScan() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        enterBarcode.addTextChangedListener(myTextWatcher);
        imm.hideSoftInputFromWindow(enterBarcode.getWindowToken(), 0);
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
            case R.id.menu_preexport:
                exportBaKamo();
                return true;
            case R.id.menu_add:
                importAndCompare();
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

        public void importAndCompare() {
            Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
            fileintent.setType("text/csv");
            try {
                startActivityForResult(fileintent, requestcode);

            } catch (ActivityNotFoundException e) {
                //Textview Result
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "No app found for importing the file", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null){
            return;
        }
        switch (requestCode) {
            case requestcode:
                String filepath = data.getData().getPath();
                dbhelper.dbWriter.execSQL("delete from " + TABLE_ITEM_IMPORT);
                try {
                    if (resultCode == RESULT_OK) {
                        try {
                            FileReader file = new FileReader(filepath);
                            BufferedReader buffer = new BufferedReader(file);
                            ContentValues contentValues = new ContentValues();
                            String line = "";
                            while ((line = buffer.readLine()) != null) {
                                StringTokenizer tokens = new StringTokenizer(line, ",");
                                String barcode = tokens.nextToken();
                                String description = tokens.nextToken();
                                //id, barcode,description,quantity
                                contentValues.put(COLUMN_BARCODE_IMPORT, barcode);
                                contentValues.put(COLUMN_DESCRIPTION_IMPORT, description);
                                dbhelper.dbWriter.insert(TABLE_ITEM_IMPORT, null, contentValues);
                            }

                            //Textview Result
//                            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Successfully Imported File\n" + filepath, Snackbar.LENGTH_LONG);
//                            snackbar.show();

                            Intent intent = new Intent(this, CompareActivity.class);
                            startActivity(intent);
//                            resultMsg.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    resultMsg.setVisibility(View.GONE);
//                                }
//                            }, 4000);
                        } catch (SQLException e) {
                            Log.e("Error",e.getMessage());
                        }
                    }
                } catch (Exception ex) {
                        //Textview Result
                        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Failed Import File", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    ex.printStackTrace();
                }
        }
    }

    void exportBaKamo() {
        File exportDir = new File(Environment.getExternalStorageDirectory() + "/tmp", "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy HH_mm_ss");
        Date date = new Date();

        File file = new File(exportDir, "collection_" + dateFormat.format(date) + ".csv");
        try {
            file.createNewFile();
            csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curSV = dbhelper.exportAllItems();

            curSV.moveToFirst();
            while (!curSV.isAfterLast()) {
                String arrStr[] = {curSV.getString(0)};
                csvWrite.writeNext(arrStr);
                curSV.moveToNext();
            }
            csvWrite.close();
            curSV.close();

            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Export Successful", Snackbar.LENGTH_LONG);
            snackbar.show();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }

}