package ca.uwaterloo.cs446.teamdroids.technosync.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LoopPad implements Serializable {
    //Make beat pad size customizable to account for larger screens (Maybe)
    //Default is 5 x 5
    private int size = 5;

    //Launchpad Tiles
    public List<Tile> tiles;

    //States of Launchpad buttons
    //0 = not pressed
    //1 = pressed
    public int stateArray[] = new int[25];

    public LoopPad (){
        tiles = new ArrayList<>();
    }



}
