package com.example.expreg.p8_program.Model;

import java.util.Date;

public class SensorMeasure {

    private int id;
    private Date date;
    private float acc_x;
    private float acc_y;
    private float acc_z;

    public SensorMeasure(){}

    public SensorMeasure(float acc_x, float acc_y, float acc_z) {
        this.acc_x = acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
        date = new Date();
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "SensorMeasure [id=" + id + ", acc_x=" + acc_x + ", acc_y=" + acc_y + ", acc_z=" + acc_z + ", date=" + date.toString()
                + "]";
    }
}