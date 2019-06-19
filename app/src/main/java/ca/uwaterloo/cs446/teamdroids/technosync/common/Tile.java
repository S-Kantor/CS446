package ca.uwaterloo.cs446.teamdroids.technosync.common;

import java.io.Serializable;

public class Tile implements Serializable {
    private int fileId;
    private String fileString;
    private boolean state;

    public int getFileId(){ return  fileId;}
    public void setFileId(int fileId) {this.fileId = fileId;}
    public String getFileString(){return fileString;}
    public void setFileString(String fileString){this.fileString = fileString;}
    public boolean getState() { return  state;}
    public void  setState(boolean state){this.state = state;}
}
