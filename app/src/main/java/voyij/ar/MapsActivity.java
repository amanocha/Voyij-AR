package voyij.ar;

import android.*;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.location.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng dukeChapel = new LatLng(36.001901, -78.940278);
        LatLng westUnion = new LatLng(36.000798, -78.939011);
        LatLng LSRC = new LatLng(36.004361, -78.941871);
        mMap.addMarker(new MarkerOptions().position(dukeChapel).title("Marker at Duke Chapel").icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
        mMap.addMarker(new MarkerOptions().position(westUnion).title("Marker at West Union").icon(BitmapDescriptorFactory.fromResource(R.drawable.food)));
        mMap.addMarker(new MarkerOptions().position(LSRC).title("Marker at LSRC").icon(BitmapDescriptorFactory.fromResource(R.drawable.building)));

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        //LocationManager myLocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        TrackGPS gps = new TrackGPS(MapsActivity.this);
        double longitude = -78.940278, latitude = 36.001901;

        if(gps.canGetLocation()){
            System.out.println("hi");
            longitude = gps.getLongitude();
            latitude = gps.getLatitude();
        } else {
            System.out.println("bye");
            gps.showSettingsAlert();
        }

        LatLng userLocation = new LatLng(latitude,longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }
}
