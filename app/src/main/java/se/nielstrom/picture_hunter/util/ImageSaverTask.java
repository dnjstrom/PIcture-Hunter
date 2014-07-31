package se.nielstrom.picture_hunter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class ImageSaverTask extends AsyncTask<Void, Void, Void> {
    private static final int IMAGE_SIZE = 1024;
    private File sourceFile;
    private File destination;
    private byte[] sourceBytes;
    private AsyncTaskListener listener;
    private boolean includeLocation;
    private Location location;

    public ImageSaverTask(File source, File destination) {
        this.sourceFile = source;
        this.destination = destination;
    }

    public ImageSaverTask(byte[] source, File destination) {
        this.sourceBytes = source;
        this.destination = destination;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Bitmap bitmap;

        if (sourceFile != null) {
            bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
        } else {
            bitmap = BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.length);
        }

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, IMAGE_SIZE, IMAGE_SIZE);

        try {
            FileOutputStream fout = new FileOutputStream(destination);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        if (location != null) {
            try {
                ExifInterface exif = new ExifInterface(destination.getAbsolutePath());
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LocationConverter.toDMS(location.getLatitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, LocationConverter.getLatitudeRef(location.getLatitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LocationConverter.toDMS(location.getLongitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, LocationConverter.getLongitudeRef(location.getLongitude()));
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (listener != null) {
            listener.onTaskComplete();
        }
    }

    public ImageSaverTask setLocation(Location location) {
        this.location = location;
        return this;
    }

    public ImageSaverTask setAsyncTaskListener(AsyncTaskListener listener) {
        this.listener = listener;
        return this;
    }
}
