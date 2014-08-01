package se.nielstrom.picture_hunter.albums;

import android.content.Intent;
import android.os.Bundle;
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
                AlbumListFragment fragment = AlbumListFragment.newInstance(folders.get(position).getAbsolutePath());
                return fragment.setBehavior(new Behavior(fragment));
            }
        };

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

    }

    private void enterAlbum(File album) {
    }

    private class Behavior extends AlbumBehavior {
        public Behavior(Fragment fragment) {
            super(fragment);
        }

        @Override
        public void onClick(View view) {
            storage.createAlbumAt(adapter.getFolder(pager.getCurrentItem()));
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            File file = (File) adapterView.getAdapter().getItem(i);
            Intent intent = new Intent(AlbumListActivity.this, PhotoListActivity.class);
            intent.putExtra(PhotoListActivity.KEY_PATH, file.getAbsolutePath());
            startActivity(intent);
        }
    }
}
