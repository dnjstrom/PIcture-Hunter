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

public class ComparisonActivity extends FragmentActivity implements CameraFragment.PictureCapturedListener {

    public static final String KEY_REF_IMAGE_PATH = "KEY_REF_IMAGE_PATH";
    public static final String MATCHED = "MATCHED";

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

        storage = Storage.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

                Log.d(getClass().getSimpleName(), "Distance: " + distance);

                if (distance < 1000) {
                    ImageSaverTask.writeModelData(referenceFile, MATCHED);
                    Toast.makeText(ComparisonActivity.this, R.string.successful_match, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ComparisonActivity.this, R.string.failed_match, Toast.LENGTH_LONG).show();
                }

                if (tmpFile != null) {
                    tmpFile.delete();
                }

                getSupportFragmentManager().popBackStack();

                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.shake, R.anim.vanish)
                        .replace(R.id.container, detailsFragment)
                        .commit();
            }
        });
    }
}
