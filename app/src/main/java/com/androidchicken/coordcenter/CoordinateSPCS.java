package com.androidchicken.coordcenter;

/**
 * This is coordinate in the State Plane Coordinate System
 * Created by Elisabeth Huhn on 11/11/2016 for GeoBot.
 * cloned on 1/3/2018 for CoordCenter
 *
 * * This coordinate extends the basic capabilities of CoordinateEN
 * and adds:
 *
 * A) returns its type from getCoordinateType as "CoordinateSPCS"
 * B) remembers its coordinate system specific constants, e.g. Dataum
 *

 */
class CoordinateSPCS extends CoordinateEN {

    //+*******************************************************************/
    //+**********      Static Constants                         **********/
    //+*******************************************************************/

    private static final String sDatum = "SPCS";



    static final double E0     = 2000000.0000; //meters Easting of projection and grid origion
    static final double E0feet = 6561666.667; //E0 in feet
    static final double Nb     = 500000.0000;  //meters northing of the grid base
    static final double Nbfeet = 1640416.667; //feet





    //+*******************************************************************/
    //+**********Instance Variables, Setters & Getters          **********/
    //+*******************************************************************/

    private CharSequence mState;

    CharSequence getState() {
        return mState;
    }
    void setState(CharSequence state) {
        mState = state;
    }



    //+*******************************************************************/
    //+**********     Constructors                              **********/
    //+*******************************************************************/

    CoordinateSPCS() {initializeDefaultVariables(); }

    CoordinateSPCS(MainActivity activity,
                   String zoneString,
                   String stateString,
                   String eastingString,
                   String northingString,
                   String elevationString,
                   String geoidString,
                   String convergenceString,
                   String scaleString){
        initializeDefaultVariables();


        int distUnits = CCSettings.getDistUnits(activity);

        if (Utilities.isEmpty(zoneString)){
            setZone((int) Utilities.ID_DOES_NOT_EXIST);
        } else {
            setZone(Integer.valueOf(zoneString));
        }
        setState    (stateString);
        if (!Utilities.isEmpty(eastingString)) {

            setEasting(getMeters(eastingString, distUnits));
        } else {
            setEasting(0d);
        }
        if (!Utilities.isEmpty(northingString)) {
            setNorthing(getMeters(northingString, distUnits));
        } else {
            setNorthing(0d);
        }
        if (!Utilities.isEmpty(elevationString)) {

            setElevation(getMeters(elevationString, distUnits));
        } else {
            setElevation(0d);
        }
        if (!Utilities.isEmpty(geoidString)) {
            setGeoid(getMeters(geoidString, distUnits));
        } else {
            setGeoid(0d);
        }

        if (!Utilities.isEmpty(convergenceString)) {

            setConvergenceAngle(Double.valueOf(convergenceString));
        } else {
            setConvergenceAngle(0d);
        }
        if (!Utilities.isEmpty(scaleString)) {

            setScaleFactor(Double.valueOf(scaleString));
        } else {
            setScaleFactor(0d);
        }

        setValidCoordinate(true);
    }





    CoordinateSPCS(CoordinateWGS84 wsg84Coordinate, int zone){

        initializeDefaultVariables();

        CoordinateConstants constants = new CoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone == (int)Utilities.ID_DOES_NOT_EXIST) return;

        super.setZone(spcsZone);
        setState(constants.getState());

        if (constants.getProjection() == CoordinateConstants.sLAMBERT){
            convertWithLambert(wsg84Coordinate, constants);
        } else {
            convertWithMercator(wsg84Coordinate, constants);
        }

