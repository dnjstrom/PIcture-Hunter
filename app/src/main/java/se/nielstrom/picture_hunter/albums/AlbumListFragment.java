package se.nielstrom.picture_hunter.albums;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.photos.FileAdapter;
import se.nielstrom.picture_hunter.photos.PhotoListActivity;

/**
 * This fragment handles the display of albums in a grid and all associated interactions.
 */
public class AlbumListFragment extends Fragment{
    private static final String KEY_PATH = "KEY_PATH";

    private String path;
    private File folder;
    private AlbumAdapter adapter;
    private GridView grid;


    /**
     * Creates a new instance of the fragment with the path to the directory containing album
     * directories.
     *
     * @param path
     * @return
     */
    public static AlbumListFragment newInstance(String path) {
        AlbumListFragment fragment = new AlbumListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    public AlbumListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(KEY_PATH);
            folder = new File(path);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album_grid, container, false);

        grid = (GridView) root.findViewById(R.id.grid);
        adapter = new AlbumAdapter(getActivity(), folder);
        grid.setAdapter(adapter);


        // Set the interaction handler object
        AlbumBehavior behavior = new AlbumBehavior(this);
        grid.setOnItemClickListener(behavior);
        grid.setMultiChoiceModeListener(behavior);
        adapter.setAddListener(behavior);

        return root;
    }

    /**
     * Provides the album folder views for the GridView.
     */
    private class AlbumAdapter extends FileAdapter {

        public AlbumAdapter(Context context, File location) {
            super(context, R.layout.add_album, location, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory(); // Only list folders
                }
            });
        }

        @Override
        protected View createFileView(int position, View view, ViewGroup parent) {
            File file = getItem(position);

            ViewHolder holder;

            if (view == null ) {
                // Create a new view holder if it doesn't already exist.
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.folder_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag(); // Retrieve existing view holder
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
}
