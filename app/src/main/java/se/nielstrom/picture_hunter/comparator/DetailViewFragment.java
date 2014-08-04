package se.nielstrom.picture_hunter.comparator;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.io.File;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.util.AsyncTaskListener;
import se.nielstrom.picture_hunter.util.ImageLoaderTask;
import se.nielstrom.picture_hunter.util.ImageSaverTask;


public class DetailViewFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final String KEY_IMAGE_PATH = "KEY_REF_IMAGE_PATH";
    private String path;
    private File image;
    private ImageView imgView;
    private LocationClient locationClient;

    public static DetailViewFragment newInstance(String image_path) {
        DetailViewFragment fragment = new DetailViewFragment();
        Bundle args = new Bundle();
        args.putString(KEY_IMAGE_PATH, image_path);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(KEY_IMAGE_PATH);
            image = new File(path);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_detail_view, container, false);

        imgView = (ImageView) root.findViewById(R.id.image);
        final ProgressBar progress = (ProgressBar) root.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);

        new ImageLoaderTask(imgView).setAsyncTaskListener(new AsyncTaskListener() {
            @Override
            public void onTaskComplete() {
                progress.setVisibility(View.INVISIBLE);
            }
        }).execute(path);

        boolean isMatched = ImageSaverTask.readModelData(image).equals(ComparisonActivity.MATCHED);
        View matchedBadge = root.findViewById(R.id.matched);
        matchedBadge.setVisibility(isMatched ? View.VISIBLE : View.INVISIBLE);

        return root;
    }



    @Override
    public void onResume() {
        super.onResume();
        locationClient = new LocationClient(getActivity(), this, this);
        locationClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationClient != null) {
            locationClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest request = LocationRequest.create();
        request.setInterval(30 * 1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationClient.requestLocationUpdates(request, this);
    }

    @Override
    public void onDisconnected() {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        Log.d(getClass().getSimpleName(), "Latitude: " + location.getLatitude());
        Log.d(getClass().getSimpleName(), "Longitude: " + location.getLongitude());

        Location target = ImageSaverTask.readLocationData(image);

        if (target != null) {
            float distance = location.distanceTo(target);

            ProgressBar progress = (ProgressBar) getView().findViewById(R.id.distance_progress);
            progress.setVisibility(View.GONE);

            TextView distanceText = (TextView) getView().findViewById(R.id.distance_text);

            String separator = " ";
            if (distance < 20) {
                separator = " ~";
            }

            if (distance >= 1500) {
                distanceText.setText(getResources().getText(R.string.distance) + separator + String.format("%.2fkm", distance));
            } else {
                distanceText.setText(getResources().getText(R.string.distance) + separator + Math.round(distance) + "m");
            }

        }
    }
}
