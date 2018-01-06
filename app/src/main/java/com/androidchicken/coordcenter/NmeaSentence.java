package com.androidchicken.coordcenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elisabeth Huhn on 5/15/2016 for GeoBot.
 * Cloned on 1/3/2018
 * Represents a NMEA sentence
 */
class NmeaSentence {

    //Tags for fragment arguments
    //public static final String sNmeaTag  = "NMEA";




    private CharSequence mNmeaSentence;


    //Fields of interest to skyplot Prism4D that can be pulled out of the NMEA Sentence

    //Info about the NMEA Sentence
    private CharSequence mNmeaType; //e.g. GLL, GGA, etc.
    double mTime;//UTC time: milliseconds since 1/1/1970

    long mTimeStamp;

    //Position
    double mLatitude;
    double mLongitude;
    private double mEllipsoidalElevation;
    double mGeoid;
    double mOrthometricElevation;
    private double mLocalization;
    private double mLocalNorthing;
    private double mLocalEasting;
    private double mLocalElevation;

    //Satellites in view information
    private int    mSatelliteStatus;
    int    mSatellites;
    private List<Satellite> mSatelliteList = new ArrayList<>();

    //Quality of fix Information
            double mHdop;
            double mVdop;
    private double mTdop;
            double mPdop;
    private double mGdop;
    private double mHrms;
    private double mVrms;


            int    mQuality;
            boolean mFixed;


    //****************************** Static Methods *********************/



    //************************** Setters and Getters ********************/

    CharSequence getNmeaSentence() {
        return mNmeaSentence;
    }
    void setNmeaSentence(CharSequence nmeaSentence) {
        this.mNmeaSentence = nmeaSentence;
    }

    CharSequence getNmeaType() {
        return mNmeaType;
    }
    void         setNmeaType(CharSequence type) {
        mNmeaType = type;
    }

    double getTime() {
        return mTime;
    }
    void   setTime(double time) {
        mTime = time;
    }

    long   getTimeStamp() {
        return mTimeStamp;
    }
    void   setTimeStamp(long time) {
        mTimeStamp = time;
    }

    double getLatitude() {
        return mLatitude;
    }
    void   setLatitude(double latitude) {
        mLatitude = latitude;
    }

    double getLongitude() {
        return mLongitude;
    }
    void   setLongitude(double longitude) {
        mLongitude = longitude;
    }

    double getEllipsoidalElevation() {
        return mEllipsoidalElevation;
    }
    void   setEllipsoidalElevation(double ellipsoidalElevation) {
        mEllipsoidalElevation = ellipsoidalElevation;
    }

    double getGeoid() {
        return mGeoid;
    }
    void   setGeoid(double geoid) {
        mGeoid = geoid;
    }

    double getOrthometricElevation() {
        return mOrthometricElevation;
    }
    void   setOrthometricElevation(double orthometricElevation) {
        mOrthometricElevation = orthometricElevation;
    }

    double getLocalization() {
        return mLocalization;
    }
    void   setLocalization(double localization) {
        mLocalization = localization;
    }

    double getLocalNorthing() {
        return mLocalNorthing;
    }
    void   setLocalNorthing(double localNorthing) {
        mLocalNorthing = localNorthing;
    }

    double getLocalEasting() {
        return mLocalEasting;
    }
    void   setLocalEasting(double localEasting) {
        mLocalEasting = localEasting;
    }

    double getLocalElevation() {
        return mLocalElevation;
    }
    void   setLocalElevation(double localElevation) {
        mLocalElevation = localElevation;
    }

    int  getSatelliteStatus() {
        return mSatelliteStatus;
    }
    void setSatelliteStatus(int satelliteStatus) {
        mSatelliteStatus = satelliteStatus;
    }

    int  getSatellites() {
        return mSatellites;
    }
    void setSatellites(int satellites) {
        mSatellites = satellites;
    }

    List<Satellite> getSatelliteList() {
        return mSatelliteList;
    }
    void setSatelliteList(List<Satellite> satelliteList) {
        mSatelliteList = satelliteList;
    }

    Satellite getSatellite(int position) {
        return mSatelliteList.get(position);
    }
    void setSatellite(Satellite satellite) {
        mSatelliteList.add(satellite);
    }



    double getHdop() {
        return mHdop;
    }
    void   setHdop(double hdop) {
        mHdop = hdop;
    }

    double getVdop() {
        return mVdop;
    }
    void   setVdop(double vdop) {
        mVdop = vdop;
    }

    double getTdop() {
        return mTdop;
    }
    void   setTdop(double tdop) {
        mTdop = tdop;
    }

    double getPdop() {
        return mPdop;
    }
    void   setPdop(double pdop) {
        mPdop = pdop;
    }

    double getGdop() {
        return mGdop;
    }
    void   setGdop(double gdop) {
        mGdop = gdop;
    }

    double getHrms() {
        return mHrms;
    }
    void   setHrms(double hrms) {
        mHrms = hrms;
    }

    double getVrms() {
        return mVrms;
    }
    void   setVrms(double vrms) {
        mVrms = vrms;
    }

    int  getQuality() {
        return mQuality;
    }
    void setQuality(int quality) {
        mQuality = quality;
    }

    boolean isFixed() {
        return mFixed;
    }
    void    setFixed(boolean fixed) {
        mFixed = fixed;
    }

    //********************* constructors ********************************/

    NmeaSentence(CharSequence nmeaSentence) {
        //save the raw sentence
        mNmeaSentence = nmeaSentence;

    }

    NmeaSentence(){
        initializeDefaultData();
    }
    private void initializeDefaultData(){
        mNmeaType = "GPGGA"; //e.g. GLL, GGA, etc.
        mTime = 0d ;//UTC time: milliseconds since 1/1/1970
        mTimeStamp = 0;

        //Position
        mLatitude = 0d ;
        mLongitude = 0d ;
        mEllipsoidalElevation = 0d ;
        mGeoid = 0d ;
        mOrthometricElevation = 0d ;
        mLocalization = 0d ;
        mLocalNorthing = 0d ;
        mLocalEasting = 0d ;
        mLocalElevation = 0d ;

        //Satellites in view information
        mSatelliteStatus = 0;
        mSatellites = 0;
        //mSatelliteList = new ArrayList<>(); //already initialized above

        //Quality of fix Information
        mHdop = 0d ;
        mVdop = 0d ;
        mTdop = 0d ;
        mPdop = 0d ;
        mGdop = 0d ;
        mHrms = 0d ;
        mVrms = 0d ;


        mQuality =0;
        mFixed = false;

    }
}
