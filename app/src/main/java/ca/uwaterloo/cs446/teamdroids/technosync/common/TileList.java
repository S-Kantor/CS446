package ca.uwaterloo.cs446.teamdroids.technosync.common;

import java.io.Serializable;
import java.util.List;

/**
 * Created by harshmistry on 2019-06-20.
 */

public class TileList implements Serializable {
    private List<Tile> tiles;
    public  void  setTiles(List<Tile> tiles){this.tiles = tiles;}
    public  List<Tile> getTiles() {return  tiles;}
}
