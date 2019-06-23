package ca.uwaterloo.cs446.teamdroids.technosync;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;

public class NotePad extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_pad);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        Button b1 =  findViewById(R.id.note1);
        Button b2 =  findViewById(R.id.note2);
        Button b3 =  findViewById(R.id.note3);
        Button b4 =  findViewById(R.id.note4);

        SoundPool soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

        int note1 = soundPool.load(this,R.raw.kick3 ,1);
        int note2 = soundPool.load(this, R.raw.hat1, 1);
        int note3  = soundPool.load(this, R.raw.kick2, 1);
        int note4 = soundPool.load(this,R.raw.hat1 ,1);

        HashMap soundsMap = new HashMap<Button, int[]>();
        HashMap clickedMap = new HashMap<Button, Integer>();

        int[] b1array = {note1,0};
        int[] b2array = {note2,0};
        int[] b3array = {note3,0};
        int[] b4array = {note4,0};


        soundsMap.put(b1, b1array);
        soundsMap.put(b2, b2array);
        soundsMap.put(b3, b3array);
        soundsMap.put(b4, b4array);

        clickedMap = new HashMap<Button, Integer>();
        clickedMap.put(b1, 1);
        clickedMap.put(b2, 0);
        clickedMap.put(b3, 0);
        clickedMap.put(b4, 0);


    }

}
