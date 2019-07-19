package ca.uwaterloo.cs446.teamdroids.technosync.audioengine;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;

public class FileManager {

    private Map<Integer, Integer> loopableFiles;
    private Map<Integer, Integer> regularFiles;

    //Unload a maps worth of sounds
    private void unloadSoundMap(Map<Integer, Integer> soundMap, SoundPool soundPool){
        Iterator it = soundMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            //Unload value
            Integer value = (Integer) pair.getValue();
            if(value != null) soundPool.unload(value.intValue());


            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    //Unload all sounds
    public void unloadAllSoundIds(boolean loopable){
        if(loopable) {
            loopableFiles.clear();
        }
        else {
            regularFiles.clear();
        }
    }


    //Load all files associated with tiles
    public void loadFilesFromTiles(List<Tile> tiles, SoundPool soundPool, Context applicationContext){
        for(int i = 0; i < tiles.size(); i++){
            Tile currentTile = tiles.get(i);

            if(currentTile.getDisabled()) continue;

            //Load file from app package
            Integer soundId;
            if(currentTile.getFileId() != -1){
                soundId = soundPool.load(applicationContext, currentTile.getFileId(), 1);
            }
            //Load file from internal storage
            else{
                String filename = currentTile.getFileString() + ".mp4";
                String filePath = applicationContext.getFilesDir().getAbsolutePath() + "/" + filename;
                Log.i("Loading Custom Beat", filePath);
                soundId = soundPool.load(filePath, 1);
            }


            //Update mapping
            if(currentTile.getLoopable()){
                loopableFiles.put(currentTile.getTileId(), soundId);
            }
            else {
                regularFiles.put(currentTile.getTileId(), soundId);
            }
        }
    }

    //Fetch a sound id for a tile
    public Integer getSoundId(Integer tileId, boolean loopable){
        if(loopable) return loopableFiles.get(tileId);
        else return  regularFiles.get(tileId);
    }

    public FileManager(){
        loopableFiles = new HashMap<>();
        regularFiles = new HashMap<>();
    }

}
