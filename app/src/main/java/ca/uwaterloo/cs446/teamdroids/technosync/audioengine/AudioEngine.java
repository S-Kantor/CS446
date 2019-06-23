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
    private List<Integer> loopSoundIds;
    private List<Integer> instrumentSoundIds;
    private Map<Integer, Integer> soundToStream;
    private int currentLoopStates[] = new int[25];
    private Context applicationContext;

    //Load in all loops for current loop pad
    private void loadLoops(List<Tile> tiles){
        //Clear all current streams
        loopSoundIds.clear();
        // soundPool.release();

        //Read in all audio loops
        for(int i = 0; i < tiles.size(); i++){
            if(!tiles.get(i).getDisabled()) {
                //Load file and get soundId
                int fileId = tiles.get(i).getFileId();
                int streamId = soundPool.load(applicationContext, fileId, 1);
                loopSoundIds.add(streamId);
            }
            else{
                //Add fake soundId
                loopSoundIds.add(-1);
            }
        }
    }

    //Load in all instrument hits for current instrument pad
    private void loadInstrumentHits(List<Tile> tiles){
        //Clear all current streams
        instrumentSoundIds.clear();
        // soundPool.release();

        //Read in all audio loops
        for(int i = 0; i < tiles.size(); i++){
            if(!tiles.get(i).getDisabled()) {
                //Load file and get soundId
                int fileId = tiles.get(i).getFileId();
                int streamId = soundPool.load(applicationContext, fileId, 1);
                instrumentSoundIds.add(streamId);
            }
            else{
                //Add fake soundId
                instrumentSoundIds.add(-1);
            }
        }
    }

    //Play/Stop Audio
    private void playAudio(int soundId, int state, boolean loop){
        //Get stream
        Integer streamId = soundToStream.get(soundId);
        int loopStatus = loop ? -1 : 0;

        //Stop
        if(state == 0){
            if(streamId != null){
                soundPool.stop(streamId);
                soundToStream.remove(soundId);
            }
        }
        //Play
        else {
            //Create new stream
            if(streamId == null){
                streamId = soundPool.play(soundId, 1, 1, 0, loopStatus, 1);
                soundToStream.put(soundId, streamId);
            }
            //Use existing stream
            else{
                soundPool.setLoop(streamId, loopStatus);
                soundPool.resume(streamId);
            }
        }
    }

    //Update playback of loops
    //States length should always be 25
    private void loopStateUpdate(int states[]){
        //Compare against current state
        for(int i = 0; i < 25; i++){
            //State change
            if(currentLoopStates[i] != states[i]){
                currentLoopStates[i] = states[i];
                playAudio(loopSoundIds.get(i), currentLoopStates[i], true);
            }
        }
    }

    //Play instrument hit
    private void instrumentHit(int tileId){
        playAudio(instrumentSoundIds.get(tileId), 1, false);
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
                loadLoops(tileList.getTiles());
            }
            //Update collections of instruments
            else if (eventType == EventType.INSTRUMENTPAD_MAPPING_UPDATE) {
                TileList tileList = (TileList) objectInputStream.readObject();
                loadInstrumentHits(tileList.getTiles());
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
        loopSoundIds = new ArrayList<>();
        instrumentSoundIds = new ArrayList<>();
        soundToStream = new HashMap<>();
        this.applicationContext = applicationContext;
    }
}
