package com.androidchicken.coordcenter;

import android.os.Bundle;

/**
 * Created by Elisabeth Huhn on 5/15/2016 for GeoBot
 * cloned on 1/3/2018 for CoordCenter
 *
 * One of the principal Data Classes of the model
 * A Point is contained within one and only one project
 */
class Point {

    /* ***************************************************/
    /* ******    Static constants                *********/
    /* ***************************************************/

    //Tags for fragment arguments
    static final String sPointProjectIDTag  = "PROJECT_ID";
    static final String sPointTag           = "POINT_OBJECT";
    static final String sPointNameTag       = "POINT_NAME";
    static final String sPointIDTag         = "POINT_ID";
    static final String sPointEastingTag    = "POINT_EASTING";
    static final String sPointNorthingTag   = "POINT_NORTHING";
    static final String sPointElevationTag  = "POINT_ELEVATION";
    static final String sPointFCTag         = "POINT_FEATURE_CODE";
    static final String sPointNotesTag      = "POINT_NOTES";

    static final int    sPointDefaultsID   = -1;
    static final String sPointDefaultsDesc =
            "This point represents the defaults that all other projects start with";

    static final int    sPointNewID   = -2;
    static final String sPointNewDesc = "";


    //Tags for shared pref settings for whether the property was exported last time
    static final String sPointProjectIDExportTag  = "POINT_PROJECT_ID_EXPORT";
    static final String sPointNameExportTag       = "POINT_NAME_EXPORT";
    static final String sPointIsMeanedExportTag   = "POINT_IS_MEANED_EXPORT";
    static final String sPointCoordinateExportTag = "POINT_COORD_EXPORT";
    static final String sPointNumberExportTag    = "POINT_NUMBER_EXPORT";
    static final String sPointHeightExportTag    = "POINT_HEIGHT_EXPORT";
    static final String sPointFCExportTag        = "POINT_FC_EXPORT";
    static final String sPointNotesExportTag     = "POINT_NOTES_EXPORT";
    static final String sPointTokenExportTag     = "POINT_TOKEN_EXPORT";
    static final String sPointOffDistExportTag   = "POINT_OFF_DIST_EXPORT";
    static final String sPointOffHeadExportTag   = "POINT_OFF_HEAD_EXPORT";
    static final String sPointOffEleExportTag    = "POINT_OFF_ELE_EXPORT";
    static final String sPointHdopExportTag      = "POINT_HDOP_EXPORT";
    static final String sPointVdopExportTag      = "POINT_VDOP_EXPORT";
    static final String sPointPdopExportTag      = "POINT_PDOP_EXPORT";
    static final String sPointTdopExportTag      = "POINT_TDOP_EXPORT";
    static final String sPointHrmsExportTag      = "POINT_HRMS_EXPORT";
    static final String sPointVrmsExportTag      = "POINT_VRMS_EXPORT";




    /* ***********************************/
    /*    Static (class) Variables       */
    /* ***********************************/


    /* ***********************************/
    /*    Member (instance) Variables    */
    /* ***********************************/

    /* ***************************************************/
    /* ******    Attributes stored in the DB     *********/
    /* ***************************************************/

    private long         mPointID;
    private long         mForProjectID;

    //Actual location of point is given by Coordinate
    //The coordinate location does NOT reflect the offsets
    //It is the pre-offset location
    private long         mHasACoordinateID;
    private Coordinate   mCoordinate;

    private MeanToken    mMeanToken;

    private int          mPointNumber;
    private long         mMeanTokenID;

    //The original offset. The Coordinate has this offset calculated into it
    private double       mOffsetDistance;
    private double       mOffsetHeading;
    private double       mOffsetElevation;

    //Original height due to artificial means, e.g. tripod
    private double       mHeight;

    private CharSequence mPointFeatureCode;
    private CharSequence mPointNotes;


