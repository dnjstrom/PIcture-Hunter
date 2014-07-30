package se.nielstrom.picture_hunter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import se.nielstrom.picture_hunter.fragments.CameraFragment;
import se.nielstrom.picture_hunter.fragments.DetailViewFragment;

public class ComparisonActivity extends FragmentActivity {

    public static final String KEY_IMAGE_PATH = "KEY_IMAGE_PATH";
    private String path;
    private File file;
    private View container;
    private boolean showingDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        container = findViewById(R.id.container);

        Bundle extras = getIntent().getExtras();
        path = extras.getString(KEY_IMAGE_PATH);
        file = new File(path);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, DetailViewFragment.newInstance(path))
                .commit();

        showingDetails = true;

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comparison, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void flipCard() {
        if (showingDetails) {
            getSupportFragmentManager()
                    .beginTransaction()
                    //.setCustomAnimations(R.anim.to_middle, R.anim.from_middle)
                    .setCustomAnimations(R.anim.from_middle, R.anim.to_middle, R.anim.from_middle, R.anim.to_middle)
                    .replace(R.id.container, CameraFragment.newInstance(path))
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().popBackStack();
        }

        showingDetails = !showingDetails;
    }
}
