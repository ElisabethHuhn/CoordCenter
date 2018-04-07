package com.androidchicken.coordcenter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Date;
import java.util.Locale;



/**
 * The Collect Fragment is the UI
 * when the workflow from WGS84 GPS to NAD83 to UTM/State Plane Coordinates
 * Created by Elisabeth Huhn on 6/15/2016 for GeoBot.
 *  Cloned from GeoBot on 1/3/2018
 */
public class CoordinateMeasureFragment extends Fragment implements GpsStatus.Listener,
                                                                    LocationListener,
                                                                    GpsStatus.NmeaListener {

    boolean isFirst = true;


    //These must be in the same order as the items are
    // added to the spinner in wireDataSourceSpinner{}

    private static final boolean sENABLE = true;
    private static final boolean sDISABLE = false;


    private NmeaParser mNmeaParser = NmeaParser.getInstance();
    private LocationManager locationManager;
    private NmeaSentence mNmeaData; //latest nmea sentence received

    private Location mCurLocation;


    //Contains all raw data and current results of meaning such data
    private MeanToken mMeanToken;

    private Point mPointBeingMaintained;


     private boolean isGpsOn            = true;

    //**********************************************************/
    //*****     Coordinates being displayed           **********/
    //**********************************************************/
    private CoordinateWGS84 mCurrentUIWGS84;
    //private CoordinateUTM mCurrentUIUTM;
    //private CoordinateSPCS mCurrentUISPCS;


    //**********************************************************/
    //*****  DataSource types for Spinner Widgets     **********/
    //**********************************************************/
    private int    mCurDataSource;

    //**********************************************************/
    //*****      Static Methods                       **********/
    //**********************************************************/
    public static CoordinateMeasureFragment newInstance(Point point) {

        Bundle args = Point.putPointInArguments(new Bundle(), point);

        CoordinateMeasureFragment fragment = new CoordinateMeasureFragment();

        fragment.setArguments(args);
        return fragment;
    }

    //**********************************************************/
    //*****  Constructor                              **********/
    //**********************************************************/

    public CoordinateMeasureFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created
    }


    //**********************************************************/
    //*****  Lifecycle Methods                        **********/
    //**********************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);


        mPointBeingMaintained = Point.getPointFromArguments((MainActivity)getActivity(), getArguments());
        if (mPointBeingMaintained == null){
            mPointBeingMaintained = new Point();
            //Theoretically the project id might be null,
            // but you can't really get this far if the project does not exist and is not open
            initializePoint();
        }
        long projectID = mPointBeingMaintained.getForProjectID();
        if (projectID == Utilities.ID_DOES_NOT_EXIST){
            initializePoint();
        }

    }
    private void initializePoint(){
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

        mPointBeingMaintained.setForProjectID(Utilities.ID_DOES_NOT_EXIST);
        mPointBeingMaintained.setHeight(CCSettings.getHeight(activity));
        mPointBeingMaintained.setPointNumber((int)Utilities.ID_DOES_NOT_EXIST);

    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity activity = (MainActivity)getActivity();
        if (activity == null) return null;

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coord_measure, container, false);

        Utilities utilities = Utilities.getInstance();
        wireWidgets(v);
        wireSpinners(v);
        utilities.wireOffsets (activity, v);

        initializeUI(v);
        utilities.initializeSpinners(activity, v);
        utilities.initializeOffsets (activity, v);

        initializeMeanProgressUI(v);


        //get rid of the soft keyboard if it is visible

        EditText aboutWho = v.findViewById(R.id.gpsWgs84LatDirInput);
        Utilities.getInstance().showSoftKeyboard(activity, aboutWho);
        //Utilities.getInstance().hideSoftKeyboard(activity);
        //Utilities.getInstance().hideKeyboard(activity);

        isFirst = true;

        return v;
    }


    //Ask for location events to start
    @Override
    public void onResume() {
        super.onResume();

        if ((mCurDataSource == CCSettings.sDataSourcePhoneGps) && (isGpsOn)) {
            startGps();
        }
        setSubtitle();
    }

    private void setSubtitle() {
        ((MainActivity) getActivity()).setSubtitle(R.string.subtitle_measure);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    //Ask for location events to stop
    @Override
    public void onPause() {
        super.onPause();

        stopGps();
    }


   //+****************************************************


    //+**********************************************

    private void wireWidgets(View v) {

        //make DD vs DMS on the project work by making various views invisible
        turnOffViews(v);

        //Start/Stop Data  Button
        Button startStopDataButton = v.findViewById(R.id.startStopDataButton);
        startStopDataButton.setLongClickable(true);
        startStopDataButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //((MainActivity)getActivity()).switchToListSatellitesScreen();
                return false;
            }
        });
        startStopDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){


                int msg = 0;
                switch(mCurDataSource){
                    case CCSettings.sDataSourceNoneSelected:
                        msg = R.string.select_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        break;
                    case CCSettings.sDataSourceWGSManual: //manual
                        msg = R.string.manual_wgs_data_source;
                        stopGps();
                        enableManualWgsInput(sENABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);

                        break;
                    case CCSettings.sDataSourceSPCSManual: //manual
                        msg = R.string.manual_spcs_data_source;
                        stopGps();
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sENABLE);
                        enableManualUtmInput(sDISABLE);

                        break;
                    case CCSettings.sDataSourceUTMManual: //manual
                        msg = R.string.manual_utm_data_source;
                        stopGps();
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sENABLE);

                        break;
                    case CCSettings.sDataSourcePhoneGps://pnone GPS
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        //msg = R.string.phone_gps;
                        if (isGpsOn){
                            stopGps();
                            isGpsOn = false;
                            msg = R.string.stop_gps_button_label;
                        } else {
                            startGps();
                            isGpsOn = true;
                            msg = R.string.start_gps_button_label;
                        }

                        break;
                    case CCSettings.sDataSourceExternalGps://external gps
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        //msg = R.string.external_gps;
                        msg = R.string.external_gps_not_available;
                        stopGps();
                        break;
                    case CCSettings.sDataSourceCellTowerTriangulation: //cell tower triangulation
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        //msg = R.string.cell_tower_triangulation;
                        msg = R.string.cell_tower_triangu_not_available;
                        stopGps();

                        break;
                    default:
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        msg = R.string.select_data_source;
                }

                 Utilities.getInstance().showStatus(getActivity(), msg);
            }//End on Click
        });



        //Conversion Button
        Button convertButton = v.findViewById(R.id.convertButton);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                int message = R.string.convert_error_mean;
                if (!isMeanInProgress() ) {
                    message = onConvert();
                }
                Utilities.getInstance().showStatus(getActivity(), message);

            }//End on Click
        });

        //Clear Form Button
        Button clearButton = v.findViewById(R.id.clearFormButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //Can always clear the form, even if during mean. It will just come right back
                clearForm();
            }
        });


        //Start / stop Mean Button
        Button startMeanButton = v.findViewById(R.id.startMeanButton);
        startMeanButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                return true;
            }
        });
        startMeanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                MainActivity activity = (MainActivity)getActivity();
                if (activity == null)return;

                int message;
                if (isMeanInProgress()){

                    message = completeMeanProcessing(mMeanToken, mNmeaData);

                } else if (mCurDataSource == CCSettings.sDataSourceNoneSelected){
                    message = R.string.select_data_source;
                } else if ((mCurDataSource == CCSettings.sDataSourceWGSManual) ||
                           (mCurDataSource == CCSettings.sDataSourceSPCSManual)||
                           (mCurDataSource == CCSettings.sDataSourceUTMManual))  {

                    message = R.string.can_not_mean_manual;
                } else {
                    message = startMeaning();
                }
                updateMeanProgressUI();

                Utilities.getInstance().showStatus(getActivity(), message);
            }//End on Click
        });

        //Set RMS v Standard Deviation Label
        //Set the label to Standard Deviation if in settings
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;
        if (CCSettings.isRms(activity)) {


            int coordType = CCSettings.getCoordinateType(activity);
            int eRmsLabel = R.string.ele_rms_label;
            int hRmsLabel = R.string.hrms_label;
            int vRmsLabel = R.string.vrms_label;
            if ((coordType == Coordinate.sCoordinateDBTypeSPCS) ||
                (coordType == Coordinate.sCoordinateDBTypeUTM)) {
                hRmsLabel = R.string.eing_rms_label;
                vRmsLabel = R.string.ning_rms_label;
            }
            TextView eleLabelView  = v.findViewById(R.id.meanWgs84ElevSigmaLabel);
            TextView hrmsLabelView = v.findViewById(R.id.meanWgs84LngSigmaLabel);
            TextView vrmsLabelView = v.findViewById(R.id.meanWgs84LatSigmaLabel);
            eleLabelView.setText (eRmsLabel);
            hrmsLabelView.setText(hRmsLabel);
            vrmsLabelView.setText(vRmsLabel);
        }
    }
    private void wireSpinners(View v){

        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

        //Do most of the stuff in common
        Utilities.getInstance().wireSpinners(activity, v);

        //But need a different listener here
        Spinner dataSourceSpinner          = v.findViewById(R.id.data_source_spinner);

        dataSourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearForm();
                mCurDataSource = position;

                int msg = 0;
                switch(mCurDataSource){
                    case CCSettings.sDataSourceNoneSelected:
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        msg = R.string.select_data_source;
                        break;
                    case CCSettings.sDataSourceWGSManual:
                        msg = R.string.manual_wgs_data_source;
                        enableManualWgsInput(sENABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();

                        break;
                    case CCSettings.sDataSourceSPCSManual:
                        msg = R.string.manual_spcs_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sENABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();

                        break;
                    case CCSettings.sDataSourceUTMManual:
                        msg = R.string.manual_utm_data_source;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sENABLE);
                        stopGps();

                        break;
                    case CCSettings.sDataSourcePhoneGps:

                        if (isGPSEnabled()) {
                            initializeGPS();
                            startGps();
                            enableManualWgsInput(sDISABLE);
                            enableManualSpcsInput(sDISABLE);
                            enableManualUtmInput(sDISABLE);
                            msg = R.string.phone_gps;
                        } else {
                            //tell user no GPS, but otherwise do nothing
                            msg = R.string.phone_gps_not_enabled;
                        }

                        break;
                    case CCSettings.sDataSourceExternalGps:
                        //msg = R.string.external_gps;
                        msg = R.string.external_gps_not_available;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        break;
                    case CCSettings.sDataSourceCellTowerTriangulation:
                        //msg = R.string.cell_tower_triangulation;
                        msg = R.string.cell_tower_triangu_not_available;
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();

                        break;
                    default:
                        enableManualWgsInput(sDISABLE);
                        enableManualSpcsInput(sDISABLE);
                        enableManualUtmInput(sDISABLE);
                        stopGps();
                        msg = R.string.select_data_source;
                }

                Utilities.getInstance().showStatus(getActivity(), msg);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //for now, do nothing
            }
        });

    }


    private void showDop() {
        SatelliteManager satelliteManager = SatelliteManager.getInstance();
        double hdopValue  = satelliteManager.getHdop();
        String hdopString = String.format(Locale.getDefault(),"%.3f", hdopValue);
        double vdopValue  = satelliteManager.getHdop();
        String vdopString = String.format(Locale.getDefault(),"%.3f", vdopValue);
        double pdopValue  = satelliteManager.getHdop();
        String pdopString = String.format(Locale.getDefault(),"%.3f", pdopValue);

        View view = getView();
        if (view == null)return ;

        TextView dopView = view.findViewById(R.id.hdopOutput);
        dopView.setText(hdopString);
        dopView = view.findViewById(R.id.vdopOutput);
        dopView.setText(vdopString);
        dopView = view.findViewById(R.id.pdopOutput);
        dopView.setText(pdopString);

        //Snackbar.make(view, dopValues, Snackbar.LENGTH_LONG).setAction("Action", null).show();

    }

    private void turnOffViews(View v){

        //set up the control flags based on Global and Project settings
        MainActivity activity = (MainActivity)getActivity();

        //int distUnits         = CCSettings.getDistUnits(activity);
        boolean isDD          = CCSettings.isLocDD(activity);
        boolean isPM          = CCSettings.isPM(activity);

        //Raw GPS
        //Meaned WGS84
        //SPCS
        //UTM

        //Raw GPS
        //GPS Latitude
        TextView LatitudeDirInput = v.findViewById(R.id.gpsWgs84LatDirInput);
        TextView LatitudeInput   = v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView LatDegreesInput = v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView LatMinutesInput = v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView LatSecondsInput = v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView LongitudeDirInput = v.findViewById(R.id.gpsWgs84LngDirInput);
        TextView LongitudeInput   = v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView LongDegreesInput = v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView LongMinutesInput = v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView LongSecondsInput = v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        //TextView ElevationMetersInput   = v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        //TextView GeoidHeightMetersInput = v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput  = v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput  = v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput  = v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        if (isPM){
            LatitudeDirInput.setVisibility(View.GONE);
            LongitudeDirInput.setVisibility(View.GONE);
        }

        if (isDD){
            LatDegreesInput.setVisibility(View.GONE);
            LatMinutesInput.setVisibility(View.GONE);
            LatSecondsInput.setVisibility(View.GONE);

            LongDegreesInput.setVisibility(View.GONE);
            LongMinutesInput.setVisibility(View.GONE);
            LongSecondsInput.setVisibility(View.GONE);

            CAdegInput .setVisibility(View.GONE);
            CAminInput .setVisibility(View.GONE);
            CAsecInput .setVisibility(View.GONE);

        } else { //is DMS
            LatitudeInput        .setVisibility(View.GONE);
            LongitudeInput       .setVisibility(View.GONE);
            ConvergenceAngleInput.setVisibility(View.GONE);
        }


        //Mean Latitude
        TextView meanWgs84LatitudeDirInput = v.findViewById(R.id.meanWgs84LatDirInput);
        TextView meanWgs84LatitudeInput   = v.findViewById(R.id.meanWgs84LatitudeInput);
        TextView meanWgs84LatDegreesInput = v.findViewById(R.id.meanWgs84LatDegreesInput);
        TextView meanWgs84LatMinutesInput = v.findViewById(R.id.meanWgs84LatMinutesInput);
        TextView meanWgs84LatSecondsInput = v.findViewById(R.id.meanWgs84LatSecondsInput);

        TextView meanWgs84LongitudeDirInput = v.findViewById(R.id.meanWgs84LngDirInput);
        TextView meanWgs84LongitudeInput   = v.findViewById(R.id.meanWgs84LongitudeInput);
        TextView meanWgs84LongDegreesInput = v.findViewById(R.id.meanWgs84LongDegreesInput);
        TextView meanWgs84LongMinutesInput = v.findViewById(R.id.meanWgs84LongMinutesInput);
        TextView meanWgs84LongSecondsInput = v.findViewById(R.id.meanWgs84LongSecondsInput);

        //Elevation
        //TextView meanWgs84ElevationMetersInput   = v.findViewById(
         //                                                       R.id.meanWgs84ElevationMetersInput);

        //TextView meanWgs84GeoidHeightMetersInput = v.findViewById(
         //                                                       R.id.meanWgs84GeoidHeightMetersInput);


        if (isPM){
            meanWgs84LatitudeDirInput.setVisibility(View.GONE);
            meanWgs84LongitudeDirInput.setVisibility(View.GONE);
        }

        if (isDD){
            meanWgs84LatDegreesInput.setVisibility(View.GONE);
            meanWgs84LatMinutesInput.setVisibility(View.GONE);
            meanWgs84LatSecondsInput.setVisibility(View.GONE);

            meanWgs84LongDegreesInput.setVisibility(View.GONE);
            meanWgs84LongMinutesInput.setVisibility(View.GONE);
            meanWgs84LongSecondsInput.setVisibility(View.GONE);


        } else { //is DMS
            meanWgs84LatitudeInput        .setVisibility(View.GONE);
            meanWgs84LongitudeInput       .setVisibility(View.GONE);
         }





        //SPC
        //TextView spcEastingMetersOutput  = v.findViewById(R.id.spcEastingMetersOutput);
        //TextView spcNorthingMetersOutput = v.findViewById(R.id.spcNorthingMetersOutput);

        //Elevation
        //TextView spcsElevationMetersInput   = v.findViewById(R.id.spcsElevationMetersInput);
        //TextView spcsGeoidHeightMetersInput = v.findViewById(R.id.spcsGeoidHeightMetersInput);

        //convergence & scale factor
        ConvergenceAngleInput  = v.findViewById(R.id.spcConvergenceInput);
        CAdegInput  = v.findViewById(R.id.spcConvDegreesInput);
        CAminInput  = v.findViewById(R.id.spcConvMinutesInput);
        CAsecInput  = v.findViewById(R.id.spcConvSecondsInput);




        if (isDD){
            CAdegInput .setVisibility(View.GONE);
            CAminInput .setVisibility(View.GONE);
            CAsecInput .setVisibility(View.GONE);

        } else { //is DMS

            ConvergenceAngleInput.setVisibility(View.GONE);

        }


        //TextView utmEastingMetersOutput  = v.findViewById(R.id.utmEastingMetersOutput);
        //TextView utmNorthingMetersOutput = v.findViewById(R.id.utmNorthingMetersOutput);

        //TextView utmElevationOutput       = v.findViewById(R.id.utmElevationMetersInput) ;
        //TextView utmGeoidOutput           = v.findViewById(R.id.utmGeoidHeightMetersInput) ;

        //convergence & scale factor
        ConvergenceAngleInput  = v.findViewById(R.id.utmConvergenceInput);
        CAdegInput  = v.findViewById(R.id.utmConvDegreesInput);
        CAminInput  = v.findViewById(R.id.utmConvMinutesInput);
        CAsecInput  = v.findViewById(R.id.utmConvSecondsInput);


        if (isDD){
            CAdegInput .setVisibility(View.GONE);
            CAminInput .setVisibility(View.GONE);
            CAsecInput .setVisibility(View.GONE);

        } else { //is DMS

            ConvergenceAngleInput.setVisibility(View.GONE);
        }
    }


    private void initializeUI(View v){

        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

        int zone = CCSettings.getZone(activity);
        updateSpcsZone(v, zone);

        TextView latitudeLabel        = v.findViewById(R.id.gpsWgs84LatitudeInputLabel);
        TextView longitudeLabel       = v.findViewById(R.id.gpsWgs84LongitudeInputLabel);

        TextView meanaLatitudeLabel   = v.findViewById(R.id.meanWgs84LatitudeInputLabel);
        TextView meanLongitudeLabel   = v.findViewById(R.id.meanWgs84LongitudeInputLabel);

        TextView northingLabel        = v.findViewById(R.id.utmNorthingInputLabel) ;
        TextView eastingLabel         = v.findViewById(R.id.utmEasingInputLabel);

        TextView spcNorthingLabel     = v.findViewById(R.id.spcNorthingInputLabel);
        TextView spcEastingLabel      = v.findViewById(R.id.spcEasingInputLabel);


        if (CCSettings.isLngLat((MainActivity)getActivity())){


            //Longitude comes before Latitude
            //Easting   comes before Northing
            //confusingly, this is done by switching lables on the screen

            latitudeLabel        = v.findViewById(R.id.gpsWgs84LongitudeInputLabel);
            longitudeLabel       = v.findViewById(R.id.gpsWgs84LatitudeInputLabel);

            meanaLatitudeLabel   = v.findViewById(R.id.meanWgs84LongitudeInputLabel);
            meanLongitudeLabel   = v.findViewById(R.id.meanWgs84LatitudeInputLabel);

            northingLabel        = v.findViewById(R.id.utmEasingInputLabel) ;
            eastingLabel         = v.findViewById(R.id.utmNorthingInputLabel);

            spcNorthingLabel     = v.findViewById(R.id.spcEasingInputLabel);
            spcEastingLabel      = v.findViewById(R.id.spcNorthingInputLabel);

        }
        latitudeLabel     .setText(getString(R.string.latitude_label));
        longitudeLabel    .setText(getString(R.string.longitude_label));
        meanaLatitudeLabel.setText(getString(R.string.latitude_label));
        meanLongitudeLabel.setText(getString(R.string.longitude_label));
        northingLabel     .setText(getString(R.string.northing_label));
        eastingLabel      .setText(getString(R.string.easting_label));
        spcNorthingLabel  .setText(getString(R.string.northing_label));
        spcEastingLabel   .setText(getString(R.string.easting_label));
    }


    //******************************************************************//
    //            Button Handlers                                       //
    //******************************************************************//


     //******************************************************************//
    //            Process the received NMEA sentence                    //
    //******************************************************************//
    void handleNmeaReceived(long timestamp, String nmea) {

        //update ui with DOP values
        showDop();


        try {
            //no need to process the sentence if no longer attached to the activity
            if (getActivity() == null)return;

            //create an object with all the fields from the string
            if (mNmeaParser == null)mNmeaParser = NmeaParser.getInstance();

            NmeaSentence nmeaData = mNmeaParser.parse(nmea);
            if (nmeaData == null) return;

            nmeaData.setTimeStamp(timestamp);

            nmeaData = filterNmeaData(nmeaData);
            if (nmeaData == null)return;

            //so we know it's a good point
            mNmeaData = nmeaData;


            CoordinateWGS84 coordinateWGS84;
            if (isMeanInProgress()){
                if (mMeanToken == null){
                    initializeMeanToken();
                }

                //Fold the new nmea sentence into the ongoing mean
                CoordinateMean meanCoordinate = mMeanToken.updateMean((MainActivity)getActivity(),
                                                                         mNmeaData);
                if (meanCoordinate != null) {
                    //Is this the first point we have processed?
                    if (isFirstMeanPoint()) {
                        mMeanToken.setStartMeanTime(mNmeaData.getTimeStamp());
                        mMeanToken.setFirstPointInMean(false);
                    }

                    updateNmeaUI(mMeanToken.getLastCoordinate());

                    updateMeanUI(meanCoordinate, mMeanToken);

                    //determine if we have enough fixed points to be done with the mean
                    MainActivity activity = (MainActivity)getActivity();
                    int numMean = CCSettings.getNumMean(activity);
                    if (numMean <= mMeanToken.getFixedReadings()){
                        completeMeanProcessing(mMeanToken, mNmeaData);

                    }
                }
            } else {
                if ((mMeanToken != null) && (mMeanToken.isLastPointInMean())){
                    //no need to recalcuclate the mean.
                    updateMeanUI(mMeanToken.getMeanCoordinate(false), mMeanToken);
                    mMeanToken.setLastPointInMean(false);
                }
                coordinateWGS84 = new CoordinateWGS84((MainActivity)getActivity(), nmeaData);

                //update the UI from the coordinate
                updateNmeaUI(coordinateWGS84);
            }

        } catch (RuntimeException e){
            //there was an exception processing the NMEA Sentence
            Utilities.getInstance().showStatus(getActivity(), e.getMessage());
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    private NmeaSentence filterNmeaData(NmeaSentence nmeaData){
        if (nmeaData == null) {
            Utilities.getInstance().showStatus(getActivity(), R.string.null_type_found);
            return  null;
        }


        //Which fields have meaning depend upon the type of the sentence
        String type = nmeaData.getNmeaType().toString();
        if (Utilities.isEmpty(type)) {
            Utilities.getInstance().showStatus(getActivity(), R.string.null_type_found);
            return  null;
        }


        // TODO: 7/6/2017 need to include Satellite sentences to be able to reject points from satellites too low on the horizon
        if (!type.contains("GGA")) {
            if (!type.contains("GNS")){
                //put satellite DOP in footer
                return null;
            }
        }


        if ((nmeaData.getLatitude() == 0.0) && (nmeaData.getLongitude() == 0.0)) {
            return null;
        }
        return nmeaData;
    }



    private boolean isMeanInProgress(){
        return ((mMeanToken != null) && mMeanToken.isMeanInProgress());
    }
    private boolean isFirstMeanPoint(){
        return mMeanToken.isFirstPointInMean();
    }

    private void initializeMeanToken(){
        if (mMeanToken == null)mMeanToken = new MeanToken();

        long pointID = mPointBeingMaintained.getPointID();
        mMeanToken.setProjectID(Utilities.ID_DOES_NOT_EXIST);
        mMeanToken.setPointID(pointID);
        mMeanToken.setMeanInProgress(false);
        mMeanToken.setFirstPointInMean(false);
        mMeanToken.setLastPointInMean(false);
        mMeanToken.resetCoordinates();
        updateMeanProgressUI();
    }


    private int startMeaning(){
        //set flags to start taking mean
        initializeMeanToken();
        mMeanToken.setFirstPointInMean(true);
        mMeanToken.setMeanInProgress(true);
        updateMeanProgressUI();

        //Remove UI focus so screen will scroll
        Utilities.getInstance().clearFocus(getActivity());

        return R.string.start_mean_button_label;
    }

    private int completeMeanProcessing(MeanToken token, NmeaSentence nmeaData){
        //set flags that mean is done
        token.setMeanInProgress(false);
        token.setEndMeanTime(nmeaData.getTimeStamp());
        token.setLastPointInMean(true);
        Utilities.soundMeanComplete((MainActivity)getActivity());
        updateMeanProgressUI();
        onConvert();
        return  R.string.stop_mean_button_label;
    }



    //******************************************************************//
    //       Update UI with a coordinate                                //
    //******************************************************************//

    private void updateNmeaUI(CoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return ;

        //Time
        TextView TimestampOutput = v.findViewById(R.id.gpsWgs84TimestampOutput);

        //String nmeaTimeString = String.format(Locale.getDefault(), "%.0f", nmeaData.getTime());
        //String wgsTimeString  = String.format(Locale.getDefault(), "%.0f", coordinateWGS84.getTime());
        //TimeOutput.setText(nmeaTimeString);
                //Eventually fix time between nmea and coordinate WGS84
                //setText(Double.toString(coordinateWGS84.getTime()));
                //setText(Double.toString(nmeaData.getTime()));

        //String timestampString = Utilities.getDateTimeString((long)nmeaData.getTimeStamp());

        String timestampString = Utilities.getDateTimeString(coordinateWGS84.getTime());
        TimestampOutput.setText(timestampString);

        updateWgsLocUI(coordinateWGS84);

    }
    private void updateWgsUI(CoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return ;

        //Time
        TextView TimestampOutput = v.findViewById(R.id.gpsWgs84TimestampOutput);

        long timeStamp = coordinateWGS84.getTime();
        String timestampString = Utilities.getDateTimeString(timeStamp);

        //String timeString = String.format(Locale.getDefault(), "%d", coordinateWGS84.getTime());

        if (timeStamp == 0){
            timestampString = "0";
        }
        TimestampOutput.setText(timestampString);


        updateWgsLocUI(coordinateWGS84);

    }
    private void updateWgsLocUI(CoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return ;

        mCurrentUIWGS84 = coordinateWGS84;

        //Latitude comes before Longitude

        //GPS Latitude
        TextView latitudeLabel   = v.findViewById(R.id.gpsWgs84LatitudeInputLabel);
        TextView latitudeDir     = v.findViewById(R.id.gpsWgs84LatDirInput);
        TextView latitudeInput   = v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView latDegreesInput = v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView latMinutesInput = v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView latSecondsInput = v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView longitudeLabel   = v.findViewById(R.id.gpsWgs84LongitudeInputLabel);
        TextView longitudeDir     = v.findViewById(R.id.gpsWgs84LngDirInput);
        TextView longitudeInput   = v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView longDegreesInput = v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView longMinutesInput = v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView longSecondsInput = v.findViewById(R.id.gpsWgs84LongSecondsInput);

        if (CCSettings.isLngLat((MainActivity)getActivity())){


            //Longitude comes before Latitude

            //GPS Latitude
            latitudeLabel   = v.findViewById(R.id.gpsWgs84LongitudeInputLabel);
            latitudeDir     = v.findViewById(R.id.gpsWgs84LngDirInput);
            latitudeInput   = v.findViewById(R.id.gpsWgs84LongitudeInput);
            latDegreesInput = v.findViewById(R.id.gpsWgs84LongDegreesInput);
            latMinutesInput = v.findViewById(R.id.gpsWgs84LongMinutesInput);
            latSecondsInput = v.findViewById(R.id.gpsWgs84LongSecondsInput);

            //GPS Longitude
            longitudeLabel   = v.findViewById(R.id.gpsWgs84LatitudeInputLabel);
            longitudeDir     = v.findViewById(R.id.gpsWgs84LatDirInput);
            longitudeInput   = v.findViewById(R.id.gpsWgs84LatitudeInput);
            longDegreesInput = v.findViewById(R.id.gpsWgs84LatDegreesInput);
            longMinutesInput = v.findViewById(R.id.gpsWgs84LatMinutesInput);
            longSecondsInput = v.findViewById(R.id.gpsWgs84LatSecondsInput);

        }

        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.gpsWgs84ConvSecondsInput);
        EditText ScaleFactorOutput      = v.findViewById(R.id.gpsWgs84ScaleFactor);


        MainActivity activity = (MainActivity)getActivity();
        boolean isDD = CCSettings.isLocDD(activity);
        boolean isDir = CCSettings.isDir(activity);

        int locDigOfPrecision = CCSettings.getLocPrecision(activity);
        if (isDD){
            double latitude  = coordinateWGS84.getLatitude();
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;
            Utilities.locDD(activity, latitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, latitudeDir, latitudeInput);

            double longitude = coordinateWGS84.getLongitude();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;
            Utilities.locDD(activity, longitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, longitudeDir, longitudeInput);

            double convAngle = coordinateWGS84.getConvergenceAngle();
            Utilities.caDD(activity, convAngle, locDigOfPrecision, ConvergenceAngleInput);

        } else {
            int deg = coordinateWGS84.getLatitudeDegree();
            int min = coordinateWGS84.getLatitudeMinute();
            double sec = coordinateWGS84.getLatitudeSecond();

            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;

            Utilities.locDMS(activity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, latitudeDir,
                    latDegreesInput, latMinutesInput, latSecondsInput);

            deg = coordinateWGS84.getLongitudeDegree();
            min = coordinateWGS84.getLongitudeMinute();
            sec = coordinateWGS84.getLongitudeSecond();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;

            Utilities.locDMS(activity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, longitudeDir,
                    longDegreesInput, longMinutesInput, longSecondsInput);


            int caDegOfPrecision = CCSettings.getCAPrecision(activity);
            deg = coordinateWGS84.getConvergenceAngleDegree();
            min = coordinateWGS84.getConvergenceAngleMinute();
            sec = coordinateWGS84.getConvergenceAngleSecond();
            Utilities.caDMS(activity,deg, min, sec,
                              caDegOfPrecision,
                              CAdegInput, CAminInput, CAsecInput);

        }

        //Elevation
        TextView ElevationMetersInput   = v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);

        double elevation = coordinateWGS84.getElevation();
        double geoid     = coordinateWGS84.getGeoid();

        Utilities.locDistance(activity, elevation, ElevationMetersInput);
        Utilities.locDistance(activity, geoid, GeoidHeightMetersInput);

        int sfPrecision  = CCSettings.getSfPrecision(activity);
        ScaleFactorOutput.
                setText(truncatePrecisionString(sfPrecision, coordinateWGS84.getScaleFactor()));

        Utilities.getInstance().clearFocus(activity);

    }
    private void updateMeanUI(CoordinateMean meanCoordinate, MeanToken meanToken){

        View v = getView();
        if (v == null)return ;

        //Mean Parameters
        TextView meanWgs84PointsInMeanOutput = v.findViewById(R.id.meanWgs84PointsInMeanOutput);
        //TextView meanWgs84StartTimeOutput = v.findViewById(R.id.meanWgs84StartTimeOutput);
        //TextView meanWgs84EndTimeOutput   = v.findViewById(R.id.meanWgs84EndTimeOutput);
        TextView meanWgs84StartTimestampOutput = v.findViewById(R.id.meanWgs84StartTimestampOutput);
        TextView meanWgs84EndTimestampOutput   = v.findViewById(R.id.meanWgs84EndTimestampOutput);

        //Latitude comes before Longitude

        TextView latitudeLabel   = v.findViewById(R.id.meanWgs84LatitudeInputLabel);
        TextView latitudeDir     = v.findViewById(R.id.meanWgs84LatDirInput);
        TextView latitudeInput   = v.findViewById(R.id.meanWgs84LatitudeInput);
        TextView latDegreesInput = v.findViewById(R.id.meanWgs84LatDegreesInput);
        TextView latMinutesInput = v.findViewById(R.id.meanWgs84LatMinutesInput);
        TextView latSecondsInput = v.findViewById(R.id.meanWgs84LatSecondsInput);

        //Longitude
        TextView longitudeLabel   = v.findViewById(R.id.meanWgs84LongitudeInputLabel);
        TextView longitudeDir     = v.findViewById(R.id.meanWgs84LngDirInput);
        TextView longitudeInput   = v.findViewById(R.id.meanWgs84LongitudeInput);
        TextView longDegreesInput = v.findViewById(R.id.meanWgs84LongDegreesInput);
        TextView longMinutesInput = v.findViewById(R.id.meanWgs84LongMinutesInput);
        TextView longSecondsInput = v.findViewById(R.id.meanWgs84LongSecondsInput);

        if (CCSettings.isLngLat((MainActivity)getActivity())){


            //Longitude comes before Latitude

            latitudeLabel   = v.findViewById(R.id.meanWgs84LongitudeInputLabel);
            latitudeDir     = v.findViewById(R.id.meanWgs84LngDirInput);
            latitudeInput   = v.findViewById(R.id.meanWgs84LongitudeInput);
            latDegreesInput = v.findViewById(R.id.meanWgs84LongDegreesInput);
            latMinutesInput = v.findViewById(R.id.meanWgs84LongMinutesInput);
            latSecondsInput = v.findViewById(R.id.meanWgs84LongSecondsInput);

            //Longitude
            longitudeLabel   = v.findViewById(R.id.meanWgs84LatitudeInputLabel);
            longitudeDir     = v.findViewById(R.id.meanWgs84LatDirInput);
            longitudeInput   = v.findViewById(R.id.meanWgs84LatitudeInput);
            longDegreesInput = v.findViewById(R.id.meanWgs84LatDegreesInput);
            longMinutesInput = v.findViewById(R.id.meanWgs84LatMinutesInput);
            longSecondsInput = v.findViewById(R.id.meanWgs84LatSecondsInput);

        }



        //Mean Standard Deviations
        TextView meanWgs84LatSigmaOutput = v.findViewById(R.id.meanWgs84LatSigmaOutput);
        TextView meanWgs84LongSigmaOutput= v.findViewById(R.id.meanWgs84LngSigmaOutput);
        TextView meanWgs84ElevSigmaOutput= v.findViewById(R.id.meanWgs84ElevSigmaOutput);





        //show the mean and standard deviation on the screen
        meanWgs84PointsInMeanOutput.setText(String.valueOf(meanCoordinate.getMeanedReadings()));

        long startTimestamp = (long)meanToken.getStartMeanTime();
        String startTimeStampString = Utilities.getDateTimeString(startTimestamp);

        long endTimestamp   = (long)meanToken.getEndMeanTime();
        String endTimestampString = Utilities.getDateTimeString(endTimestamp);

        meanWgs84StartTimestampOutput.setText(String.valueOf(startTimeStampString));

        if (endTimestamp == 0){
            endTimestampString = "0";
        }
        meanWgs84EndTimestampOutput.setText(String.valueOf(endTimestampString));

        MainActivity activity = (MainActivity)getActivity();
        int locPrecision = CCSettings.getLocPrecision(activity);
        int stdPrecision = CCSettings.getStdDevPrecision(activity);

        latitudeLabel         .setText(getString(R.string.latitude_label));
        longitudeLabel        .setText(getString(R.string.longitude_label));



        boolean isDD = CCSettings.isLocDD(activity);
        boolean isDir = CCSettings.isDir(activity);

        int locDigOfPrecision = CCSettings.getLocPrecision(activity);
        if (isDD){
            double latitude  = meanCoordinate.getLatitude();
            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;
            Utilities.locDD(activity, latitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, latitudeDir, latitudeInput);

            double longitude = meanCoordinate.getLongitude();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;
            Utilities.locDD(activity, longitude, locDigOfPrecision,
                    isDir, posHemi, negHemi, longitudeDir, longitudeInput);

        } else {
            int deg = meanCoordinate.getLatitudeDegree();
            int min = meanCoordinate.getLatitudeMinute();
            double sec = meanCoordinate.getLatitudeSecond();

            int posHemi = R.string.hemisphere_N;
            int negHemi = R.string.hemisphere_S;

            Utilities.locDMS(activity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, latitudeDir,
                    latDegreesInput, latMinutesInput, latSecondsInput);

            deg = meanCoordinate.getLongitudeDegree();
            min = meanCoordinate.getLongitudeMinute();
            sec = meanCoordinate.getLongitudeSecond();
            posHemi = R.string.hemisphere_E;
            negHemi = R.string.hemisphere_W;

            Utilities.locDMS(activity, deg, min, sec, locDigOfPrecision,
                    isDir, posHemi, negHemi, longitudeDir,
                    longDegreesInput, longMinutesInput, longSecondsInput);

        }

        //Elevation
        TextView ElevationMetersInput   = v.findViewById(R.id.meanWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = v.findViewById(R.id.meanWgs84GeoidHeightMetersInput);

        double elevation = meanCoordinate.getElevation();
        double geoid     = meanCoordinate.getGeoid();

        Utilities.locDistance(activity, elevation, ElevationMetersInput);
        Utilities.locDistance(activity, geoid, GeoidHeightMetersInput);



        meanWgs84LatSigmaOutput .
                setText(truncatePrecisionString(stdPrecision, meanCoordinate.getLatitudeStdDev()));
        meanWgs84LongSigmaOutput.
                setText(truncatePrecisionString(stdPrecision, meanCoordinate.getLongitudeStdDev()));
        meanWgs84ElevSigmaOutput.
                setText(truncatePrecisionString(stdPrecision, meanCoordinate.getElevationStdDev()));

    }
    private void updateUtmUI(CoordinateWGS84 coordinateWGS84){
        CoordinateUTM utmCoordinate = new CoordinateUTM(coordinateWGS84);
        updateUtmUI(utmCoordinate);
    }
    private void updateUtmUI(CoordinateUTM coordinateUTM){
        View v = getView();
        if (v == null  )return ;

        if (!coordinateUTM.isValidCoordinate()) return ;


        TextView utmZoneOutput           = v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        = v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = v.findViewById(R.id.utmHemisphereOutput);

        //Easting before Northing
        TextView eastingLabel         = v.findViewById(R.id.utmEasingInputLabel);
        TextView northingLabel        = v.findViewById(R.id.utmNorthingInputLabel) ;
        TextView eastingMetersOutput  = v.findViewById(R.id.utmEastingMetersOutput);
        TextView northingMetersOutput = v.findViewById(R.id.utmNorthingMetersOutput);


        if (CCSettings.isNE((MainActivity)getActivity())){

            //Northing comes before Easting

            eastingLabel         = v.findViewById(R.id.utmNorthingInputLabel);
            northingLabel        = v.findViewById(R.id.utmEasingInputLabel) ;
            eastingMetersOutput  = v.findViewById(R.id.utmNorthingMetersOutput);
            northingMetersOutput = v.findViewById(R.id.utmEastingMetersOutput);
        }

        TextView utmElevationOutput       = v.findViewById(R.id.utmElevationMetersInput) ;
        TextView utmGeoidOutput           = v.findViewById(R.id.utmGeoidHeightMetersInput) ;

        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.utmConvergenceInput);
        TextView CAdegInput  = v.findViewById(R.id.utmConvDegreesInput);
        TextView CAminInput  = v.findViewById(R.id.utmConvMinutesInput);
        TextView CAsecInput  = v.findViewById(R.id.utmConvSecondsInput);

        //scale factor
        TextView ScaleFactorInput       = v.findViewById(R.id.utmScaleFactor);


        MainActivity activity = (MainActivity)getActivity();
        boolean isDD = CCSettings.isLocDD(activity);
        int locDigOfPrecision = CCSettings.getLocPrecision(activity);
        int caDigOfPrecision  = CCSettings.getCAPrecision(activity);

        //Also output the result in separate fields
        utmZoneOutput        .setText(String.valueOf(coordinateUTM.getZone()));
        utmHemisphereOutput  .setText(String.valueOf(coordinateUTM.getHemisphere()));
        utmLatbandOutput     .setText(String.valueOf(coordinateUTM.getLatBand()));

        eastingLabel       .setText(getString(R.string.easting_label));
        northingLabel      .setText(getString(R.string.northing_label));

        Utilities.locDistance(activity, coordinateUTM.getEasting(),  eastingMetersOutput);
        Utilities.locDistance(activity, coordinateUTM.getNorthing(), northingMetersOutput);

        Utilities.locDistance(activity, coordinateUTM.getElevation(),utmElevationOutput);
        Utilities.locDistance(activity, coordinateUTM.getGeoid(),     utmGeoidOutput);

        if (isDD){
            double convAngle = coordinateUTM.getConvergenceAngle();
            Utilities.caDD(activity, convAngle, caDigOfPrecision, ConvergenceAngleInput);

        } else {

            int deg = coordinateUTM.getConvergenceAngleDegree();
            int min = coordinateUTM.getConvergenceAngleMinute();
            double sec = coordinateUTM.getConvergenceAngleSecond();
            Utilities.caDMS(activity,deg, min, sec, caDigOfPrecision,
                    CAdegInput, CAminInput, CAsecInput);

        }

        int sfPrecision  = CCSettings.getSfPrecision(activity);
        ScaleFactorInput .setText(truncatePrecisionString(sfPrecision, coordinateUTM.getScaleFactor()));
    }
    private void updateSpcsUI(CoordinateWGS84 coordinateWgs){
        View v = getView();
        if (v == null)return ;
        //need to ask for zone, then convert based on the zone
        TextView spcZoneInput = v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput  = v.findViewById(R.id.spcStateOutput);
        String zoneString = spcZoneInput.getText().toString();
        if (Utilities.isEmpty(zoneString)){
            spcStateOutput.setText(getString(R.string.spc_zone_error));
            return ;
        }
        int zone = Integer.valueOf(zoneString);

        CoordinateSPCS coordinateSPCS = new CoordinateSPCS(coordinateWgs, zone);

        if ((coordinateSPCS.getZone() == (int)Utilities.ID_DOES_NOT_EXIST) ||
            (coordinateSPCS.getZone() != zone))    {
            clearSpcUI(v);
            spcZoneInput  .setText(String.valueOf(coordinateSPCS.getZone()));
            spcStateOutput.setText(getString(R.string.spc_zone_error));
            return ;
        }

        updateSpcsUI(coordinateSPCS);
    }
    private void updateSpcsUI(CoordinateSPCS coordinateSPCS){
        View v = getView();
        if (v == null)return ;


        TextView spcZoneInput            = v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = v.findViewById(R.id.spcStateOutput);

        //Easting before Northing
        TextView eastingLabel         = v.findViewById(R.id.spcEasingInputLabel);
        TextView northingLabel        = v.findViewById(R.id.spcNorthingInputLabel);
        TextView eastingMetersOutput  = v.findViewById(R.id.spcEastingMetersOutput);
        TextView northingMetersOutput = v.findViewById(R.id.spcNorthingMetersOutput);




        if (CCSettings.isNE((MainActivity)getActivity())) {
            //Northing comes before Easting
            eastingLabel         = v.findViewById(R.id.spcNorthingInputLabel);
            northingLabel        = v.findViewById(R.id.spcEasingInputLabel);
            eastingMetersOutput  = v.findViewById(R.id.spcNorthingMetersOutput);
            northingMetersOutput = v.findViewById(R.id.spcEastingMetersOutput);

        }
            //Elevation
        TextView spcsElevationMetersInput   = v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = v.findViewById(R.id.spcsGeoidHeightMetersInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput  = v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput  = v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput  = v.findViewById(R.id.spcConvSecondsInput);

        //scale factor
        TextView ScaleFactorInput       = v.findViewById(R.id.spcScaleFactor);


        spcZoneInput  .setText(String.valueOf(coordinateSPCS.getZone()));
        spcStateOutput.setText(coordinateSPCS.getState());

        eastingLabel  .setText(getString(R.string.easting_label));
        northingLabel .setText(getString(R.string.northing_label));

        MainActivity activity = (MainActivity)getActivity();
        boolean isDD = CCSettings.isLocDD(activity);

        int caPrecision  = CCSettings.getCAPrecision(activity);
        int sfPrecision  = CCSettings.getSfPrecision(activity);

        Utilities.locDistance(activity, coordinateSPCS.getEasting(),  eastingMetersOutput);
        Utilities.locDistance(activity, coordinateSPCS.getNorthing(), northingMetersOutput);

        Utilities.locDistance(activity, coordinateSPCS.getElevation(),spcsElevationMetersInput);
        Utilities.locDistance(activity, coordinateSPCS.getGeoid(),    spcsGeoidHeightMetersInput);

        if (isDD){
            double convAngle = coordinateSPCS.getConvergenceAngle();
            Utilities.caDD(activity, convAngle, caPrecision, ConvergenceAngleInput);

        } else {

            int deg = coordinateSPCS.getConvergenceAngleDegree();
            int min = coordinateSPCS.getConvergenceAngleMinute();
            double sec = coordinateSPCS.getConvergenceAngleSecond();
            Utilities.caDMS(activity,deg, min, sec, caPrecision,
                    CAdegInput, CAminInput, CAsecInput);
        }

        ScaleFactorInput .setText(truncatePrecisionString(sfPrecision, coordinateSPCS.getScaleFactor()));
    }

    private void updateGpsUI(boolean isGpsOn) {
        View v = getView();
        if (v == null) return ;

        //Time
        TextView gpsOnOffOutput = v.findViewById(R.id.gps_on_off_output);
        int message;
        if (isGpsOn){
            message = R.string.gps_on;
        } else {
            message = R.string.gps_off;
        }
        gpsOnOffOutput.setText(getString(message));

    }

    private void initializeMeanProgressUI(View v) {

        //Time
        TextView meanOnOffOutput = v.findViewById(R.id.gpsMeanInProgressOutput);
        int message;
        if (isMeanInProgress()){
            message = R.string.mean_in_progress_string;
        } else {
            message = R.string.mean_not_in_progress_string;
        }
        meanOnOffOutput.setText(getString(message));

    }
    private void updateMeanProgressUI() {
        View v = getView();
        if (v == null) return ;

        initializeMeanProgressUI(v);
    }

    private void updateSpcsZone(View v, int zone){


        //need to ask for zone, then convert based on the zone
        TextView spcZoneInput = v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput  = v.findViewById(R.id.spcStateOutput);
        if (zone == 0){
            spcStateOutput.setText(getString(R.string.spc_zone_error));
            return ;
        }

        String zoneString = String.valueOf(zone);
        if (Utilities.isEmpty(zoneString)){
            spcStateOutput.setText(getString(R.string.spc_zone_error));
            return ;
        }



        CoordinateConstants constants = new CoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone != (int)Utilities.ID_DOES_NOT_EXIST) {
            String state = constants.getState();
            spcStateOutput.setText(state);
            spcZoneInput.setText(zoneString);
        }

    }


    //truncate digits of precision
    private String truncatePrecisionString(int digitsOfPrecision, double reading) {
       return Utilities.truncatePrecisionString(reading, digitsOfPrecision);
    }


    //******************************************************************//
    //            Convert input WGS84 fields into a CoordinateWGS84     //
    //******************************************************************//
    private int onConvert(){

        if (isMeanInProgress())return R.string.convert_error_mean;
        View v = getView();
        if (v == null)return R.string.convert_error_ui;


        CoordinateWGS84 coordinateWGS84;
        switch(mCurDataSource){

            case CCSettings.sDataSourceWGSManual:

                //Create teh coordinate from the user inputs
                coordinateWGS84 = convertWgsInputs();

                if ((coordinateWGS84 == null) || !coordinateWGS84.isValidCoordinate()){

                    return R.string.convert_error_source;
                }

                //add in any point offsets before converting
                coordinateWGS84 = addOffsetsToCoordinate(coordinateWGS84);

                //Convert the WGS84 to UTM
                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);

                //Convert to State Plane
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                break;

            case CCSettings.sDataSourceSPCSManual:
                CoordinateSPCS coordinateSPCS = convertSpcsInputs();
                if ((coordinateSPCS == null) || !coordinateSPCS.isValidCoordinate()){
                    return R.string.convert_error_source;
                }
                coordinateWGS84 = new CoordinateWGS84(coordinateSPCS);
                if ( !coordinateWGS84.isValidCoordinate()){
                    return R.string.convert_failed;
                }

                //Add in offsets
                coordinateWGS84 = addOffsetsToCoordinate(coordinateWGS84);

                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);

                //convert back to SPCS using the offset values
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);
                break;

            case CCSettings.sDataSourceUTMManual:
                CoordinateUTM coordinateUTM = convertUtmInputs();
                if ((coordinateUTM == null) || !coordinateUTM.isValidCoordinate()){
                    return R.string.convert_error_source;
                }
                coordinateWGS84 = new CoordinateWGS84(coordinateUTM);
                if ( !coordinateWGS84.isValidCoordinate()){
                    return R.string.convert_failed;
                }

                //add in offsets
                coordinateWGS84 = addOffsetsToCoordinate(coordinateWGS84);

                //display WGS Coordinate
                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);

                //Convert to UTM Coordinate and display it
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                //convert back to UTM with any offsets

                //display UTM Coordinate
                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);
                break;

            case CCSettings.sDataSourcePhoneGps:
            case CCSettings.sDataSourceExternalGps:
                stopGps();
                coordinateWGS84 = convertMeanedOrRaw();
                if ((coordinateWGS84 == null) || (!coordinateWGS84.isValidCoordinate())){
                    return R.string.convert_error_source;
                }

                coordinateWGS84 = addOffsetsToCoordinate(coordinateWGS84);

                clearWgsUI(v);
                updateWgsUI(coordinateWGS84);

                //Convert the WGS84 to UTM, and display the UTM coordinate
                clearUtmUI(v);
                updateUtmUI(coordinateWGS84);

                //Convert to State Plane and display the SPCS coordinate
                clearSpcUIxZone(v);
                updateSpcsUI(coordinateWGS84);

                break;


            case CCSettings.sDataSourceCellTowerTriangulation:
                //for now, cell tower conversion is not supported
                return R.string.cell_tower_triangu_not_available;

            default:
                return R.string.unknown_data_source;
        }
        return R.string.convert_success;
    }

    private CoordinateWGS84 addOffsetsToCoordinate(CoordinateWGS84 coordinateWGS84){
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return null;

        // TODO: 1/7/2018 This is not being calculated properly
        double offsetDistance  = CCSettings.getDistanceOffset (activity);
        double offsetHeading   = CCSettings.getHeadingOffset  (activity);
        double offsetElevation = CCSettings.getElevationOffset(activity);

        //distance must be in meters
        double distanceMeters = offsetDistance;
        int distanceUnits = CCSettings.getDistanceUnits(activity);

        if (distanceUnits == CCSettings.sFeet){
            distanceMeters = Utilities.convertFeetToMeters(offsetDistance);
        } else if (distanceUnits == CCSettings.sIntFeet){
            distanceMeters = Utilities.convertIFeetToMeters(offsetDistance);
        }

        //calculate the location using the offset

        LatLng fromLocation = new LatLng(coordinateWGS84.getLatitude(), coordinateWGS84.getLongitude());
        LatLng toLocation = SphericalUtil.computeOffset(fromLocation, distanceMeters, offsetHeading);

        double newElevation = coordinateWGS84.getElevation() + offsetElevation;

        coordinateWGS84.setLatitude(toLocation.latitude);
        coordinateWGS84.setLongitude(toLocation.longitude);
        coordinateWGS84.setElevation(newElevation);
        return coordinateWGS84;

    }

    private CoordinateWGS84 convertMeanedOrRaw(){
        CoordinateWGS84 coordinateWGS84;

        //even though the mean is not in progress, it might not have been run yet
        //or it might have been reset
        if ((mMeanToken == null) || (mMeanToken.getCoordinateSize() == 0)) {
            coordinateWGS84 = mCurrentUIWGS84;
        } else if (isMeanInProgress()){
            return null;
        } else {
            //convert the meaned coordinate
            coordinateWGS84 = new CoordinateWGS84((MainActivity)getActivity(),
                                                    mMeanToken.getMeanCoordinate(true));
        }
        return coordinateWGS84;
    }
    private CoordinateWGS84 convertWgsInputs() {
        View v = getView();
        if (v == null)return null;

        //Time
        //EditText TimeOutput      = v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView TimeStampOutput = v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText LatitudeDirInput = v.findViewById(R.id.gpsWgs84LatDirInput);
        EditText LatitudeInput   = v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText LatDegreesInput = v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText LatMinutesInput = v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText LatSecondsInput = v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText LngDirInput      = v.findViewById(R.id.gpsWgs84LngDirInput);
        EditText LongitudeInput   = v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText LongDegreesInput = v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText LongMinutesInput = v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText LongSecondsInput = v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText ElevationMetersInput   = v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText GeoidHeightMetersInput = v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.gpsWgs84ConvergenceInput);
        //TextView CAdegInput             = v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        //TextView CAminInput             = v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        //TextView CAsecInput             = v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        TextView ScaleFactorInput       = v.findViewById(R.id.gpsWgs84ScaleFactor);


        String timeStampUIString = TimeStampOutput.getText().toString();

        if (timeStampUIString.equals("")){
            Date now = new Date();
            timeStampUIString = Utilities.getDateTimeString(now.getTime());
        }
        long timeStamp = Utilities.getDateTimeFromString(getActivity(), timeStampUIString);
        CoordinateWGS84 coordinateWGS84 = new CoordinateWGS84((MainActivity)getActivity(),
                                                timeStamp,
                                                CCSettings.isDir((MainActivity)getActivity()),
                                                LatitudeDirInput.getText().toString(),
                                                LatitudeInput.getText().toString(),
                                                LatDegreesInput.getText().toString(),
                                                LatMinutesInput.getText().toString(),
                                                LatSecondsInput.getText().toString(),
                                                LngDirInput.getText().toString(),
                                                LongitudeInput.getText().toString(),
                                                LongDegreesInput.getText().toString(),
                                                LongMinutesInput.getText().toString(),
                                                LongSecondsInput.getText().toString(),
                                                ElevationMetersInput.getText().toString(),
                                                GeoidHeightMetersInput.getText().toString(),
                                                ConvergenceAngleInput.getText().toString(),
                                                ScaleFactorInput.getText().toString());
        if (!coordinateWGS84.isValidCoordinate()){

            Utilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }
        return coordinateWGS84;
    }
    private CoordinateSPCS  convertSpcsInputs() {
        View v = getView();
        if (v == null)return null;

        TextView spcZoneInput            = v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = v.findViewById(R.id.spcStateOutput);


        //Easting before Northing
        TextView eastingLabel         = v.findViewById(R.id.spcEasingInputLabel);
        TextView northingLabel        = v.findViewById(R.id.spcNorthingInputLabel);
        TextView eastingMetersOutput  = v.findViewById(R.id.spcEastingMetersOutput);
        TextView northingMetersOutput = v.findViewById(R.id.spcNorthingMetersOutput);




        if (CCSettings.isNE((MainActivity)getActivity())) {
            //Northing comes before Easting
            eastingLabel         = v.findViewById(R.id.spcNorthingInputLabel);
            northingLabel        = v.findViewById(R.id.spcEasingInputLabel);
            eastingMetersOutput  = v.findViewById(R.id.spcNorthingMetersOutput);
            northingMetersOutput = v.findViewById(R.id.spcEastingMetersOutput);

        }

        //Elevation
        TextView spcsElevationMetersInput   = v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = v.findViewById(R.id.spcsGeoidHeightMetersInput);


        TextView ConvergenceInput       = v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.spcConvSecondsInput);
        EditText ScaleFactorOutput      = v.findViewById(R.id.spcScaleFactor);

        String geoidMString           = spcsGeoidHeightMetersInput.getText().toString();
        String convergenceAngleString = ConvergenceInput.getText().toString();
        String scaleFactorString      = ScaleFactorOutput.getText().toString();




        CoordinateSPCS coordinateSPCS = new CoordinateSPCS((MainActivity)getActivity(),
                                                    spcZoneInput.getText().toString(),
                                                    spcStateOutput.getText().toString(),
                                                    eastingMetersOutput.getText().toString(),
                                                    northingMetersOutput.getText().toString(),
                                                    spcsElevationMetersInput.getText().toString(),
                                                    spcsGeoidHeightMetersInput.getText().toString(),
                                                    ConvergenceInput.getText().toString(),
                                                    ScaleFactorOutput.getText().toString());
        if (!coordinateSPCS.isValidCoordinate()){

            Utilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }
        return coordinateSPCS;
    }
    private CoordinateUTM   convertUtmInputs() {
        View v = getView();
        if (v == null)return null;


        TextView utmZoneOutput           = v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        = v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = v.findViewById(R.id.utmHemisphereOutput);

        TextView utmEastingMetersOutput  = v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = v.findViewById(R.id.utmNorthingMetersOutput);


        //Elevation
        TextView utmElevationMetersInput   = v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = v.findViewById(R.id.utmGeoidHeightMetersInput);


        TextView ConvergenceOutput      = v.findViewById(R.id.utmConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.utmConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.utmConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.utmConvSecondsInput);
        TextView ScaleFactorOutput      = v.findViewById(R.id.utmScaleFactor);


        CoordinateUTM coordinateUTM = new CoordinateUTM((MainActivity)getActivity(),
                                                    utmZoneOutput.getText().toString(),
                                                    utmLatbandOutput.getText().toString(),
                                                    utmHemisphereOutput.getText().toString(),
                                                    utmEastingMetersOutput.getText().toString(),
                                                    utmNorthingMetersOutput.getText().toString(),
                                                    utmElevationMetersInput.getText().toString(),
                                                    utmGeoidHeightMetersInput.getText().toString(),
                                                    ConvergenceOutput.getText().toString(),
                                                    ScaleFactorOutput.getText().toString());
        if (!coordinateUTM.isValidCoordinate()){

            Utilities.getInstance().showStatus(getActivity(), R.string.coordinate_try_again);
            return null;
        }

        return coordinateUTM;
    }

    //******************************************************************//
    //            Screen UI stuff                                       //
    //******************************************************************//

    private void enableManualWgsInput(boolean enableFlag){
        View v = getView();
        if (v == null)return;

        //Time
        // EditText TimeOutput   = v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView TimeStampOutput = v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText LatitudeInput   = v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText LatDegreesInput = v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText LatMinutesInput = v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText LatSecondsInput = v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText LongitudeInput   = v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText LongDegreesInput = v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText LongMinutesInput = v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText LongSecondsInput = v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText ElevationMetersInput   = v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText GeoidHeightMetersInput = v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);



        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.gpsWgs84ConvSecondsInput);
        TextView ScaleFactorOutput      = v.findViewById(R.id.gpsWgs84ScaleFactor);

        //TimeOutput     .setEnabled(enableFlag);
        TimeStampOutput.setEnabled(enableFlag);

        LatitudeInput  .setEnabled(enableFlag);
        LatDegreesInput.setEnabled(enableFlag);
        LatMinutesInput.setEnabled(enableFlag);
        LatSecondsInput.setEnabled(enableFlag);

        LongitudeInput  .setEnabled(enableFlag);
        LongDegreesInput.setEnabled(enableFlag);
        LongMinutesInput.setEnabled(enableFlag);
        LongSecondsInput.setEnabled(enableFlag);

        ElevationMetersInput  .setEnabled(enableFlag);
        GeoidHeightMetersInput.setEnabled(enableFlag);

        ConvergenceAngleInput.setEnabled(enableFlag);
        CAdegInput           .setEnabled(enableFlag);
        CAminInput           .setEnabled(enableFlag);
        CAsecInput           .setEnabled(enableFlag);
        ScaleFactorOutput    .setEnabled(enableFlag);

    }

    private void enableManualSpcsInput(boolean enableFlag){
        View v = getView();
        if (v == null)return;

        TextView spcZoneInput            = v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = v.findViewById(R.id.spcStateOutput);

        //SPC
        TextView spcEastingMetersOutput  = v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = v.findViewById(R.id.spcNorthingMetersOutput);


        //Elevation
        TextView spcsElevationMetersInput   = v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = v.findViewById(R.id.spcsGeoidHeightMetersInput);



        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.spcConvSecondsInput);

        TextView ScaleFactorOutput      = v.findViewById(R.id.spcScaleFactor);

        //can only change the zone in the settings
        //spcZoneInput           .setEnabled(true);//always enabled for input, regardless

        spcStateOutput         .setEnabled(enableFlag);
        spcEastingMetersOutput .setEnabled(enableFlag);
        spcNorthingMetersOutput.setEnabled(enableFlag);

        spcsElevationMetersInput  .setEnabled(enableFlag);
        spcsGeoidHeightMetersInput.setEnabled(enableFlag);

        ConvergenceAngleInput.setEnabled(enableFlag);
        CAdegInput.setEnabled(enableFlag);
        CAminInput.setEnabled(enableFlag);
        CAsecInput.setEnabled(enableFlag);
        ScaleFactorOutput.setEnabled(enableFlag);

    }

    private void enableManualUtmInput(boolean enableFlag){
        View v = getView();
        if (v == null)return;

        TextView utmZoneOutput           = v.findViewById(R.id.utmZoneOutput);
        TextView utmLatbandOutput        = v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = v.findViewById(R.id.utmHemisphereOutput);
        TextView utmEastingMetersOutput  = v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = v.findViewById(R.id.utmNorthingMetersOutput);

        //Elevation
        TextView utmElevationMetersInput   = v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = v.findViewById(R.id.utmGeoidHeightMetersInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.spcConvSecondsInput);

        TextView ScaleFactorOutput      = v.findViewById(R.id.spcScaleFactor);

        utmZoneOutput          .setEnabled(enableFlag);

        utmLatbandOutput       .setEnabled(enableFlag);
        utmHemisphereOutput    .setEnabled(enableFlag);
        utmEastingMetersOutput .setEnabled(enableFlag);
        utmNorthingMetersOutput.setEnabled(enableFlag);

        utmElevationMetersInput. setEnabled(enableFlag);
        utmGeoidHeightMetersInput.setEnabled(enableFlag);

        ConvergenceAngleInput.setEnabled(enableFlag);
        CAdegInput.setEnabled(enableFlag);
        CAminInput.setEnabled(enableFlag);
        CAsecInput.setEnabled(enableFlag);
        ScaleFactorOutput   .setEnabled(enableFlag);

    }


    private void clearForm() {
        View v = getView();
        if (v == null)return;

        clearWgsUI(v);

        clearMeanUI(v);

        clearSpcUIxZone(v);

        clearUtmUI(v);

        //clearNadUI(v);
    }

    private void clearWgsUI(View v){

        //Timestamp
        TextView TimestampOutput = v.findViewById(R.id.gpsWgs84TimestampOutput);

        //GPS Latitude
        TextView LatitudeInput   = v.findViewById(R.id.gpsWgs84LatitudeInput);
        TextView LatDegreesInput = v.findViewById(R.id.gpsWgs84LatDegreesInput);
        TextView LatMinutesInput = v.findViewById(R.id.gpsWgs84LatMinutesInput);
        TextView LatSecondsInput = v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        TextView LongitudeInput   = v.findViewById(R.id.gpsWgs84LongitudeInput);
        TextView LongDegreesInput = v.findViewById(R.id.gpsWgs84LongDegreesInput);
        TextView LongMinutesInput = v.findViewById(R.id.gpsWgs84LongMinutesInput);
        TextView LongSecondsInput = v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        TextView ElevationMetersInput   = v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        TextView GeoidHeightMetersInput = v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.gpsWgs84ConvSecondsInput);
        TextView ScaleFactorOutput      = v.findViewById(R.id.gpsWgs84ScaleFactor);


        TimestampOutput   .setText("");

        LatitudeInput     .setText("");
        LatDegreesInput   .setText("");
        LatMinutesInput   .setText("");
        LatSecondsInput   .setText("");

        LongitudeInput     .setText("");
        LongDegreesInput   .setText("");
        LongMinutesInput   .setText("");
        LongSecondsInput   .setText("");

        ElevationMetersInput  .setText("");
        GeoidHeightMetersInput.setText("");

        ConvergenceAngleInput.setText("");
        CAdegInput.setText("");
        CAminInput.setText("");
        CAsecInput.setText("");
        ScaleFactorOutput.setText("");

    }
    private void clearMeanUI(View v){

        mMeanToken = null;

        //Mean Parameters
        //TextView meanWgs84StartTimeOutput    = v.findViewById(meanWgs84StartTimeOutput);
        //TextView meanWgs84EndTimeOutput      = v.findViewById(meanWgs84EndTimeOutput);
        TextView meanWgs84PointsInMeanOutput = v.findViewById(R.id.meanWgs84PointsInMeanOutput);
        TextView meanWgs84StartTimestampOutput = v.findViewById(R.id.meanWgs84StartTimestampOutput);
        TextView meanWgs84EndTimestampOutput   = v.findViewById(R.id.meanWgs84EndTimestampOutput);


        //Mean Standard Deviations
        TextView meanWgs84LatSigmaOutput = v.findViewById(R.id.meanWgs84LatSigmaOutput);
        TextView meanWgs84LongSigmaOutput= v.findViewById(R.id.meanWgs84LngSigmaOutput);
        TextView meanWgs84ElevSigmaOutput= v.findViewById(R.id.meanWgs84ElevSigmaOutput);

        //Mean Latitude
        TextView meanWgs84LatitudeInput   = v.findViewById(R.id.meanWgs84LatitudeInput);
        TextView meanWgs84LatDegreesInput = v.findViewById(R.id.meanWgs84LatDegreesInput);
        TextView meanWgs84LatMinutesInput = v.findViewById(R.id.meanWgs84LatMinutesInput);
        TextView meanWgs84LatSecondsInput = v.findViewById(R.id.meanWgs84LatSecondsInput);

        TextView meanWgs84LongitudeInput   = v.findViewById(R.id.meanWgs84LongitudeInput);
        TextView meanWgs84LongDegreesInput = v.findViewById(R.id.meanWgs84LongDegreesInput);
        TextView meanWgs84LongMinutesInput = v.findViewById(R.id.meanWgs84LongMinutesInput);
        TextView meanWgs84LongSecondsInput = v.findViewById(R.id.meanWgs84LongSecondsInput);

        //Elevation
        TextView meanWgs84ElevationMetersInput   = v.findViewById(
                                                                R.id.meanWgs84ElevationMetersInput);
        TextView meanWgs84GeoidHeightMetersInput = v.findViewById(
                                                                R.id.meanWgs84GeoidHeightMetersInput);


        //meanWgs84StartTimeOutput   .setText("");
        //meanWgs84EndTimeOutput     .setText("");
        meanWgs84StartTimestampOutput.setText("");
        meanWgs84EndTimestampOutput.setText("");
        meanWgs84PointsInMeanOutput.setText("");

        meanWgs84LatSigmaOutput     .setText("");
        meanWgs84LongSigmaOutput    .setText("");
        meanWgs84ElevSigmaOutput    .setText("");


        meanWgs84LatitudeInput     .setText("");
        meanWgs84LatDegreesInput   .setText("");
        meanWgs84LatMinutesInput   .setText("");
        meanWgs84LatSecondsInput   .setText("");

        meanWgs84LongitudeInput     .setText("");
        meanWgs84LongDegreesInput   .setText("");
        meanWgs84LongMinutesInput   .setText("");
        meanWgs84LongSecondsInput   .setText("");

        meanWgs84ElevationMetersInput  .setText("");
        meanWgs84GeoidHeightMetersInput.setText("");


    }
    private void clearUtmUI(View v){
        TextView utmZoneOutput           = v.findViewById(R.id.utmZoneOutput);

        TextView utmLatbandOutput        = v.findViewById(R.id.utmLatbandOutput);
        TextView utmHemisphereOutput     = v.findViewById(R.id.utmHemisphereOutput);

        TextView utmEastingMetersOutput  = v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = v.findViewById(R.id.utmNorthingMetersOutput);

        //Elevation
        TextView utmElevationMetersInput   = v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = v.findViewById(R.id.utmGeoidHeightMetersInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.utmConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.utmConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.utmConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.utmConvSecondsInput);
        TextView ScaleFactorOutput      = v.findViewById(R.id.utmScaleFactor);


        utmZoneOutput.          setText("");
        utmLatbandOutput.       setText("");
        utmHemisphereOutput.    setText("");

        utmEastingMetersOutput. setText("");
        utmNorthingMetersOutput.setText("");


        utmElevationMetersInput. setText("");
        utmGeoidHeightMetersInput.setText("");


        ConvergenceAngleInput.   setText("");
        CAdegInput.setText("");
        CAminInput.setText("");
        CAsecInput.setText("");
        ScaleFactorOutput.   setText("");

    }
    private void clearSpcUIxZone(View v) {
        TextView spcZoneOutput = v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput = v.findViewById(R.id.spcStateOutput);
        String zone = spcZoneOutput.getText().toString().trim();
        String state = spcStateOutput.getText().toString().trim();
        clearSpcUI(v);
        spcZoneOutput.setText(zone);
        spcStateOutput.setText(state);
    }
    private void clearSpcUI(View v){

        //SPC
        TextView spcZoneOutput           = v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = v.findViewById(R.id.spcStateOutput);

        TextView spcEastingMetersOutput  = v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = v.findViewById(R.id.spcNorthingMetersOutput);


        //Elevation
        TextView spcsElevationMetersInput   = v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = v.findViewById(R.id.spcsGeoidHeightMetersInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.spcConvSecondsInput);
        TextView ScaleFactorOutput      = v.findViewById(R.id.spcScaleFactor);


        spcZoneOutput          .setText("");
        spcStateOutput         .setText("");

        spcEastingMetersOutput .setText("");
        spcNorthingMetersOutput.setText("");


        spcsElevationMetersInput. setText("");
        spcsGeoidHeightMetersInput.setText("");


        ConvergenceAngleInput.   setText("");
        CAdegInput.setText("");
        CAminInput.setText("");
        CAsecInput.setText("");
        ScaleFactorOutput.   setText("");


    }


    private void setColor(CoordinateWGS84 coordinateWGS84){
        View v = getView();
        if (v == null)return;

        //Time
        //EditText TimeOutput      = v.findViewById(R.id.gpsWgs84TimeOutput);
        TextView TimeStampOutput = v.findViewById(R.id.gpsWgs84TimestampOutput);
        //GPS Latitude
        EditText LatitudeInput   = v.findViewById(R.id.gpsWgs84LatitudeInput);
        EditText LatDegreesInput = v.findViewById(R.id.gpsWgs84LatDegreesInput);
        EditText LatMinutesInput = v.findViewById(R.id.gpsWgs84LatMinutesInput);
        EditText LatSecondsInput = v.findViewById(R.id.gpsWgs84LatSecondsInput);

        //GPS Longitude
        EditText LongitudeInput   = v.findViewById(R.id.gpsWgs84LongitudeInput);
        EditText LongDegreesInput = v.findViewById(R.id.gpsWgs84LongDegreesInput);
        EditText LongMinutesInput = v.findViewById(R.id.gpsWgs84LongMinutesInput);
        EditText LongSecondsInput = v.findViewById(R.id.gpsWgs84LongSecondsInput);

        //Elevation
        EditText ElevationMetersInput   = v.findViewById(R.id.gpsWgs84ElevationMetersInput);
        EditText GeoidHeightMetersInput = v.findViewById(R.id.gpsWgs84GeoidHeightMetersInput);


        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.gpsWgs84ConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.gpsWgs84ConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.gpsWgs84ConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.gpsWgs84ConvSecondsInput);

        TextView ScaleFactorInput       = v.findViewById(R.id.gpsWgs84ScaleFactor);

        int uiColor;
        if (coordinateWGS84.getLatitude() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }


        LatitudeInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LatDegreesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LatMinutesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LatSecondsInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getLongitude() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        LongitudeInput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LongDegreesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LongMinutesInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        LongSecondsInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getElevation() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        ElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));



        if (coordinateWGS84.getGeoid() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        GeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateWGS84.getConvergenceAngle() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        ConvergenceAngleInput.setTextColor(ContextCompat.getColor(getActivity(),uiColor));
        CAdegInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAminInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAsecInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateWGS84.getScaleFactor() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        ScaleFactorInput     .setTextColor(ContextCompat.getColor(getActivity(),uiColor));

    }
    private void setColor(CoordinateSPCS coordinateSPCS){
        View v = getView();
        if (v == null)return;

        //SPC
        TextView spcZoneOutput           = v.findViewById(R.id.spcZoneOutput);
        TextView spcStateOutput          = v.findViewById(R.id.spcStateOutput);

        TextView spcEastingMetersOutput  = v.findViewById(R.id.spcEastingMetersOutput);
        TextView spcNorthingMetersOutput = v.findViewById(R.id.spcNorthingMetersOutput);

        //Elevation
        TextView spcsElevationMetersInput   = v.findViewById(R.id.spcsElevationMetersInput);
        TextView spcsGeoidHeightMetersInput = v.findViewById(R.id.spcsGeoidHeightMetersInput);

        //convergence & scale factor
        TextView ConvergenceAngleInput  = v.findViewById(R.id.spcConvergenceInput);
        TextView CAdegInput             = v.findViewById(R.id.spcConvDegreesInput);
        TextView CAminInput             = v.findViewById(R.id.spcConvMinutesInput);
        TextView CAsecInput             = v.findViewById(R.id.spcConvSecondsInput);

        TextView ScaleFactorInput       = v.findViewById(R.id.spcScaleFactor);

        int uiColor;
        if (coordinateSPCS.getEasting() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }


        spcEastingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateSPCS.getNorthing() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        spcNorthingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateSPCS.getElevation() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        spcsElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateSPCS.getGeoid() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        spcsGeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateSPCS.getConvergenceAngle() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        ConvergenceAngleInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAdegInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAminInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));
        CAsecInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateSPCS.getScaleFactor() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        ScaleFactorInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

    }
    private void setColor(CoordinateUTM coordinateUTM){
        View v = getView();
        if (v == null)return;


        TextView utmEastingMetersOutput  = v.findViewById(R.id.utmEastingMetersOutput);
        TextView utmNorthingMetersOutput = v.findViewById(R.id.utmNorthingMetersOutput);

        //Elevation
        TextView utmElevationMetersInput   = v.findViewById(R.id.utmElevationMetersInput);
        TextView utmGeoidHeightMetersInput = v.findViewById(R.id.utmGeoidHeightMetersInput);


        TextView utmConvergenceOutput    = v.findViewById(R.id.utmConvergenceInput);
        TextView utmScaleFactorOutput    = v.findViewById(R.id.utmScaleFactor);

        int uiColor;

        if (coordinateUTM.getEasting() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }


        utmEastingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateUTM.getNorthing() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        utmNorthingMetersOutput  .setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateUTM.getElevation() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        utmElevationMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));


        if (coordinateUTM.getGeoid() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        utmGeoidHeightMetersInput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateUTM.getConvergenceAngle() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        utmConvergenceOutput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

        if (coordinateUTM.getScaleFactor() >= 0.0) {
            uiColor = R.color.colorPosNumber;

        } else {
            uiColor = R.color.colorNegNumber;
        }

        utmScaleFactorOutput.setTextColor(ContextCompat.getColor(getActivity(), uiColor));

    }

    private int getSpcZone(){
        View v = getView();
        if (v == null)return 0;
        return getSpcZone(v);
    }
    private int getSpcZone(View v) {
        TextView spcZoneOutput = v.findViewById(R.id.spcZoneOutput);
        String zoneString = spcZoneOutput.getText().toString().trim();
        return Integer.valueOf(zoneString);
    }


    //+**************  GPS Stuff  ****************************
    private void initializeGPS(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){return;}

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

    }

    private boolean isGPSEnabled() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return false;

        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return ((lm != null) && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)));

    }
    private void    startGps(){

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){return;}


        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        //ask the Location Manager to start sending us updates
        locationManager.requestLocationUpdates("gps", 0, 0.0f, this);
        //locationManager.addGpsStatusListener(this);
        locationManager.addNmeaListener(this);

        isGpsOn = true;
        updateGpsUI(isGpsOn);
    }
    private void    stopGps() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){return;}

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationManager.removeUpdates(this);
        //locationManager.removeGpsStatusListener(this);
        locationManager.removeNmeaListener(this);

        isGpsOn = false;
        updateGpsUI(isGpsOn);
    }

    //+*******************************************

    //+**********************************************

    //******************************************************************//
    //             GPS Listener Callbacks                               //
    //            Called by the OS to handle GPS events                 //
    //******************************************************************//

    //GpsStatus.Listener Callback


    //OS calls this callback when
    // a change has been detected in GPS satellite status
    //Called to report changes in the GPS status.

    // The parameter event type is one of:

    // o GPS_EVENT_STARTED
    // o GPS_EVENT_STOPPED
    // o GPS_EVENT_FIRST_FIX
    // o GPS_EVENT_SATELLITE_STATUS

    //When this method is called,
    // the client should call getGpsStatus(GpsStatus)
    // to get additional status information.
    @Override
    public void onGpsStatusChanged(int eventType) {
        //setGpsStatus();
    }



    //******************************************************************//
    //             Location Listener Callbacks                          //
    //            Called by the OS to handle GPS events                 //
    //******************************************************************//

    // called when the GPS provider is turned off
    // (i.e. user turning off the GPS on the phone)
    // If requestLocationUpdates is called on an already disabled provider,
    // this method is called immediately.
    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            //setGpsStatus();
        }
    }

    // called when the GPS provider is turned on
    // (i.e. user turning on the GPS on the phone)
    @Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            //setGpsStatus();
        }
    }

    //Called when the provider status changes.
    // This method is called when
    // o a provider is unable to fetch a location or
    // o if the provider has recently become available
    //    after a period of unavailability.
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (LocationManager.GPS_PROVIDER.equals(provider)){
            //setGpsStatus();


        }

    }


    // called when the listener is notified with a location update from the GPS
    @Override
    public void onLocationChanged(Location loc) {
        mCurLocation = new Location(loc); // copy location
    }



    //******************************************************************//
    //             NMEA Listener Callbacks                              //
    //            Called by the OS to handle GPS events                 //
    //******************************************************************//
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        handleNmeaReceived(timestamp, nmea);
    }

}


