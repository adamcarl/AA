package com.example.sydney.aa;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompareActivity extends AppCompatActivity {
    DBHelper dbhelper;
    CoordinatorLayout coordinatorLayout;
    CSVWriter csvWrite;
    String enteredStore = null;
    private List<Item> itemList;
    private RecyclerView recyclerView;
    private GridLayoutManager mLayoutManager;
    private AlertDialog.Builder builder = null;
    private AlertDialog alertDialog = null;

    private ProgressDialog myProgDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        dbhelper = new DBHelper(this);
        //RECYCLERVIEW INITIALIZATION
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_compare);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator123);

        createMyDialog();
        alertDialog = builder.create();
        myProgDialog = new ProgressDialog(this);

        refreshRecyclerView();
    }
    private void refreshRecyclerView() {
        //REFRESHING THE RECYCLER
        itemList = fill_with_data();
        ItemAdapter itemAdapter = new ItemAdapter(getApplication(), itemList);
        mLayoutManager = new GridLayoutManager(CompareActivity.this,1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemAdapter);
        itemAdapter.notifyDataSetChanged();
    }
    private List<Item> fill_with_data() {
        itemList = new ArrayList<>();
        itemList.clear();

        Cursor cursor = dbhelper.getAllItems();

        String itemCode,itemDescription,status;
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            itemCode = cursor.getString(0);
            itemDescription = cursor.getString(1);

            status = cursor.getString(2);
            if(status == null){
                status = "0";
            }
            else {
                status = "1";
            }

            itemList.add(new Item(itemCode,itemDescription,status));
        cursor.moveToNext();
        }
        cursor.close();
        return itemList;
    }

    void exportBaKamo(String mEnteredStore) {
        File exportDir = new File(Environment.getExternalStorageDirectory()+"/tmp", "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy HH_mm_ss");
        Date date = new Date();

        File file = new File(exportDir, "dataResult"+ dateFormat.format(date) +".csv");
        try
        {
            file.createNewFile();
            csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curSV = dbhelper.getAllItems();

            String a[] = {"Store: " + mEnteredStore};
            csvWrite.writeNext(a);
            int countBaKamo[] = dbhelper.countAll();
            String b[] = {"Item", "Pos(" + countBaKamo[0] + ")", "Collector(" + countBaKamo[1] + ")", "Remarks"};
            csvWrite.writeNext(b);

            String status, status1;
            curSV.moveToFirst();


            while(!curSV.isAfterLast()){

                status = curSV.getString(2);
                status1 = curSV.getString(0);
                if (status == null || status1 == null) {
                    status = "Unmatched";
                }
                else {
                    status = "Matched";
                }
                String arrStr[] = {curSV.getString(1), curSV.getString(0), curSV.getString(2), status};
                csvWrite.writeNext(arrStr);
                curSV.moveToNext();
            }
            csvWrite.close();
            curSV.close();

            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Export Successful", Snackbar.LENGTH_LONG);
            snackbar.show();
            myProgDialog.dismiss();

        }
        catch(Exception sqlEx)
        {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }

    private void createMyDialog() {
        builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.custom_alertdialog_enter_ip, null);
        builder.setView(alertLayout);

        final EditText serverIP = (EditText) alertLayout.findViewById(R.id.etIP);
        final Button submit = (Button) alertLayout.findViewById(R.id.btnSubmit);
        final Button cancel = (Button) alertLayout.findViewById(R.id.btnCancel);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eIp = serverIP.getText().toString();


                if (!eIp.isEmpty()) {
                    serverIP.setText("");
                    enteredStore = eIp;
                    alertDialog.dismiss();


                    if (enteredStore != null) {
                        alertDialog.dismiss();
                        myProgDialog.setMessage("Exporting...");
                        myProgDialog.show();
                    }
                    exportBaKamo(enteredStore);
                } else if (eIp.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Empty input", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Invalid store", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_export,menu);

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
            case R.id.menu_export:
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}