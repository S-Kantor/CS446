package ca.uwaterloo.cs446.teamdroids.technosync;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import ca.uwaterloo.cs446.teamdroids.technosync.audioengine.AudioEngine;
import ca.uwaterloo.cs446.teamdroids.technosync.common.LoopPad;
import ca.uwaterloo.cs446.teamdroids.technosync.common.StateArray;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;
import ca.uwaterloo.cs446.teamdroids.technosync.common.TileList;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventBus;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventPackage;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventType;

public class CreationView extends AppCompatActivity {

    LoopPad loopPad;
    AudioEngine audioEngine;
    EventBus eventBus;

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getId(View view) {
        if (view.getId() == View.NO_ID) return "no-id";
        else return view.getResources().getResourceName(view.getId());
    }

    String tileListSerializer(){
        try {
            TileList tileList = new TileList();
            tileList.setTiles(loopPad.tiles);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(tileList);
            so.flush();
            return new String(Base64.encode(bo.toByteArray(), 0));
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }

    String stateArraySerializer(){
        try {
            StateArray stateArray = new StateArray();
            stateArray.setStateArray(loopPad.stateArray);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(stateArray);
            so.flush();

            return new String(Base64.encode(bo.toByteArray(), 0));
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }




    //Define action for when loop button is clicked
    private View.OnClickListener loopButtonClick = new View.OnClickListener() {
        boolean clicked = false;

        public void onClick(View v) {
            //Get image from loop button
            ImageView loopButtonImage = (ImageView) v;
            AnimationDrawable x = (AnimationDrawable) loopButtonImage.getBackground();

            //Determine Tile Id
            String id = getId(v);
            id = id.substring(48);
            int tileId = Integer.parseInt(id);

            //Update state array //Generalize later
            int newState = clicked ? 0 : 1;
            loopPad.stateArray[tileId - 1] = newState;
            EventPackage newPackage = new EventPackage();
            newPackage.setEventType(EventType.LOOPPAD_STATE_UPDATE);
            newPackage.setSerializedData(stateArraySerializer());
            eventBus.newEvent(newPackage);


            //Start Stop Animation
            if(!clicked){
                x.start();
                clicked = true;
            }
            else {
                x.stop();
                clicked = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_view);

        //Setup on click animations for all image views
        for(int i = 1; i <=25; i++){
            String buttonId = "loop" + i;
            int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
            ImageView loopButtonImage = (ImageView) findViewById(resId);
            loopButtonImage.setBackgroundResource(R.drawable.loop_button);
            loopButtonImage.setOnClickListener(loopButtonClick);
        }

        //Setup Loop pad with prototype loops
        //This needs to be generalized later
        loopPad = new LoopPad();
        for(int i = 1; i <= 25; i ++){
            Tile current = new Tile();
            current.setTileId(i);

            if(i > 16){
                current.setDisabled(true);
            }
            else {
                String fileName = "prototype_loop" + i;
                current.setFileId(getResId(fileName, R.raw.class));
            }

            loopPad.tiles.add(current);
        }

        //Setup eventbus
        audioEngine = new AudioEngine();
        eventBus = new EventBus();
        eventBus.register(audioEngine);

        //Setup audioengine
        audioEngine.setupAudioEngine(getApplicationContext());

        EventPackage initialSetup = new EventPackage();
        initialSetup.setEventType(EventType.LOOPPAD_MAPPING_UPDATE);
        initialSetup.setSerializedData(tileListSerializer());
        eventBus.newEvent(initialSetup);

    }
}
