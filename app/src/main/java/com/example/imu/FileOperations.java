package com.example.imu;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileOperations {

    public static void writetofile(Activity av, String fname) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String dir = av.getExternalFilesDir(null).toString();
                    File path = new File(dir);
                    if (!path.exists()) {
                        path.mkdirs();
                    }
                    File file = new File(dir, fname+"-acc.txt");
                    BufferedWriter outfile = new BufferedWriter(new FileWriter(file,false));
                    for (int i = 0; i < Constants.accx.size(); i++) {
                        outfile.append(Constants.timestamps.get(i)+","+Constants.accx.get(i)+","+Constants.accy.get(i)+","+Constants.accz.get(i));
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

                    file = new File(dir, fname+"-gyro.txt");
                    outfile = new BufferedWriter(new FileWriter(file, false));
                    for (int i = 0; i < Constants.gyrox.size(); i++) {
                        outfile.append(Constants.timestamps.get(i) + "," +Constants.gyrox.get(i)+","+Constants.gyroy.get(i)+","+Constants.gyroz.get(i));
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
}
