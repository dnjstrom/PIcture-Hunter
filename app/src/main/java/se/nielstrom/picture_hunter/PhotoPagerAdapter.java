package se.nielstrom.picture_hunter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.File;
import java.io.FileFilter;

import se.nielstrom.picture_hunter.fragments.AlbumListFragment;
import se.nielstrom.picture_hunter.fragments.PhotoListFragment;


public class PhotoPagerAdapter extends FragmentStatePagerAdapter {
    File[] albums;

    public PhotoPagerAdapter(FragmentManager fm, File folder) {
        super(fm);

        albums = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
    }

    @Override
    public Fragment getItem(int position) {
        return PhotoListFragment.newInstance(albums[position].getAbsolutePath());
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
