package com.androidchicken.coordcenter;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Elisabeth Huhn on 6/13/2016.
 *
 * This is a holder for constants and utilities that are not data type specific
 * enough to live in another class
 */
public class Utilities {

    //***********************************/
    //******** Static Constants *********/
    //***********************************/

    //Use this before an object gets saved to the DB
    static final long ID_DOES_NOT_EXIST = -1;


    static final boolean BUTTON_DISABLE = false;
    static final boolean BUTTON_ENABLE  = true;

    static final int sHundredthsDigitsOfPrecision = 2;
    static final int sMicrometerDigitsOfPrecision = 6;
    static final int sNanometerDigitsOfPrecision = 9;
    static final int sPicometerDigitsOfPrecision = 12;





    //************************************************************************************

    // TODO: 7/1/2017 These constants need to be moved to the CoordConstants object
    static final double sEquatorialRadiusA = 6378137.0; //equatorial radius in meters
    static final double sSemiMajorRadius   = sEquatorialRadiusA;
    static final double sPolarRadiusB      = 6356752.314245; //polar semi axis
    static final double sSemiMinorRadius   = sPolarRadiusB;


    //flatteningF = (equatorialRadiusA-polarRadiusB)/equatorialRadiusA;
    static final double sFlattening        = 0.0033528106811837;

    static final double sGravitationalConstant = 3.986004418e14; // cubic meters/square seconds
    static final double sMeanAngularVelocity   = 7.292115e-5;    // rads / s


    //used in calculating Moments of Inertia, based on EGM2008
    static final double sDynSecDegZonal     = -4.84165143790815e-4; //
    static final double sSectorialHarmonics = 2.43938357328313e-6; //

    //********************************************************************************

    // The U.S. Metric Law of 1866 provided the relationship that
    // one meter is equal to 39.37 inches, exactly

    //one yard equal to 0.9144 meters, exactly.
    // From that,  one foot is equal to one-third of that constant, or 0.3048 meters.
    // This is also equivalent to 2.54 centimeters equal to 1 inch,

    //3.28083989501 International Feet are equal to one meter

    static final double feetPerMeter   = 3.280833333;
    static final double inchesPerMeter = feetPerMeter*12.;   //39.37
    static final double cmPerInch      = 100. / (feetPerMeter * 12.); //2.54

    static final double ifeetPerMeter  = 3.28083989501;



    //***********************************/
    //******** Static Variables *********/
    //***********************************/
    private static Utilities ourInstance ;


    //***********************************/
    //******** Member Variables *********/
    //***********************************/



    //***********************************/
    //******** Static Methods   *********/
    //***********************************/
    static Utilities getInstance() {
        if (ourInstance == null){
            ourInstance = new Utilities();

        }
        return ourInstance;
    }



    static double convertMetersToFeet(double meters) {
        //function converts Feet to Meters.

        return meters * Utilities.feetPerMeter;
    }

    static double convertMetersToIFeet(double meters) {
        //function converts Meters to Ifeet.
        return meters * Utilities.ifeetPerMeter;
    }

    static double convertFeetToMeters(double feet){
        return feet / Utilities.feetPerMeter;
    }

    static double convertIFeetToMeters(double iFeet) {
        //function converts IFeet to Meters.
        return iFeet / Utilities.ifeetPerMeter;
    }

    static boolean convertMetersToFeet(Context context,
                                      EditText metersWidget,
                                      EditText feetWidget){
        double meters, feet;
        String meterString; //, feetString;

        try {
            //The inputs are limited to digital numbers by the xml, so don't need to check
            meterString = metersWidget.getText().toString();
            meters = Double.parseDouble(meterString);
        } catch  (NumberFormatException e) {
            return false;
        }

        int locPrecision = CCSettings.getLocPrecision((MainActivity)context);
        feet = meters * Utilities.feetPerMeter;
        feetWidget.setText(truncatePrecisionString(feet, locPrecision));

        if (meters < 0){
            metersWidget.setTextColor(ContextCompat.getColor(context, R.color.colorNegNumber));
            feetWidget  .setTextColor(ContextCompat.getColor(context, R.color.colorNegNumber));
        } else {
            metersWidget.setTextColor(ContextCompat.getColor(context, R.color.colorPosNumber));
            feetWidget  .setTextColor(ContextCompat.getColor(context, R.color.colorPosNumber));
        }

        return true;

    }

