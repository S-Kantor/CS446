package ca.uwaterloo.cs446.teamdroids.technosync.recordingengine;

import ca.uwaterloo.cs446.teamdroids.technosync.common.StateArray;
import ca.uwaterloo.cs446.teamdroids.technosync.common.Tile;
import ca.uwaterloo.cs446.teamdroids.technosync.common.TileList;

public class CurrentState {

    TileList loopList;
    TileList instrumentList;
    StateArray stateArray;

    public void setLoopList(TileList loopList) {this.loopList = loopList;}
    public TileList getLoopList (){return  loopList;}
    public void  setInstrumentList(TileList instrumentList) {this.instrumentList = instrumentList;}
    public TileList getInstrumentList() {return  instrumentList;}
    public void  setStateArray(StateArray stateArray){this.stateArray = stateArray;}
    public StateArray getStateArray(){return  stateArray;}

    //Get loop tile that was changed
    // -1 for no change
    public Integer determineChange(StateArray newStateArray){
        for(int i = 0; i < newStateArray.getLength(); i++){
            if(stateArray != null && newStateArray.getStateArray()[i] != stateArray.getStateArray()[i]){
                return  i + 1;
            }
            else if(stateArray == null && newStateArray.getStateArray()[i] == 1){
                return  i + 1;
            }
        }

        //No Change
        return  -1;
    }

    //Get file name form TileList
    public String tileIdToFileName(final Integer tileId, boolean loopable){
        TileList matchingList;

        //Get list to filter
        if(loopable) matchingList = loopList;
        else matchingList = instrumentList;

        //Get matching (Use loops instead of streams for android 24 compatibility)
        for(int i = 0; i < matchingList.getTiles().size(); i++){
            if(matchingList.getTiles().get(i).getTileId() == tileId){
                return matchingList.getTiles().get(i).getFileString();
            }
        }

        return "";
    }


}
