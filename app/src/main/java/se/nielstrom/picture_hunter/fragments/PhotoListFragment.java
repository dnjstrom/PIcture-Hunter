package se.nielstrom.picture_hunter.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.util.FileAdapter;
import se.nielstrom.picture_hunter.util.Storage;

public class PhotoListFragment extends Fragment {
    private static final String KEY_PATH = "KEY_PATH";
    private String path;
    private File file;
    private PictureAdapter adapter;
    private Storage storage;

    public static Fragment newInstance(String absolutePath) {
        PhotoListFragment fragment = new PhotoListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_PATH, absolutePath);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoListFragment() {
        // Required empty public constructor
        storage = new Storage();
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

        adapter = new PictureAdapter(getActivity(), file);
        adapter.setAddListener(new ClickListener());
        grid.setAdapter(adapter);

        grid.setOnItemClickListener(new ClickListener());

        return root;
    }


    private class ClickListener implements AdapterView.OnItemClickListener, View.OnClickListener {

        private static final int REQUEST_IMAGE_CAPTURE = 1;
        private File latestPicture;

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = (File) adapterView.getItemAtPosition(position);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            startActivity(intent);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                latestPicture = storage.createImageFileAt(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(latestPicture));

                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private class PictureAdapter extends FileAdapter {

        public PictureAdapter(Context context, File location) {
            super(context, R.layout.add_picture, location, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile();
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
