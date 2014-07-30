package se.nielstrom.picture_hunter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class ImageSaverTask extends AsyncTask<byte[], Void, Bitmap> {
    private static final int IMAGE_SIZE = 1024;
    private final File file;
    private AsyncTaskListener listener;
    private boolean includeLocation;

    public ImageSaverTask(File file) {
        this.file = file;
    }

    @Override
    protected Bitmap doInBackground(byte[]... bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes[0], 0, bytes[0].length);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, IMAGE_SIZE, IMAGE_SIZE);
        try {
            FileOutputStream fout = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }


        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap thumb) {
        if (listener != null) {
            listener.onTaskComplete();
        }
    }

    public ImageSaverTask includeLocation(boolean include) {
        this.includeLocation = include;
        return this;
    }

    public ImageSaverTask setAsyncTaskListener(AsyncTaskListener listener) {
        this.listener = listener;
        return this;
    }
}
