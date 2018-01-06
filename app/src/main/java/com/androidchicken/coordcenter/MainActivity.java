package com.androidchicken.coordcenter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    /* *********************************************************************/
    /* ********   Static Constants  ****************************************/
    /* *********************************************************************/

    //GeoBot Version

    static final String sGeoBotVersion = "Alpha Release 0.1";

    //DEFINE constants / literals
    static final int MY_PERMISSIONS_REQUEST_COURSE_LOCATIONS = 1;
    static final int MY_PERMISSIONS_REQUEST_FINE_LOCATIONS   = 2;

    static final int    sCollectPointsRequestCode  = 1;
    static final String sDestinationFragmentKey    = "DESTINATION_KEY";
    static final String sPopToBackStackTag     = "POP_TO_BACKSTACK";


    private static final String sHomeTag        = "HOME";//HOME screen fragment
    private static final String sMeasureTag     = "MEASURE";
    private static final String sSettingsTag    = "SETTINGS";

    private static final String sCurrentFragmentTag    = "CURRENT_FRAGMENT";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)restoreState(savedInstanceState);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        initializeFragment();

        //initialize the floating action bar here if we add one
        initializeFAB();

        setSubtitle(R.string.subtitle_home);

        initializeGps();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);

            onBackPressed();

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // finish();
    }


    private void restoreState(Bundle savedInstanceState){

        // TODO: 1/3/2018 restore state if necessary
    }


    //* *********************************************************************/
    //* ********    Location Methods & Callbacks ****************************/
    //* *********************************************************************/

    private void initializeGps() {

        //make sure we have GPS permissions
        //check for permission to continue
        int permissionCheckCourse = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheckFine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If we don't currently have permission, we have to ask for it
        if (permissionCheckCourse != PackageManager.PERMISSION_GRANTED){
            //find out if we need to explain to the user why we need GPS
/*
 if (
 //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
 false){
 //// TODO: 9/5/2016 need to add code if GPS is off
 //tell the user why GPS is required
 // Show an expanation to the user *asynchronously* -- don't block
 // this thread waiting for the user's response! After the user
 // sees the explanation, try again to request the permission.
 } else {
*/
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MainActivity.MY_PERMISSIONS_REQUEST_COURSE_LOCATIONS);

            // MY_PERMISSIONS_REQUEST_COURSE_LOCATIONS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            //}
        }

        //If we don't currently have permission, we have to ask for it
        if (permissionCheckFine != PackageManager.PERMISSION_GRANTED) {
            //find out if we need to explain to the user why we need GPS
/*
 if (false) {
 //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){

 // TODO: 9/5/2016 so write the code to tell user why we need GPS permissions
 //tell the user why GPS is required
 // Show an expanation to the user *asynchronously* -- don't block
 // this thread waiting for the user's response! After the user
 // sees the explanation, try again to request the permission.
 } else {
 *******/
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.MY_PERMISSIONS_REQUEST_FINE_LOCATIONS);

            // MY_PERMISSIONS_REQUEST_FINE_LOCATIONS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

            //}
            //So now signup for the GpsStatus.NmeaListener

        }
    }

    //Callbacks for permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COURSE_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                // TODO: 9/5/2016 Build in this functionality
