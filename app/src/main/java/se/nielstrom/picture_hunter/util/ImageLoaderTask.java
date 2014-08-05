package se.nielstrom.picture_hunter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Asynchronously loads an image, shrinks it down if desired, and sets it to an ImageView.
 */
public class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
    private final ImageView thumbView;
    private boolean extractThumbnail;
    private int height;
    private int width;
    private AsyncTaskListener listener;

    /**
     * @param thumbView The ImageView in which to set the loaded bitmap
     */
    public ImageLoaderTask(ImageView thumbView) {
        this.thumbView = thumbView;
        this.extractThumbnail = false;
    }

    /**
     * @param thumbView The ImageView in which to set the loaded bitmap
     * @param width The width of the after compression. Will clip the image if wrong ratio.
     * @param height The height of the image after compression. Will clip the image if wrong ratio.
     */
    public ImageLoaderTask(ImageView thumbView, int width, int height) {
        this.thumbView = thumbView;
        this.extractThumbnail = true;
        this.width = width;
        this.height = height;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        // Check for correct number of arguments
        if (strings == null || strings.length != 1) {
            return null;
        }

        // Decode bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(strings[0]);

        // Compress the image to size, if desired.
        if (extractThumbnail) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap thumb) {
        // Set the bitmap to the ImageView
        thumbView.setImageBitmap(thumb);

        if (listener != null) {
            listener.onTaskComplete();
        }
    }

    public ImageLoaderTask setAsyncTaskListener(AsyncTaskListener listener) {
        this.listener = listener;
        return this;
    }
}
