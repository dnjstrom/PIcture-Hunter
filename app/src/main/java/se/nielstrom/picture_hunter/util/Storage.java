package se.nielstrom.picture_hunter.util;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Storage {
    public static final String APP_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "PictureHunter";
    public static final String USER_ALBUMS = APP_FOLDER + File.separator + "My Albums";
    public static final String FOREIGN_ALBUMS = APP_FOLDER + File.separator + "Other Albums";

    private static Storage instance;
    private final Context context;

    private File userAlbums;
    private File foreignAlbums;
    private File clipboard;

    private boolean externalStorageExists;
    private File appFolder;
    private List<ClipboardListener> listeners;

    private Storage(Context context) {
        this.context = context;

        externalStorageExists = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        clipboard = new File(context.getExternalCacheDir(), "clipboard");
        clearClipboard();

        if (exists()){
            appFolder = getOrCreateFolder(APP_FOLDER);
            userAlbums = getOrCreateFolder(USER_ALBUMS);
            foreignAlbums = getOrCreateFolder(FOREIGN_ALBUMS);
        }

        listeners = new ArrayList<ClipboardListener>();
    }

    public static Storage getInstance(Context context) {
        if (instance == null) {
            instance = new Storage(context);
        }
        return instance;
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
        return makeUnique(new File(location, createTimeStamp() + ".jpg"));
    }

    public File createTmpFile() throws IOException {
        return File.createTempFile(createTimeStamp(), ".jpg", appFolder);
    }

    private File makeUnique(File file) {
        File unique = file;

        for (int i = 0; unique.exists(); i++) {
            unique = new File(file.getAbsolutePath() + " " + i);
        }

        return unique;
    }

    private String createTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

    public int getClipboardCount() {
        return clipboard.listFiles().length;
    }

    public void copy(File... files) throws IOException {
        clearClipboard();

        for (File src : files) {
            File dst = File.createTempFile("copy", "-" + src.getName(), clipboard);
            copy(src, dst);
        }
        notifyClipboardListeners();
    }

    private void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public void cut(File... files) throws IOException {
        copy(files);
        delete(files);
        notifyClipboardListeners();
    }

    public void delete(File... files) {
        for (File file : files) {
            deleteRecursive(file);
        }
    }

    private void clearClipboard() {
        deleteRecursive(clipboard);
        clipboard.mkdirs();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public void pasteTo(File location) {
        if (!location.isDirectory()) {
            throw new IllegalArgumentException("Tried to paste to non-directory: " + location.getAbsolutePath());
        }

        for (File file : clipboard.listFiles()) {
            file.renameTo(new File(location, file.getName().replaceFirst("copy-\\d*-", "")));
        }

        notifyClipboardListeners();
    }


    private void notifyClipboardListeners() {
        for (ClipboardListener listener : listeners) {
            listener.onClipboardChanged();
        }
    }

    public Storage addOnClipboardListener(ClipboardListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public interface ClipboardListener {
        public void onClipboardChanged();
    }
}
