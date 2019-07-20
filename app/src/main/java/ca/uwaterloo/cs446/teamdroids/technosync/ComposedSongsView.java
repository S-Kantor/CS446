package ca.uwaterloo.cs446.teamdroids.technosync;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.uwaterloo.cs446.teamdroids.technosync.api.WebApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComposedSongsView extends AppCompatActivity {

    private Button refreshButton;
    private ListView songsListView;
    private ListViewAdapter listAdapter;
    private ArrayList<ComposedSongModel> composedSongsList;

    private MediaPlayer player = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composed_songs_view);

        composedSongsList = fetchSavedComposedSongs();

        refreshButton = (Button) findViewById(R.id.refreshButton);
        setupRefreshButton();
        songsListView = (ListView) findViewById(R.id.listOfComposedSongs);
        listAdapter = new ListViewAdapter(this, composedSongsList);
        songsListView.setAdapter(listAdapter);
    }

    private void setupRefreshButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: IMPLEMENT THIS
            }
        });
    }

    private ArrayList<ComposedSongModel> fetchSavedComposedSongs() {
        // Fetch the list of group IDs the user has been a part of
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String finishedSongsString = preferences.getString("composedSongGroups", "");
        String[] songs = {"room1", "room2", "room3", "room4"}; //finishedSongsString.split(",");
        ArrayList<ComposedSongModel> songsList = new ArrayList<ComposedSongModel>();

        Map<String, String> fileDict = new HashMap<String, String>();
        File[] files = getFilesDir().listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".mp3") & file.getName().contains("/composed_song")) {
                fileDict.put(getFileNameFromPath(file.getAbsolutePath()), file.getAbsolutePath());
            }
        }

        for (int i = songs.length - 1; i >= 0; i--) {
            String groupId = songs[i];
            String filePath = fileDict.get("composed_song_" + groupId + ".mp3");

            songsList.add(new ComposedSongModel(groupId, filePath));
        }

        return songsList;
    }

    private String getFileNameFromPath(String filePath) {
        int lastSlash = filePath.lastIndexOf("/");
        return filePath.substring(lastSlash + 1);
    }

    private void startPlaying(String filePath) {
        stopPlaying();

        player = new MediaPlayer();
        try {
            player.setDataSource(filePath);
            player.prepare();
            player.start();
        } catch (IOException e) {
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void downloadComposedSong(ComposedSongModel song, View view) {
        try {

            view.setAlpha(0.5f);
            view.setEnabled(false);

            Call<ResponseBody> call = WebApi.getInstance().getTechnoSyncService().downloadComposedSongAsMp3(song.getGroupId());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i("TechnoSync", "Room available to join room");
                    if (response.isSuccessful()) {
                        new AsyncFileDownload(
                                ComposedSongsView.this,
                                response.body(),
                                getFilesDir().getAbsolutePath(),
                                song.getGroupId()).execute();

                        song.setIsDownloaded(true);
                        listAdapter.notifyDataSetChanged();
                    } else {
                        String message = "Song is being processed by the server. Please try later!";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                        view.setAlpha(1.0f);
                        view.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("TechnoSync", "Failed to join the room");

                    String message = "Failed to download the song. Please try again!";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    view.setAlpha(1.0f);
                    view.setEnabled(true);
                }
            });
        } catch (Exception e) {
            Log.i("Server Error" , "Can not connect! Restart App!");
            view.setAlpha(1.0f);
            view.setEnabled(true);
        }
    }

    public Button.OnClickListener onDownloadButtonClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() != R.id.downloadSongButton) {
                return;
            }

            ComposedSongModel selectedSong = composedSongsList.get((int)view.getTag());

            if (!selectedSong.isDownloaded()) {
                downloadComposedSong(selectedSong, view);
            }
        }
    };

    public Button.OnClickListener onPlayButtonClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() != R.id.playSongButton) {
                return;
            }

            ComposedSongModel selectedSong = composedSongsList.get((int)view.getTag());
            Button playButton = (Button) view;

            if (selectedSong.isPlaying()) {
                // Pause here
                selectedSong.setIsPlaying(false);
                playButton.setText("PLAY");

                stopPlaying();
            } else {
                // Play here
                selectedSong.setIsPlaying(true);
                playButton.setText("PAUSE");

                startPlaying(selectedSong.getFilePath());
            }
        }
    };

    private static class AsyncFileDownload extends AsyncTask<Void, Void, Void> {

        private WeakReference<ComposedSongsView> activityReference;
        private ResponseBody body;
        private String filesDirPath;
        private String roomId;

        AsyncFileDownload(ComposedSongsView context, ResponseBody body, String filesDirPath, String roomId) {
            this.activityReference = new WeakReference<>(context);
            this.body = body;
            this.filesDirPath = filesDirPath;
            this.roomId = roomId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            boolean writtenToDisk = writeResponseBodyToDisk(body);
            return null;
        }


        private boolean writeResponseBodyToDisk(ResponseBody body) {
            try {
                File futureStudioIconFile = new File(filesDirPath + "/composed_song_" + roomId + ".mp3");

                InputStream inputStream = null;
                OutputStream outputStream = null;

                try {
                    byte[] fileReader = new byte[4096];

                    long fileSize = body.contentLength();
                    long fileSizeDownloaded = 0;

                    inputStream = body.byteStream();
                    outputStream = new FileOutputStream(futureStudioIconFile);

                    while (true) {
                        int read = inputStream.read(fileReader);

                        if (read == -1) {
                            break;
                        }

                        outputStream.write(fileReader, 0, read);

                        fileSizeDownloaded += read;
                    }

                    outputStream.flush();

                    return true;
                } catch (IOException e) {
                    return false;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } catch (IOException e) {
                return false;
            }
        }
    }

    private class ListViewAdapter extends BaseAdapter {
        private final Context mContext;
        private ArrayList<ComposedSongModel> songList;

        public ListViewAdapter(Context context, ArrayList<ComposedSongModel> songList) {
            this.mContext = context;
            this.songList = songList;
        }

        public void setSongs(ArrayList<ComposedSongModel> songs) { this.songList = songs; }

        @Override
        public int getCount() { return this.songList.size(); }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ComposedSongModel song = this.songList.get(position);

            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                convertView = layoutInflater.inflate(R.layout.song_cell_layout, null);

            }

            TextView songNameTextView = convertView.findViewById(R.id.songName);
            String text = "Song_" + position;
            songNameTextView.setText(text);

            Button downloadButton = convertView.findViewById(R.id.downloadSongButton);
            downloadButton.setTag(position);
            downloadButton.setOnClickListener(onDownloadButtonClickListener);

            Button playButton = convertView.findViewById(R.id.playSongButton);
            playButton.setTag(position);
            playButton.setOnClickListener(onPlayButtonClickListener);

            if (song.isDownloaded()) {
                downloadButton.setAlpha(0.0f);
                downloadButton.setEnabled(false);
                playButton.setAlpha(1.0f);
                playButton.setEnabled(true);
            } else {
                downloadButton.setAlpha(1.0f);
                downloadButton.setEnabled(true);
                playButton.setAlpha(0.5f);
                playButton.setEnabled(false);
            }

            return convertView;
        }
    }
}
