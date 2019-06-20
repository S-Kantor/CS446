package ca.uwaterloo.cs446.teamdroids.technosync.common;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventPackage;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventType;

public class InstrumentPad extends LoopPad {

    //Convert tile into a serialized string
    private String serializeTile(int tileId){
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(tiles.get(tileId));
            so.flush();
            return new String(Base64.encode(bo.toByteArray(), 0));
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }

    //Publish a played note/tile
    public void publishTileHit(int tileId){
        EventPackage eventPackage = new EventPackage();
        eventPackage.setEventType(EventType.INSTRUMENTPAD_SOUND_HIT);
        eventPackage.setSerializedData(serializeTile(tileId));
        getEventBus().newEvent(eventPackage);
    }

    //Publish tile list
    public void publishTileList(){
        EventPackage eventPackage = new EventPackage();
        eventPackage.setEventType(EventType.INSTRUMENTPAD_MAPPING_UPDATE);
        eventPackage.setSerializedData(serializeTileList());
        getEventBus().newEvent(eventPackage);
    }


}
