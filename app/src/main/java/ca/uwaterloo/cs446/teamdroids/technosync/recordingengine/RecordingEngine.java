package ca.uwaterloo.cs446.teamdroids.technosync.recordingengine;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.cs446.teamdroids.technosync.common.StateArray;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;
import ca.uwaterloo.cs446.teamdroids.technosync.common.TileList;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventPackage;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventType;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.Subscriber;
public class RecordingEngine extends Subscriber {

    CurrentState currentState;
    RecordingList recordingList;

    //Record changes to instrument pad
    public void instrumentPadUpdate(Tile tile){
        RecordingEntry recordingEntry = new RecordingEntry(tile.getFileString(), false);
        recordingList.newEntry(recordingEntry);
    }


    //Record changes playing beats
    public void loopPadUpdate(StateArray stateArray){
        //Get hit tile value
        Integer changedTile = currentState.determineChange(stateArray);
        String fileString = currentState.tileIdToFileName(changedTile, true);

        //No chaange, so don't record
        if(changedTile == -1){
            return;
        }


        //Update current state
        currentState.setStateArray(stateArray);

        //Record value
        recordingList.newEntry(new RecordingEntry(fileString, true));
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
                currentState.setLoopList(tileList);
            }
            //Update collections of instruments
            else if (eventType == EventType.INSTRUMENTPAD_MAPPING_UPDATE) {
                TileList tileList = (TileList) objectInputStream.readObject();
                currentState.setInstrumentList(tileList);
            }
            //Update instrument playback
            else if (eventType == EventType.INSTRUMENTPAD_SOUND_HIT) {
                Tile tile = (Tile) objectInputStream.readObject();
                instrumentPadUpdate(tile);
            }
            //Update loop playback
            else if (eventType == EventType.LOOPPAD_STATE_UPDATE) {
                StateArray stateArray = (StateArray) objectInputStream.readObject();
                loopPadUpdate(stateArray);
            }
        }
        catch(Exception e){
            //Error
            // TODO needs logger.
        }
    }


    //Initialize
    public RecordingEngine(){
        currentState = new CurrentState();
        recordingList = new RecordingList();
    }



}