    static boolean convertFeetToMeters(Context context,
                                      EditText metersWidget,
                                      EditText feetWidget){
        double meters, feet;
        String feetString; //meterString too if needed

        try {
            //The inputs are limited to digital numbers by the xml, so don't need to check
            feetString = feetWidget.getText().toString();
            feet = Double.parseDouble(feetString);

        } catch  (NumberFormatException e) {
            return false;
        }

        meters = feet  / Utilities.feetPerMeter;
        int locPrecision = CCSettings.getLocPrecision((MainActivity)context);
        metersWidget.setText(truncatePrecisionString(meters, locPrecision));


        if (feet < 0){
            metersWidget.setTextColor(ContextCompat.getColor(context, R.color.colorNegNumber));
            feetWidget  .setTextColor(ContextCompat.getColor(context, R.color.colorNegNumber));
        } else {
            metersWidget.setTextColor(ContextCompat.getColor(context, R.color.colorPosNumber));
            feetWidget  .setTextColor(ContextCompat.getColor(context, R.color.colorPosNumber));
        }

        return true;
    }



    //this tells you how many screen inches are in a meter displayed on the map
    static double getMetersPerScreenInch(double latitude, float zoomLevel){
        return (160. * getMetersPerPixel(latitude, zoomLevel));
    }



    //This is meters per dp, NOT physical pixel
    //There are 160 dp in an inch, so 160 dpPerInch
    //         mapMeters per screenInch = getMetersPerPixel() X PixelsPerInch
    private static double getMetersPerPixel(double latitude, float zoomLevel){
        return (getCircumferenceInMetersAtLatitude(latitude) /
                getCircumferenceInPixelsAtLatitude(latitude, zoomLevel));
        /*
        double metersPerPixelZoomZero  = 156543.03392; //by definition
        double metersPerPixel = metersPerPixelZoomZero *
                                Math.cos(latitude * Math.PI / 180) / Math.pow(2, zoomLevel);

        return metersPerPixel;
        */
    }


    private static double getCircumferenceInMetersAtLatitude(double latitude){
        double radius = getRadiusInMetersAtLatitude(latitude);
        return 2 * Math.PI * radius;
    }


    private static double getRadiusInMetersAtLatitude(double latitude) {
        //latitude in radians
        double latRad = latitude * Math.PI / 180.;
        double equRSqr = sEquatorialRadiusA * sEquatorialRadiusA;//in meters
        double polRSqr = sPolarRadiusB * sPolarRadiusB;
        double cosLat = Math.cos(latRad);

        double numerator = ((equRSqr * cosLat) * (equRSqr * cosLat)) +
                           ((polRSqr * cosLat) * (polRSqr * cosLat));
        double denominator = ((sEquatorialRadiusA * cosLat) * (sEquatorialRadiusA * cosLat)) +
                             ((sPolarRadiusB      * cosLat) * (sPolarRadiusB      * cosLat));

        return Math.sqrt(numerator / denominator);
    }

    private static double getCircumferenceInPixelsAtLatitude(double latitude, float zoomLevel){

        //at zoom level N, the circumference at the equator is approximately 256 * (2 to the N) dp
        double nPlus8 = (double)zoomLevel + 8.;
        double pixelsAtEquator = Math.pow(2, nPlus8);

        //Unit at Latitude = (Cosine of Latitude in Radians) X (Unit at Equator)
        double latRad = latitude * Math.PI / 180.;
        return Math.cos(latRad) * pixelsAtEquator;
    }



    static double getFeetPerPixel(double latitude, float zoomLevel){
        return convertMetersToFeet(getMetersPerPixel(latitude, zoomLevel));
    }



    private static int getPixelDensityHeight(Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;

    }
    private static int getPixelDensityWidth(Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return  dm.widthPixels;
    }


