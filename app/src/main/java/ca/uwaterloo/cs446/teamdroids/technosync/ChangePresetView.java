package ca.uwaterloo.cs446.teamdroids.technosync;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ChangePresetView extends AppCompatActivity {

    private Button doneButton = null;
    private ListView fileListView = null;
    private ListViewAdapter myListAdapter = null;
    private ArrayList<String> fileNames = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_preset_view);

        doneButton = findViewById(R.id.doneButton);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPreset();
                finish();
            }
        });

        fileListView = findViewById(R.id.listOfFileNames);

        fileNames = new ArrayList<>(50);
        for (int i = 0; i <= 50; i++) {
            fileNames.add("missing");
        }
        myListAdapter = new ListViewAdapter(this, fileNames);
        fileListView.setAdapter(myListAdapter);
    }

    private void createPreset() {
        File directory = getFilesDir();
        File[] files = directory.listFiles();
        int countOfPresets = 0;
        for (File file : files) {
            if (file.getName().contains("preset")) {
                countOfPresets += 1;
            }
        }
        File newPresetFile = new File(directory, "preset_" + String.valueOf(countOfPresets + 1) + ".txt");

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(newPresetFile);
            for (String fileName : fileNames) {
                stream.write(fileName.getBytes());
                stream.write("\n".getBytes());
            }
        } catch (IOException exception) {
            Log.e("technosync", exception.getMessage());
        } finally {
            try {
                if (stream != null) stream.close();
            } catch (IOException exception) {
                Log.e("technosync", exception.getMessage());
            }
        }
    }


    public String getFileNameFromPath(String filePath) {
        int lastSlash = filePath.lastIndexOf("/");
        return filePath.substring(lastSlash + 1);
    }

    private ArrayList<String> getAllSamplesAndUserBeats() {
        ArrayList<String> fileNames = new ArrayList<>();
        File[] files = getFilesDir().listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".mp4")) {
                fileNames.add(getFileNameFromPath(file.getAbsolutePath()));
            }
        }

        Field[] rawMusic =  ca.uwaterloo.cs446.teamdroids.technosync.R.raw.class.getFields();
        for (int i = 0; i < rawMusic.length; i++) {
            String name = rawMusic[i].getName();
            if (name.contains("preset")) {
                continue;
            }
            fileNames.add(name + ".mp3");
        }
        return fileNames;
    }

    private void updateListView(int position, String fileName) {
        fileNames.set(position, fileName);

        myListAdapter = new ListViewAdapter(this, fileNames);
        fileListView.setAdapter(myListAdapter);
    }

    //Display Preset Selector
    private void displayPresetSelector(int position){

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a music file");

        ArrayList<String> listOfUserBeats = getAllSamplesAndUserBeats();
        String[] stringArr = new String[listOfUserBeats.size()];
        stringArr = listOfUserBeats.toArray(stringArr);

        //Setup List
        builder.setItems(stringArr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateListView(position, listOfUserBeats.get(which));
            }
        });

        builder.show();
    }

    public class ListViewAdapter extends ArrayAdapter<String> {

        public ListViewAdapter(Context context, ArrayList<String> fileNames) {
            super(context, 0, fileNames);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_file, container, false);
            }

            ((TextView) convertView.findViewById(R.id.fileName)).setText(getFileNameFromPath(getItem(position)));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayPresetSelector(position);
                }
            });
            return convertView;
        }
    }

}
