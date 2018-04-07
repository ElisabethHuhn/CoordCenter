package com.androidchicken.coordcenter;

/**
 * Created by Elisabeth Huhn on 11/20/2016.
 *
 * This is the superclass of all coordinate types
 * It doesn't do much, as it will be subtypes that are actually instantiated.
 * This level just provides the capability for the subtypes to
 * identify the type of the instance that is actually instantiated
 */

abstract class Coordinate {

    static final String sCoordinateTypeUnknown = "Unknown";
    static final String sCoordinateTypeWGS84 = "WGS84 G1762";
    static final String sCoordinateTypeSPCS  = "US State Plane Coordinates";
    static final String sCoordinateTypeUTM   = "UTM";
    static final String sCoordinateTypeNAD83 = "NAD83 2011";

    //This is the order of the items in the Project UI spinner
    //Unfortunately, this is a hard coded order.
    // The ProjectEditFragment spinner must be coordinated with any changes
    static final int sCoordinateDBTypeUnknown = -1;
    static final int sCoordinateDBTypeWGS84   = 0;
    static final int sCoordinateDBTypeSPCS    = 1;
    static final int sCoordinateDBTypeUTM     = 2;
    static final int sCoordinateDBTypeNAD83   = 3;



    /* *********************************************************/
    /* ***    Variables common to ALL coordinates        *******/
    /* *********************************************************/
    private long    mCoordinateID; //All coordinates have a DB ID
    private long    mProjectID; //May or may not describe a point
    private long    mPointID;   //These will be null if not describing a point
    private int     mCoordinateDBType;

    private long    mTime; //time coordinate taken in milliseconds

    private double  mElevation; //Orthometric Elevation in Meters
    private double  mGeoid;     //Mean Sea Level in Meters

    private double  mScaleFactor;
    private double  mConvergenceAngle;

    private boolean mValidCoordinate = true;
    private boolean mIsFixed         = true;

    private CharSequence mDatum = ""; //eg WGS84




    /* *********************************************************/
    /* ***    Setters and Getters for common variables   *******/
    /* *********************************************************/



     long getCoordinateID(){return mCoordinateID;}
     void setCoordinateID(long coordinateID){this.mCoordinateID = coordinateID;}


    long  getProjectID()              { return mProjectID; }
    void setProjectID(long projectID) { mProjectID = projectID; }

    long  getPointID()            { return mPointID; }
    void setPointID(long pointID) { mPointID = pointID; }


    int  getCoordinateDBType()            { return mCoordinateDBType; }
    void setCoordinateDBType(int coordinateDBType) { mCoordinateDBType = coordinateDBType; }

    CharSequence getCoordinateType(){

        CharSequence returnCode = Coordinate.sCoordinateTypeUnknown;

        switch (getCoordinateDBType()) {
            case Coordinate.sCoordinateDBTypeWGS84:
                returnCode = Coordinate.sCoordinateTypeWGS84;
                break;
            case Coordinate.sCoordinateDBTypeSPCS:
                returnCode = Coordinate.sCoordinateTypeSPCS;
                break;
            case Coordinate.sCoordinateDBTypeUTM:
                returnCode = Coordinate.sCoordinateTypeUTM;
                break;
            case Coordinate.sCoordinateDBTypeNAD83:
                returnCode = Coordinate.sCoordinateTypeNAD83;
                break;
        }
        return returnCode;
    }


    long getTime()              {  return mTime;    }
    void setTime(long time)     {  mTime = time;  }


    double getElevation()       {  return mElevation;   }
    void setElevation(double elevation) { mElevation = elevation;   }
    double getElevationFeet() {return Utilities.convertMetersToFeet(mElevation); }
    double getElevationIFeet() {return Utilities.convertMetersToIFeet(mElevation);}

    double getGeoid()           {  return mGeoid; }
    double getGeoidFeet() { return Utilities.convertMetersToFeet(mGeoid);}
    double getGeoidIFeet() {return Utilities.convertMetersToIFeet(mGeoid);}
    void setGeoid(double geoid) { mGeoid = geoid;  }

    double getScaleFactor()       { return mScaleFactor;       }
    void setScaleFactor(double scaleFactor) { mScaleFactor = scaleFactor; }

    double getConvergenceAngle()       { return mConvergenceAngle;       }
    int    getConvergenceAngleDegree()  { return Utilities.getDegrees(mConvergenceAngle);  }
    int    getConvergenceAngleMinute()  { return Utilities.getMinutes(mConvergenceAngle);  }
    double getConvergenceAngleSecond()  { return Utilities.getSeconds(mConvergenceAngle);  }
    void setConvergenceAngle(double convergenceAngle) { mConvergenceAngle = convergenceAngle; }


    void    setValidCoordinate(boolean validCoordinate){ this.mValidCoordinate = validCoordinate;}
    boolean isValidCoordinate() {
        return mValidCoordinate;
    }

    void    setIsFixed(boolean isFixed){this.mIsFixed = isFixed;}
    boolean isFixed()                  {return mIsFixed;}

    CharSequence getDatum() { return mDatum;    }
    void setDatum(CharSequence datum)   { mDatum = datum;  }


    /* *********************************************************/
    /* ***     Methods common to ALL coordinates         *******/
    /* *********************************************************/

    protected void initializeDefaultVariables() {
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        mCoordinateID     = Utilities.ID_DOES_NOT_EXIST;

        mProjectID        = Utilities.ID_DOES_NOT_EXIST; //assume does not describe a point
        mPointID          = Utilities.ID_DOES_NOT_EXIST;
        mCoordinateDBType = sCoordinateDBTypeUnknown;



        mTime             = 0; //time coordinate taken
        mValidCoordinate  = false;

        mElevation        = 0d;
        mGeoid            = 0d;
        mConvergenceAngle = 1d; //
        mScaleFactor      = 1d;


    }

    protected double getMeters(String distString, int distUnits){
        //The distString is in the units defined by the UI setting in the project
        //Must convert to meters to store in the object or the DB
        double distValue;
        // TODO: 7/21/2017 need to get rid of commas, and be able to differentiate between 6,0 AND 6.0
        if (Utilities.isEmpty(distString)) return 0.d;

        distValue = Double.valueOf(distString);
        //if (distUnits == CCSettings.sMeters)//do nothing, the value is already in meters

        if (distUnits == CCSettings.sFeet){
            distValue = Utilities.convertFeetToMeters(distValue);
        }
        if (distUnits == CCSettings.sIntFeet){
            distValue = Utilities.convertIFeetToMeters(distValue);
        }

        return distValue;

    }



}
