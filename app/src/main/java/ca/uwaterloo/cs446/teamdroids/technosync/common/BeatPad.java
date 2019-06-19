package ca.uwaterloo.cs446.teamdroids.technosync.common;

import java.io.Serializable;
import java.util.List;

public class BeatPad implements Serializable {
    //Make beat pad size customizable to account for larger screens (Maybe)
    //Default is 5 x 5
    private int xSize = 5;
    private int ySize = 5;

    //Launchpad Tiles
    private List<Tile> tiles;

}
