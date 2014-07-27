package se.nielstrom.picture_hunter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.io.FileFilter;

import se.nielstrom.picture_hunter.PhotoListActivity;
import se.nielstrom.picture_hunter.R;


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
        View root = inflater.inflate(R.layout.fragment_album_list, container, false);

        GridView grid = (GridView) root.findViewById(R.id.gridView);

        grid.setAdapter(new FolderAdapter(getActivity(), file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        })));

        grid.setOnItemClickListener(this);

        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), PhotoListActivity.class);
        File file = (File) adapterView.getAdapter().getItem(i);
        intent.putExtra(PhotoListActivity.KEY_PATH, file.getAbsolutePath());
        startActivity(intent);

    }
}
