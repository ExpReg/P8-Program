package com.example.expreg.p8_program.Model;

public class AccelerometerMeasure extends SensorMeasure {
    private float acc_x;
    private float acc_y;
    private float acc_z;

    public AccelerometerMeasure(){super();}

    public AccelerometerMeasure(int trip, float acc_x, float acc_y, float acc_z) {
        super(trip);
        this.acc_x = acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
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

    @Override
    public String toString() {
        return "AccelerometerMeasure [id=" + id + ", " +
                "acc_x=" + acc_x + ", " +
                "acc_y=" + acc_y + ", " +
                "acc_z=" + acc_z + ", " +
                "created_at=" + getDateTime()
                + "]";
    }
}
