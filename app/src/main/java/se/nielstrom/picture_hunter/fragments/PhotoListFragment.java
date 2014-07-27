package se.nielstrom.picture_hunter.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.File;
import java.io.FileFilter;

import se.nielstrom.picture_hunter.R;

public class PhotoListFragment extends Fragment {
    private static final String KEY_PATH = "KEY_PATH";
    private String path;
    private File file;

    public static Fragment newInstance(String absolutePath) {
        PhotoListFragment fragment = new PhotoListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_PATH, absolutePath);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(KEY_PATH);
            file = new File(path);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_grid, container, false);

        GridView grid = (GridView) root.findViewById(R.id.photo_grid);

        grid.setAdapter(new FileAdapter(getActivity(), file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        })));
        return root;
    }

}
