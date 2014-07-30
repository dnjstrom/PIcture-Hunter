package se.nielstrom.picture_hunter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.widget.ImageView;


public class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
    private final ImageView thumbView;
    private boolean extractThumbnail;
    private int height;
    private int width;
    private AsyncTaskListener listener;

    public ImageLoaderTask(ImageView thumbView) {
        this.thumbView = thumbView;
        this.extractThumbnail = false;
    }

    public ImageLoaderTask(ImageView thumbView, int width, int height) {
        this.thumbView = thumbView;
        this.extractThumbnail = true;
        this.width = width;
        this.height = height;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        if (strings == null || strings.length != 1) {
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(strings[0]);

        if (extractThumbnail) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap thumb) {
        thumbView.setImageBitmap(thumb);

        if (listener != null) {
            listener.onTaskComplete();
        }
    }

    public AsyncTask<String, Void, Bitmap> setAsyncTaskListener(AsyncTaskListener listener) {
        this.listener = listener;
        return this;
    }
}
