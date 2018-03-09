package com.example.sydney.aa;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignatureActivity extends AppCompatActivity {

    DrawingView dv ;
    private Paint mPaint;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Paint circlePaint;
    private Path circlePath;
    Context context;
    String value;
    CSVWriter csvWrite;
    DBHelper dbhelper;

    private boolean isConnected=false;
    private Socket socket;
    private OutputStream os = null;
    private BufferedInputStream bis = null;
    private AlertDialog.Builder builder = null;
    private AlertDialog alertDialog = null;
    private String enteredIP = null;
    private ProgressDialog myProgDialog = null;
    private String fileToSend = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dv = new DrawingView(this);
        dv.setBackgroundColor(Color.WHITE);
        dv.setDrawingCacheEnabled(true);
        setContentView(dv);

        dbhelper = new DBHelper(this);

        createMyDialog();
        alertDialog = builder.create();
        myProgDialog = new ProgressDialog(this);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3);

        Intent intent = getIntent();
        value = intent.getStringExtra("name");
    }

    public class DrawingView extends View {

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }

        public void clearDrawing() {
            setDrawingCacheEnabled(false);
            // don't forget that one and the match below,
            // or you just keep getting a duplicate when you save.

            onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
            invalidate();

            setDrawingCacheEnabled(true);
        }
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    void exportBaKamo(String mName) {
        File exportDir = new File(Environment.getExternalStorageDirectory() + "/Datascan", "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        DateFormat dateFormat = new SimpleDateFormat("MM_dd_yy HH_mm_ss", Locale.ENGLISH);
        Date date = new Date();

        File file = new File(exportDir, "datascan_" + dateFormat.format(date) + ".csv");
        try {
            file.createNewFile();
            csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curSV = dbhelper.exportAllItems();
            csvWrite.writeNext(new String[] {mName} );
            curSV.moveToFirst();
            while (!curSV.isAfterLast()) {
                String arrStr[] = {curSV.getString(0),curSV.getString(1),curSV.getString(2),curSV.getString(3)};
                csvWrite.writeNext(arrStr);
                curSV.moveToNext();
            }
            csvWrite.close();
            curSV.close();
            Toast.makeText(SignatureActivity.this,"Finalize Complete",Toast.LENGTH_SHORT);
        } catch (Exception sqlEx) {
            Log.e("Scan", sqlEx.getMessage(), sqlEx);
        }
    }

    private static final int CLEAR_MENU_ID = Menu.FIRST;
    private static final int SAVE_MENU_ID = Menu.FIRST+1;
    private static final int SAVESEND_MENU_ID = Menu.FIRST+2;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, CLEAR_MENU_ID, 0, "Clear").setShortcut('1', 'c');
        menu.add(0, SAVE_MENU_ID, 0, "Save").setShortcut('2', 's');
        menu.add(0, SAVESEND_MENU_ID, 0, "Save and Send").setShortcut('3', 'q');
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case CLEAR_MENU_ID:
                dv.clearDrawing();
                return true;
            case SAVE_MENU_ID:
                saveFunction();
                Intent myIntent = new Intent(SignatureActivity.this, Scan.class);
                SignatureActivity.this.startActivity(myIntent);
                SignatureActivity.this.finish();
                return true;
            case SAVESEND_MENU_ID:
                alertDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            Toast.makeText(context,isConnected?"Connected to server!":"Error while connecting",Toast.LENGTH_LONG).show();
            try {
                if(isConnected) {
                    try {
                        //SEND FILES
                        File sendDir = new File(Environment.getExternalStorageDirectory() + "/Datascan", "");
                        String mFilename = fileToSend+".csv";
                        File myFile = new File (sendDir,mFilename);
                        byte [] mybytearray  = new byte [(int)myFile.length()]; //(int)myFile.length()
                        BufferedInputStream bis;

//                        String mFileImg = fileToSend+".jpg";
//                        File myFileImg = new File (sendDir,mFileImg);
//                        byte [] mybytearrayImg  = new byte [(int)myFileImg.length()]; //(int)myFile.length()
//                        BufferedInputStream bisImg;
                        try{
                            bis = new BufferedInputStream(new FileInputStream(myFile));
                            bis.read(mybytearray, 0, mybytearray.length);
                            OutputStream os = socket.getOutputStream();
                            os.write(mybytearray, 0, mybytearray.length);
                            os.flush();
                            os.close();

//                            bisImg = new BufferedInputStream(new FileInputStream(myFileImg));
//                            bisImg.read(mybytearrayImg, 0, mybytearrayImg.length);
//                            OutputStream osImg = socket.getOutputStream();
//                            osImg.write(mybytearrayImg, 0, mybytearrayImg.length);
//                            osImg.flush();
//                            osImg.close();

                            myProgDialog.dismiss();
                            Intent myIntent = new Intent(SignatureActivity.this, Scan.class);
                            SignatureActivity.this.startActivity(myIntent);
                            SignatureActivity.this.finish();
                        }catch (FileNotFoundException fnfe){
                            fnfe.printStackTrace();
                            myProgDialog.dismiss();
                            }

                    } catch (IOException io){
                        Log.e("Sending Data: ", "Error sending data I/O: ", io);
                        myProgDialog.dismiss();
                        Toast.makeText(SignatureActivity.this, "Export Failed!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SignatureActivity.this, "Export Failed!", Toast.LENGTH_SHORT).show();
                }
            } catch (NullPointerException e){
                myProgDialog.dismiss();
                Log.e("Sending Data: ", "No files has been sent!", e);
                Toast.makeText(context,"No files has been sent!",Toast.LENGTH_LONG).show();
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
                saveFunction();

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
                    Toast.makeText(SignatureActivity.this, "Empty input!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SignatureActivity.this, "Invalid IP!", Toast.LENGTH_SHORT).show();
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
    void saveFunction(){
        Bitmap bitmap = dv.getDrawingCache();
        File exportDir = new File(Environment.getExternalStorageDirectory() + "/Datascan", "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        DateFormat dateFormat = new SimpleDateFormat("MM_dd_yy HH_mm_ss", Locale.ENGLISH);
        Date date = new Date();

        File file = new File(exportDir, "datascan_" + dateFormat.format(date) + ".jpg");
        fileToSend = "datascan_" + dateFormat.format(date);
        try {
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, ostream);
            ostream.flush();
            ostream.close();
            dv.invalidate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }finally
        {
            dv.setDrawingCacheEnabled(false);
            exportBaKamo(value);
        }
    }
}
