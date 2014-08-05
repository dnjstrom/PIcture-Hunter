package se.nielstrom.picture_hunter.comparator;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.util.ImageComparator;
import se.nielstrom.picture_hunter.util.ImageSaverTask;
import se.nielstrom.picture_hunter.util.Storage;

/**
 * Handles the communication between the details fragment and the camera fragment.
 */
public class ComparisonActivity extends FragmentActivity implements CameraFragment.PictureCapturedListener {

    public static final String KEY_REF_IMAGE_PATH = "KEY_REF_IMAGE_PATH";
    public static final String MATCHED = "MATCHED";
    private static final int MATCHING_THRESHOLD = 800; // What constitutes a "winning" score.

    private String reference_path;
    private File referenceFile;
    private View container;
    private boolean showingDetails;
    private Storage storage;
    private File tmpFile;
    private DetailViewFragment detailsFragment;
    private CameraFragment cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        container = findViewById(R.id.container);

        // Get the picture to compare (to a new image)
        Bundle extras = getIntent().getExtras();
        reference_path = extras.getString(KEY_REF_IMAGE_PATH);
        referenceFile = new File(reference_path);

        detailsFragment = DetailViewFragment.newInstance(reference_path);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, detailsFragment)
                .commit();

        showingDetails = true;

        // Show the camera when the user touches the picture
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard();
            }
        });

        storage = Storage.getInstance(this);


        // Flip the card if necessary on restoring state from an orientation change.
        if (savedInstanceState != null && !savedInstanceState.getBoolean("STATE")) {
            flipCard();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("STATE", showingDetails);
    }

    /**
     * Change which fragment is shown using a custom semi-rotation animation
     */
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

    /**
     * Houses the actual comparison and decides whether an image has been correctly matched.
     *
     * @param picture
     */
    @Override
    public void onPictureCaptured(File picture) {
        new ImageComparator().compare(referenceFile, picture, new ImageComparator.ResultCallback() {
            @Override
            public void onComparisonResult(File a, File b, double distance) {

                Log.d(getClass().getSimpleName(), "Distance: " + distance);

                if (distance < MATCHING_THRESHOLD) {
                    ImageSaverTask.writeModelData(referenceFile, MATCHED); // Save matched state to the image file.
                    Toast.makeText(ComparisonActivity.this, R.string.successful_match, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ComparisonActivity.this, R.string.failed_match, Toast.LENGTH_LONG).show();
                }

                if (tmpFile != null) {
                    tmpFile.delete();
                }

                flipCard();
            }
        });
    }
}
