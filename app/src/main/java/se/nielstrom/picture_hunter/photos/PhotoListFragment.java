package se.nielstrom.picture_hunter.photos;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.comparator.ComparisonActivity;
import se.nielstrom.picture_hunter.util.ImageLoaderTask;
import se.nielstrom.picture_hunter.util.ImageSaverTask;
import se.nielstrom.picture_hunter.util.Storage;

public class PhotoListFragment extends Fragment implements NfcAdapter.CreateNdefMessageCallback {
    private static final String KEY_PATH = "KEY_REF_IMAGE_PATH";
    private static final int IMAGE_SIZE = 512;
    private static final int THUMB_SIZE = 384;

    private String path;
    private File file;
    private PictureAdapter adapter;
    private Storage storage;
    private File tmpFile;
    PhotoBehavior behavior;
    private NfcAdapter nfcAdapter;
    private GridView grid;
    private boolean showAddButton = true;


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
            file = new File(path);
        }
        storage = Storage.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_grid, container, false);

        grid = (GridView) root.findViewById(R.id.grid);
        grid.setOnItemClickListener(behavior);
        grid.setMultiChoiceModeListener(behavior);

        adapter = new PictureAdapter(getActivity(), file);
        adapter.setShowAddButton(showAddButton);
        adapter.setAddListener(behavior);
        grid.setAdapter(adapter);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            Toast.makeText(getActivity(), "NFC is not available", Toast.LENGTH_LONG).show();
        } else {
            nfcAdapter.setNdefPushMessageCallback(this, getActivity());
        }

        return root;
    }

    public PhotoListFragment setBehavior(PhotoBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        Toast.makeText(getActivity(), "Creating ndef-message!", Toast.LENGTH_SHORT).show();
        return null;
    }

    public PhotoListFragment setShowAddButton(boolean showAddButton) {
        this.showAddButton = showAddButton;
        return this;
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
}
