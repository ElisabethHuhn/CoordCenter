package com.androidchicken.coordcenter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;

/**
 * Created by Elisabeth Huhn on 6/17/2016.
 *
 * This class is part of the Coordinate's hierarchy
 * This is the super class of any Coordinate expressed with
 * Latitude / Longitude spherical surface coordinate systems
 *
 *
 * The class knows how to:
 *   A) store all attributes required of Latitude / Longitude coordinate systems.
 *   B) convert from any other coordinate system to this one
 *   C) convert swapping between DD and DMS formats of Latitude / Longitude
 *
 */


abstract class CoordinateLL extends Coordinate {
    //

    /* ***************************************************/
    /* ******    Attributes stored in the DB     *********/
    /* ***************************************************/

     //Latitude in DD and DMS formats
    double mLatitude;

    //Longitude in DD and DMS formats
    double mLongitude;




    /* ***************************************************/
    /* ******    Static Conversion Utilities     *********/
    /* ***************************************************/



    //Conversion for UI fields
    //last parameter indicates whether latitude (true) or longitude (false)
    static boolean convertDDtoDMS(Context context,
                                         EditText tudeDDInput,
                                         EditText tudeDInput,
                                         EditText tudeMInput,
                                         EditText tudeSInput,
                                         boolean  isLatitude) {

        String tudeString = tudeDDInput.getText().toString().trim();
        if (tudeString.isEmpty()) {
            tudeString = context.getString(R.string.zero_decimal_string);
            tudeDDInput.setText(tudeString);
        }

        double tude = Double.parseDouble(tudeString);

        //The user inputs have to be within range to be
        if (   (isLatitude   && ((tude < -90.0) || (tude >= 90.0)))  || //Latitude
             ((!isLatitude)  && ((tude < -180.) || (tude >= 180.)))) {  //Longitude

            tudeDInput.setText(R.string.zero_decimal_string);
            tudeMInput.setText(R.string.zero_decimal_string);
            tudeSInput.setText(R.string.zero_decimal_string);
            return false;
        }

        //check sign of tude
        boolean isTudePos = true;
        int tudeColor= R.color.colorPosNumber;
        if (tude < 0) {
            //tude is negative, remember this and work with the absolute value
            tude = Math.abs(tude);
            isTudePos = false;
            tudeColor = R.color.colorNegNumber;
        }

        //strip out the decimal parts of tude
        int tudeDegree = (int) tude;

        double degree = tudeDegree;

        //digital degrees minus degrees will be decimal minutes plus seconds
        //converting to int strips out the seconds
        double minuteSec = tude - degree;
        double minutes = minuteSec * 60.;
        int tudeMinute = (int) minutes;
        double minuteOnly = (double) tudeMinute;

        //start with the DD, subtract out Degrees, subtract out Minutes
        //convert the remainder into whole seconds
        double tudeSecond = (tude - degree - (minuteOnly / 60.)) * (60. * 60.);
        //tudeSecond = (tude - minutes) * (60. *60.);

        //If tude was negative before, restore it to negative
        if (!isTudePos) {
            //tude       = 0. - tude;
            tudeDegree = 0 - tudeDegree;
            tudeMinute = 0 - tudeMinute;
            tudeSecond = 0. - tudeSecond;
        }

        int locPrecision = CCSettings.getLocPrecision((MainActivity)context);
        //show the user the result
        tudeDDInput.setText(Utilities.truncatePrecisionString(tude, locPrecision));
        tudeDInput.setText(String.valueOf(tudeDegree));
        tudeMInput.setText(String.valueOf(tudeMinute));
        tudeSInput.setText(Utilities.truncatePrecisionString(tudeSecond,  locPrecision));

        tudeDDInput.setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeDInput .setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeMInput .setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeSInput .setTextColor(ContextCompat.getColor(context, tudeColor));

        return true;
    }


    //last parameter indicates whether latitude (true) or longitude (false)
    static boolean convertDMStoDD(Context context,
                                         EditText tudeDDInput,
                                         EditText tudeDInput,
                                         EditText tudeMInput,
                                         EditText tudeSInput,
                                         boolean  isLatitude){

        //String tudeString = tudeDDInput.getText().toString().trim();
        String tudeDString = tudeDInput.getText().toString().trim();
        String tudeMString = tudeMInput.getText().toString().trim();
        String tudeSString = tudeSInput.getText().toString().trim();

        if (tudeDString.isEmpty()){
            tudeDString = context.getString(R.string.zero_decimal_string);
        }

        //double tude = Double.parseDouble(tudeString);
        int    tudeD = Integer.parseInt(tudeDString);
        int    tudeM = Integer.parseInt(tudeMString);
        double tudeS = Double.parseDouble(tudeSString);

        if (((isLatitude) && ((tudeD <   -90) || (tudeD >=   90))) ||
            ((isLatitude) && ((tudeM <   -60) || (tudeM >    60))) ||
            ((isLatitude) && ((tudeS <   -60.)|| (tudeS >    60.)))||
           ((!isLatitude) && ((tudeD <  -180) || (tudeD >=  180))) ||
           ((!isLatitude) && ((tudeM <   -60) || (tudeM >    60))) ||
           ((!isLatitude) && ((tudeS <   -60.)|| (tudeS >    60.)))) {

            tudeDDInput.setText(R.string.zero_decimal_string);
            return false;
        }

        boolean isTudePos = true;
        int tudeColor = R.color.colorPosNumber;
        if ((tudeD < 0) || (tudeM < 0) || (tudeS < 0)){
            isTudePos = false;
            tudeD = Math.abs(tudeD);
            tudeM = Math.abs(tudeM);
            tudeS = Math.abs(tudeS);
            tudeColor = R.color.colorNegNumber;
        }

        double degrees = (double)tudeD;
        double minutes = (double)tudeM ;
        double seconds =         tudeS ;
        double tude =  degrees + (minutes/ 60.) + (seconds/ (60.*60.));


        if (!isTudePos) {
            tude  = 0. - tude;
        }

        tudeDDInput.setText(String.valueOf(tude));

        tudeDDInput.setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeDInput .setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeMInput .setTextColor(ContextCompat.getColor(context, tudeColor));
        tudeSInput .setTextColor(ContextCompat.getColor(context, tudeColor));

        return true;
    }




