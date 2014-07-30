package se.nielstrom.picture_hunter;

import android.content.Intent;
import android.graphics.Bitmap;
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

public class PhotoListActivity extends FragmentActivity {

    public static final String KEY_PATH = "KEY_IMAGE_PATH";
    public static final String KEY_POSITION = "KEY_POSITION";

    private String path;
    private File file;
    private ViewPager pager;
    private FoldersPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

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
}
