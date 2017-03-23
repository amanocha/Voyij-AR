package voyij.ar;

import android.content.pm.PackageManager;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.widget.Toast;
import java.lang.Math;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
//    private SensorManager mSensorManager;
//    private Sensor accelerometer;
//    private Sensor magnetometer;
//
//    private float[] mGravity;
//    private float[] mGeomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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

        LatLng chapel = new LatLng(36.001901, -78.940278);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            System.out.println("FAILED");
//            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(chapel).title("The Chapel"));
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(chapel));
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        System.out.println("MARKER CLICK");
        return false;
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            Toast.makeText(this, "LOCATION", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation == null) {
            Toast.makeText(this, "Null Location", Toast.LENGTH_LONG).show();
        }
        else {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            Toast.makeText(this, "Location Changed", Toast.LENGTH_LONG).show();

        }
    }

    protected void onResume() {
        super.onResume();
//        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
//        mSensorManager.unregisterListener(this);
    }

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            mGravity = event.values;
//
//        }
//
//        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            mGeomagnetic = event.values;
//        }
//
//        if (mGravity != null && mGeomagnetic != null) {
//            float R[] = new float[9];
//            float I[] = new float[9];
//            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
//
//            if (success) {
//                float orientation[] = new float[3];
//                SensorManager.getOrientation(R, orientation);
//                double azimuth = normalize(180*orientation[0]/Math.PI); // orientation contains: azimuth, pitch and roll
//                double pitch = normalize(180*orientation[1]/Math.PI);
//                double roll = normalize(180*orientation[2]/Math.PI);
//
//                System.out.println(Double.toString(azimuth) + " " + Double.toString(pitch) + " " + Double.toString(roll));
//            }
//        }
//    }
//
//    private double normalize(double value) {
//        if (value >= 0.0f && value <= 180.0f) {
//            return value;
//        } else {
//            return 180 + (180 + value);
//        }
//    }
}
