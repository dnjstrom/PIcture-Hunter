package se.nielstrom.picture_hunter.albums;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;

import se.nielstrom.picture_hunter.common.FoldersPagerAdapter;
import se.nielstrom.picture_hunter.photos.PhotoListActivity;
import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.util.ImageSaverTask;
import se.nielstrom.picture_hunter.util.Storage;

/**
 * This activity holds a viewpager with fragments showing the users albums.
 */
public class AlbumListActivity extends FragmentActivity {

    private FoldersPagerAdapter adapter;
    private ViewPager pager;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);

        storage = Storage.getInstance(this);

        // Stop the app if there is no SD-card available
        if (!storage.exists()) {
            Toast.makeText(this, R.string.sdcard_unavailable, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        adapter = new FoldersPagerAdapter(getSupportFragmentManager(), storage.getAppFolder()) {
            @Override
            public Fragment getItem(int position) {
                return AlbumListFragment.newInstance(folders.get(position).getAbsolutePath());
            }
        };

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        handleViewIntent(getIntent());
    }

    /**
     * Called after an NFC transfer has been completed.
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleViewIntent(intent);
    }

    /**
     * Copies the sent files from their initial location to the apps storage area.
     *
     * @param intent
     */
    private void handleViewIntent(Intent intent) {
        // Make sure this is the correct intent type
        if (!intent.getAction().equals(Intent.ACTION_VIEW)) {
            return;
        }

        Uri beamUri = intent.getData();

        File file = null;

        // get the directory of the sent files according to the scheme
        if (beamUri.getScheme().equals("file")) {
            file = handleFileUri(beamUri);
        } else if (beamUri.getScheme().equals("content")) {
            file = handleContentUri(beamUri);
        }

        // act accordingly if only one file is beamed
        if (file.getParentFile().getName().equals("beam")) {
            receiveFile(file);
        } else {
            receiveDirectory(file);
        }
    }

    /**
     * Handles a single file transfer.
     *
     * @param source
     */
    private void receiveFile(File source) {
        File album = new File(Storage.FOREIGN_ALBUMS, "New Beam Album");
        album = Storage.makeUnique(album);
        File destination = new File(album, source.getName());
        destination.getParentFile().mkdirs();

        // move the file
        source.renameTo(destination);

        // clear matched status
        ImageSaverTask.writeModelData(destination, "");

        // Display the new file
        Intent i = new Intent(this, PhotoListActivity.class);
        i.putExtra(PhotoListActivity.KEY_PATH, album.getAbsolutePath());
        startActivity(i);
    }

    /**
     * Handle the transfer of multiple files.
     *
     * @param dir
     */
    private void receiveDirectory(File dir) {
        File destination = new File(Storage.FOREIGN_ALBUMS, "New Beam Album");
        destination = Storage.makeUnique(destination);
        destination.getParentFile().mkdirs();

        //Move the directory to the app storage
        dir.getParentFile().renameTo(destination);

        // Reset any already matched images among the files
        for (File file : destination.listFiles()) {
            ImageSaverTask.writeModelData(file, "");
        }

        // Display the new files
        Intent i = new Intent(this, PhotoListActivity.class);
        i.putExtra(PhotoListActivity.KEY_PATH, destination.getAbsolutePath());
        startActivity(i);
    }

    /**
     * Simply gets the file path portion of a Uri.
     *
     * @param uri
     * @return the directory part of the uri file path
     */
    private File handleFileUri(Uri uri) {
        return new File(uri.getPath());
    }

    /**
     * Finds the directory of the new files from a content Uri.
     * @param uri
     * @return the directory that the uri points to a file in.
     */
    public File handleContentUri(Uri uri) {
        // Check the authority of the uri
        if (uri.getAuthority().equals(MediaStore.AUTHORITY)) {
            String[] projection = { MediaStore.MediaColumns.DATA };
            Cursor pathCursor = getContentResolver().query(uri, projection, null, null, null);

            // Read the data from the cursor
            if (pathCursor != null && pathCursor.moveToFirst()) {
                int filenameIndex = pathCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                String path = pathCursor.getString(filenameIndex);
                return new File(path);
            }
        }

        return null;
    }
}
