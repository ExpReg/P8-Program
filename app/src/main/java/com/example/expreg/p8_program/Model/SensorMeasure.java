package com.example.expreg.p8_program.Model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class SensorMeasure {

    protected int id;
    protected int trip;
    protected Date created_at;

    public SensorMeasure(){}

    public SensorMeasure(int trip) {
        this.trip = trip;
        created_at = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return dateFormat.format(created_at);
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

    public int getTrip() {
        return trip;
    }

    public void setTrip(int trip) {
        this.trip = trip;
    }
}
