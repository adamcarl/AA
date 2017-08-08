package com.example.sydney.aa;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by PROGRAMMER2 on 5/2/2017.
 */
public class AddItem extends AppCompatActivity {

    EditText barcode,description;
    Button add, importExcel, exportExcel;

    DBHelper dbhelper = new DBHelper(this);

    //INTEGRATING EXCEL UPLOAD
    TextView resultMsg;
//    ListView myListview;
//    ListAdapter adapter;
    ArrayList<HashMap<String, String>> myList;
    public static final int requestcode = 1;

    //VARIABLES WHEN CONNECTING TO SERVER IN PC
    Context context;
    private boolean isConnected=false;
    private Socket socket;

    private OutputStream os = null;
    private BufferedInputStream bis = null;

    private AlertDialog.Builder builder = null;
    private AlertDialog alertDialog = null;
    private String enteredIP = null;

    private ProgressDialog myProgDialog = null;

    Cursor cursor = null;

    Date currDate = new Date();
    public static final SimpleDateFormat sdfFormat = new SimpleDateFormat( "dd-MM-yyyy hh:mm a" );
    String dateToStr = sdfFormat.format(currDate);
    Date strToDate = sdfFormat.parse(dateToStr);
    String dateSaved = null;

    public AddItem() throws ParseException {
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        context = this; //save the context to show Toast messages

        init();


        //CREATE DIALOG
        createMyDialog();
        alertDialog = builder.create();

        //ADDING RECORDS TO TABLE ITEM
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                SQLiteDatabase sqldb = dbhelper.getWritableDatabase();
//                cursor = sqldb.rawQuery("select * from item", null);
                cursor = dbhelper.queryDataRead("select * from item");

                try {
                    if(barcode.getText().toString().trim().isEmpty() || description.getText().toString().trim().isEmpty()){
                        //Textview Result
                        resultMsg.setVisibility(View.VISIBLE);
                        resultMsg.setText("Fill all fields!");
                        resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultOrange));

                        resultMsg.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                resultMsg.setVisibility(View.GONE);
                            }
                        }, 4000);
                    }
                    else if(!barcode.getText().toString().trim().isEmpty() && !description.getText().toString().trim().isEmpty()){
                        Item item = new Item();
                        String bcode = barcode.getText().toString().trim();
                        String des = description.getText().toString().trim();
                        int qty = 0;

                        int result = dbhelper.searchForDuplicate(bcode);

                        if(result > 0){
                            //Textview Result
                            resultMsg.setVisibility(View.VISIBLE);
                            resultMsg.setText("Duplicate Barcode!");
                            resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultRed));

                            resultMsg.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    resultMsg.setVisibility(View.GONE);
                                }
                            }, 4000);

                            barcode.setText("");
                        }else if(result <= 0){
                            item.setBarcode(bcode);
                            item.setDescription(des);
//                            item.setQuantity(qty);
//                            dbhelper.insertItem(item);

//                            cursor = sqldb.rawQuery("select * from item", null);

                            //Textview Result
                            resultMsg.setVisibility(View.VISIBLE);
                            resultMsg.setText("Successfully Added!");
                            resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultGreen));

                            resultMsg.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    resultMsg.setVisibility(View.GONE);
                                }
                            }, 4000);
                        }


                        barcode.setText("");
                        description.setText("");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        //IMPORTING FILE

        importExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
                fileintent.setType("text/csv");
                try {
                    startActivityForResult(fileintent, requestcode);
                } catch (ActivityNotFoundException e) {
                    //Textview Result
                    resultMsg.setVisibility(View.VISIBLE);
                    resultMsg.setText("No app found for importing the file.");
                    resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultRed));

                    resultMsg.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resultMsg.setVisibility(View.GONE);
                        }
                    }, 4000);
                }
            }
        });
        //EXPORTING FILE
        exportExcel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                cursor = dbhelper.queryDataRead("select * from item");
                //WRITE FILE TO EXTERNAL STORAGE
                try {
                    int rowcount = 0;
                    int colcount = 0;
                    File sdCardDir = Environment.getExternalStorageDirectory();
                    String filename = strToDate + ".txt"; // the name of the file to export with
                    dateSaved = filename;
                    File saveFile = new File(sdCardDir, filename);
                    FileWriter fw = new FileWriter(saveFile);
                    BufferedWriter bw = new BufferedWriter(fw);
                    rowcount = cursor.getCount();
                    colcount = cursor.getColumnCount();
//                    if (rowcount > 0) {
//                        cursor.moveToFirst();
//                        for (int i = 1; i < colcount; i++) {
//                            if (i != colcount - 1) {
//                                bw.write(cursor.getColumnName(i) + "\t");
//                            } else {
//                                bw.write(cursor.getColumnName(i));
//                            }
//                        }
//                        bw.newLine();
                    //I COMMENTED THE ABOVE CODE SNIPPET TO REMOVE THE FIELD NAMES WHEN EXPORTING FILE
                        for (int i = 0; i < rowcount; i++) {
                            cursor.moveToPosition(i);
                            for (int j = 1; j < colcount; j++) { //I'VE CHANGED THE VALUE OF VARIABLE int j to 1. (original value 0)
                                if (j != colcount - 1)
                                bw.write(cursor.getString(j) + "\t");
                                else
                                bw.write(cursor.getString(j));
                            }
                            bw.newLine();
//                        }
                            bw.flush();
                            resultMsg.setText("");
                    }


                } catch (Exception ex) {
                        //Textview Result
                        resultMsg.setVisibility(View.VISIBLE);
                        resultMsg.setText("Can't write file!");
                        resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultRed));

                        resultMsg.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                resultMsg.setVisibility(View.GONE);
                            }
                        }, 4000);
                        //resultMsg.setText(ex.getMessage().toString());
                }
                //CHECKING FOR SERVER IP AND AFTER IP IS RECEIVED, PROCEED TO SEND FILE IN SERVER

                alertDialog.show();

            }
        });
