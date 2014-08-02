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
import se.nielstrom.picture_hunter.util.InteractionBehavior;


public class AlbumListFragment extends Fragment{
    private static final String PATH = "path";

    private String path;
    private File folder;
    private AlbumAdapter adapter;
    private GridView grid;


    public static AlbumListFragment newInstance(String path) {
        AlbumListFragment fragment = new AlbumListFragment();
        Bundle args = new Bundle();
        args.putString(PATH, path);
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
            path = getArguments().getString(PATH);
            folder = new File(path);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album_grid, container, false);

        grid = (GridView) root.findViewById(R.id.grid);
        adapter = new AlbumAdapter(getActivity(), folder);
        grid.setAdapter(adapter);


        Behavior behavior = new Behavior();
        grid.setOnItemClickListener(behavior);
        grid.setMultiChoiceModeListener(behavior);
        adapter.setAddListener(behavior);

        return root;
    }

    private class AlbumAdapter extends FileAdapter {

        public AlbumAdapter(Context context, File location) {
            super(context, R.layout.add_album, location, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
        }

        @Override
        protected View createFileView(int position, View view, ViewGroup parent) {
            File file = getItem(position);

            ViewHolder holder;

            if (view == null ) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private class Behavior extends AlbumBehavior {
        public Behavior() {
            super(AlbumListFragment.this);
        }

        @Override
        public void onClick(View view) {
            storage.createAlbumAt(folder);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            File file = (File) adapterView.getAdapter().getItem(i);
            Intent intent = new Intent(getActivity(), PhotoListActivity.class);
            intent.putExtra(PhotoListActivity.KEY_PATH, file.getAbsolutePath());
            startActivity(intent);
        }
    }
}
