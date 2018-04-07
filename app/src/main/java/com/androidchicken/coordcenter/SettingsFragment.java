package com.androidchicken.coordcenter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The UI Settings Fragment
 * These settings deal with how data is shown to the User
 *
 * Created by Elisabeth Huhn on 7/29/17 for GeoBot.
 * Cloned on 1/3/2018 For CoordCenter
 */
public class SettingsFragment extends Fragment {

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

        MainActivity activity = (MainActivity)getActivity();
        if (activity == null) return null;

        //Inflate the layout for this fragment
        View v = inflater.inflate( R.layout.fragment_settings, container,  false);

        wireWidgets(v);
        Utilities.getInstance().wireSpinners(activity, v);

        initializeUI(v);
        Utilities.getInstance().initializeSpinners(activity, v);

        //get rid of the soft keyboard if it is visible
        EditText aboutWho = v.findViewById(R.id.settingsSpcZoneInput);
        Utilities.getInstance().showSoftKeyboard(activity, aboutWho);

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        //set the title bar subtitle
        setSubtitle();

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
        Utilities.getInstance().wireOffsets(activity, v);
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

        final View view = v;

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

                if (!savePrecisions()) {
                    Utilities.getInstance().errorHandler(activity, R.string.error_saving_offsets);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };
        View.OnFocusChangeListener changeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus){
                //loosing focus, save
                if (!savePrecisions()) {
                    Utilities.getInstance().errorHandler(activity,
                                                         R.string.error_saving_precisions);
                }
            }
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

        locPrecisionView.setOnFocusChangeListener(changeListener);
        stdPrecisionView.setOnFocusChangeListener(changeListener);
        sfPrecisionView .setOnFocusChangeListener(changeListener);
        caPrecisionView .setOnFocusChangeListener(changeListener);

        locPrecisionView.setEnabled(true);
        stdPrecisionView.setEnabled(true);
        sfPrecisionView.setEnabled(true);
        caPrecisionView.setEnabled(true);

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

                if (!saveHeightMean()) {
                    Utilities.getInstance().errorHandler(activity, R.string.error_saving_height);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };
        View.OnFocusChangeListener changeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if (!saveHeightMean()) {
                        Utilities.getInstance().errorHandler(activity,
                                                             R.string.error_saving_height);
                    }
                }
            }
        };


        final EditText heightView  = v.findViewById(R.id.settingsHeightOutput);
        final EditText maxMeanView = v.findViewById(R.id.settingsNumMeanOutput);

        heightView .addTextChangedListener(heightMeanTextWatcher);
        maxMeanView.addTextChangedListener(heightMeanTextWatcher);

        heightView .setOnFocusChangeListener(changeListener);
        maxMeanView.setOnFocusChangeListener(changeListener);
    }
    private void wireZone(View v){
        final MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

        TextWatcher zoneTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence zone, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence zone, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters
                if (!updateZoneState(zone)) {
                    Utilities.getInstance().errorHandler(activity, R.string.error_updating_zone);
                }
            }

            @Override
            public void afterTextChanged(Editable zon) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };
        View.OnFocusChangeListener changeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if (!saveZone()) {
                        Utilities.getInstance().errorHandler(activity, R.string.error_saving_zone);
                    }
                }
            }
        };

        final EditText zoneView    = v.findViewById(R.id.settingsSpcZoneInput);
        zoneView.setOnFocusChangeListener(changeListener);
        zoneView.addTextChangedListener(zoneTextWatcher);
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
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

        initializeSwitches(v);
        Utilities.getInstance().initializeSpinners(activity, v);
        initializeZone(v);
        initializeHeightMean(v);
        initializePrecision(v);
        Utilities.getInstance().initializeOffsets(activity, v);

    }
    private void initializeSwitches(View v) {
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return;

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
        return true;
    }

    private void setSubtitle(){

        ((MainActivity) getActivity()).setSubtitle(getString(R.string.subtitle_settings));

    }


    //***********************************/
    //****        Save            *******/
    //***********************************/

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

        return true;
    }
    boolean saveZone(){
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null)return false;

        View v = getView();
        if (v == null)return false;

        EditText zoneView    = v.findViewById(R.id.settingsSpcZoneInput);

        String zoneString = zoneView.getText().toString().trim();
        if (!Utilities.isEmpty(zoneString)){
            int zone = Integer.valueOf(zoneString);
            updateStateUI(activity, v, zone);
        } else {
            CCSettings.setZone(activity, CCSettings.sZoneDefault);
        }
        return true;
    }

}

