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

    public static double getRelativeAngleOfPOI(double phoneAngle, double poiAbsoluteAngle){
        double difference = Math.abs(phoneAngle-poiAbsoluteAngle);
        if(difference > 180){
            return 360 - difference;
        } else {
            return difference;
        }
    }

    public static double determineDisplayRatio(double poiRelativeAngle){
        return 0;
    }

}
