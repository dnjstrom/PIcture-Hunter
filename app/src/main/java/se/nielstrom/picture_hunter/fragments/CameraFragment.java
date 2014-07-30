package se.nielstrom.picture_hunter.fragments;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.views.CameraPreview;

public class CameraFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);
        CameraPreview preview = (CameraPreview) root.findViewById(R.id.camera_preview);
        Camera camera = getCameraInstance();

        if (camera != null) {
            preview.setCamera(camera);
        }

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
}
