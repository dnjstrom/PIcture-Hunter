package se.nielstrom.picture_hunter.util;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Daniel on 2014-07-27.
 */
public abstract class FoldersPagerAdapter extends FragmentStatePagerAdapter {
    protected List<File> folders;
    private File location;

    public FoldersPagerAdapter(FragmentManager fm, File location) {
        super(fm);

        this.location = location;

        folders = new ArrayList<File>(Arrays.asList(location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        })));
    }


    public File getFolder(int position) {
        return folders.get(position);
    }

    public File getLocation() {
        return location;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public String getPageTitle(int position) {
        return folders.get(position).getName();
    }

    @Override
    public int getCount() {
        return folders.size();
    }
}
