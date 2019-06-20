package ca.uwaterloo.cs446.teamdroids.technosync.common;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventPackage;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.EventType;
import ca.uwaterloo.cs446.teamdroids.technosync.eventbus.Publisher;

public class LoopPad extends Publisher {
    //Make beat pad size customizable to account for larger screens (Maybe)
    //Default is 5 x 5
    private int size = 5;

    //Launchpad Tiles
    public List<Tile> tiles;

    //States of Launchpad buttons
    //0 = not pressed
    //1 = pressed
    public int stateArray[] = new int[25];


    //Convert tiles into a serialized string
    public String serializeTileList(){
        try {
            TileList tileList = new TileList();
            tileList.setTiles(tiles);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(tileList);
            so.flush();
            return new String(Base64.encode(bo.toByteArray(), 0));
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }

    //Convert tiles into a serialized string
    private String serializeStateArray(){
        try {
            StateArray stateArray = new StateArray();
            stateArray.setStateArray(this.stateArray);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(stateArray);
            so.flush();

            return new String(Base64.encode(bo.toByteArray(), 0));
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }

    public LoopPad (){
        tiles = new ArrayList<>();
    }

    public void publishStateArray(){
        EventPackage eventPackage = new EventPackage();
        eventPackage.setEventType(EventType.LOOPPAD_STATE_UPDATE);
        eventPackage.setSerializedData(serializeStateArray());
        getEventBus().newEvent(eventPackage);
    }

    public void publishTileList(){
        EventPackage eventPackage = new EventPackage();
        eventPackage.setEventType(EventType.LOOPPAD_MAPPING_UPDATE);
        eventPackage.setSerializedData(serializeTileList());
        getEventBus().newEvent(eventPackage);
    }








}
