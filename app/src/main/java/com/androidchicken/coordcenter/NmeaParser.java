package com.androidchicken.coordcenter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Created by Elisabeth Huhn on 6/2/2016 for GeoBot.
 * Cloned from GeoBot on 1/3/2018
 *
 * copied from https://gist.github.com/javisantana/1326141
 * and modified:
 *  double rather than float
 *  precision greater than two places
 *  also need more sentences, but will modify as needed
 */
class NmeaParser {

    //specify the interface
    interface SentenceParser {
        boolean parse(String[] tokens, NmeaSentence nemaInfo);
    }

    private static NmeaParser ourInstance ;

    static NmeaParser getInstance() {
        if (ourInstance == null){
            ourInstance = new NmeaParser();
        }
        return ourInstance;
    }



    // utils

    //Latitude arrives with format DDMM.MMM... with variable number of decimals
    //returns decimal degrees DD format
    static double Latitude2Decimal(String lat, String NS) {
        //first get minutes, ignore first two digits which are degrees
        //and padded with leading zeros if necessary
        //convert minutes to degrees by dividing by 60.
        double degrees = Double.parseDouble(lat.substring(2))/60.0d;
        //top two digits are degrees
        degrees +=  Double.parseDouble(lat.substring(0, 2));
        if(NS.startsWith("S")) {
            degrees = -degrees;
        }
        return degrees;
    }


    //Longitude arrives with format DDDMM.MMM... with variable number of decimals
    //returns decimal degrees DD format
    static double Longitude2Decimal(String lon, String WE) {
        //first get minutes, ignore first three digits which are degrees
        //and padded with leading zeros if necessary
        //convert minutes to degrees by dividing by 60.
        double degrees = Double.parseDouble(lon.substring(3))/60.0d;
        //top three digits are degrees
        degrees +=  Double.parseDouble(lon.substring(0, 3));
        if(WE.startsWith("W")) {
            degrees = -degrees;
        }
        return degrees;
    }

