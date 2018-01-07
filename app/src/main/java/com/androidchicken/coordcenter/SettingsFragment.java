package com.androidchicken.coordcenter;

import android.content.Context;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The UI Settings Fragment
 * These settings deal with how data is shown to the User
 *
 * Created by Elisabeth Huhn on 7/29/17 for GeoBot.
 * Cloned on 1/3/2018 For CoordCenter
 */
public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener{

    //private static final String TAG = "GENERAL_SETTINGS_FRAGMENT";

    //**********************************************************************/
    //*********   Member Variables  ****************************************/
    //**********************************************************************/

    //Constructor
    public SettingsFragment() {
        //for now, we don't need to initialize anything when the fragment
        //  is first created with this constructor
    }


    //**********************************************************/
    //****     Lifecycle Methods                         *******/
    //**********************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate( R.layout.fragment_settings, container,  false);

        wireWidgets(v);
        wireSpinners(v);

        initializeUI(v);

        MainActivity activity = (MainActivity)getActivity();
        if (activity != null) {
            //get rid of the soft keyboard if it is visible

            EditText aboutWho = v.findViewById(R.id.settingsSpcZoneInput);
            Utilities.getInstance().showSoftKeyboard(activity, aboutWho);
            //Utilities.getInstance().hideSoftKeyboard(activity);
            //Utilities.getInstance().hideKeyboard(activity);
        }

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        //set the title bar subtitle
        setSubtitle();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            enableSaveButton();
        } else {
            enableSaveButton();
        }
    }
    //**********************************************************/
    //****     Initialize                                *******/
    //**********************************************************/
    private void wireWidgets(View v){
        final MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

        Button saveButton = v.findViewById(R.id.settingSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveZone();
            }
        });

        wireSwitches(v);
        wirePrecisions(v);
        wireHeightMean(v);
        wireZone(v);
        wireOffsets(v);
    }
    private void wireSwitches(View v){
        final MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;


        final SwitchCompat rmsVstddevSwitch     = v.findViewById(R.id.switchRmsVStdDev);
        final SwitchCompat latlngVlnglatSwitch  = v.findViewById(R.id.switchLatLng);
        final SwitchCompat neVenSwitch          = v.findViewById(R.id.switchNeEn);
        final SwitchCompat locDDvDMSSwitch      = v.findViewById(R.id.switchLocDDvDMS);
        final SwitchCompat caDDvDMSSwitch       = v.findViewById(R.id.switchCADDvDMS);
        final SwitchCompat hemiIndicatorSwitch  = v.findViewById(R.id.switchHemiDirVpm);



        rmsVstddevSwitch    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (rmsVstddevSwitch.isChecked()){
                    CCSettings.setRms(activity);
                } else {
                    CCSettings.setStdDev(activity);
                }
            }
        });
        latlngVlnglatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (latlngVlnglatSwitch.isChecked()){
                    CCSettings.setLatLng(activity);
                } else {
                    CCSettings.setLngLat(activity);
                }
            }
        });
        neVenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (neVenSwitch.isChecked()){
                    CCSettings.setNE(activity);
                } else {
                    CCSettings.setEN(activity);
                }
            }
        });
        locDDvDMSSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (locDDvDMSSwitch.isChecked()){
                    CCSettings.setLocDD(activity);
                } else {
                    CCSettings.setLocDMS(activity);
                }

            }
        });

        caDDvDMSSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (caDDvDMSSwitch.isChecked()){
                    CCSettings.setCADD(activity);
                } else {
                    CCSettings.setCADMS(activity);
                }
            }
        });
        hemiIndicatorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (hemiIndicatorSwitch.isChecked()){
                    CCSettings.setDir(activity);
                } else {
                    CCSettings.setPM(activity);
                }
            }
        });

    }
    private void wirePrecisions(View v){
        final MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

        TextWatcher precisionTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters

                if (!savePrecisions()) enableSaveButton();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };

        final EditText locPrecisionView = v.findViewById(R.id.locPrecisionInput);
        final EditText stdPrecisionView = v.findViewById(R.id.stdDevPrecisionInput);
        final EditText sfPrecisionView  = v.findViewById(R.id.sfPrecisionInput);
        final EditText caPrecisionView  = v.findViewById(R.id.caPrecisionInput);

        locPrecisionView.addTextChangedListener(precisionTextWatcher);
        stdPrecisionView.addTextChangedListener(precisionTextWatcher);
        sfPrecisionView .addTextChangedListener(precisionTextWatcher);
        caPrecisionView .addTextChangedListener(precisionTextWatcher);



        locPrecisionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    enableSaveButton();
                } else {
                    String precisionString = locPrecisionView.getText().toString().trim();
                    if (!Utilities.isEmpty(precisionString)) {
                        CCSettings.setLocPrecision(activity, Integer.valueOf(precisionString));
                    } else {
                        CCSettings.setLocPrecision(activity, CCSettings.sLocPrcDefault);
                    }
                    disableSaveButton();
                }
            }
        });

        stdPrecisionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    enableSaveButton();
                } else {
                    String precisionString = stdPrecisionView.getText().toString().trim();
                    if (!Utilities.isEmpty(precisionString)) {
                        CCSettings.setStdDevPrecision(activity, Integer.valueOf(precisionString));
                    } else {
                        CCSettings.setStdDevPrecision(activity, CCSettings.sStdPrcDefault);
                    }
                    disableSaveButton();
                }
            }
        });


        sfPrecisionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    enableSaveButton();
                } else {
                    String precisionString = sfPrecisionView.getText().toString().trim();
                    if (!Utilities.isEmpty(precisionString)) {
                        CCSettings.setSfPrecision(activity, Integer.valueOf(precisionString));
                    } else {
                        CCSettings.setSfPrecision(activity, CCSettings.sSfPrcDefault);
                    }
                    disableSaveButton();

                }
            }
        });
        caPrecisionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    enableSaveButton();
                } else {
                    String precisionString = caPrecisionView.getText().toString().trim();
                    if (!Utilities.isEmpty(precisionString)) {
                        CCSettings.setCAPrecision(activity, Integer.valueOf(precisionString));
                    } else {
                        CCSettings.setCAPrecision(activity, CCSettings.sCAPrcDefault);
                    }
                    disableSaveButton();

                }
            }
        });


        locPrecisionView.setEnabled(true);
        stdPrecisionView.setEnabled(true);
        sfPrecisionView.setEnabled(true);
        caPrecisionView.setEnabled(true);

    }
    private void wireOffsets(View v){
        final MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

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
                if (!saveOffsets()) enableSaveButton();
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
                    CCSettings.setHeadingOffset(activity, CCSettings.sHeadingOffsetDefault);
                } else {
                    //  do a check on value
                    double heading = Double.valueOf(headingString.toString().trim());
                    if ((heading >= 0) && (heading <= 360)) {
                        if (!saveOffsets()) enableSaveButton();
                    } else {
                        Utilities.getInstance().showStatus(activity, R.string.heading_invalid);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable heading) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };

        final EditText distanceOffsetView   = v.findViewById(R.id.offsetDistanceInput);
        final EditText headingOffsetView    = v.findViewById(R.id.offsetHeadingInput);
        final EditText elevationOffsetView  = v.findViewById(R.id.offsetElevationInput);


        distanceOffsetView .addTextChangedListener(offsetTextWatcher);
        headingOffsetView  .addTextChangedListener(headingOffsetTextWatcher);
        elevationOffsetView.addTextChangedListener(offsetTextWatcher);




        distanceOffsetView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    saveOffsets();
                }
            }
        });


        headingOffsetView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    saveOffsets();
                }
            }
        });


        elevationOffsetView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    saveOffsets();
                }
            }
        });



        distanceOffsetView.setEnabled(true);
        headingOffsetView.setEnabled(true);
        elevationOffsetView.setEnabled(true);
    }
    private void wireHeightMean(View v){
        final MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;


        TextWatcher heightMeanTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters

                if (!saveHeightMean()) enableSaveButton();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };


        final EditText heightView  = v.findViewById(R.id.settingsHeightOutput);
        final EditText maxMeanView = v.findViewById(R.id.settingsNumMeanOutput);

        heightView.addTextChangedListener(heightMeanTextWatcher);
        maxMeanView.addTextChangedListener(heightMeanTextWatcher);

        heightView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    //focus has shifted to height
                    enableSaveButton();
                } else {
                    //focus has shifted away from height
                    String heightString = heightView.getText().toString().trim();
                    if (!Utilities.isEmpty(heightString)) {
                        CCSettings.setHeight(activity, Double.valueOf(heightString));
                    } else {
                        CCSettings.setHeight(activity, CCSettings.sHeightDefault);
                    }
                    disableSaveButton();
                }
            }
        });


        maxMeanView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    //focus has shifted to maxMean
                    enableSaveButton();
                } else {
                    //focus has shifted away from maxMean
                    String maxMeanString = maxMeanView.getText().toString().trim();
                    if (!Utilities.isEmpty(maxMeanString)){
                        CCSettings.setNumMean(activity, Integer.valueOf(maxMeanString));
                    } else {
                        CCSettings.setNumMean(activity, CCSettings.sNumMeanDefault);
                    }
                    disableSaveButton();
                }
            }
        });
    }
    private void wireZone(View v){
        final MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;


        final EditText zoneView    = v.findViewById(R.id.settingsSpcZoneInput);
        final View fragmentView = v;
        zoneView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    //focus has shifted away from zone

                    String zoneString = zoneView.getText().toString().trim();
                    int zone = 0;  //if the string is invalid, use an invalid zone
                    if (!Utilities.isEmpty(zoneString)) {
                        zone = Integer.valueOf(zoneString);
                    }
                    updateStateUI(activity, fragmentView, zone);
                    disableSaveButton();
                }
            }
        });
        TextWatcher zoneTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters
                if (!updateZoneState(charSequence)) enableSaveButton();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };

        zoneView.addTextChangedListener(zoneTextWatcher);
    }
    private void wireSpinners(View v){

        //Create the array of spinner choices from the Types of Coordinates defined
        //The order of these is used when an item is selected, so if you change these,
        // change the item selected listener as well
        String [] distanceUnits = new String[]{ CCSettings.sMetersString,
                                                CCSettings.sFeetString,
                                                CCSettings.sIntFeetString};

        String [] dataSourceTypes = new String[]{   getString(R.string.select_data_source),
                                                    getString(R.string.manual_wgs_data_source),
                                                    getString(R.string.manual_spcs_data_source),
                                                    getString(R.string.manual_utm_data_source),
                                                    getString(R.string.phone_gps),
                                                    getString(R.string.external_gps),
                                                    getString(R.string.cell_tower_triangulation)};


        //Then initialize the spinner itself

        Spinner distUnitsSpinner           = v.findViewById(R.id.distance_units_spinner);
        Spinner dataSourceSpinner          = v.findViewById(R.id.data_source_spinner);

        // Create the ArrayAdapters using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> duAdapter = new ArrayAdapter<>(getActivity(),
                                                            android.R.layout.simple_spinner_item,
                                                            distanceUnits);


        ArrayAdapter<String> dsAdapter = new ArrayAdapter<>(getActivity(),
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
                MainActivity activity = (MainActivity)getActivity();
                if (activity != null) {
                    //Based on the order presented to the user,
                    // which should be in the same order as the constants: Settings.sMeter, etc

                    CCSettings.setDistUnits(activity, position);
                    disableSaveButton();
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

                        if (isGPSEnabled()) {
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

                String selectMsg = getString(R.string.selected_data_source, getString(msg) );

                Utilities.getInstance().showStatus(getActivity(), selectMsg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //for now, do nothing
            }
        });

    }

    private boolean isGPSEnabled() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return false;

        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return ((lm != null) && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)));

    }

    /* a Better more complete check
    public boolean isLocationServicesAvailable() {
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return false;

        int locationMode = 0;
        String locationProviders;
        boolean isAvailable = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(activity.getContentResolver(),
                                                      Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                return false;
            }

            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(activity.getContentResolver(),
                                                        Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }

        boolean coarsePermissionCheck = (
                ContextCompat.checkSelfPermission(activity,
                  Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean finePermissionCheck = (
                ContextCompat.checkSelfPermission(activity,
                  Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        return isAvailable && (coarsePermissionCheck || finePermissionCheck);
    }
*/

    private void initializeUI(View v) {

        initializeSwitches(v);
        initializeSpinners(v);
        initializeZone(v);
        initializeHeightMean(v);
        initializePrecision(v);
        initializeOffsets(v);

        disableSaveButton();
    }
    private void initializeSwitches(View v) {
        MainActivity activity = (MainActivity)getActivity();

        //
        //Switches
        //
        SwitchCompat rmsVstddevSwitch     = v.findViewById(R.id.switchRmsVStdDev);
        SwitchCompat latlngVlnglatSwitch  = v.findViewById(R.id.switchLatLng);
        SwitchCompat neVenSwitch          = v.findViewById(R.id.switchNeEn);
        SwitchCompat locDDvDMSSwitch      = v.findViewById(R.id.switchLocDDvDMS);
        SwitchCompat caDDvDMSSwitch       = v.findViewById(R.id.switchCADDvDMS);
        SwitchCompat hemiIndicatorSwitch  = v.findViewById(R.id.switchHemiDirVpm);


        rmsVstddevSwitch    .setChecked(CCSettings.isRms(activity));
        latlngVlnglatSwitch .setChecked(CCSettings.isLatLng(activity));
        neVenSwitch         .setChecked(CCSettings.isNE(activity));
        locDDvDMSSwitch     .setChecked(CCSettings.isLocDD(activity));
        caDDvDMSSwitch      .setChecked(CCSettings.isCADD(activity));
        hemiIndicatorSwitch .setChecked(CCSettings.isDir(activity));

    }
    private void initializePrecision(View v) {
        MainActivity activity = (MainActivity)getActivity();


        //
        // Precision Inputs
        //
        EditText locPrecisionView = v.findViewById(R.id.locPrecisionInput);
        EditText stdPrecisionView = v.findViewById(R.id.stdDevPrecisionInput);
        EditText sfPrecisionView  = v.findViewById(R.id.sfPrecisionInput);
        EditText caPrecisionView  = v.findViewById(R.id.caPrecisionInput);

        int precision = CCSettings.getLocPrecision(activity);
        String precisionString = String.valueOf(precision);
        locPrecisionView.setText(precisionString);

        precision = CCSettings.getStdDevPrecision(activity);
        precisionString = String.valueOf(precision);
        stdPrecisionView.setText(precisionString);

        precision = CCSettings.getSfPrecision(activity);
        precisionString = String.valueOf(precision);
        sfPrecisionView.setText(precisionString);

        precision = CCSettings.getCAPrecision(activity);
        precisionString = String.valueOf(precision);
        caPrecisionView.setText(precisionString);

    }
    private void initializeOffsets(View v) {
        MainActivity activity = (MainActivity)getActivity();


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
    private void initializeZone(View v) {
        MainActivity activity = (MainActivity)getActivity();

        //
        // Zone, Height, # of Meaning samples
        //
        EditText zoneView    = v.findViewById(R.id.settingsSpcZoneInput);

        int zone = CCSettings.getZone(activity);
        zoneView.setText(String.valueOf(zone));

        updateStateUI(activity, v, zone);

    }
    private void initializeHeightMean(View v) {
        MainActivity activity = (MainActivity)getActivity();

        //
        // Height, # of Meaning samples
        //
        EditText heightView  = v.findViewById(R.id.settingsHeightOutput);
        EditText maxMeanView = v.findViewById(R.id.settingsNumMeanOutput);

        double height = CCSettings.getHeight(activity);
        heightView.setText(String.valueOf(height));

        int maxMean = CCSettings.getNumMean(activity);
        maxMeanView.setText(String.valueOf(maxMean));

    }
    private void initializeSpinners(View v) {
        MainActivity activity = (MainActivity)getActivity();

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

    void    updateStateUI(MainActivity activity, View v, int zone){
        TextView stateView   = v.findViewById(R.id.settingsSpcStateOutput);

        CoordinateConstants constants = new CoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone == (int)Utilities.ID_DOES_NOT_EXIST) {
            stateView.setText(getString(R.string.spc_zone_error));
        } else {
            stateView.setText(constants.getState());
            CCSettings.setZone(activity, zone);
        }
    }

    boolean updateZoneState(CharSequence zoneString){
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return false;

        View v = getView();
        if (v == null)return false;

        TextView stateView   = v.findViewById(R.id.settingsSpcStateOutput);
        if (Utilities.isEmpty(zoneString)){
            stateView.setText(getString(R.string.spc_zone_error));
            CCSettings.setZone(activity, CCSettings.sZoneDefault);
            return false;
        } else {
            int zone = Integer.valueOf(zoneString.toString().trim());
            CoordinateConstants constants = new CoordinateConstants(zone);
            int spcsZone = constants.getZone();
            if (spcsZone == (int)Utilities.ID_DOES_NOT_EXIST) {
                stateView.setText(getString(R.string.spc_zone_error));
                CCSettings.setZone(activity, CCSettings.sZoneDefault);
                return false;
            } else {
                stateView.setText(constants.getState());
                CCSettings.setZone(activity, zone);
            }

        }
        disableSaveButton();
        return true;
    }

    private void setSubtitle(){

        ((MainActivity) getActivity()).setSubtitle(getString(R.string.subtitle_settings));

    }


    //***********************************/
    //****     Save Button        *******/
    //***********************************/

    void enableSaveButton(){
        View v = getView();
        if (v == null)return;

        Button saveButton = v.findViewById(R.id.settingSaveButton);
        saveButton.setEnabled(true);
        saveButton.setTextColor(Color.BLACK);
    }
    void disableSaveButton(){
        View v = getView();
        if (v == null)return;

        Button saveButton = v.findViewById(R.id.settingSaveButton);
        saveButton.setEnabled(false);
        saveButton.setTextColor(Color.GRAY);
    }

    boolean savePrecisions(){
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return false;

        View v = getView();
        if (v == null)return false;



        EditText locPrecisionView = v.findViewById(R.id.locPrecisionInput);
        EditText stdPrecisionView = v.findViewById(R.id.stdDevPrecisionInput);
        EditText sfPrecisionView  = v.findViewById(R.id.sfPrecisionInput);
        EditText caPrecisionView  = v.findViewById(R.id.caPrecisionInput);


        String precisionString = locPrecisionView.getText().toString().trim();
        if (!Utilities.isEmpty(precisionString)) {
            CCSettings.setLocPrecision(activity, Integer.valueOf(precisionString));
        } else {
            CCSettings.setLocPrecision(activity, CCSettings.sLocPrcDefault);
        }

        precisionString = stdPrecisionView.getText().toString().trim();
        if (!Utilities.isEmpty(precisionString)) {
            CCSettings.setStdDevPrecision(activity, Integer.valueOf(precisionString));
        } else {
            CCSettings.setStdDevPrecision(activity, CCSettings.sStdPrcDefault);
        }

        precisionString = sfPrecisionView.getText().toString().trim();
        if (!Utilities.isEmpty(precisionString)) {
            CCSettings.setSfPrecision(activity, Integer.valueOf(precisionString));
        } else {
            CCSettings.setSfPrecision(activity, CCSettings.sSfPrcDefault);
        }

        precisionString = caPrecisionView.getText().toString().trim();
        if (!Utilities.isEmpty(precisionString)) {
            CCSettings.setCAPrecision(activity, Integer.valueOf(precisionString));
        } else {
            CCSettings.setCAPrecision(activity, CCSettings.sCAPrcDefault);
        }

        disableSaveButton();
        return true;
    }
    boolean saveOffsets(){
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return false;

        View v = getView();
        if (v == null)return false;


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

        disableSaveButton();
        return true;
    }
    boolean saveHeightMean(){
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return false;

        View v = getView();
        if (v == null)return false;


        EditText heightView  = v.findViewById(R.id.settingsHeightOutput);
        EditText maxMeanView = v.findViewById(R.id.settingsNumMeanOutput);

        String heightString = heightView.getText().toString().trim();
        if (!Utilities.isEmpty(heightString)) {
            CCSettings.setHeight(activity, Double.valueOf(heightString));
        } else {
            CCSettings.setHeight(activity, CCSettings.sHeightDefault);
        }


        String maxMeanString = maxMeanView.getText().toString().trim();
        if (!Utilities.isEmpty(maxMeanString)){
            CCSettings.setNumMean(activity, Integer.valueOf(maxMeanString));
        } else {
            CCSettings.setNumMean(activity, CCSettings.sNumMeanDefault);
        }

        disableSaveButton();
        return true;
    }
    void saveZone(){
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return ;

        View v = getView();
        if (v == null)return ;


        EditText zoneView    = v.findViewById(R.id.settingsSpcZoneInput);

        String zoneString = zoneView.getText().toString().trim();
        if (!Utilities.isEmpty(zoneString)){
            int zone = Integer.valueOf(zoneString);
            updateStateUI(activity, v, zone);
        } else {
            CCSettings.setZone(activity, CCSettings.sZoneDefault);
        }


        disableSaveButton();
    }

}

