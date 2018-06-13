package com.kbaquri.gaze.Second;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Range;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.kbaquri.gaze.ARCore.helpers.CameraPermissionHelper;
import com.kbaquri.gaze.ARCore.helpers.DisplayRotationHelper;
import com.kbaquri.gaze.ARCore.helpers.SnackbarHelper;
import com.kbaquri.gaze.ARCore.helpers.TapHelper;
import com.kbaquri.gaze.ARCore.rendering.BackgroundRenderer;
import com.kbaquri.gaze.ARCore.rendering.ObjectRenderer;
import com.kbaquri.gaze.Api.NearbyPlaces;
import com.kbaquri.gaze.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector3f;

import static android.content.ContentValues.TAG;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;

public class MyCameraFragment extends Fragment implements GLSurfaceView.Renderer, SensorEventListener, LocationListener {

    private GLSurfaceView surfaceView;

    private boolean installRequested;

    private Session session;

    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private TapHelper tapHelper;

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final ObjectRenderer virtualObject = new ObjectRenderer();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];

    //Location-based stuff
    private SensorManager mSensorManager;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private LocationManager mLocationManager;
    public Location mLocation;

    LatLng tempLatLng = null;


    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;

    private float[] mZeroMatrix = new float[16];

    float[] translation = new float[]{0.0f, -0.8f, -0.8f};
    float[] rotation = new float[]{0.0f, -1.00f, 0.0f, 0.3f};
    Pose mPose = new Pose(translation, rotation);
    Frame frame;
    Camera camera;


    List<HashMap<String, String>> nearbyPlacesList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);

        surfaceView = rootView.findViewById(R.id.surfaceView);
        displayRotationHelper = new DisplayRotationHelper(getActivity());
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);

        Matrix.setIdentityM(mZeroMatrix, 0);
        mPose.toMatrix(anchorMatrix, 0);

        tapHelper = new TapHelper(getActivity());
        surfaceView.setOnTouchListener(tapHelper);

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        installRequested = false;

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(getActivity(), !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
                    CameraPermissionHelper.requestCameraPermission(getActivity());
                    return;
                }

                // Create the session.
                session = new Session(getContext());

            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(getActivity(), message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            messageSnackbarHelper.showError(getActivity(), "Camera not available. Please restart the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();

        requestLocationPermission();
        registerSensors();

    }

    public void requestLocationPermission() {
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }

    private void initLocationService() {

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            this.mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }

            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (mLocationManager != null) {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (mLocationManager != null) {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());

        }
    }

    @Override
    public void onLocationChanged(Location myLocation) {

        mLocation = myLocation;
        HashMap<String, String> marker;
        Location markerLocation;

        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            marker = nearbyPlacesList.get(i);
            markerLocation = new Location(marker.get("place_name"));
            markerLocation.setLatitude(Double.parseDouble(marker.get("lat")));
            markerLocation.setLongitude(Double.parseDouble(marker.get("lng")));
            marker.put("distance", String.valueOf(myLocation.distanceTo(markerLocation)));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void registerSensors() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            float azimuth, pitch, bearing;
            Range<Float> azimuthRange, pitchRange;

            float[] rotationMatrixFromVector = new float[16];
            float[] updatedRotationMatrix = new float[16];
            float[] orientationValues = new float[3];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);
            SensorManager
                    .remapCoordinateSystem(rotationMatrixFromVector,
                            SensorManager.AXIS_X, SensorManager.AXIS_Y,
                            updatedRotationMatrix);
            SensorManager.getOrientation(updatedRotationMatrix, orientationValues);

            if (nearbyPlacesList.isEmpty()) {
                return;
            }

            Location markerLocation;

            for (int i = 0; i < nearbyPlacesList.size(); i++) {

                HashMap<String, String> marker = nearbyPlacesList.get(i);

                markerLocation = new Location(marker.get("place_name"));
                markerLocation.setLatitude(Double.parseDouble(marker.get("lat")));
                markerLocation.setLongitude(Double.parseDouble(marker.get("lng")));

                bearing = mLocation.bearingTo(markerLocation);
                azimuth = (float) Math.toDegrees(orientationValues[0]);
                pitch = (float) Math.toDegrees(orientationValues[1]);

                azimuthRange = new Range<>(bearing - 10, bearing + 10);
                pitchRange = new Range<>(-90.0f, -45.0f);

                if (azimuthRange.contains(azimuth) && pitchRange.contains(pitch)) {
                    marker.put("InRange", "true");
                } else {
                    marker.put("InRange", "false");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }


    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onNearbyPlaces(NearbyPlaces nearbyPlaces) {
        this.nearbyPlacesList = nearbyPlaces.getNearbyPlacesList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCoordinate(Coordinate coordinate) {
        tempLatLng = coordinate.getLatLng();

        mLocation = new Location("my_location");
        mLocation.setLatitude(tempLatLng.latitude);
        mLocation.setLongitude(tempLatLng.longitude);

        HashMap<String, String> marker;
        Location markerLocation;

        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            marker = nearbyPlacesList.get(i);
            markerLocation = new Location(marker.get("place_name"));
            markerLocation.setLatitude(Double.parseDouble(marker.get("lat")));
            markerLocation.setLongitude(Double.parseDouble(marker.get("lng")));
            marker.put("distance", String.valueOf(mLocation.distanceTo(markerLocation)));
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();

            messageSnackbarHelper.hide(getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
            Toast.makeText(getContext(), "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(getActivity())) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(getActivity());
            }
            return;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(/*context=*/ getActivity());

            virtualObject.createOnGlThread(/*context=*/ getActivity(), "models/sign.obj", "models/sign.png");
            virtualObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            frame = session.update();
            camera = frame.getCamera();

            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.

            MotionEvent tap = tapHelper.poll();
            /*if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(tap)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon
                    Trackable trackable = hit.getTrackable();
                    // Creates an anchor if a plane or an oriented point was hit.
                    if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                            || (trackable instanceof Point
                            && ((Point) trackable).getOrientationMode()
                            == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                        // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
                        // Cap the number of objects created. This avoids overloading both the
                        // rendering system and ARCore.
                        if (anchors.size() >= 20) {
                            anchors.get(0).detach();
                            anchors.remove(0);
                        }
                        // Adding an Anchor tells ARCore that it should track this position in
                        // space. This anchor is created on the Plane to place the 3D model
                        // in the correct position relative both to the world and to the plane.
                        anchors.add(hit.createAnchor());
                        break;
                    }
                }
            }*/

            // Draw background.
            backgroundRenderer.draw(frame);

            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            // Visualize anchors created by touch.
            float scaleFactor = 0.5f;

            HashMap<String, String> marker;

            if (nearbyPlacesList.isEmpty()) {
                return;
            }

            for (int i = 0; i < nearbyPlacesList.size(); i++) {

                marker = nearbyPlacesList.get(i);

                if (marker.get("InRange").equals("true")) {
                    if (marker.get("ZeroMatrix") == null) {
                        marker.put("ZeroMatrix", Arrays.toString(getCalibrationMatrix()));
                    }
                }

                if (marker.get("ZeroMatrix") == null) {
                    break;
                }

                String[] strings = marker.get("ZeroMatrix").replace("[", "").replace("]", "").split(", ");
                float result[] = new float[strings.length];
                for (int x = 0; x < result.length; x++) {
                    result[x] = Float.parseFloat(strings[x]);
                }

                Matrix.multiplyMM(viewmtx, 0, viewmtx, 0, result, 0);

                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba);

                if (tap != null) {
                    showToast(marker.get("place_name") + ", " + marker.get("vicinity"));
                }
            }

        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    public float[] getCalibrationMatrix() {
        float[] t = new float[3];
        float[] m = new float[16];

        camera.getPose().getTranslation(t, 0);
        float[] z = camera.getPose().getZAxis();
        Vector3f zAxis = new Vector3f(z[0], z[1], z[2]);
        zAxis.y = 0;
        zAxis.normalize();

        double rotate = Math.atan2(zAxis.x, zAxis.z);

        Matrix.setIdentityM(m, 0);
        Matrix.translateM(m, 0, t[0], t[1], t[2]);
        Matrix.rotateM(m, 0, (float) Math.toDegrees(rotate), 0, 1, 0);
        return m;
    }

    private void showToast(final String category) {
        Toast.makeText(getContext(), category, Toast.LENGTH_SHORT).show();
    }

}
