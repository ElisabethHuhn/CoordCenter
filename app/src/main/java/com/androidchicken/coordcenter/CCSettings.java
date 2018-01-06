package com.androidchicken.coordcenter;

import android.content.Context;
import android.content.SharedPreferences;


import static android.R.attr.defaultValue;

/**
 * Created by Elisabeth Huhn on 7/29/17 for GeoBot
 * Cloned on 1/4/2018 for CoordCenter
 *
 * General Settings
 * Values saved in Shared Preferences
 *
 */
public class CCSettings {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/


    static final int sDataSourceNoneSelected            = 0;
    static final int sDataSourceWGSManual               = 1;
    static final int sDataSourceSPCSManual              = 2;
    static final int sDataSourceUTMManual               = 3;
    static final int sDataSourcePhoneGps                = 4;
    static final int sDataSourceExternalGps             = 5;
    static final int sDataSourceCellTowerTriangulation  = 6;


    private static final String sAutosaveTag  = "AUTOSAVE";
    private static final String sDistUnitsTag = "DIST_UNITS";
    private static final String sRmsVStdTag   = "RMS_V_STD";
    private static final String sUIOrderTag   = "UI_ORDER";
    private static final String sLocDDvDMSTag = "LOC_DD_V_DMS";
    private static final String sCADDvDMSTag  = "CA_DD_V_DMS";
    private static final String sDirVPMTag    = "DIR_V_PM";
    private static final String sHeightTag    = "HEIGHT";
    private static final String sCoordTypeTag = "COORD_TYPE";
    private static final String sDataSourceTag= "DATA_SOURCE";
    private static final String sNumMeanTag   = "NUM_MEAN";
    private static final String sZoneTag      = "ZONE";


    private static final String sLocPrcTag    = "LOC_PRC";
    private static final String sStdPrcTag    = "STD_PRC";
    private static final String sSfPrcTag     = "SF_PRC";
    private static final String sCAPrcTag     = "CA_PRC";
    private static final String sExLocPrcTag  = "EXLOC_PRC";
    private static final String sExStdPrcTag  = "EXSTD_PRC";
    private static final String sExSfPrcTag   = "EXSF_PRC";
    private static final String sExCAPrcTag   = "EXCA_PRC";

   // static final int sLocPrc    = 0;
    static final int sStdPrc    = 1;
    static final int sSfPrc     = 2;
    static final int sCAPrc     = 3;

    private static final boolean sAutosaveDefault = true;
    private static final boolean sRmsDefault      = true;
    private static final boolean sStdDefault      = false;
    private static final boolean sUIOrderDefault  = true;
    private static final boolean sLatLngDefault   = true;
    private static final boolean sLngLatDefault   = false;
    private static final boolean sNEDefault       = true;
    private static final boolean sENDefault       = true;

    private static final boolean sLocDDDefault    = true;
    private static final boolean sLocDMSDefault   = false;
    private static final boolean sCADDDefault     = true;
    private static final boolean sCADMSDefault    = false;
    private static final boolean sDirDefault      = true;
    private static final boolean sPMDefault       = false;


    static final int sLocPrcDefault          = 6;
    static final int sStdPrcDefault          = 6;
    static final int sSfPrcDefault           = 6;
    static final int sCAPrcDefault           = 6;
    static final int sExLocPrcDefault        = 6;
    static final int sExStdPrcDefault        = 6;
    static final int sExSfPrcDefault         = 6;
    static final int sExCAPrcDefault         = 6;

    static final int sHeightDefault          = 6;
    static final int sCoordinateTypeDefault  = Coordinate.sCoordinateDBTypeWGS84;
    static final int sNumMeanDefault         = 180;
    static final int sDataSourceDefault      = sDataSourcePhoneGps;
    static final int sZoneDefault            = 0 ;


