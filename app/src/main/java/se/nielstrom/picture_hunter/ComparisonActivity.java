package se.nielstrom.picture_hunter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import se.nielstrom.picture_hunter.fragments.CameraFragment;
import se.nielstrom.picture_hunter.fragments.DetailViewFragment;
import se.nielstrom.picture_hunter.util.ImageComparator;
import se.nielstrom.picture_hunter.util.LocationFinder;
import se.nielstrom.picture_hunter.util.Storage;

public class ComparisonActivity extends FragmentActivity implements CameraFragment.PictureCapturedListener {

    public static final String KEY_REF_IMAGE_PATH = "KEY_REF_IMAGE_PATH";
    private String reference_path;
    private File referenceFile;
    private View container;
    private boolean showingDetails;
    private LocationFinder locationFinder;
    private Storage storage;
    private File tmpFile;
    private DetailViewFragment detailsFragment;
    private CameraFragment cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        container = findViewById(R.id.container);

        Bundle extras = getIntent().getExtras();
        reference_path = extras.getString(KEY_REF_IMAGE_PATH);
        referenceFile = new File(reference_path);

        detailsFragment = DetailViewFragment.newInstance(reference_path);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, detailsFragment)
                .commit();

        showingDetails = true;

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard();
            }
        });

        locationFinder = new LocationFinder(this);

        storage = new Storage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationFinder.connect();
    }

    @Override
    protected void onStop() {
        locationFinder.disconnect();
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comparison, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void flipCard() {
        if (showingDetails) {

            try {
                tmpFile = storage.createTmpFile();
                cameraFragment = CameraFragment.newInstance(tmpFile.getAbsolutePath());
                cameraFragment.setOnPictureTakenListener(this);

                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.from_middle, R.anim.to_middle)
                        .replace(R.id.container, cameraFragment)
                        .addToBackStack(null)
                        .commit();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            getSupportFragmentManager().popBackStack();
        }

        showingDetails = !showingDetails;
    }

    @Override
    public void onPictureCaptured(File picture) {
        new ImageComparator().compare(referenceFile, picture, new ImageComparator.ResultCallback() {
            @Override
            public void onComparisonResult(File a, File b, double distance) {
                Toast.makeText(ComparisonActivity.this, "Distance: " + distance, Toast.LENGTH_LONG).show();

                if (tmpFile != null) {
                    tmpFile.delete();
                }

                getSupportFragmentManager().popBackStack();

                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.shake, R.anim.vanish)
                        .replace(R.id.container, detailsFragment)
                        .commit();


                detailsFragment.showFailure();
            }
        });
    }
}
