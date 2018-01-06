package com.androidchicken.coordcenter;

/**
 * Created by Elisabeth Huhn on 5/23/2016.
 *
 * * This coordinate extends the basic capabilities of Prism4DCoordinateLL
 * and adds:
 *
 * A) returns its type from getCoordinateType as "Pris4DCoordinateWGS84"
 * B) remembers its coordinate system specific constants, e.g. Dataum
 *

 */


class CoordinateWGS84 extends CoordinateLL {

    //WGS84 Datum constants
    //Karney 2010 page 5
    //From the Ellipsoid
    //Get these constants from Utilities
    //static final double sEquatorialRadiusA = 6378137.0;      //equatorial radius in meters
    //static final double sPolarRadiusB      = 6356752.314245; //polar semi axis

    //WGS84 Constants
    //static final double sSemiMajorAxis      = 6378137.0;    // a meters
    //static final double sFlattening         = 298.257223563; // 1/f unitless


    static final String sDatum = "WGS84";



    /* ******
     *
     * Setters and Getters
     *
     **********/


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

    CoordinateWGS84(){
        initializeDefaultVariables();
    }

    CoordinateWGS84(CoordinateSPCS coordinateSPCS){

        initializeDefaultVariables();
        int zone = coordinateSPCS.getZone();
        CoordinateConstants constants = new CoordinateConstants(zone);
        int spcsZone = constants.getZone();
        if (spcsZone == (int)Utilities.ID_DOES_NOT_EXIST) {
            setValidCoordinate(false);
            return;
        }



        if (constants.getProjection() == CoordinateConstants.sLAMBERT){
            convertInverseLambert(coordinateSPCS, constants);
        } else {
            convertInverseMercator(coordinateSPCS, constants);
        }
        setValidCoordinate(true);
    }

    CoordinateWGS84(CoordinateUTM coordinateUTM){

        initializeDefaultVariables();

        setValidCoordinate(false);
    }


    CoordinateWGS84(MainActivity activity, NmeaSentence nmeaData){
        //CoordinateWGS84 wgsCoordinate = new CoordinateWGS84(nmeaData.getLatitude(),
                                                            //nmeaData.getLongitude());
        initializeDefaultVariables();
        latLongDD(nmeaData.getLatitude(), nmeaData.getLongitude());

        setElevation(nmeaData.getOrthometricElevation());
        setGeoid(nmeaData.getGeoid());

        setProjectID(Utilities.ID_DOES_NOT_EXIST);
        //setTime(System.currentTimeMillis());

        setTime(nmeaData.getTimeStamp());
    }

    CoordinateWGS84(MainActivity activity, CoordinateMean meanCoordinate){
       // CoordinateWGS84 wgs84Coordinate = new CoordinateWGS84(meanCoordinate.getLatitude(),
                                                              //meanCoordinate.getLongitude());
        initializeDefaultVariables();
        latLongDD(meanCoordinate.getLatitude(), meanCoordinate.getLongitude());


        setElevation(meanCoordinate.getElevation());
        setGeoid    (meanCoordinate.getGeoid());

        setProjectID(Utilities.ID_DOES_NOT_EXIST);
        setTime(System.currentTimeMillis());
    }

