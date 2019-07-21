package ca.uwaterloo.cs446.teamdroids.technosync.recordingengine;

import java.io.Serializable;

public class RecordingEntry implements Serializable {

    private String dateTime;
    private String fileName;
    private Boolean loopable;

    public String getDateTime() {
        return dateTime;
    }

    public String getFileName() {
        return fileName;
    }

    public Boolean getLoopable() {
        return loopable;
    }

    RecordingEntry(String fileName, Boolean loopable, String dateTime) {
        this.fileName = fileName;
        this.loopable = loopable;
        this.dateTime = dateTime;
    }

}
