package se.nielstrom.picture_hunter;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import se.nielstrom.picture_hunter.fragments.PhotoListFragment;
import se.nielstrom.picture_hunter.util.FoldersPagerAdapter;
import se.nielstrom.picture_hunter.util.Storage;

public class PhotoListActivity extends FragmentActivity {

    public static final String KEY_PATH = "KEY_PATH";
    public static final String KEY_POSITION = "KEY_POSITION";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private String path;
    private File file;
    private ViewPager pager;
    private FoldersPagerAdapter adapter;
    private Storage storage;
    private File latestPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

        storage = new Storage();

        Bundle extras = getIntent().getExtras();
        path = extras.getString(KEY_PATH);
        int startPosition = extras.getInt(KEY_POSITION);
        file = new File(path);

        pager = (ViewPager) findViewById(R.id.pager);

        adapter = new FoldersPagerAdapter(getSupportFragmentManager(), file) {
            @Override
            public Fragment getItem(int position) {
                return PhotoListFragment.newInstance(folders.get(position).getAbsolutePath());
            }
        };

        pager.setAdapter(adapter);

        pager.setCurrentItem(startPosition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo_list, menu);
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

    public void addPicture(View button) {
        File location = adapter.getFolder(pager.getCurrentItem());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            latestPicture = storage.createImageFileAt(location);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(latestPicture));

            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getSimpleName(), "Created: " + latestPicture.getAbsolutePath());
    }
}
