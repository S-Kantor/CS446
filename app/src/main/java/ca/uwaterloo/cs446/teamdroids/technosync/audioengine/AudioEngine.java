package ca.uwaterloo.cs446.teamdroids.technosync.audioengine;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.cs446.teamdroids.technosync.R;
import ca.uwaterloo.cs446.teamdroids.technosync.common.StateArray;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;
import ca.uwaterloo.cs446.teamdroids.technosync.common.TileList;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventPackage;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventType;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.Subscriber;

public class AudioEngine extends Subscriber {

    private  SoundPool soundPool;
    private Context applicationContext;
    private FileManager fileManager;
    private PlaybackManager playbackManager;

    //Update playback of loops
    //States length should always be 25
    private void loopStateUpdate(int states[]){
        for(int i = 0; i < 25; i++){
            Integer tileId = i + 1;
            if(states[i] == 0){
                playbackManager.stopStream(tileId, soundPool);
            }
            else {
                playbackManager.playFile(tileId, true, fileManager, soundPool);
            }
        }
    }

    //Play instrument hit
    private void instrumentHit(int tileId){
        playbackManager.playFile(tileId, false, fileManager, soundPool);
    }


    //Receive event from event bus
    public void notify(EventPackage eventPackage){
        try {
            EventType eventType = eventPackage.getEventType();

            //Get stream of event data
            byte bytes[] = Base64.decode(eventPackage.getSerializedData().getBytes(), 0);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);


            //Update collections of loops
            if (eventType == EventType.LOOPPAD_MAPPING_UPDATE) {
                TileList tileList = (TileList) objectInputStream.readObject();
                fileManager.loadFilesFromTiles(tileList.getTiles(), soundPool, applicationContext);
            }
            //Update collections of instruments
            else if (eventType == EventType.INSTRUMENTPAD_MAPPING_UPDATE) {
                TileList tileList = (TileList) objectInputStream.readObject();
                fileManager.loadFilesFromTiles(tileList.getTiles(), soundPool, applicationContext);
            }
            //Update loop playback
            else if (eventType == EventType.INSTRUMENTPAD_SOUND_HIT) {
                Tile tile = (Tile) objectInputStream.readObject();
                instrumentHit(tile.getTileId());
            }
            //Update loop playback
            else if (eventType == EventType.LOOPPAD_STATE_UPDATE) {
                StateArray stateArray = (StateArray) objectInputStream.readObject();
                loopStateUpdate(stateArray.getStateArray());
            }
        }
        catch(Exception e){
            //Error
            // TODO needs logger.
        }
    }

    //Initialize SoundPool
    public void setupAudioEngine(Context applicationContext){
        soundPool = new SoundPool(50, 3, 0);
        fileManager = new FileManager();
        playbackManager = new PlaybackManager();
        this.applicationContext = applicationContext;
    }

    public SoundPool getSoundPool() {
        return soundPool;
    }
}
