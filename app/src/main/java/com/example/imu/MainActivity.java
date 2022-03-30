package com.example.imu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private TextView textView;
    private Sensor linearAcclerationSensor;
    private LineChart lineChart;
    private int grantResults[];
    List<Entry> lineDataX;
    List<Entry> lineDataY;
    List<Entry> lineDataZ;
    int counter=0;
    int lim=500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        onRequestPermissionsResult(1,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},grantResults);

        Constants.startButton = (Button)findViewById(R.id.button);
        Constants.stopButton = (Button)findViewById(R.id.button2);
        lineChart = (LineChart)findViewById(R.id.linechart);
        textView = (TextView)findViewById(R.id.textView);

        Constants.startButton.setEnabled(true);
        Constants.stopButton.setEnabled(false);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAcclerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, linearAcclerationSensor, SensorManager.SENSOR_DELAY_FASTEST);

        Constants.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.accx=new ArrayList<>();
                Constants.accy=new ArrayList<>();
                Constants.accz=new ArrayList<>();
                Constants.gravx=new ArrayList<>();
                Constants.gravy=new ArrayList<>();
                Constants.gravz=new ArrayList<>();
                Constants.startButton.setEnabled(false);
                Constants.stopButton.setEnabled(true);

                lineDataX=new ArrayList<>();
                lineDataY=new ArrayList<>();
                lineDataZ=new ArrayList<>();
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
                writetofile(fname);
            }
        });
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, linearAcclerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void writetofile(String fname) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String dir = getExternalFilesDir(null).toString();
                    File path = new File(dir);
                    if (!path.exists()) {
                        path.mkdirs();
                    }
                    File file = new File(dir, fname+"-acc.txt");
                    BufferedWriter outfile = new BufferedWriter(new FileWriter(file,false));
                    for (int i = 0; i < Constants.accx.size(); i++) {
                        outfile.append(Constants.accx.get(i)+","+Constants.accy.get(i)+","+Constants.accz.get(i));
                        outfile.newLine();
                    }
                    outfile.flush();
                    outfile.close();

                    file = new File(dir, fname+"-grav.txt");
                    outfile = new BufferedWriter(new FileWriter(file,false));
                    for (int i = 0; i < Constants.gravx.size(); i++) {
                        outfile.append(Constants.gravx.get(i)+","+Constants.gravy.get(i)+","+Constants.gravz.get(i));
                        outfile.newLine();
                    }
                    outfile.flush();
                    outfile.close();
                } catch(Exception e) {
                    Log.e("ex", "writeRecToDisk");
                    Log.e("ex", e.getMessage());
                }
            }
        }).run();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Constants.start) {
            if (sensorEvent.sensor.equals(linearAcclerationSensor)) {
                Constants.accx.add(sensorEvent.values[0]);
                Constants.accy.add(sensorEvent.values[1]);
                Constants.accz.add(sensorEvent.values[2]);

                lineDataX.add(new Entry(counter,sensorEvent.values[0]));
                lineDataY.add(new Entry(counter,sensorEvent.values[1]));
                lineDataZ.add(new Entry(counter,sensorEvent.values[2]));
                if (lineDataX.size()>lim) {
                    lineDataX.remove(0);
                    lineDataY.remove(0);
                    lineDataZ.remove(0);
                }
                counter+=1;
                LineDataSet data1 = new LineDataSet(lineDataX, "x");
                LineDataSet data2 = new LineDataSet(lineDataY, "y");
                LineDataSet data3 = new LineDataSet(lineDataZ, "z");
                data1.setDrawCircles(false);
                data2.setDrawCircles(false);
                data3.setDrawCircles(false);
                data1.setColor(((MainActivity)this).getResources().getColor(R.color.red));
                data2.setColor(((MainActivity)this).getResources().getColor(R.color.green));
                data3.setColor(((MainActivity)this).getResources().getColor(R.color.blue));
                List<ILineDataSet> data = new ArrayList<>();
                data.add(data1);
                data.add(data2);
                data.add(data3);

                LineData lineData = new LineData(data);
                lineChart.setData(lineData);
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }
            else {
                Constants.gravx.add(sensorEvent.values[0]);
                Constants.gravy.add(sensorEvent.values[1]);
                Constants.gravz.add(sensorEvent.values[2]);
            }
            Log.e("log",String.format("%s %.2f %.2f %.2f",sensorEvent.sensor.getName(),sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}