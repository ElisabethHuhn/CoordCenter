package com.androidchicken.coordcenter;

/**
 * Created by Elisabeth Huhn on 5/25/2016.
 * This coordinate extends the basic capabilities of Prism4DCoordinateEN
 * and adds:
 *
 * A) returns its type from getCoordinateType as "Pris4DCoordinateUTM"
 * B) remembers its coordinate system specific constants, e.g. Dataum
 *
 */

class CoordinateUTM extends CoordinateEN {


    private static final String sDatum = "UTM";

    private   char         mHemisphere;  //N or S
    private   char         mLatBand;


    /* ******
     *
     * Constructors
     *
     **********/


    CoordinateUTM(){super.initializeDefaultVariables();}

    CoordinateUTM(MainActivity activity,
                  String zoneString,
                  String latbandString,
                  String hemisphereString,
                  String eastingString,
                  String northingString,
                  String elevationString,
                  String geoidString,
                  String convergenceString,
                  String scaleString){

        int distUnits = CCSettings.getDistUnits(activity);

        int zone = Integer.valueOf(zoneString);
        if ((zone < 1) || (zone > 60)){
            setValidCoordinate(false);
            return;
        }
        setLatBand(latbandString.charAt(0));
        setZone(zone);
        char hemi = hemisphereString.charAt(0);
        if (hemi == 'n')hemi = 'N';
        if (hemi == 's')hemi = 'S';
        if (!((hemi == 'N') || (hemi == 'S'))){
            setValidCoordinate(false);
            return;
        }
        setHemisphere(hemi);

        setEasting  (getMeters(eastingString,   distUnits));
        setNorthing (getMeters(northingString,  distUnits));
        setElevation(getMeters(elevationString, distUnits));
        setGeoid    (getMeters(geoidString,     distUnits));

        setConvergenceAngle(Double.valueOf(convergenceString));
        setScaleFactor(Double.valueOf(scaleString));

        setValidCoordinate(true);
    }

    CoordinateUTM(CoordinateWGS84 coordinate) {
        //initialize all variables to their defaults
        super.initializeDefaultVariables();
        convertWGStoUTM(coordinate.getLatitude(), coordinate.getLongitude());
        setDatum( sDatum); //eg WGS84
        setElevation(coordinate.getElevation());
        setGeoid(coordinate.getGeoid());
    }




    /* ******
     *
     * Setters and Getters
     *
     **********/

    char   getLatBand()     { return mLatBand;  }
    void   setLatBand(char latBand)       { mLatBand = latBand; }

    char   getHemisphere()  { return mHemisphere; }
    void   setHemisphere(char hemisphere) { mHemisphere = hemisphere;}


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

