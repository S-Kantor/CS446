package ca.uwaterloo.cs446.teamdroids.technosync.recordingengine;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    RecordingEntry(String fileName, Boolean loopable) {
        this.fileName = fileName;
        this.loopable = loopable;

        //Compatibility with android versions less than 26
        this.dateTime = new SimpleDateFormat("%D:%H:%M:%S.%f", Locale.CANADA).format(new Date());
    }

}