//        myList = dbhelper.getAllProducts();
//        if (myList.size() != 0) {
//            ListView lv = getListView();
//            adapter = new SimpleAdapter(AddItem.this, myList,
//                    R.layout.activity_excel_items, new String[]{"barcode", "description", "quantity"}, new int[]{
//                    R.id.txtItemBarcode, R.id.txtItemDescription, R.id.txtItemQuantity});
//            setListAdapter(adapter);
//            uploadResultMsg.setText("");
//        }
    }

       @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null){
            return;
        }
        switch (requestCode) {
            case requestcode:
                String filepath = data.getData().getPath();
                dbhelper = new DBHelper(this);
                SQLiteDatabase db = dbhelper.getWritableDatabase();
                String tableName = "item";
                db.execSQL("delete from " + tableName);
                try {
                    if (resultCode == RESULT_OK) {
                        try {
                            FileReader file = new FileReader(filepath);
                            BufferedReader buffer = new BufferedReader(file);
                            ContentValues contentValues = new ContentValues();
                            String line = "";
                            db.beginTransaction();
                            while ((line = buffer.readLine()) != null) {
                                String[] str = line.split("\t",3);  // defining 3 columns with null or blank field //values acceptance
                                //id, barcode,description,quantity
                                String barcode = str[0].toString();
                                String description = str[1].toString();
                                String quantity = str[2].toString();
                                contentValues.put("barcode", barcode);
                                contentValues.put("description", description);
                                contentValues.put("quantity", quantity);
                                db.insert(tableName, null, contentValues);
                            }

                            db.setTransactionSuccessful();
                            cursor = db.rawQuery("select * from item", null);
                            db.endTransaction();

                            //Textview Result
                            resultMsg.setVisibility(View.VISIBLE);
                            resultMsg.setText("Successfully Imported File\n" + filepath);
                            resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultGreen));
//
//                            resultMsg.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    resultMsg.setVisibility(View.GONE);
//                                }
//                            }, 4000);
                        } catch (SQLException e) {
                            Log.e("Error",e.getMessage().toString());

                        } catch (IOException e) {
                            if (db.inTransaction()){
                                //Textview Result
                                resultMsg.setVisibility(View.VISIBLE);
                                resultMsg.setText("Failed Import File");
                                resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultRed));

                                resultMsg.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        resultMsg.setVisibility(View.GONE);
                                    }
                                }, 4000);

                                db.endTransaction();
                            }
                        }
                    } else {
                        if (db.inTransaction()){
                            //Textview Result
                            resultMsg.setVisibility(View.VISIBLE);
                            resultMsg.setText("Only CSV files allowed!");
                            resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.myColorResultRed));

                            resultMsg.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    resultMsg.setVisibility(View.GONE);
                                }
                            }, 4000);
                            db.endTransaction();
                        }
                    }
                } catch (Exception ex) {
                    if (db.inTransaction()) {
                        //Textview Result
                        resultMsg.setVisibility(View.VISIBLE);
                        resultMsg.setText("Failed Import File!");
                        resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.myColorResultRed));

                        resultMsg.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                resultMsg.setVisibility(View.GONE);
                            }
                        }, 4000);
                        db.endTransaction();
                    }
                }
        }

