package se.nielstrom.picture_hunter.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import se.nielstrom.picture_hunter.R;

/**
 * Created by Daniel on 2014-07-27.
 */
public class FolderAdapter extends BaseAdapter {

    private final File[] files;
    private Context context;

    public FolderAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files;
    }


    @Override
    public int getCount() {
        return files.length + 1;
    }

    @Override
    public File getItem(int position) {
        if (position > files.length) {
            return null;
        } else {
            return files[position];
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        if (i >= files.length) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.add_button, parent, false);
            view.setTag("button");
            return view;
        }


        File file = files[i];

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
