package ca.uwaterloo.cs446.teamdroids.technosync;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordingView extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String newFileName = null;

    private MediaRecorder recorder = null;
    private MediaPlayer   player = null;

    private TextView selectedFileTextView = null;
    private Button playButton = null;
    private Button recordButton = null;
    private ListView fileListView = null;

    boolean mStartRecording = true;
    boolean mStartPlaying = true;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(newFileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(newFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        setSelectedFileText(-1);
        updateList();
    }

    private void setSelectedFileText(int position) {
        ArrayList<String> listOfUserBeatFileNames = new ArrayList<>();
        File[] listOfFiles = getFilesDir().listFiles();
        for (File file : listOfFiles) {
            if (file.getName().startsWith("UserBeat_")) {
                listOfUserBeatFileNames.add(file.getAbsolutePath());
            }
        }

        if (listOfUserBeatFileNames.size() > 0 && position < 0) {
            String lastFile = listOfUserBeatFileNames.get(listOfUserBeatFileNames.size() - 1);
            selectedFileTextView.setText(lastFile);
            newFileName = lastFile;
        } else if (listOfUserBeatFileNames.size() > 0) {
            String file = listOfUserBeatFileNames.get(position);
            selectedFileTextView.setText(file);
            newFileName = file;
        }
    }

    private void updateList() {

        // Record to the internalDirectory for future playback.
        ArrayList<String> listOfUserBeats = new ArrayList<>();
        File[] listOfFiles = getFilesDir().listFiles();
        for (File file : listOfFiles) {
            if (file.getName().startsWith("UserBeat_")) {
                listOfUserBeats.add(file.getAbsolutePath());
            }
        }

        ListViewAdapter adapter = new ListViewAdapter(getApplicationContext(), listOfUserBeats);
        fileListView.setAdapter(adapter);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_recording_view);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        // Record to the internalDirectory for future playback.
        ArrayList<String> listOfUserBeats = new ArrayList<>();
        File[] listOfFiles = getFilesDir().listFiles();
        for (File file : listOfFiles) {
            if (file.getName().startsWith("UserBeat_")) {
                listOfUserBeats.add(file.getAbsolutePath());
            }
        }

        int numFiles = listOfUserBeats.size();

        // Record to the external cache directory for visibility
        newFileName = getFilesDir().getAbsolutePath();
        if (numFiles > 0) {
            newFileName += "/UserBeat_" + numFiles + ".mp4";
        } else {
            newFileName += "/UserBeat_1.mp4";
        }

        recordButton = findViewById(R.id.recordButton);
        playButton = findViewById(R.id.playButton);
        selectedFileTextView = findViewById(R.id.selectedFile);
        fileListView = findViewById(R.id.listOfFileNames);

        updateList();

        selectedFileTextView.setText("");
        if (numFiles > 0) {
            selectedFileTextView.setText(newFileName);
        }

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    recordButton.setText("Stop recording");
                } else {
                    recordButton.setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });
        recordButton.setText("Start recording");

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    playButton.setText("Stop playing");
                } else {
                    playButton.setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        });
        playButton.setText("Start playing");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

    private class ListViewAdapter extends ArrayAdapter<String> {

        public ListViewAdapter(Context context, ArrayList<String> fileNames) {
            super(context, 0, fileNames);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_file, container, false);
            }

            ((TextView) convertView.findViewById(R.id.fileName))
                    .setText(getItem(position));
            ((TextView) convertView.findViewById(R.id.fileName))
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setSelectedFileText(position);
                        }
                    });
            return convertView;
        }
    }
}


