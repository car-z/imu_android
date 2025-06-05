package com.example.imu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private TextView textView;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private LineChart acceleration_chart;
    private int grantResults[];
    List<Entry> accDataX;
    List<Entry> accDataY;
    List<Entry> accDataZ;
    private LineChart gyroscope_chart;
    List<Entry> gyroDataX;
    List<Entry> gyroDataY;
    List<Entry> gyroDataZ;
    int counter=0;
    int lim=500;
    Activity av;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        av = this;

        // get permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        onRequestPermissionsResult(1,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},grantResults);

        // ui logic
        Constants.startButton = (Button)findViewById(R.id.button);
        Constants.stopButton = (Button)findViewById(R.id.button2);
        acceleration_chart = (LineChart)findViewById(R.id.acceleration_chart);
        gyroscope_chart = (LineChart)findViewById(R.id.gyroscope_chart);
        textView = (TextView)findViewById(R.id.textView);

        Constants.startButton.setEnabled(true);
        Constants.stopButton.setEnabled(false);

        // defining sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);

        // on click listeners
        Constants.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.timestamps=new ArrayList<>();
                Constants.accx=new ArrayList<>();
                Constants.accy=new ArrayList<>();
                Constants.accz=new ArrayList<>();
                Constants.gravx=new ArrayList<>();
                Constants.gravy=new ArrayList<>();
                Constants.gravz=new ArrayList<>();
                Constants.gyrox=new ArrayList<>();
                Constants.gyroy=new ArrayList<>();
                Constants.gyroz=new ArrayList<>();
                Constants.gyrotimestamps=new ArrayList<>();
                Constants.startButton.setEnabled(false);
                Constants.stopButton.setEnabled(true);

                accDataX=new ArrayList<>();
                accDataY=new ArrayList<>();
                accDataZ=new ArrayList<>();
                gyroDataX=new ArrayList<>();
                gyroDataY=new ArrayList<>();
                gyroDataZ=new ArrayList<>();
                counter=0;
                Constants.start=true;
            }
        });
        Constants.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.startButton.setEnabled(true);
                Constants.stopButton.setEnabled(false);
                Constants.start=false;
                String fname = System.currentTimeMillis()+"";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(fname);
                    }
                });
                FileOperations.writetofile(av,fname);
            }
        });
    }

    float grav=9.81f;
    boolean gotacc=false;
    boolean gotgyro = false;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Constants.start) {
            long currentWallTime = System.currentTimeMillis(); //ms since epoch
            long currentElapsed = android.os.SystemClock.elapsedRealtime(); //ms since boot
            long timestamp = sensorEvent.timestamp; //nanoseconds since system boot

            long unixTimeMillis = currentWallTime - currentElapsed + (timestamp/1_000_000L);

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                Constants.accx.add(x);
                Constants.accy.add(y);
                Constants.accz.add(z);
                Constants.timestamps.add(unixTimeMillis);
                graphAccData(new float[]{x,y,z});
                gotacc=true;
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Constants.gyrox.add(x);
                Constants.gyroy.add(y);
                Constants.gyroz.add(z);
                Constants.gyrotimestamps.add(unixTimeMillis);
                graphGyroData(new float[]{x,y,z});
                gotgyro=true;
            }
        }
    }

    public void graphAccData(float[] values) {
        accDataX.add(new Entry(counter,values[0]));
        accDataY.add(new Entry(counter,values[1]));
        accDataZ.add(new Entry(counter,values[2]));
        if (accDataX.size()>lim) {
            accDataX.remove(0);
            accDataY.remove(0);
            accDataZ.remove(0);
        }
        counter+=1;

        LineDataSet accx = new LineDataSet(accDataX, "acc x");
        LineDataSet accy = new LineDataSet(accDataY, "acc y");
        LineDataSet accz = new LineDataSet(accDataZ, "acc z");
        accx.setDrawCircles(false);
        accy.setDrawCircles(false);
        accz.setDrawCircles(false);
        accx.setColor(((MainActivity)this).getResources().getColor(R.color.red));
        accy.setColor(((MainActivity)this).getResources().getColor(R.color.green));
        accz.setColor(((MainActivity)this).getResources().getColor(R.color.blue));
        List<ILineDataSet> accData = new ArrayList<>();
        accData.add(accx);
        accData.add(accy);
        accData.add(accz);

        LineData lineData = new LineData(accData);
        acceleration_chart.setData(lineData);
//        acceleration_chart.getAxisLeft().setAxisMaximum(90);
//        acceleration_chart.getAxisLeft().setAxisMinimum(-90);
        acceleration_chart.notifyDataSetChanged();
        acceleration_chart.invalidate();
    }

    public void graphGyroData(float[] values) {
        gyroDataX.add(new Entry(counter,values[0]));
        gyroDataY.add(new Entry(counter,values[1]));
        gyroDataZ.add(new Entry(counter,values[2]));
        if (gyroDataX.size()>lim) {
            gyroDataX.remove(0);
            gyroDataY.remove(0);
            gyroDataZ.remove(0);
        }
        counter+=1;

        LineDataSet gx = new LineDataSet(gyroDataX, "gyro x");
        LineDataSet gy = new LineDataSet(gyroDataY,"gyro y");
        LineDataSet gz = new LineDataSet(gyroDataZ, "gyro z");
        gx.setDrawCircles(false);
        gy.setDrawCircles(false);
        gz.setDrawCircles(false);
        gx.setColor(((MainActivity)this).getResources().getColor(R.color.red));
        gy.setColor(((MainActivity)this).getResources().getColor(R.color.green));
        gz.setColor(((MainActivity)this).getResources().getColor(R.color.blue));
        List<ILineDataSet> gyroData = new ArrayList<>();
        gyroData.add(gx);
        gyroData.add(gy);
        gyroData.add(gz);

        LineData lineData = new LineData(gyroData);
        gyroscope_chart.setData(lineData);
        gyroscope_chart.notifyDataSetChanged();
        gyroscope_chart.invalidate();
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}