package voyij.ar;

import java.util.List;

/**
 * Created by Titan on 3/31/17.
 */

public class POI {
    private String title;
    private double latitude;
    private double longitude;
    private String description;
    private String imageSource;
    private String thumbnailSource;
    private List<String> couponsSource;

    public POI(String title, double latitude, double longitude) {
        this(title, latitude, longitude, null, null, null);
    }

    public POI(String title, double latitude, double longitude, String description, String imageSource, String thumbnailSource) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
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
}
