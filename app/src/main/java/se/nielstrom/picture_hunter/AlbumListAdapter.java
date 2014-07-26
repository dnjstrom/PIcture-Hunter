package se.nielstrom.picture_hunter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.io.File;

import se.nielstrom.picture_hunter.fragments.AlbumListFragment;


public class AlbumListAdapter extends FragmentPagerAdapter{
    File[] albums = createBaseFolders();

    public AlbumListAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return AlbumListFragment.newInstance(albums[position].getAbsolutePath());
    }
    @Override
    public String getPageTitle(int position) {
        return albums[position].getName();
    }

    @Override
    public int getCount() {
        return albums.length;
    }

    private static File[] createBaseFolders() {
        //TODO: create folders if they don't exist
        return new File[] {new File("/sdcard/Android/data"), new File("/sdcard/Android/obb")};
    }
}
