package voyij.ar;

/**
 * Created by samtoffler on 3/31/17.
 */

public class POI {
    private String title;
    private double latitude;
    private double longtitude;
    private String description;
    private String imageSource;
    private String thumbnailSource;

    public POI(String title, double latitude, double longtitude) {
        this.title = title;
        this.latitude = latitude;
        this.longtitude = longtitude;
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

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
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


}
