package com.example.expreg.p8_program.SensorHandlers;

import com.example.expreg.p8_program.Model.AccelerometerMeasure;

import java.util.Queue;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class MyCalibrationManager {
    Queue<AccelerometerMeasure> measures = new CircularFifoQueue<>(200);

    public MyCalibrationManager() {}

    public void add(AccelerometerMeasure measure) {
        measures.add(measure);
    }

    public AccelerometerMeasure average() {
        float avgx = 0, avgy = 0, avgz = 0;

        for (AccelerometerMeasure m : measures) {
            avgx += m.getAcc_x();
            avgy += m.getAcc_y();
            avgz += m.getAcc_z();
        }

        int size = measures.size();

        return new AccelerometerMeasure(0, avgx / size, avgy / size, avgz / size);
    }

    public AccelerometerMeasure variance() {
        float varx = 0, vary = 0, varz = 0;
        AccelerometerMeasure avg = this.average();

        for (AccelerometerMeasure m : measures) {
            varx += Math.pow(m.getAcc_x() - avg.getAcc_x() , 2);
            vary += Math.pow(m.getAcc_y() - avg.getAcc_y() , 2);
            varz += Math.pow(m.getAcc_z() - avg.getAcc_z() , 2);
        }

        int size = measures.size();

        return new AccelerometerMeasure(0, varx / size, vary / size, varz / size);
    }
}
