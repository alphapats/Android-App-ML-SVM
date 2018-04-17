package com.example.amitp.a163054001_ml;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import umich.cse.yctung.androidlibsvm.LibSVM;


public class test extends Fragment {
    String systemPath;
    String appFolderPath;
    Button predictButton;
    Button dataFilePicker;
    private static final int WRITE_REQUEST_CODE = 43;
    Uri uri = null;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View test_view=inflater.inflate(R.layout.fragment_test, container, false);

        predictButton = (Button) test_view.findViewById(R.id.predict_btn);
        dataFilePicker = (Button) test_view.findViewById(R.id.testfilepicker);

        dataFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do stuff here
                pickfile();
            }

        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do stuff here
                predictact();
            }

        });



        return test_view;
    }

    public void pickfile() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, WRITE_REQUEST_CODE );
    }

    public boolean checkpermission(String p) {
        int check = ContextCompat.checkSelfPermission(getContext(), p);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isExternalStorageWritable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        } else {
            Log.d("isExternal", "not available");
            return false;
        }
    }

    private void writeToFile(String fileName, String text, Context context) throws IOException {

        //Log.d("writeTofile","recordThread");

        Log.d("writeTofile", "recording" + checkpermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if (isExternalStorageWritable() && checkpermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            String Root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File Dir = new File(Root + File.separator + "cs653/Model");
            Log.d("dir", "" + Dir.exists());
            if (!Dir.exists()) {
                Dir.mkdir();
            }
            File file = new File(Dir.getAbsolutePath(), fileName);

            try {

                FileOutputStream stream = new FileOutputStream(file, true);
                stream.write(text.getBytes());
                stream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


            //Log.d("file pathname", file.getParent() + " " + Dir.getAbsolutePath());
        }

    }



    private void readTextFromUri(Uri uri) throws IOException {
        Context applicationContext = getContext();
        applicationContext.getContentResolver();
        InputStream inputStream = applicationContext.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));

        String line;
        float[] linear_accel = new float[3];
        int lineNo = -1;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            if (lineNo == 0) {
                continue;
            }
            if (lineNo == 1) {
                continue;
            }

            //Log.d("test Fragment", line);
            StringBuilder text = new StringBuilder();
            String filename = "test_features.csv";

            List<String> lineList = Arrays.asList(line.split(","));

            if (lineList.get(lineList.size() - 1).equalsIgnoreCase("stand")) {
                text.append("-1 ");
            } else {
                text.append("+1 ");
            }

            float acclx=Float.valueOf(lineList.get(lineList.size() - 4));
            float accly=Float.valueOf(lineList.get(lineList.size() - 3));
            float acclz=Float.valueOf(lineList.get(lineList.size() - 2));


            text.append("1:" + acclx + " ");

            text.append("2:" + accly + " ");

            text.append("3:" + acclz + " ");

            linear_accel=calc_acc(acclx,accly,acclz);

            text.append("4:" + linear_accel[0] + " ");

            text.append("5:" + linear_accel[1] + " ");

            text.append("6:" + linear_accel[2] + " ");

            text.append("\n");

            try {
                writeToFile(filename, text.toString(), getContext());
            } catch (IOException e) {
                Log.d("Thread", "Error writing to file");
            }

        }
        inputStream.close();
    }

    private void writefinalfile(Uri uri) throws IOException { Context applicationContext = getContext();
        applicationContext.getContentResolver();
        InputStream inputStream = applicationContext.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedReader reader2 = new BufferedReader(new FileReader(appFolderPath + "result.csv"));

        String line;
        String line2;
        int lineNo = -1;
        while (((line = reader.readLine()) != null) && ((line2 = reader2.readLine()) != null)) {
            //line2 = reader2.readLine();
            lineNo++;
            //Log.d("test Fragment", line);
            StringBuilder text = new StringBuilder();
            String filename = "finalresult.csv";

            if (lineNo == 0) {
               text.append(line);
               text.append("\n");

            }
            else if(lineNo == 1)
            {
                text.append(line);
                text.append(",Prediction");
                text.append("\n");
            }
            else {

                text.append(line);
                text.append(",");
                if(line2.equalsIgnoreCase("-1")){
                    text.append("stand");
                }
                else {
                    text.append("walk");
                }
                text.append("\n");
            }

            try {
                writeToFile(filename, text.toString(), getContext());
            } catch (IOException e) {
                Log.d("Thread", "Error writing to file");
            }

        }
        inputStream.close();
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == WRITE_REQUEST_CODE  && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.d("test Fragment", "Uri: " + uri.getPath());
                try {
                    readTextFromUri(uri);
                    systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" ;
                    appFolderPath = systemPath + "cs653/Model/"; // your datasets folder
                }
                catch (IOException e){
                    Log.d("test Fragment","IOException");
                }
            }
            else{
                Log.d("Test Fragment","file doesnot exist");
            }

        }


    }
    public void predictact() {

        LibSVM svm = new LibSVM();
        svm.predict(appFolderPath + "test_features.csv " + appFolderPath + "model " + appFolderPath + "result.csv");
        try {
            writefinalfile(uri);
        }
        catch (IOException e) {
            Log.d("test Fragment", "IOException");
        }

        Toast.makeText(getContext(), "PREDICTIONS DONE", Toast.LENGTH_SHORT).show();
    }

    public float[] calc_acc(float x, float y,float z){
        final float alpha = 0.8f;
//gravity is calculated here
        gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
        gravity[1] = alpha * gravity[1] + (1 - alpha)* y;
        gravity[2] = alpha * gravity[2] + (1 - alpha) * z;
//acceleration retrieved from the event and the gravity is removed
        linear_acceleration[0] = x - gravity[0];
        linear_acceleration[1] = y - gravity[1];
        linear_acceleration[2] = z - gravity[2];

        return linear_acceleration;
    }




}
