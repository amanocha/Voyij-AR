package voyij.ar;

/**
 * Created by samtoffler on 3/23/17.
 */

public class ARMath {

    public static final float PI = (float) 3.14159265359;

    public static double getAbsoluteAngleOfPOI(double phoneLong, double phoneLat, double poiLong, double poiLat){
        if(phoneLong > poiLong && phoneLat < poiLat){
            return 360 - Math.toDegrees(Math.atan((phoneLong-poiLong)/(poiLat-phoneLat)));
        } else if (phoneLong < poiLong && phoneLat < poiLat){
            return Math.toDegrees(Math.atan((poiLong-phoneLong)/(poiLat-phoneLat)));
        } else if (phoneLong > poiLong && phoneLat > poiLat){
            return 180 + Math.toDegrees(Math.atan((phoneLong-poiLong)/(phoneLat-poiLat)));
        } else if (phoneLong < poiLong && phoneLat > poiLat){
            return 180 - Math.toDegrees(Math.atan((poiLong-phoneLong)/(phoneLat-poiLat)));
        } else if (phoneLong == poiLong && phoneLat == poiLat){
            // double check this
            return -2;
        } else if (phoneLong == poiLong && phoneLat < poiLat){
            return 0;
        } else if (phoneLong == poiLong && phoneLat > poiLat){
            return 180;
        } else if (phoneLat == poiLat && phoneLong < poiLong){
            return 90;
        } else if (phoneLat == poiLat && phoneLong > poiLong){
            return 270;
        }
        // Something went wrong
        return -1;
    }

    public static double getPOIDirection(double phoneLat, double phoneLong, double poiLat, double poiLong) {
        double x = poiLong - phoneLong;
        double y = poiLat - phoneLat;

        double direction = Math.toDegrees(Math.atan2(y, x));

        if (phoneLong == poiLong && phoneLat == poiLat) {
            return -2;
        } else {
            if (direction > 0) {
                return direction;
            } else {
                return direction + 360;
            }
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

    public static double determineDisplayRatio(double poiRelativeAngle){
        return 0;
    }

}
