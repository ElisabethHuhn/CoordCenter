package com.androidchicken.coordcenter;

/**
 * Created by Elisabeth Huhn on 5/7/2016.
 * Represents a satellite from GPS data
 */
class Satellite {

    // Satellite Data
    private long mSatelliteID;
    private int mElevation;//degrees
    private int mAzimuth;  //degrees to true
    private int mSnr;      //dB
    //Satellite data as reported in GSV NMEA sentence

    //
    //Setters and Getters
    //

    long getSatelliteID() {
        return mSatelliteID;
    }
    void setSatelliteID(long satelliteID) {
        mSatelliteID = satelliteID;
    }

    int getElevation()              { return mElevation; }
    void setElevation(int elevation) {
        mElevation = elevation;
    }

    int getAzimuth() {
        return mAzimuth;
    }
    void setAzimuth(int azimuth) {
        mAzimuth = azimuth;
    }

    int getSnr() {
        return mSnr;
    }
    void setSnr(int snr) {
        mSnr = snr;
    }




    /*
     *   constructorS
     */

    Satellite(long satelliteID, int elevation, int azimuth, int snr) {
        mSatelliteID = satelliteID;
        mElevation = elevation;
        mAzimuth = azimuth;
        mSnr = snr;
    }
    Satellite(long satelliteID) {
        mSatelliteID = satelliteID;
    }
}

