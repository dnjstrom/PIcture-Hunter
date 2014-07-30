package se.nielstrom.picture_hunter.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.util.AsyncTaskListener;
import se.nielstrom.picture_hunter.util.ImageLoaderTask;


public class DetailViewFragment extends Fragment {

    private static final String KEY_IMAGE_PATH = "KEY_REF_IMAGE_PATH";
    private String path;
    private File image;

    public static DetailViewFragment newInstance(String image_path) {
        DetailViewFragment fragment = new DetailViewFragment();
        Bundle args = new Bundle();
        args.putString(KEY_IMAGE_PATH, image_path);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(KEY_IMAGE_PATH);
            image = new File(path);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_detail_view, container, false);

        ImageView imgView = (ImageView) root.findViewById(R.id.image);
        final ProgressBar progress = (ProgressBar) root.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        new ImageLoaderTask(imgView).setAsyncTaskListener(new AsyncTaskListener() {
            @Override
            public void onTaskComplete() {
                progress.setVisibility(View.INVISIBLE);
            }
        }).execute(path);
        return root;
    }
}