    //These constants define the order of the corresponding spinner positions
    static final int    sMeters         = 0;//values for Distance Units
    static final int    sFeet           = 1;
    static final int    sIntFeet        = 2; //international feet
    static final String sMetersString   = "Meters";
    static final String sFeetString     = "Feet";
    static final String sIntFeetString  = "International Feet";


    static final int sDistUnitsDefault    = sMeters;





    //************************************/
    /*          Static Methods           */
    //************************************/


    static boolean getBooleanSetting (MainActivity activity, String tag, boolean initialValue) {
        if (activity == null){
            return false;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(tag, initialValue);
    }
    static void    setBooleanSetting (MainActivity activity, String tag, boolean boolValue) {
        if (activity == null){
            return;
        }

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(tag, boolValue);
        editor.apply();
    }


    static int  getIntSetting (MainActivity activity, String tag, int initialValue) {

        if (activity == null){
            return defaultValue;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);

        return sharedPref.getInt(tag, initialValue);
    }
    static void setIntSetting (MainActivity activity, String tag, int intValue) {
        if (activity == null){
            return;
        }

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(tag, intValue);
        editor.apply();
    }



    static double getDoubleSetting (MainActivity activity, String tag, double initialValue) {

        if (activity == null){
            return defaultValue;
        }
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);

        return Double.longBitsToDouble(sharedPref.getLong(tag, Double.doubleToLongBits(initialValue)));

    }
    static void   setDoubleSetting (MainActivity activity, String tag, double doubleValue) {
        if (activity == null){
            return;
        }

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(tag, Double.doubleToRawLongBits(doubleValue));
        editor.apply();
    }



    //************************************/
    /*    Static (class) Variables       */
    //************************************/


    //************************************/
    /*    Member (instance) Variables    */
    //************************************/

    //All values are stored in shared preferences




    //************************************************************
    // *             Constructor
    //*************************************************************/

    //Default constructor, A dummy project that is not in memory list or in the DB
    //Every field is initialized. No Nulls Allowed
    public CCSettings() {

    }



    //*********************************************/
    //Setters and Getters
    //*********************************************/

    static boolean isAutosave  (MainActivity activity)   {
        return getBooleanSetting(activity, sAutosaveTag, sAutosaveDefault);
    }
    static void    setAutosave(MainActivity activity) { setBooleanSetting(activity, sAutosaveTag, true);}
    static boolean isManualSave(MainActivity activity) { return !isAutosave(activity);}
    static void    setManualSave(MainActivity activity){ setBooleanSetting(activity, sAutosaveTag, false);}


    static boolean isRms    (MainActivity activity)   {
        return getBooleanSetting(activity, sRmsVStdTag, sRmsDefault);
    }
    static void    setRms   (MainActivity activity) { setBooleanSetting(activity, sRmsVStdTag, true);}
    static boolean isStdDev (MainActivity activity) { return !isRms(activity);}
    static void    setStdDev(MainActivity activity){ setBooleanSetting(activity, sRmsVStdTag, false);}


    static boolean isLatLng  (MainActivity activity)   {
        return getBooleanSetting(activity, sUIOrderTag, sLatLngDefault);
    }
    static void    setLatLng(MainActivity activity) { setBooleanSetting(activity, sUIOrderTag, true);}
    static boolean isLngLat(MainActivity activity) { return !isLatLng(activity);}
    static void    setLngLat(MainActivity activity){ setBooleanSetting(activity, sUIOrderTag, false);}


    static boolean isNE    (MainActivity activity)   {
        return getBooleanSetting(activity, sUIOrderTag, sNEDefault);
    }
    static void    setNE   (MainActivity activity) { setBooleanSetting(activity, sUIOrderTag, true);}
    static boolean isEN (MainActivity activity) { return !isNE(activity);}
    static void    setEN(MainActivity activity){ setBooleanSetting(activity, sUIOrderTag, false);}


