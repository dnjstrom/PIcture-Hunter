package se.nielstrom.picture_hunter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.nielstrom.picture_hunter.AlbumListActivity;
import se.nielstrom.picture_hunter.R;


public class FolderAdapter extends ArrayAdapter<File> {
    private static final int FILE_EVENTS = FileObserver.CREATE | FileObserver.DELETE | FileObserver.MOVED_FROM | FileObserver.MOVED_TO;

    private static final int DATA_CHANGED = 0;
    private static final String KEY_PATH = "KEY_PATH";

    private final FileObserver observer;
    private File location;
    private Context context;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String path = bundle.getString(KEY_PATH);

            if ((FileObserver.CREATE & msg.what) != 0 || (FileObserver.MOVED_TO & msg.what) != 0) {
                add(new File(location.getAbsolutePath() + File.separator + path));
            } else if ((FileObserver.DELETE & msg.what) != 0 || (FileObserver.MOVED_FROM & msg.what) != 0){
                remove(new File(location.getAbsolutePath() + File.separator + path));
            }

            notifyDataSetChanged();
        }
    };

    public FolderAdapter(final Context context, File location) {
        super(context, R.layout.folder_item);

        this.context = context;
        this.location = location;

        addAll(location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        }));


        observer = new FileObserver(location.getAbsolutePath(), FILE_EVENTS) {
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
        };

        observer.startWatching();
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }


    @Override
    public File getItem(int position) {
        if (position == super.getCount()) {
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

        if (i == super.getCount()) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.add_button, parent, false);
            view.setTag("button");
            return view;
        }


        File file = getItem(i);

        ViewHolder holder;

        if (view == null || view.getTag() == "button") {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.folder_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.title.setText(file.getName());

        return view;
    }


    private class ViewHolder {
        public TextView title;

        public ViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.title);
        }
    }
}
