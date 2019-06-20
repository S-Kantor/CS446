package ca.uwaterloo.cs446.teamdroids.technosync;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
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

public class CreationView extends AppCompatActivity {

    private static final String MISSING_MESSAGE = "Button Not Assigned! (PROTOTYPE ONLY)";

    LoopPad loopPad;
    InstrumentPad instrumentPad;
    AudioEngine audioEngine;
    EventBus eventBus;

    boolean onLoopPad = true;
    boolean overridePublish = false;


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
            if(loopPad.tiles.get(tileId-1).getDisabled()){
                Toast.makeText(getApplicationContext(), MISSING_MESSAGE, Toast.LENGTH_SHORT).show();
                return;
            }

            //Get image from loop button
            ImageView loopButtonImage = (ImageView) v;
            AnimationDrawable x = (AnimationDrawable) loopButtonImage.getBackground();

            //Play instrument hits, if on instrument pad
            if(!onLoopPad){
                instrumentPad.publishTileHit(tileId-1);
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

    //Switch view to loopPad
    private void displayInstrumentPad(){
        //Get tiles
        List<Tile> tiles = instrumentPad.tiles;

        for(int i = 1; i <=25; i++){
            //Fetch image view
            String buttonId = "loop" + i;
            int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
            ImageView loopButtonImage = (ImageView) findViewById(resId);


            //Determine image
            if(tiles.get(i-1).getDisabled()){
                loopButtonImage.setBackgroundResource(R.drawable.missing_button);
            }
            else{
                loopButtonImage.setBackgroundResource(R.drawable.instrument_button);
            }
        }

    }

    //Switch view to loopPad
    private void displayLoopPad(){
        //Get tiles
        List<Tile> tiles = loopPad.tiles;

        for(int i = 1; i <=25; i++){
            //Fetch imageview
            String buttonId = "loop" + i;
            int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
            ImageView loopButtonImage = (ImageView) findViewById(resId);


            //Determine image
            if(tiles.get(i-1).getDisabled()){
                loopButtonImage.setBackgroundResource(R.drawable.missing_button);
            }
            else{
                loopButtonImage.setBackgroundResource(R.drawable.loop_button);
            }


            //Start/Stop Animation based on current state
            if(loopPad.stateArray[i-1] == 1){
                AnimationDrawable x = (AnimationDrawable) loopButtonImage.getBackground();
                x.start();
            }

        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_view);

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

        //Setup eventbus
        audioEngine = new AudioEngine();
        instrumentPad = new InstrumentPad();
        loopPad = new LoopPad();
        eventBus = new EventBus();

        //Register subscribers
        eventBus.register(audioEngine);

        //Register publishers
        loopPad.setEventBus(eventBus);
        instrumentPad.setEventBus(eventBus);

        //Setup Loop pad with prototype loops
        //This needs to be generalized later
        for(int i = 1; i <= 25; i ++){
            Tile current = new Tile();
            Tile instrument = new Tile();
            current.setTileId(i);

            if(i > 16){
                current.setDisabled(true);
            }
            else {
                String fileName = "prototype_loop" + i;
                current.setFileId(getResId(fileName, R.raw.class));
            }

            instrument.setFileId(getResId("prototype_instrument" +  i, R.raw.class));
            //instrument.setDisabled(true);

            loopPad.tiles.add(current);
            instrumentPad.tiles.add(instrument);
        }


        //Setup audioengine
        audioEngine.setupAudioEngine(getApplicationContext());
        loopPad.publishTileList();
        instrumentPad.publishTileList();

        //Display Loop View
        displayLoopPad();


    }
}