    static boolean isLocDD    (MainActivity activity)   {
        return getBooleanSetting(activity, sLocDDvDMSTag, sLocDDDefault);
    }
    static void    setLocDD   (MainActivity activity) { setBooleanSetting(activity, sLocDDvDMSTag, true);}
    static boolean isLocDMS (MainActivity activity) { return !isLocDD(activity);}
    static void    setLocDMS(MainActivity activity){ setBooleanSetting(activity, sLocDDvDMSTag, false);}


    static boolean isCADD    (MainActivity activity)   {
        return getBooleanSetting(activity, sCADDvDMSTag, sCADDDefault);
    }
    static void    setCADD   (MainActivity activity) { setBooleanSetting(activity, sCADDvDMSTag, true);}
    static boolean isCADMS (MainActivity activity) { return !isCADD(activity);}
    static void    setCADMS(MainActivity activity){ setBooleanSetting(activity, sCADDvDMSTag, false);}


    static boolean isDir  (MainActivity activity)   {
        return getBooleanSetting(activity, sDirVPMTag, sDirDefault);
    }
    static void    setDir (MainActivity activity) { setBooleanSetting(activity, sDirVPMTag, true);}
    static boolean isPM   (MainActivity activity) { return !isDir(activity);}
    static void    setPM  (MainActivity activity) { setBooleanSetting(activity, sDirVPMTag, false);}






    static double getHeight(MainActivity activity) {
        return getDoubleSetting(activity, sHeightTag, sHeightDefault);
    }
    static void   setHeight(MainActivity activity, double height) {
        setDoubleSetting(activity, sHeightTag, height);
    }

    static CharSequence getCoordinateTypeString(MainActivity activity) {

        String type;

        switch(getCoordinateType(activity)) {
            case Coordinate.sCoordinateDBTypeWGS84:
                type = Coordinate.sCoordinateTypeWGS84;
                break;

            case Coordinate.sCoordinateDBTypeSPCS:
                type = Coordinate.sCoordinateTypeSPCS;
                break;

            case Coordinate.sCoordinateDBTypeUTM:
                type = Coordinate.sCoordinateTypeUTM;
                break;

            case Coordinate.sCoordinateDBTypeNAD83:
                type = Coordinate.sCoordinateTypeNAD83;
                break;

            default:
                type = Coordinate.sCoordinateTypeUnknown;
        }
        return type;
    }
    static void setCoordinateType(MainActivity activity, CharSequence coordinateType){
        int type;

        if (coordinateType.equals(Coordinate.sCoordinateTypeWGS84)){
            type = Coordinate.sCoordinateDBTypeWGS84;
        } else if (coordinateType.equals(Coordinate.sCoordinateTypeSPCS)){
            type = Coordinate.sCoordinateDBTypeSPCS;
        } else if (coordinateType.equals(Coordinate.sCoordinateTypeUTM)){
            type = Coordinate.sCoordinateDBTypeUTM;
        } else if (coordinateType.equals(Coordinate.sCoordinateTypeNAD83)){
            type = Coordinate.sCoordinateDBTypeNAD83;
        } else {
            type = Coordinate.sCoordinateDBTypeWGS84;
        }
        setCoordinateType(activity, type);
    }

    static int  getCoordinateType(MainActivity activity)                  {
        return getIntSetting(activity, sCoordTypeTag, sCoordinateTypeDefault);
    }
    static void setCoordinateType(MainActivity activity, int coordinateType){
        setIntSetting(activity, sCoordTypeTag, coordinateType);
    }

    static int  getNumMean(MainActivity activity) {
        return getIntSetting(activity, sNumMeanTag, sNumMeanDefault);
    }
    static void setNumMean(MainActivity activity, int numMean) {
        setIntSetting(activity, sNumMeanTag, numMean);
    }

    static int  getZone(MainActivity activity) {
        return getIntSetting(activity, sZoneTag, sZoneDefault);
    }
    static void setZone(MainActivity activity, int zone) {
        setIntSetting(activity, sZoneTag, zone);
    }

