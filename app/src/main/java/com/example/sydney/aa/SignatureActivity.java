package com.example.sydney.aa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dv = new DrawingView(this);
        dv.setBackgroundColor(Color.WHITE);
        dv.setDrawingCacheEnabled(true);
        setContentView(dv);

        dbhelper = new DBHelper(this);

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
            Intent myIntent = new Intent(SignatureActivity.this, Scan.class);
            SignatureActivity.this.startActivity(myIntent);
            SignatureActivity.this.finish();

        } catch (Exception sqlEx) {
            Log.e("Scan", sqlEx.getMessage(), sqlEx);
        }
    }

    private static final int CLEAR_MENU_ID = Menu.FIRST;
    private static final int SAVE_MENU_ID = Menu.FIRST+1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, CLEAR_MENU_ID, 0, "Clear").setShortcut('1', 'c');
        menu.add(0, SAVE_MENU_ID, 0, "Save").setShortcut('2', 's');

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
//                dv.saveDrawing();
                //TODO Bitmap is null. Do something.
                Bitmap bitmap = dv.getDrawingCache();
                File exportDir = new File(Environment.getExternalStorageDirectory() + "/Datascan", "");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                DateFormat dateFormat = new SimpleDateFormat("MM_dd_yy HH_mm_ss", Locale.ENGLISH);
                Date date = new Date();

                File file = new File(exportDir, "datascan_" + dateFormat.format(date) + ".jpg");
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
