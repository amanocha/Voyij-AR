package voyij.ar;

import android.support.annotation.NonNull;

/**
 * @author Chirag Tamboli
 */
public class POI implements Comparable<POI> {
    public static final String TYPE_STORE = "Store";
    public static final String TYPE_RESTAURANT = "Restaurant";
    public static final String TYPE_UTILITY = "Utility";
    public static final String TYPE_LANDMARK = "Landmark";

    private String title;
    private double latitude;
    private double longitude;
    private double altitude;
    private String POIType;
    private String description;
    private String imageSource;
    private String thumbnailSource;
    private double distanceFromCurrentLocation;


    public POI(String title, double latitude, double longitude, String POIType) {
        this(title, latitude, longitude, Double.NaN, POIType, null, null, null);
    }

    public POI(String title, double latitude, double longitude, double altitude, String POIType) {
        this(title, latitude, longitude, altitude, POIType, null, null, null);
    }

    public POI(String title, double latitude, double longitude, double altitude, String POIType, String description, String imageSource, String thumbnailSource) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.POIType = POIType;
        this.description = description;
        this.imageSource = imageSource;
        this.thumbnailSource = thumbnailSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public String getThumbnailSource() {
        return thumbnailSource;
    }

    public void setThumbnailSource(String thumbnailSource) {
        this.thumbnailSource = thumbnailSource;
    }

    public String getPOIType() {
        return POIType;
    }

    public void setPOIType(String POIType) {
        this.POIType = POIType;
    }

    public double getDistanceFromCurrentLocation() {
        return distanceFromCurrentLocation;
    }

    public void setDistanceFromCurrentLocation(double distanceFromCurrentLocation) {
        this.distanceFromCurrentLocation = distanceFromCurrentLocation;
    }

    @Override
    public int compareTo(@NonNull POI o) {      //sorts from increasing distance from phone location
        if(this == o){
            return 0;
        }
        if (o.distanceFromCurrentLocation - this.distanceFromCurrentLocation < 0) {
            return 1;
        }
        if (o.distanceFromCurrentLocation - this.distanceFromCurrentLocation > 0) {
            return -1;
        }
        return this.title.compareTo(o.title);   //if distances equal, compare titles
    }

    @Override
    public String toString() {
        return "{" + title + " (" + POIType + "): " + "Lat: " + latitude + " Long: " + longitude + " Alt: " + altitude + "}";
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof POI)){
            return false;
        }
        POI p = (POI) o;
        return p == this;
    }

    @Override
    public int hashCode(){
      return 5;
    }
}