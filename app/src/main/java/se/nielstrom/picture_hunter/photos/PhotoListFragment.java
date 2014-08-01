package se.nielstrom.picture_hunter.photos;

import android.content.Context;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
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

public class PhotoListFragment extends Fragment implements NfcAdapter.CreateBeamUrisCallback{
    private static final String KEY_PATH = "KEY_REF_IMAGE_PATH";
    private static final int THUMB_SIZE = 384;

    private String path;
    private File file;
    private PictureAdapter adapter;
    PhotoBehavior behavior;
    private GridView grid;
    private boolean showAddButton = true;
    private NfcAdapter nfcAdapter;


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
            nfcAdapter.setBeamPushUrisCallback(this, getActivity());
        }

        return root;
    }

    public PhotoListFragment setBehavior(PhotoBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    public PhotoListFragment setShowAddButton(boolean showAddButton) {
        this.showAddButton = showAddButton;
        return this;
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
}