    //Quality fields
    private double      mHdop;
    private double      mVdop;
    private double      mTdop;
    private double      mPdop;
    private double      mHrms;
    private double      mVrms;




    /* **************************************************************/
    /*               Static Methods                                 */
    /* **************************************************************/

    static Bundle putPointInArguments(Bundle args, Point point) {

        if (point == null){
            //This  happens when the point is being created by this fragment on save
            args.putLong(Point.sPointIDTag,Utilities.ID_DOES_NOT_EXIST);
        } else {
            args.putLong(Point.sPointIDTag, point.getPointID());
        }
        //assume all other attributes exist on the point being managed by the PointManager
        return args;
    }


    static Point getPointFromArguments(MainActivity activity, Bundle args) {

        Point point = initializePoint(activity);

        return point;
    }

    private static Point initializePoint(MainActivity activity){

        Point point = new Point();
        point.setPointID(Utilities.ID_DOES_NOT_EXIST);

        point.setForProjectID(Utilities.ID_DOES_NOT_EXIST);
        point.setHeight(CCSettings.getHeight(activity));
        point.setPointNumber((int)Utilities.ID_DOES_NOT_EXIST);
        //the point number is not incremented until the point is saved for the first time
        //The SQL Helper is in charge of assigning both
        // the DB ID and then incrementing the point number
        //openProject.incrementPointNumber(activity);
        return point;
    }


    /* ***********************************/
    /*         CONSTRUCTORS              */
    /* ***********************************/
    //This creates a point and writes it to the DB
    Point(MainActivity activity, Coordinate coordinate, MeanToken token) {

        initializeDefaultVariables();

        setPointID( Utilities.ID_DOES_NOT_EXIST);
        setHasACoordinateID(Utilities.ID_DOES_NOT_EXIST);
        setForProjectID(Utilities.ID_DOES_NOT_EXIST);

        updatePoint(activity, coordinate, token);

    }


    //a coule of special case points flag "create new point" and "open a point" path
    Point(int specialPointID) {
        initializeDefaultVariables();

        this.mForProjectID     = specialPointID;
        this.mPointID          = specialPointID ;
        this.mPointFeatureCode = "A special point";
    }

    Point() {
        initializeDefaultVariables();
    }



    /* ***************************************************/
    /* ******    Setters and Getters             *********/
    /* ***************************************************/



    long getForProjectID()                  {  return mForProjectID;    }
    void setForProjectID(long forProjectID) {  this.mForProjectID = forProjectID;  }

    long getPointID()             {  return mPointID;    }
    void setPointID(long pointID) {  this.mPointID = pointID;  }

    long getHasACoordinateID()                    {return mHasACoordinateID; }
    void setHasACoordinateID(long isACoordinateID) { mHasACoordinateID = isACoordinateID; }

    Coordinate getCoordinate()                {
        return mCoordinate;
    }
    long setCoordinate(Coordinate coordinate) {
        mCoordinate = coordinate;
        return Utilities.ID_DOES_NOT_EXIST;
     }

    int getPointNumber() {  return mPointNumber;   }
    void setPointNumber(int pointNumber) {  mPointNumber = pointNumber; }

    long getMeanTokenID() {  return mMeanTokenID;   }
    void setMeanTokenID(long meanTokenID) {  mMeanTokenID = meanTokenID; }

    MeanToken getMeanToken()                {
        return mMeanToken;
    }
    long setMeanToken(MeanToken token) {
        mMeanToken = token;
        return Utilities.ID_DOES_NOT_EXIST;
    }

    double getOffsetDistance() {  return mOffsetDistance;   }
    void setOffsetDistance(double offsetDistance) {  mOffsetDistance = offsetDistance; }

    double getOffsetHeading() {   return mOffsetHeading;}
    void setOffsetHeading(double offsetHeading) { mOffsetHeading = offsetHeading;   }

