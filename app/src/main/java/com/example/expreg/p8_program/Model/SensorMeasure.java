package com.example.expreg.p8_program.Model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorMeasure {

    private int id;
    private int trip;
    private Date created_at;
    private float acc_x;
    private float acc_y;
    private float acc_z;

    public SensorMeasure(){}

    public SensorMeasure(int trip, float acc_x, float acc_y, float acc_z) {
        this.trip = trip;
        this.acc_x = acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
        created_at = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getAcc_x() {
        return acc_x;
    }

    public void setAcc_x(float acc_x) {
        this.acc_x = acc_x;
    }

    public float getAcc_y() {
        return acc_y;
    }

    public void setAcc_y(float acc_y) {
        this.acc_y = acc_y;
    }

    public float getAcc_z() {
        return acc_z;
    }

    public void setAcc_z(float acc_z) {
        this.acc_z = acc_z;
    }

    /*
    public Date getCreatedAt() {
        return created_at;
    }
    */

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void setCreatedAtFromDB(String databaseDate) {
        Date date = new Date();
        try {
            DateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            date = (Date) formatter.parse(databaseDate);
            created_at = date;
        } catch (Exception e) {
            new Exception(e);
        }
    }

    @Override
    public String toString() {
        return "SensorMeasure [id=" + id + ", " +
                              "acc_x=" + acc_x + ", " +
                              "acc_y=" + acc_y + ", " +
                              "acc_z=" + acc_z + ", " +
                              "created_at=" + getDateTime()
                + "]";
    }

    public int getTrip() {
        return trip;
    }

    public void setTrip(int trip) {
        this.trip = trip;
    }
}