    // parsers
    class GPGGA implements SentenceParser {
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {
            try {
                if (!(tokens[1].equals(""))){
                    nmeaInfo.mTime                 = Double.parseDouble(tokens[1]);
                }
                if (!(tokens[2].equals("")) &&
                    ((tokens[3].equals("S")) || (tokens[3].equals("N")))) {
                    nmeaInfo.mLatitude = Latitude2Decimal(tokens[2], tokens[3]);
                }
                if (!(tokens[4].equals("")) &&
                    ((tokens[5].equals("E")) || (tokens[5].equals("W")))) {
                    nmeaInfo.mLongitude = Longitude2Decimal(tokens[4], tokens[5]);
                }
                if (!(tokens[6].equals(""))){
                    nmeaInfo.mQuality              = Integer.parseInt(tokens[6]);
                    if (nmeaInfo.mQuality > 0) {nmeaInfo.mFixed = true;}
                }

                if (!(tokens[7].equals(""))){
                    nmeaInfo.mSatellites           = Integer.parseInt(tokens[7]);
                }
                if (!(tokens[8].equals(""))){
                    nmeaInfo.mHdop                 = Double.parseDouble(tokens[8]);
                }
                if (!(tokens[9].equals(""))){
                    nmeaInfo.mOrthometricElevation = Double.parseDouble(tokens[9]);
                }
                if (!(tokens[11].equals(""))){
                    nmeaInfo.mGeoid                = Double.parseDouble(tokens[11]);
                }

                return true;
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class GNGNS implements SentenceParser {
        char quality;
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {
            if (!(tokens[1].equals(""))){
                nmeaInfo.mTime                 = Double.parseDouble(tokens[1]);
            }
            if (!(tokens[2].equals("")) &&
                ((tokens[3].equals("S")) || (tokens[3].equals("N")))) {
                nmeaInfo.mLatitude = Latitude2Decimal(tokens[2], tokens[3]);
            }
            if (!(tokens[4].equals("")) &&
                ((tokens[5].equals("E")) || (tokens[5].equals("W")))) {
                nmeaInfo.mLongitude = Longitude2Decimal(tokens[4], tokens[5]);
            }
            if (!(tokens[6].equals(""))){
                int quality = 0;
                //convert the quality indicator to the other format from GGA
                switch (tokens[6]){
                    case "N":
                        quality = 0;
                        break;
                    case "A":
                        quality = 1;
                        break;
                    case "D":
                        quality = 2;
                        break;
                    case "P":
                        quality = 3;
                        break;
                    case "R":
                        quality = 4;
                        break;
                    case "F":
                        quality = 5;
                        break;
                    case "E":
                        quality = 6;
                        break;
                    case "M":
                        quality = 7;
                        break;
                    case "S":
                        quality = 8;
                        break;
                }
                nmeaInfo.mQuality = quality;
                if (nmeaInfo.mQuality > 0) {nmeaInfo.mFixed = true;}
            }

            if (!(tokens[7].equals(""))){
                nmeaInfo.mSatellites           = Integer.parseInt(tokens[7]);
            }
            if (!(tokens[8].equals(""))){
                nmeaInfo.mHdop                 = Double.parseDouble(tokens[8]);
            }
            if (!(tokens[9].equals(""))){
                nmeaInfo.mOrthometricElevation = Double.parseDouble(tokens[9]);
            }
            if (!(tokens[10].equals(""))){
                nmeaInfo.mGeoid                = Double.parseDouble(tokens[10]);
            }

            return true;
        }
    }
    class GPGNS implements SentenceParser {
        char quality;
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {
            if (!(tokens[1].equals(""))){
                nmeaInfo.mTime                 = Double.parseDouble(tokens[1]);
            }
            if (!(tokens[2].equals("")) &&
                    ((tokens[3].equals("S")) || (tokens[3].equals("N")))) {
                nmeaInfo.mLatitude = Latitude2Decimal(tokens[2], tokens[3]);
            }
            if (!(tokens[4].equals("")) &&
                    ((tokens[5].equals("E")) || (tokens[5].equals("W")))) {
                nmeaInfo.mLongitude = Longitude2Decimal(tokens[4], tokens[5]);
            }
            if (!(tokens[6].equals(""))){
                int quality = 0;
                //convert the quality indicator to the other format from GGA
                switch (tokens[6]){
                    case "N":
                        quality = 0;
                        break;
                    case "A":
                        quality = 1;
                        break;
                    case "D":
                        quality = 2;
                        break;
                    case "P":
                        quality = 3;
                        break;
                    case "R":
                        quality = 4;
                        break;
                    case "F":
                        quality = 5;
                        break;
                    case "E":
                        quality = 6;
                        break;
                    case "M":
                        quality = 7;
                        break;
                    case "S":
                        quality = 8;
                        break;
                }
                nmeaInfo.mQuality = quality;
                if (nmeaInfo.mQuality > 0) {nmeaInfo.mFixed = true;}
            }

            if (!(tokens[7].equals(""))){
                nmeaInfo.mSatellites           = Integer.parseInt(tokens[7]);
            }
            if (!(tokens[8].equals(""))){
                nmeaInfo.mHdop                 = Double.parseDouble(tokens[8]);
            }
            if (!(tokens[9].equals(""))){
                nmeaInfo.mOrthometricElevation = Double.parseDouble(tokens[9]);
            }
            if (!(tokens[10].equals(""))){
                nmeaInfo.mGeoid                = Double.parseDouble(tokens[10]);
            }

            return true;
        }
    }


    class GLGNS implements SentenceParser {
        char quality;
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {
            if (!(tokens[1].equals(""))){
                nmeaInfo.mTime                 = Double.parseDouble(tokens[1]);
            }
            if (!(tokens[2].equals("")) &&
                    ((tokens[3].equals("S")) || (tokens[3].equals("N")))) {
                nmeaInfo.mLatitude = Latitude2Decimal(tokens[2], tokens[3]);
            }
            if (!(tokens[4].equals("")) &&
                    ((tokens[5].equals("E")) || (tokens[5].equals("W")))) {
                nmeaInfo.mLongitude = Longitude2Decimal(tokens[4], tokens[5]);
            }
            if (!(tokens[6].equals(""))){
                int quality = 0;
                //convert the quality indicator to the other format from GGA
                switch (tokens[6]){
                    case "N":
                        quality = 0;
                        break;
                    case "A":
                        quality = 1;
                        break;
                    case "D":
                        quality = 2;
                        break;
                    case "P":
                        quality = 3;
                        break;
                    case "R":
                        quality = 4;
                        break;
                    case "F":
                        quality = 5;
                        break;
                    case "E":
                        quality = 6;
                        break;
                    case "M":
                        quality = 7;
                        break;
                    case "S":
                        quality = 8;
                        break;
                }
                nmeaInfo.mQuality = quality;
                if (nmeaInfo.mQuality > 0) {nmeaInfo.mFixed = true;}
            }

            if (!(tokens[7].equals(""))){
                nmeaInfo.mSatellites           = Integer.parseInt(tokens[7]);
            }
            if (!(tokens[8].equals(""))){
                nmeaInfo.mHdop                 = Double.parseDouble(tokens[8]);
            }
            if (!(tokens[9].equals(""))){
                nmeaInfo.mOrthometricElevation = Double.parseDouble(tokens[9]);
            }
            if (!(tokens[10].equals(""))){
                nmeaInfo.mGeoid                = Double.parseDouble(tokens[10]);
            }

            return true;
        }
    }

    class GPGGL implements SentenceParser {
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {

            try {

                if (!(tokens[1].equals("")) &&
                    ((tokens[2].equals("S")) || (tokens[2].equals("N")))) {
                    nmeaInfo.mLatitude = Latitude2Decimal(tokens[1], tokens[2]);
                }
                if (!(tokens[3].equals("")) &&
                    ((tokens[4].equals("E")) || (tokens[4].equals("W")))) {
                    nmeaInfo.mLongitude = Longitude2Decimal(tokens[3], tokens[4]);
                }
                //if tokens[5] starts with * it is a checksum, else it is time
                if(tokens[5].startsWith("*")) {
                    //field is checksum
                } else {
                    //time
                    nmeaInfo.mTime      = Double.parseDouble(tokens[5]);
                }

                return true;
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class GPRMA implements SentenceParser {
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {
            try {
                if (!(tokens[2].equals("")) &&
                    ((tokens[3].equals("S")) || (tokens[3].equals("N")))) {
                    nmeaInfo.mLatitude = Latitude2Decimal(tokens[2], tokens[3]);
                }
                if (!(tokens[4].equals("")) &&
                        ((tokens[5].equals("E")) || (tokens[5].equals("W")))) {
                    nmeaInfo.mLongitude = Longitude2Decimal(tokens[4], tokens[5]);
                }
                return true;
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class GPRMC implements SentenceParser {
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {
            try {
                if (!(tokens[1].equals(""))){
                    nmeaInfo.mTime                 = Double.parseDouble(tokens[1]);
                }
                if (!(tokens[3].equals("")) &&
                    ((tokens[4].equals("S")) || (tokens[4].equals("N")))) {
                    nmeaInfo.mLatitude = Latitude2Decimal(tokens[3], tokens[4]);
                }
                if (!(tokens[5].equals("")) &&
                    ((tokens[6].equals("E")) || (tokens[6].equals("W")))) {
                    nmeaInfo.mLongitude = Longitude2Decimal(tokens[5], tokens[6]);
                }
                return true;

            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*-**************************************************/
    //              Satellite Status classes            //
    /*-**************************************************/
    class GPGSV implements SentenceParser {
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {
            SatelliteManager satelliteManager = SatelliteManager.getInstance();

            try {
                int msgInCycle = Integer.parseInt(tokens[1]); //The number of messages in the cycle
                int thisMsg = Integer.parseInt(tokens[2]); //Which message this one is in the cycle
                int totSat = Integer.parseInt(tokens[3]); //total number of satellites in the cycle
                int i = totSat - ((thisMsg - 1) * 4);//number of satellites in this NMEA Sentence
                if (i > 4) {
                    i = 4; //The most satellites in one sentence is 4
                }
                nmeaInfo.mSatellites = totSat;
                Satellite satellite;
                String s;


                for (int l = 0; l < i; l++) {

                    //Any of the tokens could be null, in which case, toss the satellite
                    if (tokens[(l * 4) + 7].equals("")) tokens[(l * 4) + 7] = "-1";

                    //check if the last token has a checksum
                    if (tokens[(l * 4) + 7].contains("*")) {
                        String parts[] = tokens[(l * 4) + 7].split("[*]");
                        if ((parts[0] == null) || (parts[0].equals(""))) {
                            tokens[(l * 4) + 7] = "-1"; //todo what does it mean to have no signal to noise?
                        } else {
                            tokens[(l * 4) + 7] = parts[0]; //toss the checksum
                        }
                    }

                    if ((!(tokens[(l * 4) + 4]).equals("")) &&
                        (!(tokens[(l * 4) + 5]).equals("")) &&
                        (!(tokens[(l * 4) + 6]).equals("")) ){
                        //All the fields are non-null, so report on the satellite
                        satellite = new Satellite(
                                Integer.parseInt(tokens[(l * 4) + 4]), //Satellite ID
                                Integer.parseInt(tokens[(l * 4) + 5]), //elevation
                                Integer.parseInt(tokens[(l * 4) + 6]), //azimuth
                                Integer.parseInt(tokens[(l * 4) + 7])); //SNR
                        //remove any old records for this satellite before adding this new one
                        satelliteManager.add(satellite);
                    }
                }

                return true;

            } catch (RuntimeException e){
                throw new RuntimeException(e);
            }

        }
    }

    class GLGSV implements SentenceParser {
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {
            SatelliteManager satelliteManager = SatelliteManager.getInstance();
            try {
                int msgInCycle = Integer.parseInt(tokens[1]); //The number of messages in the cycle
                int thisMsg = Integer.parseInt(tokens[2]); //Which message this one is in the cycle
                int totSat = Integer.parseInt(tokens[3]); //total number of satelites in the cycle
                int i = totSat - ((thisMsg - 1) * 4);//number of satellites in this NMEA Sentence
                if (i > 4) {
                    i = 4; //The most satellites in one sentence is 4
                }
                nmeaInfo.mSatellites = totSat;
                Satellite satellite;
                String s;


                for (int l = 0; l < i; l++) {

                    //Any of the tokens could be null, in which case, toss the satellite
                    if (tokens[(l * 4) + 7].equals("")) tokens[(l * 4) + 7] = "-1";

                    //check if the last token has a checksum
                    if (tokens[(l * 4) + 7].contains("*")) {
                        String parts[] = tokens[(l * 4) + 7].split("[*]");
                        if ((parts[0] == null) || (parts[0].equals(""))) {
                            tokens[(l * 4) + 7] = "-1"; //todo what does it mean to have no signal to noise?
                        } else {
                            tokens[(l * 4) + 7] = parts[0]; //toss the checksum
                        }
                    }

                    if ((!(tokens[(l * 4) + 4]).equals("")) &&
                        (!(tokens[(l * 4) + 5]).equals("")) &&
                        (!(tokens[(l * 4) + 6]).equals("")) ){
                        //All the fields are non-null, so report on the satellite
                        satellite = new Satellite(
                                Integer.parseInt(tokens[(l * 4) + 4]), //Satellite ID
                                Integer.parseInt(tokens[(l * 4) + 5]), //elevation
                                Integer.parseInt(tokens[(l * 4) + 6]), //azimuth
                                Integer.parseInt(tokens[(l * 4) + 7])); //SNR
                        //nmeaInfo.setSatellite(satellite);
                        //remove any old satellite records before adding this new one
                        satelliteManager.add(satellite);
                    }
                }

                return true;

            } catch (RuntimeException e){
                throw new RuntimeException(e);
            }

        }
    }


    class GPGSA implements SentenceParser {
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo) {

            Satellite satellite;
            int counter = 0;
            int satelliteID;
            try {
                for (int l = 3; l <= 14; l++) {
                    if (!(tokens[l].equals(""))) {
                        satelliteID = Integer.parseInt(tokens[l]);

                        if (satelliteID > 0) {
                            satellite = new Satellite(satelliteID);
                            nmeaInfo.setSatellite(satellite);
                            counter++;
                        }
                    }

                }
                nmeaInfo.setSatellites(counter);
                if (!(tokens[15].equals(""))) {
                    nmeaInfo.mPdop = Double.parseDouble(tokens[15]);
                }
                if (!(tokens[16].equals(""))) {
                    nmeaInfo.mHdop = Double.parseDouble(tokens[16]);
                }
                if (!(tokens[17].equals(""))) {
                    //check if the last token has a checksum
                    if (tokens[17].contains("*")) {
                        String parts[] = tokens[17].split("[*]");
                        if ((parts[0] == null) || (parts[0].equals(""))) {
                            tokens[17] = "0"; //todo what does it mean to have no VDOP
                        } else {
                            tokens[17] = parts[0]; //toss the checksum
                        }
                    }
                    nmeaInfo.mVdop = Double.parseDouble(tokens[17]);
                }

                SatelliteManager satelliteManager = SatelliteManager.getInstance();
                satelliteManager.setHdop(nmeaInfo.getHdop());
                satelliteManager.setVdop(nmeaInfo.getVdop());
                satelliteManager.setPdop(nmeaInfo.getPdop());
                return true;
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class GLGSA implements SentenceParser {
        public boolean parse(String[] tokens, NmeaSentence nmeaInfo){
            Satellite satellite;
            int counter = 0;
            int satelliteID;
            try {
                for (int l = 3; l <= 14; l++) {
                    if (!(tokens[l].equals(""))) {
                        satelliteID = Integer.parseInt(tokens[l]);

                        if (satelliteID > 0) {
                            satellite = new Satellite(satelliteID);
                            nmeaInfo.setSatellite(satellite);
                            counter++;
                        }
                    }

                }
                nmeaInfo.setSatellites(counter);
                if (!(tokens[15].equals(""))) {
                    nmeaInfo.mPdop = Double.parseDouble(tokens[15]);
                }
                if (!(tokens[16].equals(""))) {
                    nmeaInfo.mHdop = Double.parseDouble(tokens[16]);
                }
                if (!(tokens[17].equals(""))) {
                    //check if the last token has a checksum
                    if (tokens[17].contains("*")) {
                        String parts[] = tokens[17].split("[*]");
                        if ((parts[0] == null) || (parts[0].equals(""))) {
                            tokens[17] = "0"; //todo what does it mean to have no VDOP
                        } else {
                            tokens[17] = parts[0]; //toss the checksum
                        }
                    }
                    nmeaInfo.mVdop = Double.parseDouble(tokens[17]);
                }

                SatelliteManager satelliteManager = SatelliteManager.getInstance();
                satelliteManager.setHdop(nmeaInfo.getHdop());
                satelliteManager.setVdop(nmeaInfo.getVdop());
                satelliteManager.setPdop(nmeaInfo.getPdop());
                return true;
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*-**************************************************/
    //              Data for the Parser Class           //
    /*-**************************************************/

    NmeaSentence mNmeaSentence;

    private static final Map<String, SentenceParser> sentenceParsers = new HashMap<>();


    /*-********************************************/
    /*        Constructor                         */
    /*-********************************************/

    NmeaParser() {
        //build the Map of NMEA string types we care about
  /*-****
        // first - position only NMEA sentences
        sentenceParsers.put("GPGGA", new GPGGA());
        sentenceParsers.put("GPGGL", new GPGGL());
        sentenceParsers.put("GPRMC", new GPRMC());
        sentenceParsers.put("GPRMZ", new GPRMZ());
        //only really good GPS devices have this sentence but ...
        sentenceParsers.put("GPVTG", new GPVTG());
******/

        //then a map of any strings that GeoBot is interested in

        //IN sample data
        //position
        sentenceParsers.put("GPGGA", new GPGGA());//Global Positioning System Fix Data

        sentenceParsers.put("GPGNS", new GPGNS());//fix data
        sentenceParsers.put("GNGNS", new GNGNS());
        sentenceParsers.put("GNGNS", new GLGNS());

        sentenceParsers.put("GPRMC", new GPRMC());

        //satellite information
        sentenceParsers.put("GPGSA", new GPGSA()); //PDOP, HDOP, VHOP
        //sentenceParsers.put("GNGSA", new GNGSA()); //GPS DOP and active satellites
        sentenceParsers.put("GPGSV", new GPGSV());//Satellites in view
        sentenceParsers.put("GLGSV", new GLGSV());



        //not yet seen in sample data
        //position
        //sentenceParsers.put("GPNEP", new GPNEP());
        sentenceParsers.put("GPGGL", new GPGGL());//Geographic position, latitude / longitude
        //sentenceParsers.put("GPRMZ", new GPRMZ());

    }

    //this routine parses the input string for position information
    //returns null if the NMEA sentence isn't one we are looking for
    //Note: if the string is parsed,
    // it is returned in the Nmea returned by the function
    NmeaSentence parse(String line) {

        //A NMEA sentence must start with $
        if(line.startsWith("$")) {
            //get the rest of the sentence after the $
            String nmea = line.substring(1);

            //split the sentence into tokens
            String[] tokens = nmea.split(",");

            //the type of the sentence is the first token
            String type = tokens[0];

            //if this parser class has a parser for this sentence,
            // its type will be represented in the sentenceParsers HashMap
            if(sentenceParsers.containsKey(type)) {
                //if here, we are interested in this string
                //so create an object to return and
                // store the original NMEA sentence in it
                mNmeaSentence = new NmeaSentence(line);

                mNmeaSentence.setNmeaType(type);

                sentenceParsers.get(type).parse(tokens, mNmeaSentence);
                //finally, set the fixed boolean based on the quality token
                //TODO mNmeaSentence.updatefix();
                return mNmeaSentence;
            }

        }
        return null;
    }


    /*-****************************************************************/

    private void test(){

        final String sentence = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A";
        //                          0      1   2     3    4      5    6    7     8     9    10  11 cs

        if (sentence.startsWith("$GPRMC")) {
            String[] strValues = sentence.split(",");
            double latitude = Double.parseDouble(strValues[3])*.01;
            if (strValues[4].charAt(0) == 'S') {
                latitude = -latitude;
            }
            double longitude = Double.parseDouble(strValues[5])*.01;
            if (strValues[6].charAt(0) == 'W') {
                longitude = -longitude;
            }
            double course = Double.parseDouble(strValues[8]);
            System.out.println("latitude="+latitude+" ; longitude="+longitude+" ; course = "+course);
        }
    }


        /*-**************************************************/
        //              Position Data class                 //
        /*-**************************************************/

        public class GPSPosition {
            public double time = 0.0d;
            public double lat = 0.0d;
            public double lon = 0.0d;
            public boolean fixed = false;
            public int quality = 0;
            public double dir = 0.0d;
            public double altitude = 0.0d;
            public double velocity = 0.0d;

            public void updatefix() {
                fixed = quality > 0;
            }

            //note that the formater uses float for the decimals
            public String toString() {
                return String.format(Locale.getDefault(), "POSITION: lat: %f, lon: %f, time: %f, Q: %d, dir: %f, alt: %f, vel: %f", lat, lon, time, quality, dir, altitude, velocity);
            }
        }


    }
