package se.nielstrom.picture_hunter.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Storage {
    public static final String APP_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "PictureHunter";
    public static final String USER_ALBUMS = APP_FOLDER + File.separator + "My Albums";
    public static final String FOREIGN_ALBUMS = APP_FOLDER + File.separator + "Other Albums";

    private File userAlbums;
    private File foreignAlbums;

    private boolean externalStorageExists;
    private File appFolder;

    public Storage() {
        externalStorageExists = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        if (exists()){
            appFolder = getOrCreateFolder(APP_FOLDER);
            userAlbums = getOrCreateFolder(USER_ALBUMS);
            foreignAlbums = getOrCreateFolder(FOREIGN_ALBUMS);
        }
    }

    public File getUserAlbums() {
        return userAlbums;
    }

    public File getForeignAlbums() {
        return foreignAlbums;
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

    public File createAlbumAt(File location) {
        String name = "New Album";

        File album = makeUnique(new File(location, name));

        album.mkdirs();

        return album;
    }

    public File createImageFileAt(File location) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = makeUnique(new File(location, timeStamp + ".jpg"));
        return image;
    }

    private File makeUnique(File file) {
        File unique = file;

        for (int i = 0; unique.exists(); i++) {
            unique = new File(file.getAbsolutePath() + " " + i);
        }

        return unique;
    }

    public boolean isUserFile(File file) {
        return inDirectory(file, userAlbums);
    }

    public boolean isForeignFile(File file) {
        return inDirectory(file, foreignAlbums);
    }

    private boolean inDirectory(File file, File directory) {
        boolean result = file.getAbsolutePath().contains(directory.getAbsolutePath());
        return result;
    }
}
