package com.androidchicken.coordcenter;

import java.util.Comparator;

/**
 * Created by Elisabeth Huhn on 11/28/2016.
 * For use in the status list
 */

class SatelliteComparator implements Comparator<Satellite> {
    private static final int oneLTtwo = -1;
    private static final int oneEQtwo = 0;
    private static final int oneGTtwo = 1;

    SatelliteComparator(){}

    public int compare(Satellite satellite1, Satellite satellite2) {
        //returns negative integer if satellite1 < satellite2
        //                    zero if satellite1 = satellite2
        //        positive integer if satellite1 > satellite2

        long satelliteID1 = satellite1.getSatelliteID();
        long satelliteID2 = satellite2.getSatelliteID();
        if (satelliteID1 < satelliteID2) return oneLTtwo;
        if (satelliteID1 == satelliteID2)return oneEQtwo;
        if (satelliteID1 > satelliteID2) return oneGTtwo;

        return oneGTtwo;
    }
}
