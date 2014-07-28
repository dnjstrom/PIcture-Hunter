package se.nielstrom.picture_hunter.fragments;

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

import se.nielstrom.picture_hunter.PhotoListActivity;
import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.util.FileAdapter;


public class AlbumListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String PATH = "path";

    private String path;
    private File file;


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
            file = new File(path);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album_grid, container, false);

        GridView grid = (GridView) root.findViewById(R.id.album_grid);

        grid.setAdapter(new AlbumAdapter(getActivity(), file));

        grid.setOnItemClickListener(this);

        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), PhotoListActivity.class);
        File file = (File) adapterView.getAdapter().getItem(i);
        File parent = file.getParentFile();
        intent.putExtra(PhotoListActivity.KEY_PATH, file.getParentFile().getAbsolutePath());
        intent.putExtra(PhotoListActivity.KEY_POSITION, i);
        startActivity(intent);
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
}
