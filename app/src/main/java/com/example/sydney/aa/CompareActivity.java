package com.example.sydney.aa;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompareActivity extends AppCompatActivity {
    DBHelper dbhelper;
    private List<Item> itemList;
    private RecyclerView recyclerView;
    private GridLayoutManager mLayoutManager;
    CoordinatorLayout coordinatorLayout;
    CSVWriter csvWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        dbhelper = new DBHelper(this);
        //RECYCLERVIEW INITIALIZATION
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_compare);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator123);

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
    void exportBaKamo(){
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

            String status;
            curSV.moveToFirst();
            while(!curSV.isAfterLast()){

                status = curSV.getString(2);
                if(status == null){
                    status = "0";
                }
                else {
                    status = "1";
                }
                String arrStr[] = {curSV.getString(0),curSV.getString(1), status};
                csvWrite.writeNext(arrStr);
                curSV.moveToNext();
            }
            csvWrite.close();
            curSV.close();

            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Export Successful", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        catch(Exception sqlEx)
        {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
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
                exportBaKamo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
