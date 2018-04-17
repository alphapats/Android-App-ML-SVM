package com.example.amitp.a163054001_ml;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import umich.cse.yctung.androidlibsvm.LibSVM;



public class train extends Fragment {
    String systemPath;
    String appFolderPath;
    Button trainButton;
    Button dataFilePicker;
    private static final int WRITE_REQUEST_CODE = 42;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View train_view = inflater.inflate(R.layout.fragment_train, container, false);


        trainButton = (Button) train_view.findViewById(R.id.train_btn);
        dataFilePicker = (Button) train_view.findViewById(R.id.datafilepicker);

        dataFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do stuff here
                pickfile();
            }

        });

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do stuff here
                trainmodel();
            }

        });

        return train_view;
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

        startActivityForResult(intent, WRITE_REQUEST_CODE);
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


            Log.d("file pathname", file.getParent() + " " + Dir.getAbsolutePath());
        }

    }


    private void readTextFromUri(Uri uri) throws IOException {
        Context applicationContext = getContext();
        applicationContext.getContentResolver();
        InputStream inputStream = applicationContext.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        float[] linear_accel ;
        String line;
        int lineNo = -1;
        while ((line = reader.readLine()) != null) {

            lineNo++;
            if (lineNo == 0) {
                continue;
            }
            if (lineNo == 1) {
                continue;
            }

            //Log.d("train Fragment", line);
            StringBuilder text = new StringBuilder();
            String filename = "train_features.csv";

            List<String> lineList = Arrays.asList(line.split(","));

            if (lineList.get(lineList.size() - 1).equalsIgnoreCase("stand")) {
                text.append("-1 ");
            } else {
                text.append("+1 ");
            }

            float acclx=Float.valueOf(lineList.get(lineList.size() - 4));
            float accly=Float.valueOf(lineList.get(lineList.size() - 3));
            float acclz=Float.valueOf(lineList.get(lineList.size() - 2));

            linear_accel=calc_acc(acclx,accly,acclz);
            text.append("1:" + acclx + " ");

            text.append("2:" + accly + " ");

            text.append("3:" + acclz + " ");


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

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.d("train Fragment", "Uri: " + uri.getPath());
                try {
                    readTextFromUri(uri);
                    systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                    appFolderPath = systemPath + "cs653/Model/"; // your datasets folder

                } catch (IOException e) {
                    Log.d("Train Fragment", "IOException");
                }
            } else {
                Log.d("Train Fragment", "file doesnot exist");
            }
        }


    }

    public void trainmodel() {

        LibSVM svm = new LibSVM();
        svm.train("-t 2 "/* svm kernel */ + appFolderPath + "train_features.csv " + appFolderPath + "model");
        Toast.makeText(getContext(), "MODEL TRAINED", Toast.LENGTH_SHORT).show();
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
        //Log.d("calculating accelertion", "done");

        return linear_acceleration;
    }
}


