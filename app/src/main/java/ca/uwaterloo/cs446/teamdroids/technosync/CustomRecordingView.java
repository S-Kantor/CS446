package ca.uwaterloo.cs446.teamdroids.technosync;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import ca.uwaterloo.cs446.teamdroids.technosync.api.WebApi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomRecordingView extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String newFileName = null;

    private MediaRecorder recorder = null;
    private MediaPlayer   player = null;

    private TextView selectedFileTextView = null;
    private Button playButton = null;
    private Button recordButton = null;
    private ListView fileListView = null;
    private ListViewAdapter myListAdapter = null;

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
            playButton.setEnabled(false);
        } else {
            stopRecording();
            playButton.setEnabled(true);
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            recordButton.setEnabled(false);
            startPlaying();
        } else {
            stopPlaying();
            recordButton.setEnabled(true);
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(getFilePathFromSelectedText());
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
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    // A basic 250 ms delay is required because the codec cuts off the last ~200 ms.
    private void stopRecording() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recorder.stop();
                recorder.release();
                recorder = null;
                setSelectedFileText(-1);
                updateList();
                updateNewFileName();
                sendFileToServer();
            }
        }, 250);
    }

    private void setSelectedFileText(int position) {
        ArrayList<String> listOfUserBeatFileNames = new ArrayList<>();
        File[] listOfFiles = getFilesDir().listFiles();
        for (File file : listOfFiles) {
            if (file.getName().startsWith("UserBeat_")) {
                listOfUserBeatFileNames.add(file.getAbsolutePath());
            }
        }

        if (listOfUserBeatFileNames.size() == 0) {
            // No files have been created so we can't play/select a file.
            selectedFileTextView.setText("");
            return;
        }

        String file;

        if (position < 0) {
            file = listOfUserBeatFileNames.get(listOfUserBeatFileNames.size() - 1);
        } else {
            file = listOfUserBeatFileNames.get(position);
        }
        selectedFileTextView.setText(getFileNameFromPath(file));
    }

    private void updateList() {
        // Record to the internalDirectory for future playback.
        ArrayList<String> listOfUserBeats = new ArrayList<>();
        File[] listOfFiles = getFilesDir().listFiles();
        for (File file : listOfFiles) {
            if (file.getName().endsWith(".mp4")) {
                listOfUserBeats.add(file.getAbsolutePath());
            }
        }
        myListAdapter.clear();
        myListAdapter.addAll(listOfUserBeats);
        myListAdapter.notifyDataSetChanged();
    }

    private String getFileNameFromPath(String filePath) {
        int lastSlash = filePath.lastIndexOf("/");
        return filePath.substring(lastSlash + 1);
    }

    private String getFilePathFromSelectedText() {
        String selectedFileName = selectedFileTextView.getText().toString();

        File[] listOfFiles = getFilesDir().listFiles();
        for (File file : listOfFiles) {
            if (getFileNameFromPath(file.getAbsolutePath()).equalsIgnoreCase(selectedFileName)) {
                return file.getAbsolutePath();
            }
        }

        return "";
    }

    private void updateNewFileName() {
        // Record to the external cache directory for visibility
        File[] listOfFiles = getFilesDir().listFiles();
        int countOfUserBeatFiles = 0;
        for (File file : listOfFiles) {
            if (file.getName().contains("UserBeat_")) {
                countOfUserBeatFiles += 1;
            }
        }
        newFileName = getFilesDir().getAbsolutePath();
        if (listOfFiles.length > 0) {
            newFileName += "/UserBeat_" + (countOfUserBeatFiles + 1) + "_" + UUID.randomUUID().toString().substring(0,8) + ".mp4";
        } else {
            newFileName += "/UserBeat_1_" + UUID.randomUUID().toString().substring(0,8) + ".mp4";
        }
    }

    private void sendFileToServer() {
        String lastCreatedFile = getFilePathFromSelectedText();
        File lastFile = null;

        File[] listOfFiles = getFilesDir().listFiles();
        for (File file : listOfFiles) {
            if (file.getAbsolutePath().equalsIgnoreCase(lastCreatedFile)) {
                lastFile = file;
            }
        }

        if (lastFile == null) {
            Log.e("TechnoSynch", "invalid file");
            return;
        }

        // Create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("*/*"),
                        lastFile
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("audio", lastFile.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "Custom Audio From User";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        // finally, execute the request
        Call<ResponseBody> call = WebApi.getInstance().getTechnoSyncService().uploadCustomAudio(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });

    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_custom_recording_view);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        recordButton = findViewById(R.id.recordButton);
        playButton = findViewById(R.id.playButton);
        selectedFileTextView = findViewById(R.id.selectedFile);
        fileListView = findViewById(R.id.listOfFileNames);


        ArrayList<String> fileNames = new ArrayList<>();
        File[] files = getFilesDir().listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".mp4")) {
                fileNames.add(getFileNameFromPath(file.getAbsolutePath()));
            }
        }
        myListAdapter = new ListViewAdapter(this, fileNames);
        fileListView.setAdapter(myListAdapter);

        updateList();
        updateNewFileName();
        setSelectedFileText(-1);

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

            ((TextView) convertView.findViewById(R.id.fileName)).setText(getFileNameFromPath(getItem(position)));
            convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setSelectedFileText(position);
                        }
                    });
            return convertView;
        }
    }
}


