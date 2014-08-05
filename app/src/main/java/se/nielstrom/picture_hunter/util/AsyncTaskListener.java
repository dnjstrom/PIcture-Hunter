package se.nielstrom.picture_hunter.util;

import android.os.AsyncTask;

import java.util.Objects;

/**
 * For specifying a callback after an async task is finished.
 */
public interface AsyncTaskListener {
    public void onTaskComplete();
}
