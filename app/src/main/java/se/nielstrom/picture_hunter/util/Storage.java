package se.nielstrom.picture_hunter.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by Daniel on 2014-07-27.
 */
public class Storage {
    public static final String APP_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "PictureHunter";
    public static final String MY_ALBUMS = APP_FOLDER + File.separator + "MyAlbums";
    public static final String OTHER_ALBUMS = APP_FOLDER + File.separator + "OtherAlbums";

    private File myAlbums;
    private File otherAlbums;

    private boolean externalStorageExists;
    private File appFolder;

    public Storage() {
        externalStorageExists = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        if (exists()){
            appFolder = getOrCreateFolder(APP_FOLDER);
            myAlbums = getOrCreateFolder(MY_ALBUMS);
            otherAlbums = getOrCreateFolder(OTHER_ALBUMS);
        }
    }

    public File getMyAlbums() {
        return myAlbums;
    }

    public File getOtherAlbums() {
        return otherAlbums;
    }

    public boolean exists() {
        return externalStorageExists;
    }

    private static File getOrCreateFolder(String path) {
        File folder = new File(path);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }

    public File getAppFolder() {
        return appFolder;
    }
}
