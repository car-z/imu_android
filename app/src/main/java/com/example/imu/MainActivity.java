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
    private Sensor gravitySensor;
    private TextView textView;
    private Sensor accelerometer,gyroscope;
    private LineChart lineChart;
    private int grantResults[];
    List<Entry> lineDataX;
    List<Entry> lineDataY;
    List<Entry> lineDataZ;
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
        lineChart = (LineChart)findViewById(R.id.linechart);
        textView = (TextView)findViewById(R.id.textView);

        Constants.startButton.setEnabled(true);
        Constants.stopButton.setEnabled(false);

        // defining sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);

        // on click listeners
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
                FileOperations.writetofile(av,fname);
            }
        });
    }

    private static final double NS2S = 1.0 / 1000000000.0;
    private double timestamp;
    float grav=9.81f;
    float rad2deg=57.2958f;
    float phiHat=0;
    float thetaHat=0;
    float phiHat2=0;
    float thetaHat2=0;
    float gyroTilt=0;
    float gyroTilt2=0;
    float accTilt=0;
    float accTilt2=0;
    boolean gotacc=false;
    boolean gotgyro=false;
    private final float[] deltaRotationVector = new float[4];
    float[] rotationMatrix=new float[9];
    float[] orientation=new float[3];
    float bias_acc=0;
    float bias_gyro=0;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Constants.start) {
            if (sensorEvent.sensor.equals(accelerometer)) {
                double ts = sensorEvent.timestamp;
                double dT = (ts - timestamp)*NS2S;
                timestamp=ts;
//                Log.e("asdf","ts "+dT);
                float x = sensorEvent.values[0]/grav;
                float y = sensorEvent.values[1]/grav;
                float z = sensorEvent.values[2]/grav;
                Constants.accx.add(x);
                Constants.accy.add(y);
                Constants.accz.add(z);
//                Log.e("asdf",sensorEvent.values[2]+"");
//                float phi=(float)Math.atan(sensorEvent.values[1]/sensorEvent.values[2]);
//                float theta=(float)Math.asin(sensorEvent.values[0]/grav);
//                phi*=57.2958;
//                theta*=57.2958;
//                float phi =

//                float phi=(float)Math.atan(y/z)*rad2deg;
//                float theta=(float)Math.atan(-x/Math.sqrt(y*y+z*z))*rad2deg;

                float phi=(float)Math.atan2(y,z)*rad2deg;
                float theta=(float)Math.atan2(-x,Math.sqrt(y*y+z*z))*rad2deg;
                float modulus = (float)Math.sqrt(x*x+y*y+z*z);
                accTilt = (float)Math.acos(z/modulus)*rad2deg;
                accTilt2=(float)Math.sqrt(phi*phi+theta*theta)*rad2deg;

//                float phi=(float)Math.atan(y/Math.sqrt(x*x+z*z))*rad2deg;
//                float theta=(float)Math.atan(-x/z)*rad2deg;

//                graphData(new float[]{tilt,tilt2,0});
//                graphData(new float[]{accTilt,0,0});
//                graphData(new float[]{x,y,z});
                gotacc=true;
            }
            else if (sensorEvent.sensor.equals(gyroscope)) {
                double ts = sensorEvent.timestamp;
                double dT = ((ts - timestamp)*NS2S)*2;
                timestamp=ts;

                if (dT>0 && dT<1) {
                    float p = sensorEvent.values[0];
                    float q = sensorEvent.values[1];
                    float r = sensorEvent.values[2];
//
                    float phiDot=(float)(p+Math.tan(thetaHat)*(Math.sin(phiHat)*q+Math.cos(phiHat)+r));
                    float thetaDot=(float)(Math.cos(phiHat)*q-Math.sin(phiHat)+r);

                    phiHat += dT*p;
                    thetaHat += dT*q;
                    phiHat2 += dT*phiDot;
                    thetaHat2 += dT*thetaDot;

                    gyroTilt=(float)Math.sqrt(phiHat*phiHat+thetaHat*thetaHat)*rad2deg;
                    gyroTilt2=(float)Math.sqrt(phiHat2*phiHat2+thetaHat2*thetaHat2)*rad2deg;

//                    graphData(new float[]{gyroTilt,0,0});
                    gotgyro=true;
                }
            }
            if (gotacc&&gotgyro) {
                float alpha=.98f;
                if (counter==0) {
                    bias_acc=accTilt;
                    bias_gyro=gyroTilt;
                }

                float comp=(alpha*(gyroTilt-bias_gyro))+((1-alpha)*(accTilt-bias_acc));
                graphData(new float[]{accTilt-bias_acc,gyroTilt-bias_gyro,comp});
                gotacc=false;
                gotgyro=false;
            }

//            Log.e("log",String.format("%s %.2f %.2f %.2f",sensorEvent.sensor.getName(),sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]));
        }
    }

    public void graphData(float[] values) {
        lineDataX.add(new Entry(counter,values[0]));
        lineDataY.add(new Entry(counter,values[1]));
        lineDataZ.add(new Entry(counter,values[2]));
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
        lineChart.getAxisLeft().setAxisMaximum(90);
        lineChart.getAxisLeft().setAxisMinimum(-90);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
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