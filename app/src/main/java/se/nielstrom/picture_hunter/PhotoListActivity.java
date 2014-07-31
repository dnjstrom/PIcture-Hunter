package se.nielstrom.picture_hunter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;

import se.nielstrom.picture_hunter.fragments.CameraFragment;
import se.nielstrom.picture_hunter.fragments.PhotoListFragment;
import se.nielstrom.picture_hunter.util.FoldersPagerAdapter;
import se.nielstrom.picture_hunter.util.InteractionBehavior;
import se.nielstrom.picture_hunter.util.Storage;

public class PhotoListActivity extends FragmentActivity {

    public static final String KEY_PATH = "KEY_REF_IMAGE_PATH";
    public static final String KEY_POSITION = "KEY_POSITION";

    private String path;
    private File file;
    private ViewPager pager;
    private FoldersPagerAdapter adapter;
    private Storage storage;

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

        adapter = new PhotoListAdapter(getSupportFragmentManager(), file);
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
                return fragment.setBehavior(new ForeignFileBehavior(fragment));
            }
        }
    }


    private class UserFileBehavior extends InteractionBehavior {
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
            File image = storage.createImageFileAt(file);
            CameraFragment fragment = CameraFragment.newInstance(image.getAbsolutePath());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.grid_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        }


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.context_actions, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.rename:
                    Log.d("", "rename");
                    break;
                case R.id.copy:
                    Log.d("", "copy");
                    break;
                case R.id.cut:
                    Log.d("", "cut");
                    break;
                case R.id.delete:
                    Log.d("", "delete");
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            GridView grid = (GridView) fragment.getView().findViewById(R.id.photo_grid);

            if (grid.getCheckedItemCount() == 1) {
                mode.getMenu().findItem(R.id.rename).setVisible(true);
            } else {
                mode.getMenu().findItem(R.id.rename).setVisible(false);
            }

            return;
        }
    }


    private class ForeignFileBehavior extends InteractionBehavior {
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
