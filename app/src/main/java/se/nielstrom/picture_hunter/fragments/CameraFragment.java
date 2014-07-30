package se.nielstrom.picture_hunter.fragments;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.util.AsyncTaskListener;
import se.nielstrom.picture_hunter.util.ImageSaverTask;
import se.nielstrom.picture_hunter.util.Storage;
import se.nielstrom.picture_hunter.views.CameraPreview;

public class CameraFragment extends Fragment implements View.OnClickListener {
    private static final java.lang.String KEY_IMAGE_PATH = "KEY_REF_IMAGE_PATH";
    private final Storage storage;
    private Camera camera;
    private String path;
    private File image;
    private PictureCapturedListener listener;

    public CameraFragment() {
        storage = new Storage();
    }

    public static CameraFragment newInstance(String image_path) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putString(KEY_IMAGE_PATH, image_path);
        fragment.setArguments(args);
        return fragment;
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
        View root = inflater.inflate(R.layout.fragment_camera, container, false);
        CameraPreview preview = (CameraPreview) root.findViewById(R.id.camera_preview);
        camera = getCameraInstance();

        if (camera != null) {
            preview.setCamera(camera);
        } else {
            return root;
        }

        root.setOnClickListener(this);

        return root;
    }


    private boolean hasCamera() {
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onClick(View view) {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                new ImageSaverTask(data, image)
                        .includeLocation(true)
                        .setAsyncTaskListener(new AsyncTaskListener() {
                            @Override
                            public void onTaskComplete() {
                                if (listener != null) {
                                    listener.onPictureCaptured(image);
                                }
                            }
                        })
                        .execute();
            }
        });
    }

    public CameraFragment setOnPictureTakenListener(PictureCapturedListener listener) {
        this.listener = listener;
        return this;
    }

    public interface PictureCapturedListener {
        public void onPictureCaptured(File picture);
    }
}
