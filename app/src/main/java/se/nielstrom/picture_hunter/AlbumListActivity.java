package se.nielstrom.picture_hunter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import se.nielstrom.picture_hunter.fragments.AlbumListFragment;
import se.nielstrom.picture_hunter.util.FoldersPagerAdapter;
import se.nielstrom.picture_hunter.util.Storage;

public class AlbumListActivity extends FragmentActivity {

    private FoldersPagerAdapter adapter;
    private ViewPager pager;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);

        storage = new Storage();

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

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.album_list, menu);
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

    public void addAlbum(View button) {
        File location = adapter.getFolder(pager.getCurrentItem());
        storage.createAlbumAt(location);
        //adapter.add();

    }
}
