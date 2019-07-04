package ca.uwaterloo.cs446.teamdroids.technosync.recordingengine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecordingList implements Serializable {
    private List<RecordingEntry> recordingEntries;

    //Insert new record
    public void newEntry(RecordingEntry recordingEntry){
        recordingEntries.add(recordingEntry);
    }

    public List<RecordingEntry> getRecordingEntries(){return recordingEntries;}

    public RecordingList(){
        recordingEntries = new ArrayList<>();
    }

}