    static int  getDistanceUnits(MainActivity activity) {
        return getIntSetting(activity, sDistUnitsTag, sDistUnitsDefault);
    }
    static String getDistUnitString(MainActivity activity) {
        CharSequence duString;

        if (getDistanceUnits(activity) == sMeters){
            duString = sMetersString;
        } else if (getDistanceUnits(activity) == sFeet){
            duString = sFeetString;
        } else {//international feet
            duString = sIntFeetString;
        }
        return duString.toString();

    }
    static void setDistanceUnits(MainActivity activity, int distanceUnits) {
        setIntSetting(activity, sDistUnitsTag,  distanceUnits);
    }

    static int  getDataSource(MainActivity activity) {
        return getIntSetting(activity, sDataSourceTag, sDataSourceDefault);
    }
    static void setDataSource(MainActivity activity, int dataSource) {
        setIntSetting(activity, sDataSourceTag, dataSource);
    }

    static int  getDistUnits(MainActivity activity) {
        return getIntSetting(activity, sDistUnitsTag, sDistUnitsDefault);
    }
    static String getsDistUnitsString(MainActivity activity){
        return getsDistUnitsString(getDistUnits(activity));
    }
    static String getsDistUnitsString(int distUnits){
        if (distUnits == sMeters)return sMetersString;
        if (distUnits == sFeet)  return sFeetString;
        if (distUnits == sIntFeet)return sIntFeetString;
        return "Unknown Units";
    }
    static void setDistUnits(MainActivity activity, int distUnits) {
        setIntSetting(activity, sDistUnitsTag, distUnits);
    }




    static int  getLocPrecision(MainActivity activity) {
        return getIntSetting(activity, sLocPrcTag, sLocPrcDefault);
    }
    static void setLocPrecision(MainActivity activity, int locPrecision) {
        setIntSetting(activity, sLocPrcTag, locPrecision);
    }

    static int  getStdDevPrecision(MainActivity activity) {
        return getIntSetting(activity, sStdPrcTag, sStdPrcDefault);
    }
    static void setStdDevPrecision(MainActivity activity, int stdDevPrecision) {
        setIntSetting(activity, sStdPrcTag, stdDevPrecision);
    }


    static int  getSfPrecision(MainActivity activity) {
        return getIntSetting(activity, sSfPrcTag, sSfPrcDefault);
    }
    static void setSfPrecision(MainActivity activity, int sfPrecision) {
        setIntSetting(activity, sSfPrcTag, sfPrecision);
    }

    static int  getCAPrecision(MainActivity activity) {
        return getIntSetting(activity, sCAPrcTag, sCAPrcDefault);
    }
    static void setCAPrecision(MainActivity activity, int caPrecision) {
        setIntSetting(activity, sCAPrcTag, caPrecision);
    }



    static int  getExLocPrecision(MainActivity activity) {
        return getIntSetting(activity, sExLocPrcTag, sExLocPrcDefault);
    }
    static void setExLocPrecision(MainActivity activity, int locPrecision) {
        setIntSetting(activity, sExLocPrcTag, locPrecision);
    }

    static int  getExStdDevPrecision(MainActivity activity) {
        return getIntSetting(activity, sExStdPrcTag, sExStdPrcDefault);
    }
    static void setExStdDevPrecision(MainActivity activity, int stdDevPrecision) {
        setIntSetting(activity, sExStdPrcTag, stdDevPrecision);
    }


    static int  getExSfPrecision(MainActivity activity) {
        return getIntSetting(activity, sExSfPrcTag, sExSfPrcDefault);
    }
    static void setExSfPrecision(MainActivity activity, int sfPrecision) {
        setIntSetting(activity, sExSfPrcTag, sfPrecision);
    }

    static int  getExCAPrecision(MainActivity activity) {
        return getIntSetting(activity, sExCAPrcTag, sExCAPrcDefault);
    }
    static void setExCAPrecision(MainActivity activity, int caPrecision) {
        setIntSetting(activity, sExCAPrcTag, caPrecision);
    }



}
