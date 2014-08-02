package se.nielstrom.picture_hunter.photos;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Toast;

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
    private File file;
    private ViewPager pager;
    private FoldersPagerAdapter adapter;
    private Storage storage;
    private Menu menu;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

        storage = Storage.getInstance(this);
        storage.addOnClipboardListener(this);

        Bundle extras = getIntent().getExtras();
        path = extras.getString(KEY_PATH);
        file = new File(path);

        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new FoldersPagerAdapter(getSupportFragmentManager(), file.getParentFile()) {
            @Override
            public Fragment getItem(int position) {
                return PhotoListFragment.newInstance(folders.get(position).getAbsolutePath());
            }
        };
        pager.setAdapter(adapter);

        try {
            int position = adapter.getFolderPosition(file);
            pager.setCurrentItem(position);
        } catch (IndexOutOfBoundsException e) {}
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
}