    double getOffsetElevation() {  return mOffsetElevation;  }
    void setOffsetElevation(double offsetElevation) { mOffsetElevation = offsetElevation; }


    double getHeight()              {  return mHeight; }
    void   setHeight(double height) { mHeight = height; }

    CharSequence getPointFeatureCode() { return mPointFeatureCode;  }
    void setPointFeatureCode(CharSequence description) { mPointFeatureCode = description;  }

    CharSequence getPointNotes() {  return mPointNotes;   }
    void setPointNotes(CharSequence notes) { mPointNotes = notes;   }

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

    //Cascading objects are not pulled from the DB until
    // explicitly reqested by a call to getPictures();



    /* ***************************************************/
    /* ******    Private Member Methods          *********/
    /* ***************************************************/

    void initializeDefaultVariables(){
        this.mForProjectID     = Utilities.ID_DOES_NOT_EXIST;
        this.mPointID          = Utilities.ID_DOES_NOT_EXIST;
        this.mHasACoordinateID = Utilities.ID_DOES_NOT_EXIST;
        this.mPointNumber      = 0;
        this.mMeanTokenID      = Utilities.ID_DOES_NOT_EXIST;

        this.mOffsetDistance   = 0d;
        this.mOffsetHeading    = 0d;
        this.mOffsetElevation  = 0d;

        this.mHeight           = 0d;

        this.mPointFeatureCode = "";
        this.mPointNotes       = "";

        this.mHdop = 0d;
        this.mVdop = 0d;
        this.mTdop = 0d;
        this.mPdop = 0d;
        this.mHrms = 0d;
        this.mVrms = 0d;
    }


    //Convert point to comma delimited file for exchange
    String convertToCDF() {
        // TODO: 7/2/2017 routine needs to be updated
        return String.valueOf(this.getPointID()) + ", " +
                this.getPointFeatureCode()       + ", " +
                this.getPointNotes()             + ", " +
                this.getHdop()                   + ", " +
                this.getVdop()                   + ", " +
                this.getTdop()                   + ", " +
                this.getPdop()                   + ", " +
                this.getHrms()                   + ", " +
                this.getVrms()                   + ", " +
                this.getOffsetDistance()         + ", " +
                this.getOffsetHeading()          + ", " +
                this.getOffsetElevation()        + ", " +
                this.getHeight()                 + ", " +
                //todo 12/13/2016 have to add in coordinates here
                "plus coordinate positions "      +
                System.getProperty("line.separator");
    }

    Point updatePoint(MainActivity activity, Coordinate coordinate, MeanToken token) {

        initializeDefaultVariables();

        setPointID( Utilities.ID_DOES_NOT_EXIST);
        setHasACoordinateID(Utilities.ID_DOES_NOT_EXIST);
        setForProjectID(Utilities.ID_DOES_NOT_EXIST);

        //
        //initialize with values from the Project
        //

        setForProjectID(Utilities.ID_DOES_NOT_EXIST);
        setHeight      (CCSettings.getHeight(activity));


        //
        //initialize with values from the coordinate
        //
        //Coordinate must exist and be valid
        if ((coordinate == null) || (!coordinate.isValidCoordinate())) return this;



        long coordinateID = coordinate.getCoordinateID();
        if (coordinateID == Utilities.ID_DOES_NOT_EXIST){
            //the coordinate has not yet been saved
            coordinateID = setCoordinate(coordinate);

        }


        setCoordinate(coordinate);


        //
        // Initialize Mean Token
        //
        if (token != null){

            setMeanToken(token);
            CoordinateMean coordinateMean = token.getMeanCoordinate();
            setVrms(coordinateMean.getVrms());
            setHrms(coordinateMean.getVrms());
        }
        setHdop(SatelliteManager.getInstance().getHdop());
        setVdop(SatelliteManager.getInstance().getVdop());
        setPdop(SatelliteManager.getInstance().getPdop());


        return this;

    }





}
