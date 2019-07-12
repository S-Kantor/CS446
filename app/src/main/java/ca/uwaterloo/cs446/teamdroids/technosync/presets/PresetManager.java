package ca.uwaterloo.cs446.teamdroids.technosync.presets;

import android.content.Context;

import java.lang.reflect.Field;
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
        Field[] fields = R.raw.class.getFields();

        //Add only presets
        for(Field file : fields){
            if(file.getName().contains("preset")){
                presets.add(file.getName());
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