    static void soundMeanComplete(MainActivity activity){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(activity.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }



    static String truncatePrecisionString(MainActivity activity, int precisionType, double inputValue){

        //Default is the precision for locations
        int digitsOfPrecision = CCSettings.getLocPrecision(activity);
        if (precisionType == CCSettings.sCAPrc){
            digitsOfPrecision = CCSettings.getCAPrecision(activity);

        } else if (precisionType == CCSettings.sSfPrc){
            digitsOfPrecision = CCSettings.getSfPrecision(activity);

        } else if (precisionType == CCSettings.sStdPrc){
            digitsOfPrecision = CCSettings.getStdDevPrecision(activity);
        }

        if (digitsOfPrecision == 0)digitsOfPrecision = Utilities.sMicrometerDigitsOfPrecision;

        return truncatePrecisionString(inputValue, digitsOfPrecision);
    }

    static String truncatePrecisionString(double inputValue, int digitsOfPrecision){
        String form = "%."+digitsOfPrecision+"f\n";
        return String.format(form, inputValue);
    }

    static String getDistanceString(MainActivity activity, int digitsOfPrecision, double distanceValue){

        int distUnits = CCSettings.getDistUnits(activity);

        double distanceMeters = distanceValue;
        if (distUnits == CCSettings.sFeet){
            distanceMeters = Utilities.convertFeetToMeters(distanceValue);
        }
        if (distUnits == CCSettings.sIntFeet){
            distanceMeters = Utilities.convertIFeetToMeters(distanceValue);
        }

        return Utilities.truncatePrecisionString(distanceMeters, digitsOfPrecision);
    }



    static double getDecimalDegrees(int degrees, int minutes, double seconds){
        double dd = degrees + ((minutes + (seconds / 60.))/60.);
        return dd;
    }

    static double getSeconds (double decimalDegrees){
        int degrees = (int)decimalDegrees;
        double remainderMin = ((decimalDegrees - degrees) * 60.);
        int minutes = (int) (remainderMin);
        return (remainderMin - minutes) * 60.; //seconds
    }

    static int getMinutes (double decimalDegrees){
        int degrees = (int)decimalDegrees;
        double remainderMin = ((decimalDegrees - degrees) * 60.);
        return (int) (remainderMin);//minutes
    }

    static int getDegrees (double decimalDegrees){
        return (int)decimalDegrees;
    }


    private String convertToFormat(double value){
        DecimalFormat df = new DecimalFormat("#.##");

        return df.format(value);
    }



    static String getDateTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateTimeInstance().format(date);
    }

    static long getDateTimeFromString(Context activity, String timeString){
        Date date;
        try {
            SimpleDateFormat format = getDateTimeFormat();
            date = format.parse(timeString);
        } catch ( ParseException e){
            Utilities.getInstance().errorHandler(activity, e.getMessage());
            return 0;
        }
        return date.getTime();
    }

    private static SimpleDateFormat getDateTimeFormat(){
        return new SimpleDateFormat("MMM d, yyyy hh:mm:ss", Locale.getDefault());
    }


    //***********************************/
    //****   Location UI conversions ****/
    //***********************************/

    static void locDD(MainActivity activity, double tude, int locDigOfPrec,
                      boolean isDir, int posHemi, int negHemi,
                      TextView dirView, TextView tudeView){


        int tudeColor= R.color.colorPosNumber;
        if (isDir){

            if (tude < 0.){
                dirView.setText(activity.getString(negHemi));
                tude = Math.abs(tude);
            } else {
                dirView.setText(activity.getString(posHemi));
            }
            dirView.setVisibility(View.VISIBLE);
        } else {
            dirView.setVisibility(View.GONE);
            if (tude < 0.) {
                tudeColor = R.color.colorNegNumber;
            }
        }


        String tudeString = Utilities.truncatePrecisionString(tude, locDigOfPrec);
        tudeView.setText(tudeString);
        tudeView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        tudeView .setTextColor(ContextCompat.getColor(activity, tudeColor));
    }

