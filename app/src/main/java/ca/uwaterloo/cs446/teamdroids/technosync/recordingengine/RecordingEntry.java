package ca.uwaterloo.cs446.teamdroids.technosync.recordingengine;

import java.io.Serializable;
import java.text.DateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

public class RecordingEntry implements Serializable {

    private String dateTime;
    private String fileName;
    private Boolean loopable;

    public String getDateTime(){return  dateTime;}
    public String getFileName(){return  fileName;}
    public Boolean getLoopable(){return  loopable;}
    public RecordingEntry(String fileName, Boolean loopable){
        this.fileName = fileName;
        this.loopable = loopable;

        //Compatibility with android versions less than 26
        this.dateTime = DateFormat.getDateTimeInstance().format(new Date());
    }

}
