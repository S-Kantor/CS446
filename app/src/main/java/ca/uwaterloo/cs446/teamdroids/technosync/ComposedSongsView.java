package ca.uwaterloo.cs446.teamdroids.technosync;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ComposedSongsView extends AppCompatActivity {

    public Button refreshButton;
    public ListView listOfSongs;
    public List<String> composedSongsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composed_songs_view);

        refreshButton = (Button) findViewById(R.id.refreshButton);
        setupRefreshButton();
        listOfSongs = (ListView) findViewById(R.id.listOfComposedSongs);

        composedSongsList = fetchSavedComposedSongs();
    }

    private void setupRefreshButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: IMPLEMENT THIS
            }
        });
    }

    private List<String> fetchSavedComposedSongs() {
        // Fetch the list of group IDs the user has been a part of
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String finishedSongsString = preferences.getString("composedSongGroups", "");
        String[] songs = finishedSongsString.split(",");
        List<String> songsList = new ArrayList<String>();

        for (int i = songs.length - 1; i >= 0; i--) {
            songsList.add(songs[i]);
        }

        return songsList;
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
            String text = "Song" + Integer.toString(position) + "1";
            songNameTextView.setText(text);

            Button downloadButton = convertView.findViewById(R.id.downloadSongButton);
            if song.isDownloaded() {

            }

            return convertView;
        }
    }
}
