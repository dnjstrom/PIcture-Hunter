package se.nielstrom.picture_hunter.photos;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;

import se.nielstrom.picture_hunter.comparator.ComparisonActivity;
import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.comparator.CameraFragment;
import se.nielstrom.picture_hunter.common.FoldersPagerAdapter;
import se.nielstrom.picture_hunter.util.Storage;

public class PhotoListActivity extends FragmentActivity implements Storage.ClipboardListener {

    public static final String KEY_PATH = "KEY_REF_IMAGE_PATH";
    public static final String KEY_POSITION = "KEY_POSITION";

    private String path;
    private File location;
    private ViewPager pager;
    private FoldersPagerAdapter adapter;
    private Storage storage;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

        storage = Storage.getInstance(this);
        storage.addOnClipboardListener(this);

        Bundle extras = getIntent().getExtras();
        path = extras.getString(KEY_PATH);
        int startPosition = extras.getInt(KEY_POSITION);
        location = new File(path);

        pager = (ViewPager) findViewById(R.id.pager);

        adapter = new PhotoListAdapter(getSupportFragmentManager(), location);

        pager.setAdapter(adapter);
        pager.setCurrentItem(startPosition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo_list, menu);
        this.menu = menu;

        onClipboardChanged();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.paste) {
            storage.pasteTo(getCurrentFolder());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File getCurrentFolder() {
        return adapter.getFolder(pager.getCurrentItem());
    }

    @Override
    public void onClipboardChanged() {
        menu.findItem(R.id.paste).setVisible(storage.getClipboardCount() > 0);
    }


    private class PhotoListAdapter extends FoldersPagerAdapter {
        public PhotoListAdapter(FragmentManager fm, File location) {
            super(fm, location);
        }

        @Override
        public Fragment getItem(int position) {
            PhotoListFragment fragment = PhotoListFragment.newInstance(folders.get(position).getAbsolutePath());

            if (storage.isUserFile(location)) {
                return fragment.setBehavior(new UserFileBehavior(fragment));
            } else {
                return fragment.setShowAddButton(false).setBehavior(new ForeignFileBehavior(fragment));
            }
        }
    }


    private class UserFileBehavior extends PhotoBehavior {
        public UserFileBehavior(Fragment fragment) {
            super(fragment);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = (File) adapterView.getItemAtPosition(position);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            startActivity(intent);
        }

        @Override
        public void onClick(View view) {
            File image = storage.createImageFileAt(getCurrentFolder());
            CameraFragment fragment = CameraFragment.newInstance(image.getAbsolutePath());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.grid_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    private class ForeignFileBehavior extends PhotoBehavior {
        public ForeignFileBehavior(Fragment fragment) {
            super(fragment);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            File file = (File) adapterView.getItemAtPosition(position);
            Intent intent = new Intent(PhotoListActivity.this, ComparisonActivity.class);
            intent.putExtra(ComparisonActivity.KEY_REF_IMAGE_PATH, file.getAbsolutePath());
            startActivity(intent);
        }
    }
}
