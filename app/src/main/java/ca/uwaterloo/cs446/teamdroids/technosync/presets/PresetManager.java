package ca.uwaterloo.cs446.teamdroids.technosync.presets;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.cs446.teamdroids.technosync.R;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;

public class PresetManager {

    Preset currentPreset;
    Context context;
    String presetName = "No Preset Selected";

    //Get list of presets
    public List<String> getListOfPresets(){
        List<String> presets = new ArrayList<>();

        //Get list of files
        Field[] fields = R.raw.class.getFields();
        File[] internalFiles = context.getFilesDir().listFiles();


        //Add presets from app package
        for(Field file : fields){
            if(file.getName().contains("preset")){
                presets.add(file.getName());
            }
        }

        //Add presets from internal storage
        for(File file : internalFiles){
            if(file.getName().contains("preset")){
                String fullFileName = file.getName();
                String trimmedFileName = fullFileName.substring(0, fullFileName.length() - 4);
                presets.add(trimmedFileName);
            }
        }

        return presets;
    }

    //Read new preset
    public void readNewPreset(String fileName){
        presetName = fileName;
        currentPreset.readPresetFile(fileName);
    }

    //Get Tilelists
    public List<Tile> getLoopPadTiles(){
        return  currentPreset.getLoopPadTiles();
    }
    public List<Tile> getInstrumentPadTiles(){
        return  currentPreset.getInstrumentPadTiles();
    }
    public String getPresetName(){return  presetName;}

    //Initialize
    public PresetManager(Context context){
        this.context = context;
        currentPreset = new Preset(context);
    }

}
