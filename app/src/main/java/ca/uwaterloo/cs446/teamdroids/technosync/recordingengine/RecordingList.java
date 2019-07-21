package ca.uwaterloo.cs446.teamdroids.technosync.recordingengine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecordingList implements Serializable {
    private List<RecordingEntry> recordingEntries;
    private String startTime;
    private String endTime;

    RecordingList() {
        recordingEntries = new ArrayList<>();
    }

    //Insert new record
    void newEntry(RecordingEntry recordingEntry) {
        recordingEntries.add(recordingEntry);
    }

    public List<RecordingEntry> getRecordingEntries() {
        return recordingEntries;
    }


    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
