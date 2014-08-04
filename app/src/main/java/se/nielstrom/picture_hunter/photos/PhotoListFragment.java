package se.nielstrom.picture_hunter.photos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.common.FileAdapter;
import se.nielstrom.picture_hunter.comparator.CameraFragment;
import se.nielstrom.picture_hunter.comparator.ComparisonActivity;
import se.nielstrom.picture_hunter.util.ImageLoaderTask;
import se.nielstrom.picture_hunter.util.ImageSaverTask;
import se.nielstrom.picture_hunter.util.InteractionBehavior;
import se.nielstrom.picture_hunter.util.Storage;

public class PhotoListFragment extends Fragment implements NfcAdapter.CreateBeamUrisCallback{
    private static final String KEY_PATH = "KEY_REF_IMAGE_PATH";
    private static final int THUMB_SIZE = 384;

    private String path;
    private File album;
    private PictureAdapter adapter;
    private GridView grid;
    private NfcAdapter nfcAdapter;
    private Storage storage;


    public static PhotoListFragment newInstance(String absolutePath) {
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
            album = new File(path);
        }
        storage = Storage.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_grid, container, false);

        grid = (GridView) root.findViewById(R.id.grid);

        adapter = new PictureAdapter(getActivity(), album);
        grid.setAdapter(adapter);

        InteractionBehavior behavior;

        if (storage.isUserFile(album)) {
            behavior = new UserFileBehavior();
            adapter.setShowAddButton(true);
        } else {
            behavior = new ForeignFileBehavior();
            adapter.setShowAddButton(false);
        }

        adapter.setAddListener(behavior);
        grid.setOnItemClickListener(behavior);
        grid.setMultiChoiceModeListener(behavior);


        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            Toast.makeText(getActivity(), "NFC is not available", Toast.LENGTH_LONG).show();
        } else {
            nfcAdapter.setBeamPushUrisCallback(this, getActivity());
        }

        return root;
    }

    @Override
    public Uri[] createBeamUris(NfcEvent nfcEvent) {
        Uri[] uris = new Uri[grid.getCheckedItemCount()];
        SparseBooleanArray checked = grid.getCheckedItemPositions();

        for (int i = 0, j=0; i < grid.getCount() && j < uris.length; i++) {
            if (checked.get(i)) {
                uris[j] = Uri.fromFile(adapter.getItem(i));
                j++;
            }
        }

        return uris;
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
            String s = ImageSaverTask.readModelData(file);
            boolean matched = ImageSaverTask.readModelData(file).equals(ComparisonActivity.MATCHED);

            ViewHolder holder;

            if (view == null ) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.file_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.title.setText(file.getName());
            new ImageLoaderTask(holder.image, THUMB_SIZE, THUMB_SIZE).execute(file.getAbsolutePath());
            holder.matched.setVisibility(matched ? View.VISIBLE: View.INVISIBLE);

            return view;
        }


        private class ViewHolder {
            public TextView title;
            public ImageView image;
            private ImageView matched;

            public ViewHolder(View view) {
                title = (TextView) view.findViewById(R.id.title);
                image = (ImageView) view.findViewById(R.id.image);
                matched = (ImageView) view.findViewById(R.id.matched);
            }
        }
    }

    private class UserFileBehavior extends PhotoBehavior {
        public UserFileBehavior() {
            super(PhotoListFragment.this);
        }

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
            File image = storage.createImageFileAt(album);
            CameraFragment fragment = CameraFragment.newInstance(image.getAbsolutePath());
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.grid_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    private class ForeignFileBehavior extends PhotoBehavior {
        public ForeignFileBehavior() {
            super(PhotoListFragment.this);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            File file = (File) adapterView.getItemAtPosition(position);
            Intent intent = new Intent(getActivity(), ComparisonActivity.class);
            intent.putExtra(ComparisonActivity.KEY_REF_IMAGE_PATH, file.getAbsolutePath());
            startActivity(intent);
        }
    }
}
