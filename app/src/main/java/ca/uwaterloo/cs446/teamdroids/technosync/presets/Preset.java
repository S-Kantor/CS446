package ca.uwaterloo.cs446.teamdroids.technosync.presets;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.cs446.teamdroids.technosync.R;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;

public class Preset {

    private List<Tile> loopPadTiles;
    private List<Tile> instrumentPadTiles;
    private Context context;


    //Get the id of a resource by string
    private static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            //e.printStackTrace();
            return -1;
        }
    }

    //Create Tile
    private Tile createTile(String fileName, boolean loopable, int tileId){
        Tile tile = new Tile();
        tile.setTileId(tileId);

        //Set Disabled
        if(fileName.contains("missing")){
            tile.setDisabled(true);
            return tile;
        }
        else{
            tile.setFileId(getResId(fileName, R.raw.class));
            tile.setFileString(fileName);
            tile.setLoopable(loopable);
        }

        return tile;
    }

    //Get FileInputStream
    private InputStream getFileStream(String fileName){
        //Get res id
        int resid = getResId(fileName, R.raw.class);

        try {
            //Determine if file is a packaged file or internal file
            if (resid > -1) {
                return context.getResources().openRawResource(resid);
            } else {
                return context.openFileInput(fileName);
            }
        }
        catch (Exception ex){
            return null;
        }
    }

    public Preset(Context context){
        loopPadTiles = new ArrayList<>();
        instrumentPadTiles = new ArrayList<>();
        this.context = context;
    }

    //Read in file
    public void readPresetFile(String fileName){
        try {
            //Clear existing arrays
            loopPadTiles.clear();
            instrumentPadTiles.clear();

            //Get file
            InputStream fileInputStream = getFileStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));

            //Read through file line by line
            for(int i = 1; i <= 50; i++) {
                //Get sound file name
                String line = reader.readLine();

                //Looped tiles
                if(i <= 25) loopPadTiles.add(createTile(line, true, i));
                else instrumentPadTiles.add(createTile(line, false, i));
            }
        }
        catch(Exception ex) {
            //Handle error
            //TODO
            Log.i("ERROR", "preset-read-error");
        }
    }
    public List<Tile> getLoopPadTiles(){return  loopPadTiles;}
    public List<Tile> getInstrumentPadTiles() {return instrumentPadTiles;}
}
