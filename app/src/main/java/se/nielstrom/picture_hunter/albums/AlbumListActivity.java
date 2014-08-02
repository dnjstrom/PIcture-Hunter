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

public class AlbumListActivity extends FragmentActivity {

    private FoldersPagerAdapter adapter;
    private ViewPager pager;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);

        storage = Storage.getInstance(this);

        if (!storage.exists()) {
            Toast.makeText(this, "The app requires an external storage to be present.", Toast.LENGTH_LONG).show();
            finish();
            return; // quit early
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleViewIntent(intent);
    }

    private void handleViewIntent(Intent intent) {
        if (!intent.getAction().equals(Intent.ACTION_VIEW)) {
            return;
        }

        Uri beamUri = intent.getData();

        String path = null;

        if (beamUri.getScheme().equals("file")) {
            path = handleFileUri(beamUri);
        } else if (beamUri.getScheme().equals("file")) {
            path = handleContentUri(beamUri);
        }

        if (path != null) {
            File source = new File(path);
            File destination = new File(Storage.FOREIGN_ALBUMS, "New Beam Album");
            source.renameTo(Storage.makeUnique(destination));

            for (File file : source.listFiles()) {
                ImageSaverTask.writeModelData(file, "");
            }

            Intent i = new Intent(this, PhotoListActivity.class);
            i.putExtra(PhotoListActivity.KEY_PATH, destination.getAbsolutePath());
            startActivity(i);
        }
    }

    private String handleFileUri(Uri uri) {
        File file = new File(uri.getPath());
        return file.getParent();
    }

    public String handleContentUri(Uri uri) {
        if (uri.getAuthority().equals(MediaStore.AUTHORITY)) {
            String[] projection = { MediaStore.MediaColumns.DATA };
            Cursor pathCursor = getContentResolver().query(uri, projection, null, null, null);

            if (pathCursor != null && pathCursor.moveToFirst()) {
                int filenameIndex = pathCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                String fileName = pathCursor.getString(filenameIndex);
                return new File(fileName).getParent();
            }
        }

        return null;
    }
}
