package com.example.expreg.p8_program.SensorHandlers;

import android.content.Context;

import com.example.expreg.p8_program.Model.AccelerometerMeasure;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class MyCircularQueue {
    private CircularFifoQueue<AccelerometerMeasure> measures = null;

    public MyCircularQueue(int size) {
        measures = new CircularFifoQueue<>(size);
    }

    public void add(AccelerometerMeasure measure) {
        measures.add(measure);
    }

    public AccelerometerMeasure getMin() {
        AccelerometerMeasure min = new AccelerometerMeasure(0, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        for (AccelerometerMeasure m : measures) {
            if (m.getAcc_y() < min.getAcc_y()) {
                min = m;
            }
        }
        return min;
    }

    public AccelerometerMeasure getMax() {
        AccelerometerMeasure max = new AccelerometerMeasure(0, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (AccelerometerMeasure m : measures) {
            if (m.getAcc_y() > max.getAcc_y()) {
                max = m;
            }
        }
        return max;
    }

    public AccelerometerMeasure getAverage() {
        float avgx = 0, avgy = 0, avgz = 0;

        for (AccelerometerMeasure m : measures) {
            avgx += m.getAcc_x();
            avgy += m.getAcc_y();
            avgz += m.getAcc_z();
        }

        int size = measures.size();

        return new AccelerometerMeasure(0, avgx / size, avgy / size, avgz / size);
    }

    public AccelerometerMeasure getVariance() {
        float varx = 0, vary = 0, varz = 0;
        AccelerometerMeasure avg = this.getAverage();

        for (AccelerometerMeasure m : measures) {
            varx += Math.pow(m.getAcc_x() - avg.getAcc_x() , 2);
            vary += Math.pow(m.getAcc_y() - avg.getAcc_y() , 2);
            varz += Math.pow(m.getAcc_z() - avg.getAcc_z() , 2);
        }

        int size = measures.size();

        return new AccelerometerMeasure(0, varx / size, vary / size, varz / size);
    }

    public int size() {
        return measures.size();
    }

    public boolean isFull() {
        return measures.isFull();
    }
}