    CoordinateWGS84(double latitude, double longitude) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        latLongDD(latitude, longitude);
    }


    CoordinateWGS84(int latitudeDegree, int latitudeMinute, double latitudeSecond,
                    int longitudeDegree, int longitudeMinute, double longitudeSecond){

        //initialize all variables to their defaults
        initializeDefaultVariables();

        latLongDMS( latitudeDegree,  latitudeMinute,  latitudeSecond,
                    longitudeDegree, longitudeMinute, longitudeSecond);
    }

    CoordinateWGS84(CharSequence latitudeString, CharSequence longitudeString) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        super.latLongDDStrings(latitudeString, longitudeString);
    }




    CoordinateWGS84(MainActivity activity,
                    long   timestamp,
                    boolean isDir,
                    String latDirString,
                    String latitudeString,
                    String latitudeDegreeString,
                    String latitudeMinuteString,
                    String latitudeSecondString,
                    String lngDirString,
                    String longitudeString,
                    String longitudeDegreeString,
                    String longitudeMinuteString,
                    String longitudeSecondString,
                    String elevationString,
                    String geoidString,
                    String convergenceString,
                    String scaleString) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        setTime(timestamp);

        if (latitudeString.isEmpty()) {
            latitudeString = "0";
        }
        if (latitudeDegreeString.isEmpty()) {
            latitudeDegreeString = "0";
        }
        if (latitudeMinuteString.isEmpty()){
            latitudeMinuteString = "0";
        }
        if (latitudeSecondString.isEmpty() ){
            latitudeSecondString = "0.0";
        }


        if (longitudeString.isEmpty()){
            longitudeString = "0";
        }
        if (longitudeDegreeString.isEmpty()){
            longitudeDegreeString = "0";
        }
        if (longitudeMinuteString.isEmpty() ){
            longitudeMinuteString = "0";
        }
        if (longitudeSecondString.isEmpty()) {
            longitudeSecondString = "0.0";
        }

        this.mLatitude  = Double.parseDouble(latitudeString);
        double latDMS = Utilities.getDecimalDegrees( Integer.valueOf   (latitudeDegreeString),
                                                     Integer.valueOf   (latitudeMinuteString),
                                                     Double.parseDouble(latitudeSecondString));

        this.mLongitude = Double.parseDouble(longitudeString);
        double lngDMS = Utilities.getDecimalDegrees(Integer.valueOf   (longitudeDegreeString),
                                                    Integer.valueOf   (longitudeMinuteString),
                                                    Double.parseDouble(longitudeSecondString));

        if (mLatitude != latDMS){
            if (mLatitude == 0.)mLatitude = latDMS;
        }
        if (mLongitude != lngDMS){
            if (mLongitude == 0.)mLongitude = lngDMS;
        }

        if (isDir){
            //set sign of lat & lng by directional strings
            //Latitude
            if (latDirString.equals("S")){
                //neg
                if (mLatitude > 0.)     mLatitude        = 0. - mLatitude;
            } else {
                //everything else gives a positive number
                mLatitude       = Math.abs(mLatitude);
            }
            //Longitude
            if (lngDirString.equals("W")){
                //neg
                if (mLongitude > 0.)     mLongitude        = 0. - mLongitude;
            } else {
                //everything else gives a positive number
                mLongitude       = Math.abs(mLongitude);
            }
        }

        boolean latReturnCode = true;
        boolean lngReturnCode = true;

        if ((mLatitude != 0.) && (mLongitude != 0.)){
            latReturnCode = latLongDD(mLatitude, mLongitude);
        } else {
            //everything is zero. Special case, but must handle it
            latReturnCode = latLongDD(mLatitude, mLongitude);
        }
        setValidCoordinate(latReturnCode && lngReturnCode);

        if (!isValidCoordinate())return;

        int distUnits = CCSettings.getDistUnits(activity);

        setElevation(getMeters(elevationString, distUnits));
        setGeoid    (getMeters(geoidString    , distUnits));

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


    CoordinateWGS84(CharSequence latitudeDegreeString,
                    CharSequence latitudeMinuteString,
                    CharSequence latitudeSecondString,
                    CharSequence longitudeDegreeString,
                    CharSequence longitudeMinuteString,
                    CharSequence longitudeSecondString) {

        //initialize all variables to their defaults
        initializeDefaultVariables();

        latLongDMSStrings(latitudeDegreeString,
                          latitudeMinuteString,
                          latitudeSecondString,
                          longitudeDegreeString,
                          longitudeMinuteString,
                          longitudeSecondString);
    }







    /* ******
     *
     * Setters and Getters
     *
     **********/


    /* ******
     *
     * Static methods
     *
     **********/


    /* ******
     *
     * Member methods
     *
     **********/


    protected void initializeDefaultVariables(){
        //set all variables with defaults, so that none are null
        //I know that one does not have to initialize int's etc, but
        //to be explicit about the initialization, do it anyway

        //initialize all variables common to EN coordinates
        super.initializeDefaultVariables();

        setCoordinateDBType(Coordinate.sCoordinateDBTypeWGS84);

    }


    private void convertInverseLambert(CoordinateSPCS coordinateSPCS,
                                       CoordinateConstants constants){

        //Northing and Easting parameteres are in meters

        //Equ A) 	N1 	=  N – No
        double N1  = coordinateSPCS.getNorthing() - constants.getFalseNorthing2();

        //Equ B) 	E1	=  E – Eo
        double E1  = coordinateSPCS.getEasting()  - constants.getFalseEasting();

        //Equ C) 	R1   	=  Ro – N1
        double R1  = constants.getRoMappingRadiusAtLat() - N1;

        double radER = E1 / R1;//Math.toRadians(E1 / R1);

        //Equ D) 	Y 	=  tan-1 (E1/R1)
        double convergenceAngleRad = Math.atan(radER);
        //As calculated the convergence angle is in radians, it must be converted to degrees
        double convergenceAngle = Math.toDegrees(convergenceAngleRad);

        double Bo    = constants.getCentralParallel();
        double radBo = Math.toRadians(Bo);
        double sinBo = Math.sin(radBo);
        double centralMeridian = constants.getCentralMeridian();
        double radCM = Math.toRadians(centralMeridian);

        //Equ E) 	λ 	=  λo (Lo) – Y/sin Bo
        //All terms are in radians
        double longitudeRad = radCM - (convergenceAngleRad / sinBo);
        //Convert radians to degrees
        double longitude  = Math.toDegrees(longitudeRad);

        double tanCA = Math.tan((convergenceAngleRad / 2.));

        //Equ F) 	u	=  N1 – E1 tan (Y/2)
        double u = N1 - (E1 * tanCA);
        double u2 = u*u;
        double u3 = u*u2;

        double G1 = constants.getG1();
        double G2 = constants.getG2();
        double G3 = constants.getG3();
        double G4 = constants.getG4();
        double G5 = constants.getG5();


        //Equ G) 	Δ Φ 	=  u[G1 + uG2 + uG3 + uG4 +G5u]
        //deltaLatitude in degrees
        double deltaLat = (G1*u) + (G2*u2) + (G3 *u2) + (G4*u2) + (G5*u2);

        //Equ H) 	Φ 	=  Bo + Δ Φ
        double latitude = constants.getCentralParallel() + deltaLat;

        double F1 = constants.getF1();
        double F2 = constants.getF2();
        double F3 = constants.getF3();

        //Equ I) 	k 	=   F1  +  F2u2  +  F3u3
        double scaleFactor = F1 + (F2 * u2) + (F3 * u3);


        super.setLatitude(latitude);
        super.setLongitude(longitude);
        super.setScaleFactor(scaleFactor);
        super.setConvergenceAngle(convergenceAngle);

        super.setValidCoordinate(true);

    }



    private void convertInverseMercator(CoordinateSPCS coordinateSPCS,
                                       CoordinateConstants constants){

        //Rectifying Latitude at Station w
        //Equ # AA   ω  =   (N – No + So) / (ko x r)
        double N  = coordinateSPCS.getNorthing(); //meters
        double No = constants.getFalseNorthing2();
        double So = constants.getMeridionalDistance();
        double ko = constants.getGridScaleFactor();
        double r  = constants.getRadiusOfRectifyingSphere();
        double w = ( (N - No + So) / (ko * r));

        //footprint latitude
        //Equ  #BB  Φf  =  ω + (sin ω x cos ω) x (Vo + V2 x cos2 ω + V4cos4 ω + V6Cos6 ω
        double sinw  = Math.sin(w);
        double cosw  = Math.cos(w);
        double cos2w = cosw * cosw;
        double cos4w = cos2w * cos2w;
        double cos6w = cos2w * cos4w;
        double Vo    = constants.getZGCvo();
        double V2    = constants.getZGCv2();
        double V4    = constants.getZGCv4();
        double V6    = constants.getZGCv6();
        double footprintLat = (w + ((sinw * cosw) * (Vo + ((V2 * cos2w) + (V4 * cos4w) + (V6 * cos6w)))));


        //Footprint Radius
        //Equ  #CC   Rf  =  ko x a / (1 – e2 sin2 Φf ) 1/2
        double a = constants.getSemiMajorAxis();
        double e2 = constants.getEccentricity2();
        double sin2Of = Math.sin(footprintLat);
               sin2Of = sin2Of * sin2Of;
        double cosOf = Math.cos(footprintLat);
        double tanOf = Math.tan(footprintLat);
        double tan2Of = tanOf * tanOf;
        double tan4Of = tan2Of * tan2Of;
        double tan6Of = tan4Of * tan2Of;

        double denominator = Math.sqrt(1. - (e2 * sin2Of));
        double footprintRadius = ((ko * a) / denominator);


        //E prime or Easting Departure
        //Equ #DD  E1  =  E - Eo
        double easting = coordinateSPCS.getEasting();
        double Eo      = constants.getFalseEasting();
        double E1      = easting - Eo;


        //isometric latitude in radians
        //Equ #EE =   Qf   =  E1 / Rf
        double Qf = E1 / footprintRadius;
        double Qf2 = Qf * Qf;

        //second eccentricity squared
        //Equ #FF  e12  =  e2 / (1 - e2)
        double e12 = (e2 / (1. - e2));

        //second flattening factor
        //Equ #GG nf12	= e12 x cos Φf
        double nf12 = e12 * cosOf;
        double nf4 = nf12 * nf12;

        //Second Flattening Factors

        // Equ  #HH  B2  =  - 1/2 tf x (1 + nf12)
        double B2 = ((-1./2.) * (tanOf * (1. + nf12)));

        // EQUATION #LL  B3 = - 1/6 (1 + 2tf2  +  nf12)
        double B3 = ((-1./6.) * (1. + (2 * tan2Of) + nf12));

        //Equ #II  B4  =  - 1/12 [5 + 3tf2  + nf12  (1 – 9tf2) - 4 nf4 ]
        double B4 = ((-1./12.) * (5. + (3. * tan2Of) +
                                                (nf12 * (1. - (9. * tan2Of) - (4 * nf4)))));

        // EQUATION #MM  B5  =  1/120 [5 + 28tf2  + 24tf4  +  nf12  (6 + 8tf2) ]
        double B5 = ((1./120.) * (5. + (28. * tan2Of) + (24. * tan4Of) +
                                                (nf12 * (6. + (8 * tan2Of)))));

        //Equ  #JJ B6  =  1/360 [61 + 90tf2 + 45nf4  + nf12 (46 – 252tf2) - 90 tf4)]
        double B6 = ((1./360.) * (61. + (90. * tan2Of) ));
        //Note that most of the higher order terms are dropped from the equation per RM
                //+ (45. * nf4) + (nf12 * (46. - (252. * tan2Of) - (90. * tan4Of)))));


        // EQUATION #NN  B7   =   - 1/5040 (61 + 662tf2  + 1320tf4  + 720tf6)
        double B7 = ((-1./5040.) * (61. + (662. * tan2Of) + (1320. * tan4Of) + (720. * tan6Of)));


        //Latitude at station
        //EQUATION #KK  Φ  =  Φf   +  B2Q2 [1 + Q2 (B4 + B6Q2)]
        double latitudeRad = footprintLat + ((B2 * Qf2) * (1. + (Qf2 * (B4 + (B6 * Qf2)))));

        double latitude = Math.toDegrees(latitudeRad);


        //Departure Latitude
        // EQUATION #OO   L  =  Qf x [1 + Qf2 (B3 + Qf2 x (B5 + B7Qf2))]
        double L = Qf * (1 + (Qf2 * (B3 + (Qf2 * (B5 + (B7 * Qf2))))));

        //Longitude at Station
        //EQUATION #PP λ = λo  -  (L / cos Φf )
        double Lo = constants.getCentralMeridian();
        double LoRad = Math.toRadians(Lo);
        double term2 = L / cosOf;
        double longitudeAtStationRad = LoRad - (L / cosOf);
        double longitude = Math.toDegrees(longitudeAtStationRad);


        //More 2nd Flattening Factors
        //EQUATION #QQ   D1  =   tf
        double D1 = tanOf;

        //EQUATION #RR D3  = - 1/3 (1 + tf2  - nf12  - 2 nf4)
        double D3 = ((-1./3.) * (1 + tan2Of - nf12 - (2 * nf4)));


        //EQUATION #SS D5   =   1/15 (2 + 5tf2  + 3tf4 )
        double D5 = ((1./15.) * (2. + (5. * tan2Of) + (3. * tan4Of)));



        //EQUATION #TT  γ  =  D1 Q x [1 + Q2 (D3 + D5 Q2)]
        double convergenceAngleRad = ((D1 * Qf) * (1. + (Qf2 * (D3 + (D5 * Qf2)) )));
        double convergenceAngle = Math.toDegrees(convergenceAngleRad);


        //EQUATION #UU  G2  =  1/2 (1 + nf12)
        double G2 = ((1./2.) * (1 + nf12));



        //EQUATION #VV  G4  =  1/12 (1 + 5nf12)
        double G4 = ((1./12.) * (1 + (5 * nf12)));


        //EQUATION #WW  k =  ko  x [1 + G2Q2 (1 + G4Q2)]
        double scaleFactor = ko * (1 + ((G2 * Qf2) * (1 + (G4 * Qf2))));


        super.setLatitude(latitude);
        super.setLongitude(longitude);
        super.setScaleFactor(scaleFactor);
        super.setConvergenceAngle(convergenceAngle);

        setValidCoordinate(true);


    }


}
