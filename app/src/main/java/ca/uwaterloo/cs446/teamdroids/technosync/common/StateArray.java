package ca.uwaterloo.cs446.teamdroids.technosync.common;

import java.io.Serializable;

/**
 * Created by harshmistry on 2019-06-20.
 */

public class StateArray implements Serializable {
    private int stateArray[];
    public void setStateArray(int stateArray[]){ this.stateArray = stateArray;}
    public int[] getStateArray() { return  stateArray;}
}

