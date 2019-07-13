package ca.uwaterloo.cs446.teamdroids.technosync.recordingengine;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.cs446.teamdroids.technosync.api.ApiPath;
import ca.uwaterloo.cs446.teamdroids.technosync.api.RoomResponse;
import ca.uwaterloo.cs446.teamdroids.technosync.api.WebApi;
import ca.uwaterloo.cs446.teamdroids.technosync.common.StateArray;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;
import ca.uwaterloo.cs446.teamdroids.technosync.common.TileList;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventPackage;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventType;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.Subscriber;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordingEngine extends Subscriber {

    CurrentState currentState;
    RecordingList recordingList;
    WebApi webApi;
    boolean recording = false;

    //Record changes to instrument pad
    public void instrumentPadUpdate(Tile tile){
        if (!recording) return;
        RecordingEntry recordingEntry = new RecordingEntry(tile.getFileString(), false);
        recordingList.newEntry(recordingEntry);
    }


    //Record changes playing beats
    public void loopPadUpdate(StateArray stateArray){
        if(!recording) return;

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

    //Send local recording to server
    public void sendRecording(){
        //Upload
        Call<RecordingList> call = webApi.getTechnoSynchService().publishRecording(5);
        call.enqueue(new Callback<RecordingList>() {
            @Override
            public void onResponse(Call<RecordingList> call, Response<RecordingList> response) {
                if (response.isSuccessful()) {
                    Log.i("TechnoSynch", "Call succeeded");
                } else {
                    Log.i("TechnoSynch", "Got an error response, maybe like a 403");
                }
            }

            @Override
            public void onFailure(Call<RecordingList> call, Throwable t) {
                Log.i("TechnoSynch", "Actual http call failed (no internet, wrong url, etc.");
            }
        });
    }

    //Receive event from event bus
    public void notify(EventPackage eventPackage){
        try {
            EventType eventType = eventPackage.getEventType();


            //Handle events with no data
            //Start recording
            if (eventType == EventType.RECORDING_START) {
                recording = true;
                return;
            }
            //Stop Recording
            else if (eventType == EventType.RECORDING_END) {
                recording = false;
                sendRecording();
                return;
            }

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

    //Get recording status
    public boolean isRecording(){
        return recording;
    }


    //Initialize
    public RecordingEngine(WebApi webApi){
        this.currentState = new CurrentState();
        this.recordingList = new RecordingList();
        this.webApi = webApi;
    }




}

