package se.nielstrom.picture_hunter.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import se.nielstrom.picture_hunter.ComparisonActivity;
import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.util.FileAdapter;
import se.nielstrom.picture_hunter.util.ImageLoaderTask;
import se.nielstrom.picture_hunter.util.InteractionBehavior;
import se.nielstrom.picture_hunter.util.Storage;
import se.nielstrom.picture_hunter.util.ThumbnailZoomer;

public class PhotoListFragment extends Fragment {
    private static final String KEY_PATH = "KEY_IMAGE_PATH";
    private static final int IMAGE_SIZE = 512;
    private static final int THUMB_SIZE = 384;

    private String path;
    private File file;
    private PictureAdapter adapter;
    private Storage storage;
    private File tmpFile;
    InteractionBehavior behavior;

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
        storage = new Storage(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_grid, container, false);

        GridView grid = (GridView) root.findViewById(R.id.photo_grid);

        if (storage.isUserFile(file)) {
            behavior = new UserFileBehavior(this);
        } else {
            behavior = new ForeignFileBehavior(this);
        }

        adapter = new PictureAdapter(getActivity(), file);
        adapter.setAddListener(behavior);
        grid.setAdapter(adapter);

        grid.setOnItemClickListener(behavior);

        return root;
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
                view = inflater.inflate(R.layout.file_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.title.setText(file.getName());
            new ImageLoaderTask(holder.image, THUMB_SIZE, THUMB_SIZE).execute(file.getAbsolutePath());

            return view;
        }


        private class ViewHolder {
            public TextView title;
            public ImageView image;

            public ViewHolder(View view) {
                title = (TextView) view.findViewById(R.id.title);
                image = (ImageView) view.findViewById(R.id.image);
            }
        }
    }


    private class UserFileBehavior extends InteractionBehavior {
        public UserFileBehavior(Fragment fragment) {
            super(fragment);
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
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                try {
                    tmpFile = storage.createTmpFile();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
                    startActivityForResult(intent, 0);
                } catch (IOException e) {}
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (tmpFile == null || !tmpFile.exists()) {
            return;
        }

        new SavePictureTask().execute(tmpFile);
    }


    private class SavePictureTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... files) {
            Bitmap bitmap = BitmapFactory.decodeFile(files[0].getAbsolutePath());
            int size = Math.min(IMAGE_SIZE, Math.min(bitmap.getWidth(), bitmap.getHeight()));
            Bitmap small = ThumbnailUtils.extractThumbnail(bitmap, size, size);

            File imageFile = storage.createImageFileAt(file);
            try {
                FileOutputStream fout = new FileOutputStream(imageFile);
                small.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                fout.flush();
                fout.close();
                tmpFile.delete();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    private class ForeignFileBehavior extends InteractionBehavior {
        public ForeignFileBehavior(Fragment fragment) {
            super(fragment);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            File file = (File) adapterView.getItemAtPosition(position);

            Intent intent = new Intent(getActivity(), ComparisonActivity.class);
            intent.putExtra(ComparisonActivity.KEY_IMAGE_PATH, file.getAbsolutePath());
            startActivity(intent);



            /*
            ImageView thumbView = (ImageView) view.findViewById(R.id.image);
            Bitmap thumb = ((BitmapDrawable)thumbView.getDrawable()).getBitmap();

            final ImageView fullImageView = (ImageView) getView().findViewById(R.id.full_image);

            fullImageView.setImageBitmap(thumb);
            fullImageView.setVisibility(View.VISIBLE);

            File file = (File) adapterView.getItemAtPosition(position);
            new ImageLoaderTask(fullImageView).execute(file.getAbsolutePath());

            fullImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), CameraActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });

            new ThumbnailZoomer(thumbView, fullImageView, getResources().getInteger(
                    android.R.integer.config_shortAnimTime)).zoom();
            */
        }
    }
}
