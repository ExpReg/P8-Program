package com.example.expreg.p8_program.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.expreg.p8_program.Model.SensorMeasure;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MySQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "SensorData";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_SENSOR_TABLE = "CREATE TABLE sensor ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "acc_x REAL, "+
                "acc_y REAL," +
                "acc_z REAL," +
                "date TEXT)";

        // create books table
        db.execSQL(CREATE_SENSOR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS sensor");

        // create fresh books table
        this.onCreate(db);
    }
    //---------------------------------------------------------------------

    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */

    // Books table name
    private static final String TABLE_SENSOR = "sensor";

    // Books Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ACC_X = "acc_x";
    private static final String KEY_ACC_Y = "acc_y";
    private static final String KEY_ACC_Z = "acc_z";
    private static final String KEY_DATE = "date";

    private static final String[] COLUMNS = {KEY_ID,KEY_ACC_X,KEY_ACC_Y,KEY_ACC_Z,KEY_DATE};

    public void addMeasure(SensorMeasure sensorMeasure){
        Log.d("addMeasure", sensorMeasure.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_ACC_X, sensorMeasure.getAcc_x()); // get title
        values.put(KEY_ACC_Y, sensorMeasure.getAcc_y()); // get author
        values.put(KEY_ACC_Z, sensorMeasure.getAcc_z()); // get author
        values.put(KEY_DATE, sensorMeasure.getDate().toString());

        // 3. insert
        db.insert(TABLE_SENSOR, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public SensorMeasure getSensor(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_SENSOR, // a. table
                        COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build book object
        SensorMeasure sensorMeasure = new SensorMeasure();
        sensorMeasure.setId(Integer.parseInt(cursor.getString(0)));
        sensorMeasure.setAcc_x(cursor.getFloat(1));
        sensorMeasure.setAcc_y(cursor.getFloat(2));
        sensorMeasure.setAcc_z(cursor.getFloat(3));
        // TODO find non-deprecated method
        sensorMeasure.setDate(new Date(cursor.getString(4)));

        Log.d("getSensor(" + id + ")", sensorMeasure.toString());

        // 5. return book
        return sensorMeasure;
    }

    // Get All Books
    public List<SensorMeasure> getAllSensors() {
        List<SensorMeasure> sensors = new LinkedList<SensorMeasure>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_SENSOR;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        SensorMeasure sensorMeasure = null;
        if (cursor.moveToFirst()) {
            do {
                sensorMeasure = new SensorMeasure();
                sensorMeasure.setId(Integer.parseInt(cursor.getString(0)));
                sensorMeasure.setAcc_x(cursor.getFloat(1));
                sensorMeasure.setAcc_y(cursor.getFloat(2));
                sensorMeasure.setAcc_z(cursor.getFloat(3));
                // TODO find non-deprecated method
                sensorMeasure.setDate(new Date(cursor.getString(4)));

                // Add book to books
                sensors.add(sensorMeasure);
            } while (cursor.moveToNext());
        }

        Log.d("getAllSensors()", sensors.toString());

        // return books
        return sensors;
    }

    // Updating single book
    public int updateSensor(SensorMeasure sensorMeasure) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("acc_x", sensorMeasure.getAcc_x()); // get title
        values.put("acc_y", sensorMeasure.getAcc_y()); // get author
        values.put("acc_z", sensorMeasure.getAcc_z()); // get author

        // 3. updating row
        int i = db.update(TABLE_SENSOR, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(sensorMeasure.getId()) }); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single book
    public void deleteSensor(SensorMeasure sensorMeasure) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_SENSOR,
                KEY_ID + " = ?",
                new String[]{ String.valueOf(sensorMeasure.getId()) });

        // 3. close
        db.close();

        Log.d("deleteSensor", sensorMeasure.toString());

    }
}