        setCoordinateDBType( Coordinate.sCoordinateDBTypeUTM);
        setDatum(sDatum); //eg WGS84

    }

    public String toString() {
        return String.format("%s %c %c %s %s", mZone, mHemisphere, mLatBand, mEasting, mNorthing);
    }


    //+**************************************************
    //+*****        Conversion Routines             *****
    //+**************************************************
    private void convertWGStoUTM (double lat, double longi)  {
        setWgsConstants(); //use the WGS constants for the conversion
        convertLLtoUTM(lat, longi);
    }


    private void setWgsConstants() {
        //mEquatorialRadiusA = CoordinateWGS84.sEquatorialRadiusA;
        //mPolarRadiusB      = CoordinateWGS84.sPolarRadiusB;
    }


    /**
     * Converts latitude/longitude to UTM coordinate.
     *
     * Implements Karney’s method, using Krüger series to order n^6,
     * giving results accurate to 5nm for
     * distances up to 3900km from the central meridian.
     *
     * This uses WGS constants, and that's the only reason it's tied to WGS
     *
     *  * @constructor
     *   {int}    zone       - UTM 6° longitudinal zone (1..60 covering 180°W..180°E).
     *   {string} hemisphere - N for northern hemisphere, S for southern hemisphere.
     *   {number} easting    - Easting in metres from false easting (-500km from central meridian).
     *   {number} northing   - Northing in metres from equator (N) or
     *                               from false northing -10,000km (S).
     *  Datum UTM coordinate is based on WGS84.
     *   {number} [convergence] - Meridian convergence
     *                                (bearing of grid north clockwise from true north), in degrees
     *   {number} [scale] - Grid scale factor
     *
     *  {}  Invalid Argument Exception when Lat/Long not numbers or not within range

     ***/
    private void convertLLtoUTM(double lat, double longi) {

        //Assert that the input parameters are valid (ie are actually numbers)
        if (Double.isNaN(lat) || Double.isNaN(longi)){
            setValidCoordinate(false);
            return;
        }
        // and within range
        //lat must be larger than -80 and smaller than 84 as UTM does not span the entire globe
        if ((lat < -80.0)|| (lat > 84.) ){
            setValidCoordinate(false);
            return;
        }
        //Legal range of the longitude is -180 to +180
        if ((longi <-180.) || (longi > 180.)){
            setValidCoordinate(false);
            return;
        }
        //Longitude -180/+180 is the International Date Line
        // Date Line -180  through the Americas to 0 Greenwich
        // through Europe to 180 date line

        //longitude which is between -180 and -0
        // <0 (or negative) is (in Asian hemisphere)
        // between international date line and Greenwich
        // zones 31 to 60

        //longitudes between +0 and +180
        // > 0 (or positive) are in American hemisphere
        // between Greenwich and International Date Line
        // zones 1 to 30


        // TODO: 8/16/2017 Constants need to come from CoordinateConstants
        //Karney:
        //Consider an ellipsoid of revolution with:
        // a = equatorial radius
        // b = polar semi axis, i.e. polar radius
        // f = flattening = (a - b) / a
        // e = eccentricity =  sqrt[f * (2.-f)]
        // n = third flattening = (a-b)/(a+b) = f / (2.-f)

        //WGS84 Datum constants
        //Karney 2010 page 5
        //From the Ellipsoid
        //double equatorialRadiusA = 6378137.0; //equatorial radius in meters
        //double polarRadiusB = 6356752.314245; //polar semi axis

        //flattening = (equatorialRadius-polarRadius)/equatorialRadius;
        //double a = Utilities.sEquatorialRadiusA; //equatorialRadiusA;
        //double b = Utilities.sPolarRadiusB;      //polarRadiusB;

        //flatteningF = (equatorialRadiusA-polarRadiusB)/equatorialRadiusA;
        //flatteningF = 1/298.257223563; //0.0033528107 WGS numbers
        //flatteningF = (a-b)/a;
        // TODO: 8/16/2017 Constants need to come from CoordinateConstants
        double flatteningF  = Utilities.sFlattening;
        //mean radius = sqrt (equatorialRadius * polarRadius)

   /* ***************************************************/
    /* ***    Constants and intermediate results    ******/
    /* ***************************************************/
        //Conversion Constants
        //Defined globally in the Utilities
        //protected double Utilities.sEquatorialRadiusA = 6378137.0;
        //protected double Utilities.sPolarRadiusB      = 6356752.314245; //polar semi axis;

        //protected double a = Utilities.sEquatorialRadiusA; //equatorialRadiusA;
        //private   double b = Utilities.sPolarRadiusB;      //polarRadiusB;

        //double flatteningF = (equatorialRadiusA-polarRadiusB)/equatorialRadiusA;
        //double flatteningF = 1/298.257223563; //0.0033528107 WGS numbers
        //double flatteningF = 1/298.257223563; //0.0033528107 WGS numbers

        //mean radius = sqrt (equatorialRadius * polarRadius)



          //protected double ee ;













        // eccentricity  (.0066943801)1/2 = 0.0818191915
        double e = Math.sqrt(flatteningF *(2.- flatteningF));
        //double ee = Math.sqrt(1. - Math.pow((b/a),2.));



        // *************** calculate the alpha array ************************** //
        //prepare the array for Karney (12) and (35)
        // WGS84 n = .0033528107/1.9966471893 = .0016792204
        double n = flatteningF / (2. - flatteningF); // 3rd flattening
        //double n = (equatorialRadiusA - polarRadiusB)/ (equatorialRadiusA+polarRadiusB);

        //Karney (12) takes it to n4, (35) to n8

        //alpha α is one-based array (6th order Krüger expressions)
        //The number of terms used in the series calculations below
        double n2 = n*n; //.0000028197
        double n3 = n*n2;//.0000000047
        double n4 = n*n3;
        double n5 = n*n4;
        double n6 = n*n5;
        //double n7 = n*n6; //6 terms are sufficient. Accuracy lost in 7 and 8
        //double n8 = n*n7;


        double a1dot1 = (1.0/2.0)*n;
        double a1dot2 = (2.0/3.0)*n2;
        double a1dot3 = (5.0/16.0)*n3;
        double a1dot4 = (41.0/180.0)*n4;
        double a1dot5 = (127.0/288.0)*n5;
        double a1dot6 = (7891.0/37800.0)*n6;
        //double a1dot7 = (72161/387072)*n7;
        //double a1dot8 = (18975107/50803200)*n8;


        double a2dot2 = (13.0/48.0)*n2;
        double a2dot3 = (3.0/5.0)    *n3;
        double a2dot4 = (557.0/1440.0) *n4;
        double a2dot5 = (281.0/630.0)     *n5;
        double a2dot6 = (1983433.0/1935360.0)*n6;
        //double a2dot7 = (13769.0/28800.0)      *n7;
        //double a2dot8 = (148003883.0/174182400.0)*n8;


        double a3dot3 = (61.0/240.0)*n3;
        double a3dot4 = (103.0/140.0)*n4;
        double a3dot5 = (15061.0/26880.0)*n5;
        double a3dot6 = (167603.0/181440.0)*n6;
        //double a3dot7 = (67102379.0/29030400.0)*n7;
        //double a3dot8 = (79682431.0/79833600.0) *n8;


        double a4dot4 = (49561.0/161280.0)*n4;
        double a4dot5 = (179.0/168.0)*n5;
        double a4dot6 = (6601661.0/7257600.0)*n6;
        //double a4dot7 = (97445.0/49896.00)*n7;
        //double a4dot8 = (40176129013.0/7664025600.0)*n8; //does this need to be truncated?


        double a5dot5 = (34729.0/80640.0)*n5;
        double a5dot6 = (3418889.0/1995840.0)*n6;
        //double a5dot7 = (14644087.0/9123840.0)*n7;
        //double a5dot8 = (2605413599.0/622702080.0)*n8;


        double a6dot6 = (212378941.0/319334400.0)*n6;
        //double a6dot7 = (30705481.0/10378368.0)*n7;
        //double a6dot8 = (175214326799.0/58118860800.0)*n8;


        //double a7dot7 = (1522256789.0/1383782400.0)*n7;
        //double a7dot8 = (16759934899.0/3113510400.0)*n8;


        //double a8dot8 = (1424729850961.0/743921418240.0)*n8; //Horner form

        //Karney (12) takes it to n4, (35) to n8
        double alpha[] = {a1dot1 - a1dot2 + a1dot3 + a1dot4 - a1dot5 + a1dot6, //alpha[0]
                a2dot2 - a2dot3 + a2dot4 + a2dot5 - a2dot6,//alpha[1]
                a3dot3 - a3dot4 + a3dot5 + a3dot6,//alpha[2]
                a4dot4 - a4dot5 + a4dot6,//alpha[3]
                a5dot5 - a5dot6,//alpha[4]
                a6dot6};//alpha5]

        // *************** calculate the alpha array ************************** //





        //scale the result to give the transverse Mercator easting and northing
        // UTM scale on the central meridian
        double K0 = 0.9996;



        // To avoid negative numbers, ‘false eastings’ and ‘false northings’ are used:
        //Eastings are referenced in meters from the central meridian of each zone,
        //Eastings are measured from 500,000 metres west of the central meridian
        //Eastings (at the equator) range from 166,021m to 833,978m
        // (the range decreases moving away from the equator);
        // a point on the the central meridian has the value 500,000m.
        double falseEasting = 500e3;




        //In the northern hemisphere, northings are measured in meters from the equator –
        // ranging from 0 at the equator to 9,329,005m at 84°N).
        //In the southern hemisphere they are measured from 10,000,000 metres
        // south of the equator (close to the pole) –
        // ranging from 1,116,915m at 80°S to 10,000,000m at the equator.
        double falseNorthing = 10000e3;

        // phi    φ = latitude
        // lamda  λ = longitude
        //
        //phi φ is Latitude in radians ( +/- from equator)
        // degrees = radians * (360. / 2. pi)
        // radians = degrees * (2. pi / 360.)
        double latRad = lat * Math.PI/180.;

        //Hemisphere depends upon latitude
        //lat ranges from -80 Southern Hemisphere to equator
        // and from the equator to 84 Northern Hemisphere
        if (lat >= 0.) {
            mHemisphere = 'N';
        } else {
            mHemisphere = 'S';
        }




        //Calculate the longitude zone
        //  then the longitude of the central meridian of that zone
        //

        //Each Zone is each 6° of longitude in width
        //So there are 60 zones, numbered 1-60
        // Zone 1 is longitude 0-5 degrees (and all of the minutes and seconds in 5 degrees),
        // Zone 2 is longitude 6-11 degrees,
        // etc
        //

        //floor gives the largest integer that is less than or equal to the argument
        // remember, input longitude ranges from -180 to 0 to +180, so
        // start by expressing longitude from 0 to 360
        //Each Zone is each 6° of longitude in width
        //zone ranges from 1 to 60 (not 0 to 59, hence the +1)
        mZone = (int) Math.floor((longi+180.)/6.) + 1;

        //Calculate the longitude of central meridian
        //Each zone is 6 degrees wide, so the central meridian is 3 degrees inside
        //longitude runs from -180 to +180, so we need to convert to 0 to 359
        //Lamda zero is what Karney calls this number
        double zoneCentralMeridanRad = ((6.*mZone) - 180. - 3.);
        zoneCentralMeridanRad = zoneCentralMeridanRad* Math.PI/180.;


        // ---- handle Norway/Svalbard exceptions
        // MGRS grid zones are 8° tall; 0°N is offset 10 into latitude bands array

        CharSequence mgrsLatBands = "CDEFGHJKLMNPQRSTUVWXX"; // X is repeated for 80.-84.°N
        int i = (int) Math.floor(lat/8.)+10;
        char latBand = mgrsLatBands.charAt(i);

        mLatBand = latBand; //Set the object variable on this

        // degrees = radians * (360. / 2. pi)
        // radians = degrees * (2. PI / 360.)
        double sixRadians = 6. * (Math.PI / 180.);
        // adjust zone & central meridian for Norway
        if (mZone==31 && latBand=='V' && longi>= 3.) { mZone++; zoneCentralMeridanRad += sixRadians; }
        // adjust zone & central meridian for Svalbard
        if (mZone==32 && latBand=='X' && longi<  9.) { mZone--; zoneCentralMeridanRad -= sixRadians; }
        if (mZone==32 && latBand=='X' && longi>= 9.) { mZone++; zoneCentralMeridanRad += sixRadians; }
        if (mZone==34 && latBand=='X' && longi< 21.) { mZone--; zoneCentralMeridanRad -= sixRadians; }
        if (mZone==34 && latBand=='X' && longi>=21.) { mZone++; zoneCentralMeridanRad += sixRadians; }
        if (mZone==36 && latBand=='X' && longi< 33.) { mZone--; zoneCentralMeridanRad -= sixRadians; }
        if (mZone==36 && latBand=='X' && longi>=33.) { mZone++; zoneCentralMeridanRad += sixRadians; }


        //lamda  λ is longitude in radians ( +/- from central meridian)
        double longRad = (longi * Math.PI/180.) - zoneCentralMeridanRad;



        // ---- easting, northing: Karney 2011 Eq 7-14, 29, 35:


        //lamda  λ is longitude in radians
        double cosLongRad = Math.cos(longRad);
        double sinLongRad = Math.sin(longRad);
        double tanlongRad = Math.tan(longRad);


        //tau is the tangent of the latitude in radians
        // τ ≡ tanφ, τʹ ≡ tanφʹ; prime (ʹ) indicates angles on the conformal sphere
        //var τ = Math.tan(φ);
        double tau = Math.tan(latRad); //Karney (8) page 2

        // τ = tanLatRad; //Karney (8)

        //Karney (9)
        //var σ = Math.sinh(e*Math.atanh(e*τ/Math.sqrt(1+τ*τ)));

        //* ******************************
        //Some functions not in the vanilla Math package
        //arctanh(z)
        // atanh function definition is: tanh−1(z) = ½ log((1+z) /(1-z))
        //       or equivalently:        arctanh z = (log (1+z) - log (1-z))/2.


        //* *****************************************************

        double atanhArg = (e * tau) / Math.sqrt(1. + (tau * tau));

        //atanhArg must be within -1 to 1
        if ((atanhArg < -1. ) || (atanhArg > 1.)){
            setValidCoordinate(false);
            return;
        }

        double atanh = 0.5 * Math.log((1.+atanhArg)/(1.-atanhArg));

        //assure that we are calculating arctanh correctly
        //double atanhprime = .5 * (Math.log(1.+atanhArg) - Math.log(1.-atanhArg));

        /* *  It is close enough to the tenth decimal place
         if ((atanh - atanhprime) > .0000000001) {
            setValidCoordinate(false);
            return
         }
         ***/
        double sigma = Math.sinh( e * atanh ) ; //Karney (9)


        //tau prime τʹ  Karney (7)
        //var τʹ = τ*Math.sqrt(1+σ*σ) - σ*Math.sqrt(1+τ*τ);
        //Karney (7)
        double tauPrime = (tau * Math.sqrt(1.+(sigma*sigma))) - (sigma* Math.sqrt(1.+tau*tau));

        //xi prime ξʹ
        //var ξʹ = Math.atan2(τʹ, cosλ);
        //Karney (10)
        //tan-1(tauPrime/cosLamda)    lamda is longitude
        double xiPrime = Math.atan2(tauPrime, cosLongRad);

        //eta prime  ηʹ
        //var ηʹ = Math.asinh(sinλ / Math.sqrt(τʹ*τʹ + cosλ*cosλ));
        //Karney (10)
        //* **********************************
        //asinh function is also missing from vanilla Math package
        //sinh−1(z) = log[z+(z2 + 1)½ ].

        //so assign termz = (z2 + 1)½
        //asinh (z) = log ( z + termz)


        //var ηʹ = Math.asinh(sinλ / Math.sqrt(τʹ*τʹ + cosλ*cosλ));
        double etaDenominator = Math.sqrt((tauPrime * tauPrime) + (cosLongRad * cosLongRad));
        double z = sinLongRad / etaDenominator;

        double termz = Math.sqrt((z * z) + 1.);
        double etaPrime = Math.log(z + termz);



        //xi ξ = ξʹ
        //build xi with a series of 6 members
        //the multiplying by (j+1) instead of (j) as in the paper
        // is because we are counting 0 to 5, not 1 - 6
        //Karney (11)

        double xi =  xiPrime;
        double jj;
        int position = 0;
        int last = alpha.length;
        while (position < last){
            jj=(double)position+1;
            xi = xi + (alpha[position] * Math.sin(2.*jj*xiPrime) * Math.cosh(2.*jj*etaPrime));
            position++;
        }



        //eta η = ηʹ
        //build eta with a series of 6 members
        //Karney (11)
        double eta = etaPrime;
        position = 0;
        while (position < last){
            jj = (double)position + 1;
            eta += alpha[position] * Math.cos(2.*jj*xiPrime) * Math.sinh(2.*jj*etaPrime);
            position++;
        }



        // 2πA is the circumference of a meridian
        //Karney (14) and (29)
        // TODO: 1/5/2018 this should come from the CoordinateConstants
        double A = (Utilities.sEquatorialRadiusA/(1.+n)) *
                            (1. + (1./4.)*n2 + (1./64.)*n4 + (1./256.)*n6);// + (25/16384)*n8);




        //Karney (13)
        //Karney's x is mEasting and y is mNorthing
        mEasting  = K0 * A * eta;
        mNorthing = K0 * A * xi;



        // ---- convergence: Karney 2011 Eq 23, 24

        //Karney (23)
        //rho pʹ prime
        double rhoPrime = 1.;
        position = 0;
        while (position < last){
            jj = (double)position + 1;
            rhoPrime += (2.*jj*alpha[position]) * Math.cos(2.*jj*xiPrime) * Math.cosh(2.*jj*etaPrime);
            position++;
        }

        //Karney (23)
        //q prime q'
        double qʹ = 0.;
        position = 0;
        while (position < last){
            jj = (double)position + 1;
            qʹ += (2.*jj*alpha[position]) * Math.sin(2.*jj*xiPrime) * Math.sinh(2.*jj*etaPrime);
            position++;
        }

        //Meridian convergence
        //  bearing of grid north, the y axis, measured clockwise from true north
        // nu = nuPrime + nuDoublePrime   //KARNEY paragraph between (23) and (24)

        //nu prime γʹ
        double nuPrime = Math.atan((tauPrime/ Math.sqrt(1.+tauPrime*tauPrime)) * tanlongRad);
        //nu double prime γʺ
        double nuDoublePrime = Math.atan2(qʹ, rhoPrime);
        //nu γ
        double nu = nuPrime + nuDoublePrime;



        // ---- scale: Karney 2011 Eq (25)

        //φ is latitude,  sinφ is sin of latitude (in radians)
        double sinLat = Math.sin(latRad);

        double latDenominator = Math.sqrt(tauPrime*tauPrime + cosLongRad*cosLongRad);
        //τ is tanLatRad

        //kʹ kappaPrime
        double kappaPrime = (Math.sqrt(1. - e*e*sinLat*sinLat) * Math.sqrt(1. + tau*tau)) / latDenominator;
        // TODO: 1/5/2018 Constant should come from Coordinates Constants, not utilities
        double kappaDoublePrime = (A / (Utilities.sEquatorialRadiusA)) *
                Math.sqrt(rhoPrime*rhoPrime + qʹ*qʹ);

        //kappa k
        //Scale [karney paragraph between (24) and (25)
        double kappa = K0 * kappaPrime * kappaDoublePrime;

        // ------------

        // To avoid negative numbers, ‘false eastings’ and ‘false northings’ are used:

        // shift easting/northing to false origins
        mEasting = mEasting + falseEasting;    // make easting relative to false easting


        // make northing in southern hemisphere relative to false northing
        if (mNorthing < 0.){
            mNorthing = mNorthing + falseNorthing;
        }

        //double x = mEasting;
        //double y = mNorthing;

        setValidCoordinate(true);

    }

    /**
     * Converts UTM coordinate to latitude/longitude.
     *
     * Implements Karney’s method, using Krüger series to order n^6,
     *
     *  {}  Invalid Argument Exception when easting/northing not numbers or not within range

     ***/
    private void convertUTMtoLL(double northing, double easting) {

        // *************** calculate the alpha array ************************** //
        //prepare the array for Karney (12) and (35)
        // WGS84 n = .0033528107/1.9966471893 = .0016792204
        double n = .0016792204;
        //double n = flatteningF / (2. - flatteningF); // 3rd flattening
        //double n = (equatorialRadiusA - polarRadiusB)/ (equatorialRadiusA+polarRadiusB);

        //Karney (12) takes it to n4, (35) to n8

        //alpha α is one-based array (6th order Krüger expressions)
        //The number of terms used in the series calculations below
        double n2 = n*n; //.0000028197
        double n3 = n*n2;//.0000000047
        double n4 = n*n3;
        double n5 = n*n4;
        double n6 = n*n5;
        //double n7 = n*n6; //6 terms are sufficient. Accuracy lost in 7 and 8
        //double n8 = n*n7;


        double b1dot1 = (1.0/2.0)*n;
        double b1dot2 = (2.0/3.0)*n2;
        double b1dot3 = (37.0/96.0)*n3;
        double b1dot4 = (1.0/360.0)*n4;
        double b1dot5 = (81.0/512.0)*n5;
        double b1dot6 = (96199.0/604800.0)*n6;
        //double b1dot7 = (5406467.0/38707200.0)*n7;
        //double b1dot8 = (7944359.0/67737600)*n8;


        double b2dot2 = (1.0/48.0)*n2;
        double b2dot3 = (1.0/15.0)    *n3;
        double b2dot4 = (437.0/1440.0) *n4;
        double b2dot5 = (46.0/105.0)     *n5;
        double b2dot6 = (1118711.0/3870720.0)*n6;
        //double b2dot7 = (51841.0/1209600.0)      *n7;
        //double b2dot8 = (24749483.0/348364800.0)*n8;


        double b3dot3 = (17.0/480.0)*n3;
        double b3dot4 = (37.0/840.0)*n4;
        double b3dot5 = (209.0/4480.0)*n5;
        double b3dot6 = (5569.0/90720.0)*n6;
        //double b3dot7 = (9261899.0/58060800.0)*n7;
        //double b3dot8 = (6457463.0.0/11740800.0.0) *n8;


        double b4dot4 = (4397.0/161280.0)*n4;
        double b4dot5 = (11.0/504.0)*n5;
        double b4dot6 = (830251.0/7257600.0)*n6;
        //double b4dot7 = (466511.0.0/2494800.0)*n7;
        //double b4dot8 = (324154477.0/7664025600.0)*n8; //does this need to be truncated?


        double b5dot5 = (4583.0/161280.0)*n5;
        double b5dot6 = (108847.0/3991680.0)*n6;
        //double b5dot7 = (8005831.0/63866880.0)*n7;
        //double b5dot8 = (22894433.0/124540416.0)*n8;


        double b6dot6 = (20648693.0/638668800.0)*n6;
        //double b6dot7 = (16363163.0/518918400.0)*n7;
        //double b6dot8 = (2204645983.0/12915302400.0)*n8;


        //double b7dot7 = (219941297.0/5535129600.0)*n7;
        //double b7dot8 = (497323811.0.0/12454041600.0)*n8;


        //double b8dot8 = (191773887257.0/3719607091200.0)*n8; //Horner form

        //Karney (12) takes it to n4, (35) to n8
        double beta[] = {b1dot1 - b1dot2 + b1dot3 - b1dot4 - b1dot5 + b1dot6, //beta[0]
                b2dot2 + b2dot3 - b2dot4 + b2dot5 - b2dot6,//beta[1]
                b3dot3 - b3dot4 - b3dot5 + b3dot6,//beta[2]
                b4dot4 - b4dot5 - b4dot6,//beta[3]
                b5dot5 - b5dot6,//beta[4]
                b6dot6};//beta5]

        // *************** calculate the beta array ************************** //

    }


}