    static void locDMS(MainActivity activity,
                       int tudeDeg, int tudeMin, double tudeSec, int locDigOfPrec,
                       boolean isDir, int posHemi, int negHemi, TextView dirView,
                       TextView tudeDegView, TextView tudeMinView, TextView tudeSecView){
        int tudeColor = R.color.colorPosNumber;

        if (isDir){

            if ((tudeDeg < 0) || (tudeMin < 0) || (tudeSec < 0.)){
                dirView.setText(activity.getString(negHemi));
                tudeDeg = Math.abs(tudeDeg);
                tudeMin = Math.abs(tudeMin);
                tudeSec = Math.abs(tudeSec);
            } else {
                dirView.setText(activity.getString(posHemi));
            }
        } else if ((tudeDeg < 0) || (tudeMin < 0) || (tudeSec < 0.)){
            tudeColor = R.color.colorNegNumber;
        }

        tudeDegView.setText(String.valueOf(tudeDeg));
        tudeDegView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        tudeDegView .setTextColor(ContextCompat.getColor(activity, tudeColor));


        tudeMinView.setText(String.valueOf(tudeMin));
        tudeMinView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        tudeMinView .setTextColor(ContextCompat.getColor(activity, tudeColor));


        String tudeSecString = Utilities.truncatePrecisionString(tudeSec, locDigOfPrec);
        tudeSecView.setText(tudeSecString);
        tudeSecView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        tudeSecView .setTextColor(ContextCompat.getColor(activity, tudeColor));
    }

    static void caDD(MainActivity activity, double ca, int locDigOfPrec, TextView caView){


        int caColor= R.color.colorPosNumber;
        if (ca < 0.){
            caColor = R.color.colorNegNumber;
        }


        String caString = Utilities.truncatePrecisionString(ca, locDigOfPrec);
        caView.setText(caString);
        caView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        caView .setTextColor(ContextCompat.getColor(activity, caColor));
    }

