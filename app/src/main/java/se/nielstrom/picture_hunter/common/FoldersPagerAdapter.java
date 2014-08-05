package se.nielstrom.picture_hunter.common;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages the fragment "pages" of the apps view pagers.
 */
public abstract class FoldersPagerAdapter extends FragmentStatePagerAdapter {
    protected File location; // The directory from where folders are collected
    protected List<File> folders; // The folders in location

    public FoldersPagerAdapter(FragmentManager fm, File location) {
        super(fm);

        this.location = location;

        folders = new ArrayList<File>(Arrays.asList(location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory(); // Only list folders.
            }
        })));
    }


    /**
     * Search for the folder with the given path.
     *
     * @param file
     * @return
     */
    public int getFolderPosition(File file) {
        for (int i = 0; i < folders.size(); i++) {
            if (folders.get(i).equals(file)) {
                return i; // End early if found
            }
        }

        throw new IndexOutOfBoundsException("Couldn't find the requested file: " + file.getAbsolutePath());
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
