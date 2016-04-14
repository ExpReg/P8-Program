package com.example.expreg.p8_program.SensorHandlers;

import android.content.Context;
import android.provider.MediaStore;
import android.widget.TextView;

import com.example.expreg.p8_program.Model.AccelerometerMeasure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Queue;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class MyCalibrationManager {
    private Queue<AccelerometerMeasure> measures = new CircularFifoQueue<>(200);
    private Context context = null;
    public static String filename = "calibration.conf";

    public MyCalibrationManager(Context context) {
        this.context = context;
    }

    public void add(AccelerometerMeasure measure) {
        measures.add(measure);
    }

    public AccelerometerMeasure calcAverage() {
        float avgx = 0, avgy = 0, avgz = 0;

        for (AccelerometerMeasure m : measures) {
            avgx += m.getAcc_x();
            avgy += m.getAcc_y();
            avgz += m.getAcc_z();
        }

        int size = measures.size();

        return new AccelerometerMeasure(0, avgx / size, avgy / size, avgz / size);
    }

    public AccelerometerMeasure calcVariance() {
        float varx = 0, vary = 0, varz = 0;
        AccelerometerMeasure avg = this.calcAverage();

        for (AccelerometerMeasure m : measures) {
            varx += Math.pow(m.getAcc_x() - avg.getAcc_x() , 2);
            vary += Math.pow(m.getAcc_y() - avg.getAcc_y() , 2);
            varz += Math.pow(m.getAcc_z() - avg.getAcc_z() , 2);
        }

        int size = measures.size();

        return new AccelerometerMeasure(0, varx / size, vary / size, varz / size);
    }

    public void save() {
        AccelerometerMeasure avg = this.calcAverage();
        AccelerometerMeasure var = this.calcVariance();
        try {
            File file = new File(context.getFilesDir(),filename);
            FileWriter fw = new FileWriter(file);
            fw.write((avg.getAcc_x() + "\n"));
            fw.write((avg.getAcc_y() + "\n"));
            fw.write((avg.getAcc_z() + "\n"));
            fw.write((var.getAcc_x() + "\n"));
            fw.write((var.getAcc_y() + "\n"));
            fw.write((var.getAcc_z() + "\n"));

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AccelerometerMeasure readAverage(Context context) {
        AccelerometerMeasure avg = null;

        try {
            File file = new File(context.getFilesDir(),filename);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            float avgx = Float.parseFloat(br.readLine());
            float avgy = Float.parseFloat(br.readLine());
            float avgz = Float.parseFloat(br.readLine());

            avg = new AccelerometerMeasure(0, avgx, avgy, avgz);

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return avg;
    }

    public int size() {
        return measures.size();
    }

    public static AccelerometerMeasure readVariance(Context context) {
        AccelerometerMeasure var = null;

        try {
            File file = new File(context.getFilesDir(),filename);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            br.readLine();
            br.readLine();
            br.readLine();
            float varx = Float.parseFloat(br.readLine());
            float vary = Float.parseFloat(br.readLine());
            float varz = Float.parseFloat(br.readLine());

            var = new AccelerometerMeasure(0, varx, vary, varz);

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return var;
    }
}
