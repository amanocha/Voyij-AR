package voyij.ar;

import android.location.Location;
import android.location.LocationListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by anindamanocha on 2/28/17.
 */

public abstract class MyLocationListener extends MapsActivity implements LocationListener {
    public void onLocationChanged(Location location)
    {
        if(location!=null)
        {
            //MapsActivity.setLatitude(location.getLatitude());
            //MapsActivity.setLongitude(location.getLongitude());

            //LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude);

            //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        }
    }

    public void onProviderEnabled(String string) {

    }
}
