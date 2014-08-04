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
import se.nielstrom.picture_hunter.util.Storage;

/**
 * Displays a grid of lazily loaded image miniatures.
 */
public class PhotoListFragment extends Fragment implements NfcAdapter.CreateBeamUrisCallback{
    private static final String KEY_PATH = "KEY_PATH";
    private static final int THUMB_SIZE = 384;

    private String path;
    private File album;
    private PictureAdapter adapter;
    private GridView grid;
    private NfcAdapter nfcAdapter;
    private Storage storage;


    // Creates a new instance for the path given
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

        PhotoBehavior behavior;

        // Switch behavior depending on if this is a user album or a foreign album.
        if (storage.isUserFile(album)) {
            behavior = new UserFileBehavior();
            adapter.setShowAddButton(true);
        } else {
            behavior = new ForeignFileBehavior();
            adapter.setShowAddButton(false); // Don't show an add button
        }

        // Set the behavior
        adapter.setAddListener(behavior);
        grid.setOnItemClickListener(behavior);
        grid.setMultiChoiceModeListener(behavior);


        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            // This should technically never happen as NFC-capabilities are required to install the app.
            Toast.makeText(getActivity(), "NFC is not available", Toast.LENGTH_LONG).show();
        } else {
            nfcAdapter.setBeamPushUrisCallback(this, getActivity());
        }

        return root;
    }

    /**
     * Collects the currently selected photos for beaming when an NFC transfer is initiated.
     *
     * @param nfcEvent
     * @return
     */
    @Override
    public Uri[] createBeamUris(NfcEvent nfcEvent) {
        Uri[] uris = new Uri[grid.getCheckedItemCount()];
        SparseBooleanArray checked = grid.getCheckedItemPositions();

        // Collect selected photos
        for (int i = 0, j=0; i < grid.getCount() && j < uris.length; i++) {
            if (checked.get(i)) {
                uris[j] = Uri.fromFile(adapter.getItem(i));
                j++;
            }
        }

        return uris;
    }


    /**
     * Provides the image-files for the GridView.
     */
    private class PictureAdapter extends FileAdapter {

        public PictureAdapter(Context context, File location) {
            super(context, R.layout.add_picture, location, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    // Only list non-directories. Since we don't actually check for images
                    // explicitly, if the user were to manually move in files in the app storage
                    // area we could potentially run into trouble.
                    return file.isFile();
                }
            });
        }

        @Override
        protected View createFileView(int position, View view, ViewGroup parent) {
            File file = getItem(position);
            boolean matched = ImageSaverTask.readModelData(file).equals(ComparisonActivity.MATCHED);

            ViewHolder holder;

            if (view == null ) {
                // Create a new View and ViewHolder if they don't exist
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.file_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.title.setText(file.getName());
            // Lazily load the image miniature
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

    /**
     * Handles interactions for user files.
     */
    private class UserFileBehavior extends PhotoBehavior {
        public UserFileBehavior() {
            super(PhotoListFragment.this);
        }

        /**
         * Launch the default gallery app or similar to view the image.
         *
         * @param adapterView
         * @param view
         * @param position
         * @param l
         */
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = (File) adapterView.getItemAtPosition(position);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            startActivity(intent);
        }

        /**
         * Launch the apps custom camera fragment in order to capture a photo.
         *
         * @param view
         */
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


    /**
     * Handles interactions for foreign files.
     */
    private class ForeignFileBehavior extends PhotoBehavior {
        public ForeignFileBehavior() {
            super(PhotoListFragment.this);
        }

        /**
         * Launch a ComparisonActivity for the selected file.
         *
         * @param adapterView
         * @param view
         * @param position
         * @param l
         */
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            File file = (File) adapterView.getItemAtPosition(position);
            Intent intent = new Intent(getActivity(), ComparisonActivity.class);
            intent.putExtra(ComparisonActivity.KEY_REF_IMAGE_PATH, file.getAbsolutePath());
            startActivity(intent);
        }
    }
}
