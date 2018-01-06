package com.androidchicken.coordcenter;

/**
 * Created by Elisabeth Huhn on 5/25/2016.
 *
 * This class is part of the Coordinate's hierarchy
 * This is the super class of any Coordinate expressed with
 * Easting/Northing rectangular coordinate systems
 *
 * The class knows how to:
 *   A) store all attributes required of Easting/Northing coordinate systems.
 *   B) convert from any other coordinate system to this one
 *
 */
abstract class CoordinateEN extends Coordinate {

    static final String sDatum = "EN";

    /* ***************************************************/
    /* ******    Attributes stored in the DB     *********/
    /* ***************************************************/



    protected double       mEasting;
    protected double       mNorthing;

    protected int          mZone;        //1-60






    /* ************************************************************
     * Setters and getters
     *
     *
     ****************************************************************/


    double getEasting()     { return mEasting;  }
    void   setEasting(double easting)   { mEasting = easting;  }

    double getNorthing()    { return mNorthing; }
    void   setNorthing(double northing) { mNorthing = northing;}


    double getEastingFeet() { return Utilities.convertMetersToFeet(mEasting);  }
    double getNorthingFeet(){ return Utilities.convertMetersToFeet(mNorthing); }

    double getEastingIFeet() {return Utilities.convertMetersToIFeet(mEasting);}
    double getNorthingIFeet(){return Utilities.convertMetersToIFeet(mNorthing);}

    int  getZone()        { return mZone;     }
    void setZone(int zone)              { mZone = zone;   }




    //default constructor
    /* ************************************************************/
    /* ************************************************************/
    /*       Vanilla constructor, default values only             */
    /*                                                            */
    /* ************************************************************/
    /* ************************************************************/
    CoordinateEN(){
            initializeDefaultVariables();
        }

    protected void initializeDefaultVariables(){
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        //initialize all variables common to all coordinates
        super.initializeDefaultVariables();

        //initialize all variables common to EN coordinates


        mEasting     = 0d;
        mNorthing    = 0d;
        mZone        = 0;        //1-60
        //mHemisphere  = 'N';  //N or S
        //mLatBand     = 'A';

    }



}
