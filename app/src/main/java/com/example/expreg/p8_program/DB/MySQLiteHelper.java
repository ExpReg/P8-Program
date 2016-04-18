package com.example.expreg.p8_program.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.expreg.p8_program.Model.AccelerometerMeasure;
import com.example.expreg.p8_program.Model.SensorMeasure;

import java.util.LinkedList;
import java.util.List;

public class MySQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "SensorData";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create sensor table
        String CREATE_SENSOR_TABLE = "CREATE TABLE sensor ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip INTEGER, " +
                "acc_x REAL, " +
                "acc_y REAL," +
                "acc_z REAL," +
                "created_at TEXT)";

        // create sensor table
        db.execSQL(CREATE_SENSOR_TABLE);

        // SQL statement to create detection table
        String CREATE_DETECTION_TABLE = "CREATE TABLE detection ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip INTEGER, " +
                "type TEXT, " +
                "acc_x REAL, " +
                "acc_y REAL," +
                "acc_z REAL," +
                "created_at TEXT)";

        // create detection table
        db.execSQL(CREATE_DETECTION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older sensor table if existed
        db.execSQL("DROP TABLE IF EXISTS sensor");
        db.execSQL("DROP TABLE IF EXISTS detection");

        // create fresh tables
        this.onCreate(db);
    }

    public void deleteDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS sensor");
        db.execSQL("DROP TABLE IF EXISTS detection");

        this.onCreate(db);
    }

    //---------------------------------------------------------------------

    /**
     * CRUD operations (create "add", read "get", update, delete) measure + get all measures + delete all measures
     */

    // Sensor table name
    private static final String TABLE_SENSOR = "sensor";
    private static final String TABLE_DETECTION = "detection";

    // Sensor Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TRIP = "trip";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ACC_X = "acc_x";
    private static final String KEY_ACC_Y = "acc_y";
    private static final String KEY_ACC_Z = "acc_z";
    private static final String KEY_CREATED_AT = "created_at";

    private static final String[] SENSOR_COLUMNS = {KEY_ID,KEY_TRIP,KEY_ACC_X,KEY_ACC_Y,KEY_ACC_Z,KEY_CREATED_AT};
    private static final String[] DETECTION_COLUMNS = {KEY_ID,KEY_TRIP,KEY_TYPE,KEY_ACC_X,KEY_ACC_Y,KEY_ACC_Z,KEY_CREATED_AT};

    public void addMeasure(AccelerometerMeasure sensorMeasure){
        Log.d("addMeasure", sensorMeasure.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        this.addMeasure(sensorMeasure, db);

        // 4. close
        db.close();
    }

    public void addMeasure(AccelerometerMeasure sensorMeasure, SQLiteDatabase db) {
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_TRIP, sensorMeasure.getTrip());
        values.put(KEY_ACC_X, sensorMeasure.getAcc_x());
        values.put(KEY_ACC_Y, sensorMeasure.getAcc_y());
        values.put(KEY_ACC_Z, sensorMeasure.getAcc_z());
        values.put(KEY_CREATED_AT, sensorMeasure.getDateTime());

        // 3. insert
        db.insert(TABLE_SENSOR, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
    }

    public void addMeasures(List<AccelerometerMeasure> measures) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        for (AccelerometerMeasure m : measures) {
            this.addMeasure(m, db);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addDetection(AccelerometerMeasure sensorMeasure, String type) {
        Log.d("addDetection", type + " " + sensorMeasure.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_TRIP, sensorMeasure.getTrip());
        values.put(KEY_TYPE, type);
        values.put(KEY_ACC_X, sensorMeasure.getAcc_x());
        values.put(KEY_ACC_Y, sensorMeasure.getAcc_y());
        values.put(KEY_ACC_Z, sensorMeasure.getAcc_z());
        values.put(KEY_CREATED_AT, sensorMeasure.getDateTime());

        // 3. insert
        db.insert(TABLE_DETECTION, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public AccelerometerMeasure getMeasure(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_SENSOR, // a. table
                        SENSOR_COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build measure object
        AccelerometerMeasure sensorMeasure = new AccelerometerMeasure();
        sensorMeasure.setId(Integer.parseInt(cursor.getString(0)));
        sensorMeasure.setTrip(cursor.getInt(1));
        sensorMeasure.setAcc_x(cursor.getFloat(2));
        sensorMeasure.setAcc_y(cursor.getFloat(3));
        sensorMeasure.setAcc_z(cursor.getFloat(4));
        sensorMeasure.setCreatedAtFromDB(cursor.getString(5));

        Log.d("getMeasure(" + id + ")", sensorMeasure.toString());

        // 5. return measures
        return sensorMeasure;
    }

    // Get latest trip
    public int getLastTrip() {
        int lastTrip = 0;

        String query = "SELECT MAX(trip) FROM " + TABLE_SENSOR;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null)
            cursor.moveToFirst();

        lastTrip = cursor.getInt(0);

        return lastTrip;
    }

    // Get All Measures
    public List<AccelerometerMeasure> getAllMeasures() {
        List<AccelerometerMeasure> sensors = new LinkedList<AccelerometerMeasure>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_SENSOR;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        AccelerometerMeasure sensorMeasure = null;
        if (cursor.moveToFirst()) {
            do {
                sensorMeasure = new AccelerometerMeasure();
                sensorMeasure.setId(Integer.parseInt(cursor.getString(0)));
                sensorMeasure.setTrip(cursor.getInt(1));
                sensorMeasure.setAcc_x(cursor.getFloat(2));
                sensorMeasure.setAcc_y(cursor.getFloat(3));
                sensorMeasure.setAcc_z(cursor.getFloat(4));
                sensorMeasure.setCreatedAtFromDB(cursor.getString(5));
                // Add measure to measures
                sensors.add(sensorMeasure);
            } while (cursor.moveToNext());
        }

        Log.d("getAllMeasures()", sensors.toString());

        // return measures
        return sensors;
    }

    // Updating single measure
    public int updateMeasure(AccelerometerMeasure sensorMeasure) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("trip", sensorMeasure.getTrip());
        values.put("acc_x", sensorMeasure.getAcc_x());
        values.put("acc_y", sensorMeasure.getAcc_y());
        values.put("acc_z", sensorMeasure.getAcc_z());
        //values.put("created_at", sensorMeasure.getDateTime());

        // 3. updating row
        int i = db.update(TABLE_SENSOR, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(sensorMeasure.getId()) }); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single measure
    public void deleteMeasure(SensorMeasure sensorMeasure) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_SENSOR,
                KEY_ID + " = ?",
                new String[]{ String.valueOf(sensorMeasure.getId()) });

        // 3. close
        db.close();

        Log.d("deleteMeasure", sensorMeasure.toString());

    }
}
