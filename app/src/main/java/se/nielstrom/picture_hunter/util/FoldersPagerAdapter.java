package se.nielstrom.picture_hunter.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Daniel on 2014-07-27.
 */
public abstract class FoldersPagerAdapter extends FragmentStatePagerAdapter {
    protected final File[] albums;

    public FoldersPagerAdapter(FragmentManager fm, File albums_location) {
        super(fm);

        this.albums = albums_location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
    }

    @Override
    public String getPageTitle(int position) {
        return albums[position].getName();
    }

    @Override
    public int getCount() {
        return albums.length;
    }
}
