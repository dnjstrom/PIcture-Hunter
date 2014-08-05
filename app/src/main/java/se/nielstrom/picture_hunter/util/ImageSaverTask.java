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

/**
 * Asynchronously saves a bitmap to a file. Also facilitates the reading and writing of meta data
 * to jpeg-files using the exif interface.
 */
public class ImageSaverTask extends AsyncTask<Void, Void, Void> {
    private static final int IMAGE_SIZE = 1024;
    private File sourceFile;
    private File destination;
    private byte[] sourceBytes;
    private AsyncTaskListener listener;
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

        // Check if the bitmap should be decoded from a file or a byte array
        if (sourceFile != null) {
            bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
        } else {
            bitmap = BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.length);
        }

        // Extract a bitmap of proper size
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, IMAGE_SIZE, IMAGE_SIZE);

        try {
            // Write the bitmap to the destination file
            FileOutputStream fout = new FileOutputStream(destination);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        // optionally, write the location metadata if it exists
        if (location != null) {
            writeLocationData(destination, location);
        }

        return null;
    }

    /**
     * Writes a string to the "model" field in the image meta data.
     *
     * @param file
     * @param text the text to write to the file
     * @return whether the text was written successfully to file
     */
    public static boolean writeModelData(File file, String text) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_MODEL, text);
            exif.saveAttributes();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @param file
     * @return The string in the model meta data, or an empty string
     */
    public static String readModelData(File file) {
        String text = null;

        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            text = exif.getAttribute(ExifInterface.TAG_MODEL);
        } catch (IOException e) {}

        return (text != null) ? text : "";
    }


    /**
     * Write a location to the file meta data.
     *
     * @param file
     * @param location The location to write to the file.
     * @return If the data was successfully written.
     */
    public static boolean writeLocationData(File file, Location location) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LocationConverter.toDMS(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, LocationConverter.getLatitudeRef(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LocationConverter.toDMS(location.getLongitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, LocationConverter.getLongitudeRef(location.getLongitude()));
            exif.saveAttributes();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Read the location data from a file, and convert it to an Android Location.
     *
     * @param file
     * @return A Location corresponding to the files metadata coordinates
     */
    public static Location readLocationData(File file) {
        Location location = null;
        try {
            // get the metadata location data
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

            // convert the coordinates to decimal form.
            double latitude = LocationConverter.fromDMS(lat) * LocationConverter.getSign(latRef);
            double longitude = LocationConverter.fromDMS(lon) * LocationConverter.getSign(lonRef);

            // Create a new location from coordinates.
            location = new Location("dummy");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (listener != null) {
            listener.onTaskComplete();
        }
    }

    /**
     * @param location The location to be used when an image is saved.
     * @return The instance, for chaining.
     */
    public ImageSaverTask setLocation(Location location) {
        this.location = location;
        return this;
    }

    /**
     * @param listener an object to call after completion
     * @return
     */
    public ImageSaverTask setAsyncTaskListener(AsyncTaskListener listener) {
        this.listener = listener;
        return this;
    }
}
