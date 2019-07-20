package ca.uwaterloo.cs446.teamdroids.technosync;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Button.OnClickListener onDownloadButtonClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() != R.id.downloadSongButton) {
                return;
            }

            ComposedSongModel selectedSong = composedSongsList.get((int)view.getTag());

            if (!selectedSong.isDownloaded()) {
                view.setAlpha(0.5f);
                view.setEnabled(false);

                // TODO: DOWNLOAD SONG HERE USING THE GROUP ID and set selectedSong property to downloaded
                // START DOWNLOAD NOW and enable Play Button once downloaded, set isDownloaded to true
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
