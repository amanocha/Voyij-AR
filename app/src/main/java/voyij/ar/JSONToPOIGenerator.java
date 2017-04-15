package voyij.ar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Chirag Tamboli
 *
 */
public class JSONToPOIGenerator {
    private static final String KEY_PROPERTY_TITLE = "title";
    private static final String KEY_PROPERTY_POI_TYPE = "POIType";

    private static ObjectMapper mapper = new ObjectMapper();


    private JSONToPOIGenerator() {  //don't allow instantiation
    }


    /**
     * Unmarshalls the specified JSON file into a list of POI objects.
     * @param pathname	The path to the JSON file.
     * @return A List of POI objects.
     */
    public static List<POI> unmarshallJSONFile(String pathname) {
        return unmarshallJSONFile(new File(pathname));
    }

    /**
     * Unmarshalls the specified JSON file into a list of POI objects.
     * @param file	The JSON file to unmarshall.
     * @return	A List of POI objects.
     */
    public static List<POI> unmarshallJSONFile(File file) {
        try {
            return unmarshallJSONFile(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        }
        return new ArrayList<POI>();
    }

    /**
     * Unmarshalls the specified JSON file into a list of POI objects.
     * @param in	The inputstream to unmarshall.
     * @return	A List of POI objects.
     */
    public static List<POI> unmarshallJSONFile(InputStream in) {
        List<POI> listOfPOI = new ArrayList<POI>();
        try {
            GeoJsonObject geoJsonObject = mapper.readValue(in, GeoJsonObject.class);
            if (geoJsonObject instanceof Feature) {				//GeoJSON Feature Spec (1 feature in file)
                Feature feature = (Feature) geoJsonObject;
                POI poi = convertFeatureToPOI(feature);
                if (poi == null) {
                    return listOfPOI;
                }
                listOfPOI.add(poi);
                return listOfPOI;
            }
            else if (geoJsonObject instanceof FeatureCollection) {		//GeoJSON FeatureCollection Spec (>=1 features in file)
                FeatureCollection featureCollection = (FeatureCollection) geoJsonObject;
                for (Feature feature : featureCollection.getFeatures()) {
                    POI poi = convertFeatureToPOI(feature);
                    if (poi != null) {
                        listOfPOI.add(poi);
                    }
                }
                return listOfPOI;
            }
            else {
                return new ArrayList<POI>();
            }
        } catch (JsonParseException e) {
            System.out.println("Error Parsing JSON");
        } catch (JsonMappingException e) {
            System.out.println("Error Parsing JSON");
        } catch (IOException e) {
            System.out.println("IO Exception");
        } finally {
        }
        return listOfPOI;
    }

    private static POI convertFeatureToPOI(Feature feature) {
        GeoJsonObject featureGeometry = feature.getGeometry();
        if (!(featureGeometry instanceof Point)) {		//Doesn't have a valid Point that contains coordinates
            return null;
        }
        Point point = (Point) featureGeometry;
        LngLatAlt coordinates = point.getCoordinates();
        if (coordinates == null) {
            return null;
        }
        return new POI((String) feature.getProperty(KEY_PROPERTY_TITLE), coordinates.getLatitude(), coordinates.getLongitude(),
                coordinates.getAltitude(), (String) feature.getProperty(KEY_PROPERTY_POI_TYPE));
    }

}