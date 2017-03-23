package voyij.ar;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Created by samtoffler on 3/23/17.
 */

public class LocationGPS implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationListener mLocationListener;

    public LocationGPS(Context context, LocationListener locationListener) {
        mContext = context;
        mLocationListener = locationListener;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    public void start() {
        mGoogleApiClient.connect();
    }

    public void stop() {
        mGoogleApiClient.disconnect();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, mLocationListener);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
//            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
//            Toast.makeText(this, "LOCATION", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