/*
 if (grantResults.length > 0
 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

 // permission was granted, yay! Do the
 // contacts-related task you need to do.


 } else {

 // permission denied, boo! Disable the
 // functionality that depends on this permission.
 }
 *****/
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                // TODO: 9/5/2016  fill this in
/*
 if (grantResults.length > 0
 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

 // permission was granted, yay! Do the
 // contacts-related task you need to do.


 } else {

 // permission denied, boo! Disable the
 // functionality that depends on this permission.
 }
 ********/
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        switch (item.getItemId()){
            case R.id.action_settings :
                switchToSettingsScreen();
                return true;

            case  R.id.action_measure:
                switchToMeasureScreen(null);
                return true;


        } //end switch

        return super.onOptionsItemSelected(item);
    }


    //* ********************************************************************
    //* Screen switching as a result of response from invoked Activity
    //* ********************************************************************/

    //This method is invoked when a child Activity sends back a response
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        switch(requestCode) {
            case (sCollectPointsRequestCode) : {
                if (resultCode == Activity.RESULT_OK) {
                    if (dataIntent != null) {
                        // TODO Extract the data returned from the child Activity.
                        String destinationTag = dataIntent.getStringExtra(sDestinationFragmentKey);
                        if (!destinationTag.isEmpty()){
                            if (destinationTag.equals(sPopToBackStackTag)){
                                switchToPopBackstack();
                            }

                        }
                    }
                }
                break;
            }
        }
    }



    /* ************************************************************/
    /* ******** Methods dealing with the FAB          *************/
    /* ************************************************************/
    private void initializeFAB(){
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFAB(view);
            }
        });

        hideFAB();
    }

    private void handleFAB(View view){

        /* todo remember to fix this

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        FloatingActionButton fab = findViewById(R.id.fab);

        if (fragment instanceof CoordinateMeasureFragment){
            SatelliteManager satelliteManager = SatelliteManager.getInstance();
            String dopValues = String.format(Locale.getDefault(),
                    "HDOP = %.3f     VDOP = %.3f      PDOP = %.3f",
                    satelliteManager.getHdop(),
                    satelliteManager.getVdop(),
                    satelliteManager.getPdop());




            //Snackbar.make(view, dopValues, Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();

            if (fab.getVisibility() == FloatingActionButton.VISIBLE){
                hideFAB();
            } else {
                showFAB();
            }

        }    else {
            hideFAB();
        }
        */
        hideFAB();
    }

    public void showFAB(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(FloatingActionButton.VISIBLE);
    }

    public void hideFAB(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(FloatingActionButton.GONE);
    }

    ///* ************************************************************/
    ///* *****************        Initialization        *************/
    ///* ************************************************************/

    ///* ************************************************************/
    ///* ***************** Routines to switch fragments *************/
    ///* ************************************************************/


    private void initializeFragment() {

        //Set the fragment to Home screen
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            //when we first create the activity, the fragment needs to be the home screen

            fragment = new CoordinateMeasureFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }


    private Fragment getCurrentFragment(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentById(R.id.fragment_container);
    }

    private void clearBackStack(){
        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //clear the back stack

        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }
    }

    ///* *** Routines to actually switch the screens *******/
    private void switchScreenWithStack(Fragment fragment, String tag) {

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //Are any fragments already being displayed?
        Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

        if (oldFragment == null) {
            //It shouldn't ever be the case that we got this far with no fragments on the screen,
            // but code defensively. Who knows how the app will evolve
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment, tag)
                    .commit();
        } else {
            fm.beginTransaction()
                    //replace whatever is being displayed with the Home fragment
                    .replace(R.id.fragment_container, fragment, tag)
                    //and add the transaction to the back stack
                    .addToBackStack(tag)
                    .commit();
        }

    }
    private void switchScreen(Fragment fragment, String tag) {
        //clear the back stack
        clearBackStack();

        switchScreenWithStack(fragment, tag);
    }
    private void switchScreen(Fragment fragment, String tag, int subtitle) {
        switchScreen(fragment, tag);

        setSubtitle(subtitle);
    }
    private void switchScreen(Fragment fragment, String tag, String subtitle) {
        switchScreen(fragment, tag);

        setSubtitle(subtitle);
    }

    void setSubtitle(int subtitle){

        //Put the name of the fragment on the title bar

        if (getSupportActionBar() != null){
            getSupportActionBar().setSubtitle(subtitle);
        }


    }
    void setSubtitle(String subtitle){

        //Put the name of the fragment on the title bar

        if (getSupportActionBar() != null){
            getSupportActionBar().setSubtitle(subtitle);
        }


    }


    void switchToPopBackstack(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //settings is at the top of the back stack, so pop it off
        fm.popBackStack();

    }


    void popToScreen(String tag){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        boolean stillLooking = true;
        if (fm.getBackStackEntryCount() == 0) stillLooking = false;

        int i;
        CharSequence fragName;
        while (stillLooking){
            i = fm.getBackStackEntryCount()-1;
            fragName = fm.getBackStackEntryAt(i).getName();
            if (fragName.equals(tag)){
                stillLooking = false;
            } else {
                fm.popBackStackImmediate();
                if (fm.getBackStackEntryCount() == 0) stillLooking = false;
            }
        }


    }


    void switchToHomeScreen(){
        //replace the fragment with the Home UI


        Fragment fragment    = new CoordinateMeasureFragment();
        String   tag         = sHomeTag;

        switchScreen(fragment, tag);

    }


    void switchToMeasureScreen(Point point){

        Fragment fragment    = CoordinateMeasureFragment.newInstance(point);
        String   tag         = sMeasureTag;

        switchScreen(fragment, tag);

    }

    void switchToSettingsScreen(){

        Fragment fragment    = new SettingsFragment();
        String   tag         = sSettingsTag;

        switchScreen(fragment, tag);

    }

}