    /* ******
     *
     * Setters and Getters
     *
     **********/

    double getLatitude()                { return mLatitude;        }
    void   setLatitude(double latitude) { mLatitude = latitude; }

    int    getLatitudeDegree()  { return Utilities.getDegrees(mLatitude);  }
    int    getLatitudeMinute()  { return Utilities.getMinutes(mLatitude);  }
    double getLatitudeSecond()  { return Utilities.getSeconds(mLatitude);  }


    double getLongitude()       { return mLongitude;       }
    void setLongitude(double longitude) { mLongitude = longitude; }

    int    getLongitudeDegree() { return Utilities.getDegrees(mLongitude); }
    int    getLongitudeMinute() { return Utilities.getMinutes(mLongitude); }
    double getLongitudeSecond() {
        return Utilities.getSeconds(mLongitude);
    }


    /* ******
     *
     * Static functions
     *
     **********/



    /* ******
     *
     * Constructors
     *
     **********/
    /* ************************************************************/
    /* ************************************************************/
    /*       Vanilla constructor, default values only             */
    /*                                                            */
    /* ************************************************************/
    /* ************************************************************/
    CoordinateLL() {
        //set all variables to their defaults
        initializeDefaultVariables();
    }

    protected void initializeDefaultVariables() {
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        //initialize all variables common to all coordinates
        super.initializeDefaultVariables();


        //Latitude in DD and DMS formats
        mLatitude       = 0d;

        //Longitude in DD and DMS formats
        mLongitude      = 0d;

        setElevation( 0); //Orthometric Elevation in Meters
        setGeoid(0);     //Mean Sea Level in Meters

        setScaleFactor( 0d);
        setConvergenceAngle(0d);

        setValidCoordinate(false);
    }


    /* *************
     * constructor utilities
     *
     */

    boolean latLongDD(double latitude, double longitude) {
        setLatitude(latitude);
        setLongitude(longitude);

        setValidCoordinate(convertDDToDMS ());
        return isValidCoordinate();
    }

    void latLongDMS(int latitudeDegree,  int latitudeMinute,  double latitudeSecond,
                    int longitudeDegree, int longitudeMinute, double longitudeSecond) {

        setLatitude (Utilities.getDecimalDegrees(latitudeDegree, latitudeMinute, latitudeSecond));
        setLongitude(Utilities.getDecimalDegrees(longitudeDegree, longitudeMinute, longitudeSecond));

        setValidCoordinate(convertDDToDMS());

    }

    void latLongDDStrings(CharSequence latitudeString, CharSequence longitudeString) {
        if (latitudeString.toString().isEmpty()  ) {
            latitudeString = "0.0";
        }
        if ( longitudeString.toString().isEmpty() ){
            longitudeString = "0.0";
        }
        setLatitude(Double.parseDouble(latitudeString.toString()));
        setLongitude(Double.parseDouble(longitudeString.toString()));
        setValidCoordinate(convertDDToDMS ());
    }

    void latLongDMSStrings(  CharSequence latitudeDegreeString,
                             CharSequence latitudeMinuteString,
                             CharSequence latitudeSecondString,
                             CharSequence longitudeDegreeString,
                             CharSequence longitudeMinuteString,
                             CharSequence longitudeSecondString) {

        if (latitudeDegreeString.toString().isEmpty()) {
            latitudeDegreeString = "0";
        }
        if (latitudeMinuteString.toString().isEmpty()){
            latitudeMinuteString = "0";
        }
        if (latitudeSecondString.toString().isEmpty() ){
            latitudeSecondString = "0.0";
        }
        if (longitudeDegreeString.toString().isEmpty()){
            longitudeDegreeString = "0";
        }
        if (longitudeMinuteString.toString().isEmpty() ){
            longitudeMinuteString = "0";
        }
        if (longitudeSecondString.toString().isEmpty()) {
            longitudeSecondString = "0.0";
        }
        latLongDMS(Integer.valueOf(latitudeDegreeString.toString()),
                   Integer.valueOf(latitudeMinuteString.toString()),
                   Double.parseDouble(latitudeSecondString.toString()),
                   Integer.valueOf(longitudeDegreeString.toString()),
                   Integer.valueOf(longitudeMinuteString.toString()),
                   Double.parseDouble(longitudeSecondString.toString()));

    }



    boolean convertDDToDMS(){
        return (convertLatDDToDMS() && convertLngDDToDMS());
    }


    boolean convertLatDDToDMS(){
        //The inputs have to be valid
        if (mLatitude  < -90.0 || mLatitude  >= 90.0) {
            return false;
        }

        return true;
    }



    boolean convertLngDDToDMS(){
        //The inputs have to be valid
        if (mLongitude < -180. || mLongitude >= 180.){
            return false;
        }

        return true;
    }

}
