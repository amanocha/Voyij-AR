package voyij.ar;

/**
 * Created by samtoffler on 3/23/17.
 */

public class ARMath {

    public static double getPOIDirection(double phoneLong, double phoneLat, double poiLong, double poiLat) {
        double x = poiLong - phoneLong;
        double y = poiLat - phoneLat;

        double direction = Math.toDegrees(Math.atan2(y,x));

        if (phoneLong == poiLong && phoneLat == poiLat) {
            return -1;
        } else {
            if (poiLat > phoneLat && poiLong >= phoneLong) { //quadrant I
                return 90 - direction;
            } else if (poiLat >= phoneLat && poiLong < phoneLong ) { //quadrant II
                return 450 - direction;
            } else if (poiLat < phoneLat && poiLong <= phoneLong) { //quadrant III
                return Math.abs(direction) + 90;
            } else { //quadrant IV (poiLat <= phoneLat && poiLong > phoneLong)
                return Math.abs(direction) + 90;
            }
        }
    }

    public static double getPOIDistance(double phoneLat, double phoneLong, double poiLat, double poiLong){
        //Haversine
        double a = Math.pow(Math.sin((Math.toRadians(phoneLat) - Math.toRadians(poiLat))/2),2)
                + Math.cos(Math.toRadians(poiLat)) * Math.cos(Math.toRadians(phoneLat))
                * Math.pow(Math.sin((Math.toRadians(phoneLong) - Math.toRadians(poiLong))/2),2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = 6371 * c;
        return d;
    }

    public static double getHeightDifference(double phoneAlt, double poiAlt){
        return poiAlt - phoneAlt;
    }

    public static double getAbsoluteHeightAngle(double distance, double height){
        double angle = Math.toDegrees(Math.atan2(height, distance));
        if (distance == 0 && height == 0){
            return -2;
        } else {
            return angle;
        }
    }

    public static double getRelativeHeightAngle(double phoneAngle, double absoluteAngle){
        if (phoneAngle < 0) {
            return -1*Math.abs(phoneAngle-absoluteAngle);
        } else {
            return Math.abs(phoneAngle-absoluteAngle);
        }
    }

    public static double getRelativeAngleOfPOI(double phoneAngle, double poiAbsoluteAngle){
        double difference = Math.abs(phoneAngle-poiAbsoluteAngle);
        if(difference > 180){
            return 360 - difference;
        } else {
            return difference;
        }
    }

    public static int getAboveBelow(double phoneAngle, double poiAbsoluteAngle){
        if ((phoneAngle + 90) < poiAbsoluteAngle){
            return 0; //phone is looking above poi, so poi should be on bottom of screen
        } else {
            return 1; //phone is looking below poi, so poi should be on top of screen
        }
    }

    public static int getSide(double phoneAngle, double poiAbsoluteAngle, double fov) {
        if (phoneAngle < poiAbsoluteAngle) {
            if ((poiAbsoluteAngle - phoneAngle) >= fov) {
                return 1; //right
            } else {
                return 0; //left
            }
        } else {
            if ((poiAbsoluteAngle - phoneAngle) >= fov) {
                return 0; //left
            } else {
                return 1; //right
            }
        }
    }

    public static float normalize(double value) {
        if (value < 0) {
            return (360 + (float)value);
        } else {
            return (float)value;
        }
    }

}