        setElevation(wsg84Coordinate.getElevation());
        setGeoid(wsg84Coordinate.getGeoid());
    }


    protected void initializeDefaultVariables(){
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        //initialize all variables common to EN coordinates
        super.initializeDefaultVariables();

        //initialize all variables from this level
        setCoordinateDBType( Coordinate.sCoordinateDBTypeSPCS);
        setDatum( sDatum); //eg WGS84


    }



    //+*******************************************************************/
    //+**********     Instance Methods                          **********/
    //+*******************************************************************/

    private void convertWithLambert(CoordinateWGS84 coordinateWGS84,
                                    CoordinateConstants constants){
        //Convert degrees to radians (radians = degrees * pi/180)
        //All lat lng values are in decimal degrees
        double latAts  = coordinateWGS84.getLatitude() ;
        double latAtcp = constants.getCentralParallel();

        double lngAts  = coordinateWGS84.getLongitude();
        double lngAtcm = constants.getCentralMeridian();

        //Equ #1  ΔΦ  =  Φ  –  Bo
        // deltaLatitude is in meters
        double deltaLatitude = latAts - latAtcp;
        double deltaLatitude2 = deltaLatitude * deltaLatitude;
        double deltaLatitude3 = deltaLatitude * deltaLatitude2;
        double deltaLatitude4 = deltaLatitude * deltaLatitude3;
        double deltaLatitude5 = deltaLatitude * deltaLatitude4;

        double uTerm1 = constants.getL1() * deltaLatitude;
        double uTerm2 = constants.getL2() * deltaLatitude2;
        double uTerm3 = constants.getL3() * deltaLatitude3;
        double uTerm4 = constants.getL4() * deltaLatitude4;
        double uTerm5 = constants.getL5() * deltaLatitude5;

        //Equ #2 u   =  L1 (Δ Φ)  +  L2 (Δ Φ2)  +  L3 (Δ Φ3)  +  L4 (Δ Φ4)  +  L5 (Δ Φ5)
        double u = uTerm1 +
                   uTerm2 +
                   uTerm3 +
                   uTerm4 +
                   uTerm5 ;

        //Equ #3 R = Ro – u

        double RMappingRadiusAtStation = constants.getRoMappingRadiusAtLat() - u;

        //Equ #4 Δ λ  =  λo  -  λ
        //all terms in decimal degrees

        // TODO: 6/30/2017 This equation must refer to user Project Preferences. A W value is positive, while a PlusMinus value is negative
        // TODO: 6/30/2017 In any case, this is NOT an absolute value calculation
        //if user preference is directions, a W value is positive,
        //else                              a W value is negative
        //The calculation assumes a W (ie directions) positive value
        double deltaLng = Math.abs(lngAtcm) - Math.abs(lngAts);


        double sincp    = Math.sin(Math.toRadians(latAtcp));

        //Equ #5) 	γ  =  Δ λ x sin (Φo)
        //convergenceAngle is decimal degrees
        double convergenceAngle = deltaLng * sincp;

        double sinCA = Math.sin(Math.toRadians(convergenceAngle));

        //Equ #6) 	E1  =  R (sin γ)
        double E1 = RMappingRadiusAtStation * sinCA;

        //Equ #7) 	E  =  E1 + Eo
        //Units of E are meters
        double easting = E1 + constants.getFalseEasting();

        double tanCA = Math.tan(Math.toRadians(convergenceAngle / 2.));

        //Equ #8) 	N1 = u + E1 (tan γ/2)
        double N1 = u + (E1 * tanCA);

        //Equ #9) 	N  =  N1 + No2
        //units of northing are meters
        double northing = N1 + constants.getFalseNorthing2();

        double F1 = constants.getF1();
        double F2 = constants.getF2();
        double F3 = constants.getF3();

        double u2 = u*u;
        double u3 = u*u2;

        //#10) 	k	= F1 + F2u2 +F3u3
        double scaleFactor = F1 + (F2 * u2) + (F3 * u3);

        super.setNorthing(northing);
        super.setEasting(easting);
        super.setScaleFactor(scaleFactor);
        super.setConvergenceAngle(convergenceAngle);
        super.setValidCoordinate(true);

    }


    private void convertWithMercator(CoordinateWGS84 coordinateWGS84,
                                    CoordinateConstants constants){

        //All Latitude and Longitude values are in meters

        //Convert degrees to radians (radians = degrees * pi/180)
        // TODO: 7/20/2017 Need to get the minus sign correct by using the Project Spinner for which hemi is positive
        double latAts  = Math.toRadians(coordinateWGS84.getLatitude());
        double latAtcp = Math.toRadians(constants.getGridOriginLatitude());

        double lng = Math.abs(coordinateWGS84.getLongitude());
        double lngAts  = Math.toRadians(lng);
        double lngAtcm = Math.toRadians(constants.getCentralMeridian());


        //Equ #11)	e2 	= (a2 –b2) / a2 = [2f – f2] = constant Col G
        double e2 = constants.getEccentricity2();

        //equ #12 e12 	=   (a2 –b2) / b2 	=   [e2 / (1 – e2)]
        double e12 = (e2 / (1. - e2));// (e2 / (1. – e2));
        double major = constants.getSemiMajorAxis();
        double minor = constants.getSemiMinorAxis();

        //Equ #13)	n	= (a – b) / (a +b) = [f / (2-f)]
        double n = ((major - minor) / (major + minor));
        double cosLat = Math.cos(latAts);
        double cos2Lat = cosLat * cosLat;

        //Equ #14 n12 	 =   e12 cos2 Φ
        double n12 = e12 * cos2Lat;
        double n2 = n * n;

        double n4 = n*n*n*n;

        /*
        //Equ #15)	r 	= a (1-n) (1-n12) (1 + 9n12/4 + 225n4/64)
        //But better still is to use the constant from the table.
        double oneMinusn       = 1. - n;
        double oneMinusn2      = 1. - (n*n);
        double onePlus9n2Over4 = (1. + ((9.* (n*n))/4.));
        double two25n4Over64   = ((225.*n4)/64.) ;
        double r = ( major * oneMinusn * oneMinusn2 * (onePlus9n2Over4 + two25n4Over64));
                //(1. - n) *(1. - n2) *(1. + ((9.*n2)/4.) + ((225.*n4)/64.) ));
        */
        double radiusOfRectifyingSphere = constants.getRadiusOfRectifyingSphere();

        //radiusOfRectifyingSphere is equal to r
        //Even though the calculated value corresponds, use the constants value


        //Equ #16)	ωo 	= Φo + sin Φo cos Φo (Uo + U2cos2 Φo + U4cos4 Φo + U6cos6 Φo)
        double rectifyingLatitudeAtCP = rectifyingLatitude(latAtcp, constants);

        //Equ #17)	ω	= Φ + sin Φ cos Φ (Uo + U2cos2 Φ + U4cos4 Φ + U6cos6 Φ)
        double rectifyingLatitudeAtst = rectifyingLatitude(latAts,  constants);

        //Equ #18)	L 	= (λ – λo) cos Φ
        double deltaLng = lngAts - lngAtcm;
        double L = deltaLng * cosLat;

        //Equ #19)	So 	= ko ωo r
        double meridionalDistanceCp = constants.getGridScaleFactor() *
                                        rectifyingLatitudeAtCP *
                                        radiusOfRectifyingSphere;
        //Equ #20)	S 	= ko ω r
        double meridionalDistanceSt = constants.getGridScaleFactor() *
                                        rectifyingLatitudeAtst *
                                        radiusOfRectifyingSphere;

        //Equ #21)	R 	= ko a / (1 - e2sin2 Φ)1/2
        double sinLat  = Math.sin(latAts);
        double sin2Lat = sinLat * sinLat;
        double RMappingRadiusSt = ((constants.getGridScaleFactor() * major) /
                                  ( Math.sqrt(1 - (constants.getEccentricity2() * sin2Lat))));

        double tanLat = Math.tan(latAts);
        double tan2Lat = tanLat * tanLat;
        double tan4Lat = (tan2Lat * tan2Lat);
        double tan6Lat = tan4Lat * tan2Lat;


        //Equ #22)	A1 = - R
        double A1 = -RMappingRadiusSt;

        //Equ #23)	A2 = ½ R t
        double A2 = (1./2.)    * ( RMappingRadiusSt * tanLat);

        //Equ #24)	A3 =    1/6 (1 - t2 + n12)
        double A3 = (1./6.)    * ( 1. -    tan2Lat       +            n12 );

        //Equ #25)	A4 =   1/12 [5 -   t2 + n12 (9 + 4n2)]
        double A4 = (1./12.)   * ( 5. -     tan2Lat      +           (n12 * (9. +   (4.*n2))));

        //Equ #26   A5 =  1/120 [5 - 18t2 + t4 + n12 (14 – 58t2)]
        double A5 = ((1./120.) *
                            ( 5. -  (18. * tan2Lat) + tan4Lat + (n12 * (14. -  (58. * tan2Lat)))));

        //Equ #27)	A6 =  1/360 [61 - 58t2 + t4 + n12 (270 – 330t2)]
        double A6 = (1./360.) *
                            ((61. - (58. * tan2Lat) + tan4Lat + (n12 * (270. - (330. * tan2Lat)))));

        //Equ #28)	A7 = 1/5040 (61 - 479t2 + 179t4 - t6 )
        double A7 = (1./5040.) * (61. - (479. * tan2Lat) + (179. * tan4Lat) - tan6Lat);

        double L2 = L * L;

        //Equ #29)	N = S – So +No + A2L2 [1 + L2 (A4 + A6L2)]
        double northing = ( (meridionalDistanceSt - meridionalDistanceCp) +
                            (constants.getFalseNorthing())                +
                            ((A2 * L2) * ( 1. + (L2 *(A4 + (A6*L2))) )));

        //Equ #30)	E = Eo + A1 +L [1 + L2 (A3 + L2 (A5 + A7L2))]
        double termA7L2 = A7 * L2;
        double termA5A7L2 = A5 + termA7L2;
        double termL2A5A7L2 = L2 * termA5A7L2;
        double termA3L2A5A7L2 = A3 + termL2A5A7L2;
        double termL2A3L2A5A7L2 = L2* termA3L2A5A7L2;
        double falseEasting = constants.getFalseEasting();

        //Equ #30)	E = Eo + A1 +L [1 + L2 (A3 + L2 (A5 + A7L2))]
        double easting = (falseEasting + A1 * (L * (1. + termL2A3L2A5A7L2)));
                        //falseEasting + A1 +(L * (1. + (L2 * (A3 + (L2 * (A5 + (A7 * L2))))))));

        //Equn #31	C1 	=   t x (-1)
        double C1 = -tanLat;

        //Equn #32	C3 =   1/3 (1 + 3n12 + 2n4)
        double C3 =    ((1./3.) * (1. + (3.*n12) + (2. * n4)));

        //Eqn #33	C5 =   1/15 (2 – t2)
        double C5 = ((1./15.) * (2. - tan2Lat));

        //Equ #34)	γ = C1 L [1 + L2 (C3 + C5L2)]
        double convergenceAngleRad = (C1*L) * (1. + (L2*(C3 + (C5*L2))));
        double convergenceAngle = Math.toDegrees(convergenceAngleRad);

        //Equ  #35	F2 	=   ½ (1 + n12)
        double F2 = ((1./ 2.) * (1 + n12));

        //Equ #36	F4  =  1/12 [5 - 4t2 + n12 (9 – 24t2)]
        double F4 = ((1./12.) * (5 - (4 * tan2Lat) + (n12 * (9 - (24 * tan2Lat)))));

        //Equ #37)	K = ko [1 + F2L2 (1 + F4L2)]
        double ko = constants.getGridScaleFactor();
        double gridScaleFactorAtSt = (ko * (1. + ((F2 * L2)* (1. + F4*L2) )));

        super.setNorthing(northing);
        super.setEasting(easting);
        super.setScaleFactor(gridScaleFactorAtSt);
        super.setConvergenceAngle(convergenceAngle);
        super.setValidCoordinate(true);

    }

    private double rectifyingLatitude(double lat, CoordinateConstants constants){
        //Equ #14)	ωo 	= Φo + sin Φo cos Φo (Uo + U2cos2 Φo + U4cos4 Φo + U6cos6 Φo)
        //Equ #15)	ω	= Φ  + sin Φ  cos Φ  (Uo + U2cos2 Φ  + U4cos4 Φ  + U6cos6 Φ)
        double sinLat = Math.sin(lat);
        double cosLat = Math.cos(lat);
        double cos2Lat = cosLat * cosLat;
        double cos4Lat = cos2Lat * cos2Lat;
        double cos6Lat = cos2Lat * cos4Lat;

        return (lat + ((sinLat*cosLat) * (constants.getGPCuo()              +
                                         (constants.getGPCu2() * cos2Lat)   +
                                         (constants.getGPCu4() * cos4Lat)   +
                                         (constants.getGPCu6() * cos6Lat))));

    }



}
