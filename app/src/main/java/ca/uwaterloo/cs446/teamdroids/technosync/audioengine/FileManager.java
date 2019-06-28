package ca.uwaterloo.cs446.teamdroids.technosync.audioengine;

import android.content.Context;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;

public class FileManager {

    private Map<Integer, Integer> loopableFiles;
    private Map<Integer, Integer> regularFiles;

    //Load all files associated with tiles
    public void loadFilesFromTiles(List<Tile> tiles, SoundPool soundPool, Context applicationContext){
        for(int i = 0; i < tiles.size(); i++){
            Tile currentTile = tiles.get(i);

            //Load file
            Integer soundId = soundPool.load(applicationContext, currentTile.getFileId(), 1);

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
