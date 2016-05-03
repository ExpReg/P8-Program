package com.example.expreg.p8_program.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
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
                "lat REAL, " +
                "lon REAL," +
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
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";

    private static final String[] SENSOR_COLUMNS = {KEY_ID,KEY_TRIP,KEY_ACC_X,KEY_ACC_Y,KEY_ACC_Z,KEY_CREATED_AT};
    private static final String[] DETECTION_COLUMNS = {KEY_ID,KEY_TRIP,KEY_TYPE,KEY_LAT,KEY_LON,KEY_CREATED_AT};


    // Insertions
    public void addMeasure(AccelerometerMeasure sensorMeasure){
        SQLiteDatabase db = this.getWritableDatabase();
        this.addMeasure(sensorMeasure, db);
        db.close();
    }

    public void addMeasure(AccelerometerMeasure sensorMeasure, SQLiteDatabase db) {
        Log.d("addMeasure", sensorMeasure.toString());

        ContentValues values = new ContentValues();
        values.put(KEY_TRIP, sensorMeasure.getTrip());
        values.put(KEY_ACC_X, sensorMeasure.getAcc_x());
        values.put(KEY_ACC_Y, sensorMeasure.getAcc_y());
        values.put(KEY_ACC_Z, sensorMeasure.getAcc_z());
        values.put(KEY_CREATED_AT, sensorMeasure.getDateTime());

        db.insert(TABLE_SENSOR, null, values);
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

    public void addDetection(int trip, Location loc, String type, String time) {
        Log.d("addDetection", type + " " + loc.toString());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TRIP, trip);
        values.put(KEY_TYPE, type);
        values.put(KEY_LAT, loc.getLatitude());
        values.put(KEY_LON, loc.getLongitude());
        values.put(KEY_CREATED_AT, time);

        db.insert(TABLE_DETECTION, null, values);

        db.close();
    }

    // Extractions
    public int getLastTrip() {
        int lastTrip = 0;

        String query = "SELECT MAX(trip) FROM " + TABLE_SENSOR;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();
            lastTrip = cursor.getInt(0);
            cursor.close();
        }

        return lastTrip;
    }

    public AccelerometerMeasure getMeasure(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_SENSOR, // a. table
                        SENSOR_COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        AccelerometerMeasure sensorMeasure = new AccelerometerMeasure();
        if (cursor != null) {
            cursor.moveToFirst();

            sensorMeasure.setId(Integer.parseInt(cursor.getString(0)));
            sensorMeasure.setTrip(cursor.getInt(1));
            sensorMeasure.setAcc_x(cursor.getFloat(2));
            sensorMeasure.setAcc_y(cursor.getFloat(3));
            sensorMeasure.setAcc_z(cursor.getFloat(4));
            sensorMeasure.setCreatedAtFromDB(cursor.getString(5));

            cursor.close();
        }

        Log.d("getMeasure(" + id + ")", sensorMeasure.toString());

        return sensorMeasure;
    }

    public List<AccelerometerMeasure> getAllMeasures() {
        List<AccelerometerMeasure> measures = new LinkedList<>();
        String query = "SELECT  * FROM " + TABLE_SENSOR;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);        
        AccelerometerMeasure sensorMeasure;

        if (cursor.moveToFirst()) {
            do {
                sensorMeasure = new AccelerometerMeasure();
                sensorMeasure.setId(Integer.parseInt(cursor.getString(0)));
                sensorMeasure.setTrip(cursor.getInt(1));
                sensorMeasure.setAcc_x(cursor.getFloat(2));
                sensorMeasure.setAcc_y(cursor.getFloat(3));
                sensorMeasure.setAcc_z(cursor.getFloat(4));
                sensorMeasure.setCreatedAtFromDB(cursor.getString(5));

                measures.add(sensorMeasure);
            } while (cursor.moveToNext());

            cursor.close();
        }

        Log.d("getAllMeasures()", measures.toString());

        return measures;
    }

    // Updates
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

    // Deletions
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
