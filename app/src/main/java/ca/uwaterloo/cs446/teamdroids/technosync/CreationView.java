package ca.uwaterloo.cs446.teamdroids.technosync;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.SoundPool;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.cs446.teamdroids.technosync.api.WebApi;
import ca.uwaterloo.cs446.teamdroids.technosync.audioengine.AudioEngine;
import ca.uwaterloo.cs446.teamdroids.technosync.common.InstrumentPad;
import ca.uwaterloo.cs446.teamdroids.technosync.common.LoopPad;
import ca.uwaterloo.cs446.teamdroids.technosync.common.StateArray;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;
import ca.uwaterloo.cs446.teamdroids.technosync.common.TileList;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventBus;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventPackage;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventType;
import ca.uwaterloo.cs446.teamdroids.technosync.presets.PresetManager;
import ca.uwaterloo.cs446.teamdroids.technosync.recordingengine.RecordingEngine;
import ca.uwaterloo.cs446.teamdroids.technosync.visualization.AudioBar;

public class CreationView extends AppCompatActivity {

    private static final String MISSING_MESSAGE = "Button Not Assigned! (PROTOTYPE ONLY)";
    private static final String IS_PLAYING_AUDIO = "Please Stop Playing Loops Before Executing This Action";
    private static final String IN_PRACTICE_MODE = "You Can Not Record In Practice Mode";
    private static final String IS_RECORDING = "Can Not Execute Action While Recording";

    private Toolbar toolbar;

    LoopPad loopPad;
    InstrumentPad instrumentPad;
    AudioEngine audioEngine;
    RecordingEngine recordingEngine;
    EventBus eventBus;
    WebApi webApi;
    PresetManager presetManager;

    boolean onLoopPad = true;
    boolean firstLoad = true;

    //Loading counters
    int loadedCount = 0;
    int maxCount = 0;

    //Group id
    String groupId;



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

    //Define action for when record is clicked
    //Define action for when toggle switch is clicked
    private View.OnClickListener toggleRecord = new View.OnClickListener() {
        public void onClick(View v) {

            //Check if click is valid
            if(groupId == null){
                Toast.makeText(getApplicationContext(), IN_PRACTICE_MODE, Toast.LENGTH_SHORT).show();
                return;
            }

            //Get image from view
            ImageView imageView = (ImageView) v;

            //Check for current status
            if(audioEngine.isPlayingAudio()){
                Toast.makeText(getApplicationContext(), IS_PLAYING_AUDIO, Toast.LENGTH_SHORT).show();
                return;
            }

            //End recording
            if(recordingEngine.isRecording()){
                EventPackage eventPackage = new EventPackage();
                eventPackage.setEventType(EventType.RECORDING_END);
                eventPackage.setSerializedData("---");
                eventBus.newEvent(eventPackage);
                CreationView.super.onBackPressed();
            }
            //Start Recording
            else{
                EventPackage eventPackage = new EventPackage();
                eventPackage.setEventType(EventType.RECORDING_START);
                eventPackage.setSerializedData("----");
                eventBus.newEvent(eventPackage);
                imageView.setBackgroundResource(R.drawable.record_button_progress);
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

    //Display Preset Selector
    private void displayPresetSelector(){

        //Check playback status
        if(audioEngine.isPlayingAudio()){
            Toast.makeText(getApplicationContext(), IS_PLAYING_AUDIO, Toast.LENGTH_SHORT).show();
            return;
        }

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a Preset");

        //Get list of all presets
        List<String> presets = presetManager.getListOfPresets();
        String[] presetsArray = new String[presets.size()];
        presetsArray = presets.toArray(presetsArray);

        //Remove preset from string and capitalize first letter
        for(int i = 0; i < presetsArray.length; i++){
            presetsArray[i] = presetsArray[i].substring(6);
            presetsArray[i] = presetsArray[i].substring(0, 1).toUpperCase() + presetsArray[i].substring(1);
        }

        //Setup List
        builder.setItems(presetsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Change preset
                setUpPreset(presets.get(which));
            }
        });

        // create and show the alert dialog
        //AlertDialog dialog =  builder.create();
        builder.show();
    }

    //Animate background
    private void animateBackground(){
        AnimationDrawable animationDrawable = (AnimationDrawable) findViewById(R.id.beat_pad_layout).getBackground();
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }


    //Set up Loop pad and Instrument Pad with new preset
    private void setUpPreset(String presetName){
        //Clear loading counters
        loadedCount = 0;
        maxCount = 0;
        startLoading();

        //Read file
        presetManager.readNewPreset(presetName);
        audioEngine.resetAudioEngine();
        bindLoadingIcon();

        //Get tiles
        loopPad.tiles = presetManager.getLoopPadTiles();
        instrumentPad.tiles = presetManager.getInstrumentPadTiles();

        //Update max count
        TileList tileList = new TileList();
        tileList.setTiles(loopPad.tiles);
        maxCount += tileList.getNumberOfValidTiles();
        tileList.setTiles(instrumentPad.tiles);
        maxCount += tileList.getNumberOfValidTiles();

        //Stop loading if there is no files to load
        if(maxCount == 0) stopLoading();

        //Publish changes
        loopPad.publishTileList();
        instrumentPad.publishTileList();

        //Update images
        if(onLoopPad) displayLoopPad();
        else displayInstrumentPad();
    }

    //Bind Loading Icon
    private void bindLoadingIcon(){
        //Setup stop loading
        audioEngine.getSoundPool().setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loadedCount++;
                if(loadedCount == maxCount) stopLoading();
                Log.i("loaded count", String.valueOf(loadedCount) + " " + String.valueOf(maxCount));
            }
        });
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


    private void getGroupId(){
        Bundle creationBundle = getIntent().getExtras();
        if(creationBundle != null){
            groupId = creationBundle.getString("group-id");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_view);

        //Setup Loading
        startLoading();


        //Get group id
        getGroupId();

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


        //Setup Visualization
        AudioBar audioBar = (AudioBar) findViewById(R.id.barVisualizer);
        audioBar.setPlayer(0);

        //Setup WebApi
        if(groupId != null) webApi = WebApi.getInstance();

        //Setup Preset Manager
        presetManager = new PresetManager(getApplicationContext());

        //Setup eventbus
        audioEngine = new AudioEngine();
        recordingEngine = new RecordingEngine(webApi, groupId);
        instrumentPad = new InstrumentPad();
        loopPad = new LoopPad();
        eventBus = new EventBus();

        //Register subscribers
        eventBus.register(audioEngine);
        eventBus.register(recordingEngine);

        //Register publishers
        loopPad.setEventBus(eventBus);
        instrumentPad.setEventBus(eventBus);

        //Setup audioengine
        audioEngine.setupAudioEngine(getApplicationContext());

        //Load Default Preset
        setUpPreset("presetprototype");

        //Setup recording button
        ImageView recordingButton = (ImageView) findViewById(R.id.recordPad);
        recordingButton.setOnClickListener(toggleRecord);
        recordingButton.setBackgroundResource(R.drawable.record_button);


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
            displayPresetSelector();
        }
        else if (id == R.id.end_session) {
            //Check playback status
            if(audioEngine.isPlayingAudio()){
                Toast.makeText(getApplicationContext(), IS_PLAYING_AUDIO, Toast.LENGTH_SHORT).show();
            }
            else if(recordingEngine.isRecording()){
                Toast.makeText(getApplicationContext(), IS_RECORDING, Toast.LENGTH_SHORT).show();
            }
            else {
                CreationView.super.onBackPressed();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
