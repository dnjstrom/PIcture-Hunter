package se.nielstrom.picture_hunter.photos;

import android.content.Context;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileFilter;

import se.nielstrom.picture_hunter.R;


public abstract class FileAdapter extends ArrayAdapter<File> implements View.OnClickListener {
    private static final int FILE_EVENTS = FileObserver.CREATE | FileObserver.DELETE | FileObserver.MOVED_FROM | FileObserver.MOVED_TO;

    private static final String BUTTON_TAG = FileAdapter.class + "_add_button";

    private static final int DATA_CHANGED = 0;
    private static final String KEY_PATH = "KEY_REF_IMAGE_PATH";

    private final FileObserver observer;
    private final int addButtonId;
    private final FileFilter filter;
    private File location;
    private Context context;

    private boolean showAddButton = true;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String path = bundle.getString(KEY_PATH);

            if (path == null) {
                return;
            }

            File file = new File(location, path);

            if ((FileObserver.CREATE & msg.what) != 0 || (FileObserver.MOVED_TO & msg.what) != 0) {
                if (filter.accept(file)) {
                    add(file);
                }
            } else if ((FileObserver.DELETE & msg.what) != 0 || (FileObserver.MOVED_FROM & msg.what) != 0){
                remove(file);
            }

            notifyDataSetChanged();
        }
    };

    private View.OnClickListener listener;


    public FileAdapter(final Context context, int addButtonId, File location, FileFilter filter) {
        super(context, R.layout.folder_item);

        this.context = context;
        this.addButtonId = addButtonId;
        this.location = location;
        this.filter = filter;

        addAll(location.listFiles(filter));

        observer = new Observer(location.getAbsolutePath());
        observer.startWatching();
    }

    @Override
    public int getCount() {
        return (showAddButton) ? super.getCount() + 1 : super.getCount();
    }


    public File getLocation() {
        return location;
    }

    @Override
    public File getItem(int position) {
        if (showAddButton && position == super.getCount()) {
            return null;
        } else {
            return super.getItem(position);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (showAddButton && i == super.getCount()) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(addButtonId, parent, false);
            ImageButton button = (ImageButton) view.findViewById(R.id.button_add);
            button.setOnClickListener(this);
            view.setTag(BUTTON_TAG);
            return view;
        } else if (isButton(view)) {
            return createFileView(i, null, parent);
        } else {
            return createFileView(i, view, parent);
        }
    }

    protected abstract View createFileView(int position, View view, ViewGroup parent);

    public static boolean isButton(View view) {
        return view != null && view.getTag() == BUTTON_TAG;
    }

    public void setAddListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (showAddButton && listener != null) {
            listener.onClick(view);
        }
    }

    public void setShowAddButton(boolean show) {
        if (show != showAddButton) {
            showAddButton = show;
            notifyDataSetChanged();
        }
    }


    private class Observer extends FileObserver {

        public Observer(String path) {
            super(path, FILE_EVENTS);
        }

        @Override
        public void onEvent(int event, String path) {
            // Delegate to the ui-thread
            Message msg = Message.obtain();
            msg.what = event;
            Bundle data = new Bundle();
            data.putString(KEY_PATH, path);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

}
