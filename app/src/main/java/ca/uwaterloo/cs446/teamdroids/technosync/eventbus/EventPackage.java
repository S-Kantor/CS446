package ca.uwaterloo.cs446.teamdroids.technosync.eventbus;

import android.util.EventLog;
import java.util.Map;

public class EventPackage {
    private EventType eventType;
    private String serializedData;

    public EventType getEventType(){ return eventType;}
    public void setEventType(EventType eventType) {this.eventType = eventType;}
    public String getSerializedData() { return serializedData; }
    public void setSerializedData(String serializedData) {this.serializedData = serializedData;}
}