    static void caDMS(MainActivity activity,
                      int caDeg, int caMin, double caSec, int locDigOfPrec,
                      TextView caDegView, TextView caMinView, TextView caSecView){
        int caColor = R.color.colorPosNumber;

        if ((caDeg < 0) || (caMin < 0) || (caSec < 0.)){
            caColor = R.color.colorNegNumber;
        }

        caDegView.setText(String.valueOf(caDeg));
        caDegView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        caDegView .setTextColor(ContextCompat.getColor(activity, caColor));


        caMinView.setText(String.valueOf(caMin));
        caMinView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        caMinView .setTextColor(ContextCompat.getColor(activity, caColor));


        String caSecString = Utilities.truncatePrecisionString(caSec, locDigOfPrec);
        caSecView.setText(caSecString);
        caSecView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorWhite));
        caSecView .setTextColor(ContextCompat.getColor(activity, caColor));
    }

    static void locDistance(MainActivity activity, double distance,  TextView distanceView){


        int distUnits    = CCSettings.getDistUnits(activity);
        int locDigOfPrecision = CCSettings.getLocPrecision(activity);

        if (distUnits == CCSettings.sFeet){
            distance   = Utilities.convertMetersToFeet(distance);
        } else if (distUnits == CCSettings.sIntFeet){
            distance   = Utilities.convertMetersToIFeet(distance);
        }
        String distanceString   = Utilities.truncatePrecisionString(distance, locDigOfPrecision);

        distanceView.setText(distanceString);
    }




    //***********************************/
    //******** Constructors     *********/
    //***********************************/
    private Utilities() {

    }


    //***********************************/
    //********     Offsets      *********/
    //***********************************/
    void    wireOffsets      (MainActivity activity, View v){

        if (activity == null)return;
        final MainActivity myActivity = activity;
        final View         view = v;


        //Define the watchers and listeners
        TextWatcher offsetTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence offset, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence offset, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters
                if (!saveOffsets(myActivity, view)) {
                    errorHandler(myActivity, R.string.error_saving_offsets);
                }
            }

            @Override
            public void afterTextChanged(Editable offset) {
                //This tells you that somewhere within editable, it's text has changed
                int temp = 0;
                temp++;


            }
        };
        TextWatcher headingOffsetTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence heading, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence headingString, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters

                if (Utilities.isEmpty(headingString)) {
                    CCSettings.setHeadingOffset(myActivity, CCSettings.sHeadingOffsetDefault);
                } else {
                    //  do a check on value
                    double heading = Double.valueOf(headingString.toString().trim());
                    if ((heading >= 0) && (heading <= 360)) {
                        if (!saveOffsets(myActivity, view)) {
                            errorHandler(myActivity,
                                                                 R.string.error_saving_offsets);
                        }
                    } else {
                        showStatus(myActivity, R.string.heading_invalid);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable heading) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };

        View.OnFocusChangeListener changeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (!saveOffsets(myActivity, view)) {
                        errorHandler(myActivity, R.string.error_saving_offsets);
                    }
                }
            }
        };


        final EditText distanceOffsetView   = v.findViewById(R.id.offsetDistanceInput);
        final EditText headingOffsetView    = v.findViewById(R.id.offsetHeadingInput);
        final EditText elevationOffsetView  = v.findViewById(R.id.offsetElevationInput);


        distanceOffsetView .addTextChangedListener(offsetTextWatcher);
        headingOffsetView  .addTextChangedListener(headingOffsetTextWatcher);
        elevationOffsetView.addTextChangedListener(offsetTextWatcher);

        distanceOffsetView .setOnFocusChangeListener(changeListener);
        headingOffsetView  .setOnFocusChangeListener(changeListener);
        elevationOffsetView.setOnFocusChangeListener(changeListener);

        distanceOffsetView .setEnabled(true);
        headingOffsetView  .setEnabled(true);
        elevationOffsetView.setEnabled(true);
    }
    void    initializeOffsets(MainActivity activity, View v) {


        //
        // Offset Inputs
        //

        final EditText distanceOffsetView   = v.findViewById(R.id.offsetDistanceInput);
        final EditText headingOffsetView    = v.findViewById(R.id.offsetHeadingInput);
        final EditText elevationOffsetView  = v.findViewById(R.id.offsetElevationInput);

        double offset = CCSettings.getDistanceOffset(activity);
        String offsetString = String.valueOf(offset);
        distanceOffsetView.setText(offsetString);

        offset = CCSettings.getHeadingOffset(activity);
        offsetString = String.valueOf(offset);
        headingOffsetView.setText(offsetString);

        offset = CCSettings.getElevationOffset(activity);
        offsetString = String.valueOf(offset);
        elevationOffsetView.setText(offsetString);
    }
    boolean saveOffsets      (MainActivity activity, View v){

        if ((activity == null) || (v == null))return false;


        final EditText distanceOffsetView   = v.findViewById(R.id.offsetDistanceInput);
        final EditText headingOffsetView    = v.findViewById(R.id.offsetHeadingInput);
        final EditText elevationOffsetView  = v.findViewById(R.id.offsetElevationInput);


        String offsetString = distanceOffsetView.getText().toString().trim();
        if (!Utilities.isEmpty(offsetString)) {
            CCSettings.setDistanceOffset(activity, Double.valueOf(offsetString));
        } else {
            CCSettings.setDistanceOffset(activity, CCSettings.sDistanceOffsetDefault);
        }

        offsetString = headingOffsetView.getText().toString().trim();
        if (!Utilities.isEmpty(offsetString)) {
            CCSettings.setHeadingOffset(activity, Double.valueOf(offsetString));
        } else {
            CCSettings.setHeadingOffset(activity, CCSettings.sHeadingOffsetDefault);
        }

        offsetString = elevationOffsetView.getText().toString().trim();
        if (!Utilities.isEmpty(offsetString)) {
            CCSettings.setElevationOffset(activity, Double.valueOf(offsetString));
        } else {
            CCSettings.setElevationOffset(activity, CCSettings.sElevationOffsetDefault);
        }
        return true;
    }

    //***********************************/
    //********     Spinners     *********/
    //***********************************/
    void wireSpinners(final MainActivity activity, View v){
        if ((activity == null) || (v == null))return;
        final MainActivity myActivity = activity;

        //Create the array of spinner choices from the Types of Coordinates defined
        //The order of these is used when an item is selected, so if you change these,
        // change the item selected listener as well
        String [] distanceUnits = new String[]{ CCSettings.sMetersString,
                                                CCSettings.sFeetString,
                                                CCSettings.sIntFeetString};

        String [] dataSourceTypes = new String[]{
                                            activity.getString(R.string.select_data_source),
                                            activity.getString(R.string.manual_wgs_data_source),
                                            activity.getString(R.string.manual_spcs_data_source),
                                            activity.getString(R.string.manual_utm_data_source),
                                            activity.getString(R.string.phone_gps),
                                            activity.getString(R.string.external_gps),
                                            activity.getString(R.string.cell_tower_triangulation)};


        //Then initialize the spinner itself

        Spinner distUnitsSpinner           = v.findViewById(R.id.distance_units_spinner);
        Spinner dataSourceSpinner          = v.findViewById(R.id.data_source_spinner);

        // Create the ArrayAdapters using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> duAdapter = new ArrayAdapter<>(activity,
                                                            android.R.layout.simple_spinner_item,
                                                            distanceUnits);


        ArrayAdapter<String> dsAdapter = new ArrayAdapter<>(activity,
                                                            android.R.layout.simple_spinner_item,
                                                            dataSourceTypes);


        // Specify the layout to use when the list of choices appears
        duAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dsAdapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        distUnitsSpinner    .setAdapter(duAdapter);
        dataSourceSpinner   .setAdapter(dsAdapter);

        //attach the listener to the spinner
        distUnitsSpinner .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //reset the new value

                if (myActivity != null) {
                    //Based on the order presented to the user,
                    // which should be in the same order as the constants: Settings.sMeter, etc

                    CCSettings.setDistUnits(myActivity, position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dataSourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int msg;

                switch(position){
                    case CCSettings.sDataSourceNoneSelected:

                        msg = R.string.select_data_source;
                        break;
                    case CCSettings.sDataSourceWGSManual:
                        msg = R.string.manual_wgs_data_source;

                        break;
                    case CCSettings.sDataSourceSPCSManual:
                        msg = R.string.manual_spcs_data_source;
                        break;
                    case CCSettings.sDataSourceUTMManual:
                        msg = R.string.manual_utm_data_source;
                        break;
                    case CCSettings.sDataSourcePhoneGps:

                        if (isGPSEnabled(myActivity)) {
                            msg = R.string.phone_gps;
                        } else {
                            msg = R.string.phone_gps_not_available;
                        }

                        break;
                    case CCSettings.sDataSourceExternalGps:
                        //msg = R.string.external_gps;
                        msg = R.string.external_gps_not_available;
                        break;
                    case CCSettings.sDataSourceCellTowerTriangulation:
                        //msg = R.string.cell_tower_triangulation;
                        msg = R.string.cell_tower_triangu_not_available;
                        break;
                    default:
                        msg = R.string.select_data_source;
                }

                String selectMsg = myActivity.getString(R.string.selected_data_source,
                                                        myActivity.getString(msg) );

                showStatus(activity, selectMsg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //for now, do nothing
            }
        });

    }
    void initializeSpinners(MainActivity activity, View v) {

        //
        // Spinners
        //
        Spinner distUnitsSpinner           = v.findViewById(R.id.distance_units_spinner);
        Spinner dataSourceSpinner          = v.findViewById(R.id.data_source_spinner);

        int position = CCSettings.getDistanceUnits(activity);
        distUnitsSpinner.setSelection(position);

        position = CCSettings.getDataSource(activity);
        dataSourceSpinner.setSelection(position);

    }



    private boolean isGPSEnabled(MainActivity activity) {

        if (activity == null) return false;

        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return ((lm != null) && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)));

    }


    //***********************************/
    //******** Member Methods   *********/
    //***********************************/

    //Just a stub for now, but figure out what to do
     void errorHandler(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

     void errorHandler(Context context, int messageResource) {
        errorHandler(context, context.getString(messageResource));
    }

     void showStatus(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

     void showStatus(Context context, int messageResource){
        showStatus(context, context.getString(messageResource));
    }



    //************************************/
    /*         Widget Utilities          */
    //************************************/
    void enableButton(Context context, Button button, boolean enable){
        button.setEnabled(enable);
        if (enable == BUTTON_ENABLE) {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
        } else {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }
    }

    void showSoftKeyboard(FragmentActivity activity, EditText textField){
        //Give the view the focus, then show the keyboard

        textField.requestFocus();
        InputMethodManager imm =
                (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            //second parameter is flags. We don't need any of them
            imm.showSoftInput(textField, InputMethodManager.SHOW_FORCED);
            clearFocus(activity);
        }
    }

    void hideSoftKeyboard(FragmentActivity activity){
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            //second parameter is flags. We don't need any of them
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


        }
        //close the keyboard
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    void toggleSoftKeyboard(FragmentActivity activity){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            //second parameter is flags. We don't need any of them
            imm.toggleSoftInputFromWindow(view.getWindowToken(),0, 0);
        }

    }


    void clearFocus(FragmentActivity activity){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
    }


    void hideKeyboard(Activity activity) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                View v = window.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) activity.
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm!=null){
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
    }




}
