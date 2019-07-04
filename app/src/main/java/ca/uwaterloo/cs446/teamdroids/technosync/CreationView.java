package ca.uwaterloo.cs446.teamdroids.technosync;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.cs446.teamdroids.technosync.audioengine.AudioEngine;
import ca.uwaterloo.cs446.teamdroids.technosync.common.InstrumentPad;
import ca.uwaterloo.cs446.teamdroids.technosync.common.LoopPad;
import ca.uwaterloo.cs446.teamdroids.technosync.common.StateArray;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;
import ca.uwaterloo.cs446.teamdroids.technosync.common.TileList;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventBus;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventPackage;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventType;
import ca.uwaterloo.cs446.teamdroids.technosync.recordingengine.RecordingEngine;
import ca.uwaterloo.cs446.teamdroids.technosync.visualization.AudioBar;

public class CreationView extends AppCompatActivity {

    private static final String MISSING_MESSAGE = "Button Not Assigned! (PROTOTYPE ONLY)";

    private Toolbar toolbar;

    LoopPad loopPad;
    InstrumentPad instrumentPad;
    AudioEngine audioEngine;
    RecordingEngine recordingEngine;
    EventBus eventBus;


    boolean onLoopPad = true;
    boolean overridePublish = false;
    boolean firstLoad = true;
    boolean loading = false;


    //Get the id of a resource by string
    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    //Get id string of a view
    public static String getId(View view) {
        if (view.getId() == View.NO_ID) return "no-id";
        else return view.getResources().getResourceName(view.getId());
    }


    //Define action for when toggle switch is clicked
    private View.OnClickListener toggleViewType = new View.OnClickListener() {
        public void onClick(View v) {
            //Get image from view
            ImageView imageView = (ImageView) v;

            //Toggle base image
            if(onLoopPad){
                onLoopPad = false;
                imageView.setBackgroundResource(R.drawable.looppad_button);
                displayInstrumentPad();
            }
            else{
                onLoopPad = true;
                imageView.setBackgroundResource(R.drawable.note_button);
                displayLoopPad();
            }
        }
    };


    //Define action for when loop button is clicked
    private View.OnClickListener loopButtonClick = new View.OnClickListener() {
        boolean clicked = false;

        public void onClick(View v) {


            //Determine Tile Id
            String id = getId(v);
            id = id.substring(48);
            int tileId = Integer.parseInt(id);

            //Check for disabled state
            if(loopPad.tiles.get(tileId-1).getDisabled() && onLoopPad){
                Toast.makeText(getApplicationContext(), MISSING_MESSAGE, Toast.LENGTH_SHORT).show();
                return;
            }
            if(instrumentPad.tiles.get(tileId-1).getDisabled() && !onLoopPad){
                Toast.makeText(getApplicationContext(), MISSING_MESSAGE, Toast.LENGTH_SHORT).show();
                return;
            }

            //Get image from loop button
            ImageView loopButtonImage = (ImageView) v;
            AnimationDrawable x = (AnimationDrawable) loopButtonImage.getBackground();

            //Play instrument hits, if on instrument pad
            if(!onLoopPad){
                instrumentPad.publishTileHit(tileId-1);
                x.stop();
                x.start();
                return;
            }


            //Update state array //Generalize later
            int newState = clicked ? 0 : 1;
            loopPad.stateArray[tileId - 1] = newState;
            loopPad.publishStateArray();


            //Start Stop Animation
            if(!clicked){
                clicked = true;
                x.start();
            }
            else {
                clicked = false;
                x.stop();
            }
        }
    };

