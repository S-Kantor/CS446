package ca.uwaterloo.cs446.teamdroids.technosync.common;

import java.io.Serializable;

public class Tile implements Serializable {
    private int tileId;
    private int fileId;
    private String fileString;
    private boolean disabled;
    private boolean loopable;

    public int getFileId(){ return  fileId;}
    public void setFileId(int fileId) {this.fileId = fileId;}
    public String getFileString(){return fileString;}
    public void setFileString(String fileString){this.fileString = fileString;}
    public void setTileId(int tileId){this.tileId = tileId;}
    public int getTileId() { return tileId;}
    public boolean getDisabled() { return  disabled;}
    public void setDisabled(boolean disabled) { this.disabled = disabled;}
    public boolean getLoopable(){ return  loopable;}
    public void  setLoopable(Boolean loopable){ this.loopable = loopable;}

}
