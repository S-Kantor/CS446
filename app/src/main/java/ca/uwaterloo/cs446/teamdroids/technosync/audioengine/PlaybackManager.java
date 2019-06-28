package ca.uwaterloo.cs446.teamdroids.technosync.audioengine;

import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;

public class PlaybackManager {

    private Map<Integer, Integer> tileToStream;

    //Play an audio file
    public void playFile(Integer tileId, Boolean loop, FileManager fileManager, SoundPool soundPool){

        //Setup Loop status
        Integer loopStatus = loop ? -1 : 0;
        Integer streamId;

        //Play based on current status of looped stream
        if(tileToStream.get(tileId) == null || !loop){
            streamId = soundPool.play(fileManager.getSoundId(tileId, loop), 1, 1, 0, loopStatus, 1);

            //Keep track stream for future use in case of a loopable file
            if(loop){
                tileToStream.put(tileId, streamId);
            }
        }
    }

    //Stop playback of a stream
    public void stopStream(Integer tileId, SoundPool soundPool){
        Integer streamId = tileToStream.get(tileId);
        if(streamId != null){
            soundPool.stop(streamId);
            tileToStream.remove(tileId);
        }
    }


    public PlaybackManager(){
        tileToStream = new HashMap<>();
    }

}
