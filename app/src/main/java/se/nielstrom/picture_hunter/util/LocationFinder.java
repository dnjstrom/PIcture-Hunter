package se.nielstrom.picture_hunter.util;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;


public class LocationFinder implements GooglePlayServicesClient.ConnectionCallbacks,
                                       GooglePlayServicesClient.OnConnectionFailedListener {

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 0;
    private final Activity activity;
    private final LocationClient locationClient;

    public LocationFinder(Activity activity) {
        this.activity = activity;
        locationClient = new LocationClient(activity, this, this);
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        return ConnectionResult.SUCCESS == resultCode;
    }

    @Override
    public void onConnected(Bundle bundle) {}

    @Override
    public void onDisconnected() {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect() {
        locationClient.connect();
    }

    public void disconnect() {
        locationClient.disconnect();
    }

    public android.location.Location getLocation() {
        return locationClient.getLastLocation();
    }
}