    private void flipTile(final ImageView imageView, final int backImage, final int tileId, final  boolean loopabale){
        imageView.animate().rotationY(90f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                //Determine image
                if(instrumentPad.tiles.get(tileId).getDisabled() && !loopabale){
                    imageView.setBackgroundResource(R.drawable.missing_button);
                }
                else if(loopPad.tiles.get(tileId).getDisabled() && loopabale){
                    imageView.setBackgroundResource(R.drawable.missing_button);
                }
                else{
                    imageView.setBackgroundResource(backImage);
                }

                imageView.setRotationY(270f);
                imageView.animate().rotationY(360f).setListener(null);

                //Start/Stop Animation based on current state
                if(loopPad.stateArray[tileId] == 1 && loopabale){
                    AnimationDrawable x = (AnimationDrawable) imageView.getBackground();
                    x.start();
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
    }


    //Switch view to loopPad
    private void displayInstrumentPad(){

        for(int i = 1; i <=25; i++){
            //Fetch image view
            String buttonId = "loop" + i;
            int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
            final ImageView loopButtonImage = (ImageView) findViewById(resId);
            final int tileId = i -1;

            //Flip image to instrument pad
            flipTile(loopButtonImage, R.drawable.instrument_button, tileId, false);
        }

    }

    //Switch view to loopPad
    private void displayLoopPad(){
        for(int i = 1; i <=25; i++){
            //Fetch imageview
            String buttonId = "loop" + i;
            int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
            final int tileId = i -1;
            final ImageView loopButtonImage = (ImageView) findViewById(resId);


            if(!firstLoad){
               flipTile(loopButtonImage, R.drawable.loop_button, tileId, true);
            }
            else {
                //Determine image
                if(loopPad.tiles.get(tileId).getDisabled()){
                    loopButtonImage.setBackgroundResource(R.drawable.missing_button);
                } else{
                    loopButtonImage.setBackgroundResource(R.drawable.loop_button);
                }
            }
        }

    }

    //Animate background
    private void animateBackground(){
        AnimationDrawable animationDrawable = (AnimationDrawable) findViewById(R.id.beat_pad_layout).getBackground();
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }


    //Set State to Loading
    private void startLoading(){
        //Disable all interaction
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //Display Loader
        findViewById(R.id.pBar).setVisibility(View.VISIBLE);
    }

    private void stopLoading(){
        //Enable Interaction
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //Hide Loader
        findViewById(R.id.pBar).setVisibility(View.INVISIBLE);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_view);

        //Setup Loading
        startLoading();


        // Set up the toolbar
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //Setup on click animations for all image views
        for(int i = 1; i <=25; i++){
            String buttonId = "loop" + i;
            int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
            ImageView loopButtonImage = (ImageView) findViewById(resId);
            loopButtonImage.setOnClickListener(loopButtonClick);
        }

        //Setup on click for toggle button
        int resId = getResources().getIdentifier("togglePad", "id", getPackageName());
        ImageView toggleButton = (ImageView) findViewById(resId);
        toggleButton.setBackgroundResource(R.drawable.note_button);
        toggleButton.setOnClickListener(toggleViewType);

        Button button = findViewById(R.id.notePadLauncher);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(CreationView.this, NotePad.class);
                startActivity(myIntent);
            }
        });


        //Setup Visualization
        AudioBar audioBar = (AudioBar) findViewById(R.id.barVisualizer);
        audioBar.setPlayer(0);

        //Setup eventbus
        audioEngine = new AudioEngine();
        recordingEngine = new RecordingEngine();
        instrumentPad = new InstrumentPad();
        loopPad = new LoopPad();
        eventBus = new EventBus();

        //Register subscribers
        eventBus.register(audioEngine);
        eventBus.register(recordingEngine);

        //Register publishers
        loopPad.setEventBus(eventBus);
        instrumentPad.setEventBus(eventBus);

        //Setup Loop pad with prototype loops
        //This needs to be generalized later
        for(int i = 1; i <= 25; i ++){
            Tile current = new Tile();
            Tile instrument = new Tile();
            current.setTileId(i);
            instrument.setTileId(i);

            if(i > 16){
                current.setDisabled(true);
            }
            else {
                String fileName = "prototype_loop" + i;
                current.setFileId(getResId(fileName, R.raw.class));
                current.setFileString(fileName);
            }

            instrument.setFileId(getResId("prototype_instrument" +  i, R.raw.class));
            instrument.setFileString("prototype_instrument" +  i);
            instrument.setLoopable(false);
            current.setLoopable(true);
            //instrument.setDisabled(true);

            loopPad.tiles.add(current);
            instrumentPad.tiles.add(instrument);
        }




        //Setup audioengine
        audioEngine.setupAudioEngine(getApplicationContext());

        //Setup stop loading
        audioEngine.getSoundPool().setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {

                stopLoading();
            }
        });

        loopPad.publishTileList();
        instrumentPad.publishTileList();

        //Display Loop View
        displayLoopPad();
        animateBackground();

        firstLoad = false;

    }

    // Add menu items to the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.layout.toolbar_menu, menu);
        return true;
    }

    // Handle toolbar menu items interaction
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.change_presets) {

        }
        else if (id == R.id.end_session) {

        }

        return super.onOptionsItemSelected(item);
    }
}