//        if (myList.size() != 0) {
//            ListView lv = getListView();
//            adapter = new SimpleAdapter(AddItem.this, myList,
//                    R.layout.activity_excel_items, new String[]{"barcode", "description", "quantity"}, new int[]{
//                    R.id.txtItemBarcode, R.id.txtItemDescription, R.id.txtItemQuantity});
//            setListAdapter(adapter);
//            uploadResultMsg.setText("Data Imported");
//        }
    }

    private void init() {
        barcode = (EditText) findViewById(R.id.etBarcode);
        description = (EditText) findViewById(R.id.etDescription);
        importExcel = (Button) findViewById(R.id.btnExcel);
//        myListview = getListView();
        resultMsg = (TextView)findViewById(R.id.txtResultMsg);
        exportExcel = (Button) findViewById(R.id.btnExport);

        myProgDialog = new ProgressDialog(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(isConnected && os!=null) {
            try {
                socket.close(); //close socket
            } catch (IOException e) {
                Log.e("MARKEY'S_SERVER", "Error in closing socket", e);
            }
        }
    }

    public class ConnectPhoneTask extends AsyncTask<String,Void,Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = true;
            try {

                InetAddress serverAddr = InetAddress.getByName(params[0]);
                socket = new Socket(serverAddr, Constants.SERVER_PORT);//Open socket on server IP and port
            } catch (IOException e) {
                Log.e("Connecting to device : ", "Error while connecting. . .", e);
                result = false;
                myProgDialog.dismiss();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            isConnected = result;
            Toast.makeText(context,isConnected?"Connected to server!":"Error while connecting", Toast.LENGTH_LONG).show();
            try {
                if(isConnected) {
                    try {
                        //SEND FILES
                        File mSdCardDir = Environment.getExternalStorageDirectory();
                        String mFilename = dateSaved;
                        File myFile = new File(mSdCardDir,mFilename);
                        byte [] mybytearray  = new byte [(int)myFile.length()]; //(int)myFile.length()
                        BufferedInputStream bis;
                        try{
                            bis = new BufferedInputStream(new FileInputStream(myFile));
                            bis.read(mybytearray, 0, mybytearray.length);
                            OutputStream os = socket.getOutputStream();
                            os.write(mybytearray, 0, mybytearray.length);
                            os.flush();

                            //Textview Result
                            resultMsg.setVisibility(View.VISIBLE);
                            resultMsg.setText("Exported Successfully!");
                            resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultGreen));

                            resultMsg.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    resultMsg.setVisibility(View.GONE);
                                }
                            }, 4000);

                            myProgDialog.dismiss();
                        }catch (FileNotFoundException fnfe){
                            fnfe.printStackTrace();
                            myProgDialog.dismiss();

                            //Textview Result
                            resultMsg.setVisibility(View.VISIBLE);
                            resultMsg.setText("Export Failed!");
                            resultMsg.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.color.myColorResultGreen));

                            resultMsg.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    resultMsg.setVisibility(View.GONE);
                                }
                            }, 4000);
                        }

                    } catch (IOException io){
                        Log.e("Sending Data: ", "Error sending data I/O: ", io);
                        myProgDialog.dismiss();
                        Toast.makeText(AddItem.this, "Export Failed!", Toast.LENGTH_SHORT).show();
                    } finally {
                        if(socket != null){
                            try {
                                socket.close();
                                myProgDialog.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                                myProgDialog.dismiss();
                            }
                        }
                    }
                } else {
                    myProgDialog.dismiss();
                    Toast.makeText(AddItem.this, "Export Failed!", Toast.LENGTH_SHORT).show();
                }
            } catch (NullPointerException e){
                myProgDialog.dismiss();
                Log.e("Sending Data: ", "No files has been sent!", e);
                Toast.makeText(context,"No files has been sent!", Toast.LENGTH_LONG).show();
            }
            finally {
                if (bis != null) try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (os != null) try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket !=null) try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void createMyDialog(){
        builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.custom_alertdialog_enter_ip, null);
        builder.setView(alertLayout);

        final EditText serverIP = (EditText)alertLayout.findViewById(R.id.etIP) ;
        final Button submit = (Button)alertLayout.findViewById(R.id.btnSubmit) ;
        final Button cancel = (Button)alertLayout.findViewById(R.id.btnCancel) ;


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eIp = serverIP.getText().toString();



                if(!eIp.isEmpty()){
                    serverIP.setText("");
                    enteredIP = eIp;
                    alertDialog.dismiss();


                if(enteredIP != null) {
                    ConnectPhoneTask connectPhoneTask = new ConnectPhoneTask();
                    connectPhoneTask.execute(enteredIP); //try to connect to server in another thread
                    alertDialog.dismiss();
                    myProgDialog.setMessage("Connecting...");
                    myProgDialog.show();
                }
                               }
                else if(eIp.isEmpty()){
                    Toast.makeText(AddItem.this, "Empty input!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(AddItem.this, "Invalid IP!", Toast.LENGTH_SHORT).show();
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
}
