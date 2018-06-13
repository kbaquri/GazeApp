package com.kbaquri.gaze.Second;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.kbaquri.gaze.Api.NearbyPlaces;
import com.kbaquri.gaze.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;

public class MyARFragment extends Fragment {

    private static final String TAG = MyARFragment.class.getSimpleName();

    private ArFragment arFragment;
    private ArSceneView arSceneView;
    private ViewRenderable infoCardRenderable1;
    private ViewRenderable infoCardRenderable2;
    private ViewRenderable infoCardRenderable3;
    private ViewRenderable infoCardRenderable4;
    private ViewRenderable infoCardRenderable5;
    private ViewRenderable infoCardRenderable6;
    private Node infoCardNode1 = new Node();
    private Node infoCardNode2 = new Node();
    private Node infoCardNode3 = new Node();
    private Node infoCardNode4 = new Node();
    private Node infoCardNode5 = new Node();
    private Node infoCardNode6 = new Node();

    private LocationScene locationScene;

    List<HashMap<String, String>> nearbyPlacesList = new ArrayList<>();
    LatLng tempLatLng = null;
    public Location mLocation;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_ar, container, false);

        arFragment = (ArFragment) getChildFragmentManager().findFragmentById(R.id.ux_fragment);
        arSceneView = arFragment.getArSceneView();

        ViewRenderable.builder()
                .setView(getContext(), R.layout.item_card)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCardRenderable1 = renderable;
                            infoCardNode1.setRenderable(infoCardRenderable1);
                        });
        ViewRenderable.builder()
                .setView(getContext(), R.layout.item_card)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCardRenderable2 = renderable;
                            infoCardNode2.setRenderable(infoCardRenderable2);
                        });
        ViewRenderable.builder()
                .setView(getContext(), R.layout.item_card)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCardRenderable3 = renderable;
                            infoCardNode3.setRenderable(infoCardRenderable3);
                        });
        ViewRenderable.builder()
                .setView(getContext(), R.layout.item_card)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCardRenderable4 = renderable;
                            infoCardNode4.setRenderable(infoCardRenderable4);
                        });
        ViewRenderable.builder()
                .setView(getContext(), R.layout.item_card)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCardRenderable5 = renderable;
                            infoCardNode5.setRenderable(infoCardRenderable5);
                        });
        ViewRenderable.builder()
                .setView(getContext(), R.layout.item_card)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCardRenderable6 = renderable;
                            infoCardNode6.setRenderable(infoCardRenderable6);
                        });

        arSceneView
                .getScene()
                .setOnUpdateListener(
                        frameTime -> {
                            if (locationScene == null) {
                                locationScene = new LocationScene(getContext(), getActivity(), arSceneView);

                                mLocation.setTime(locationScene.deviceLocation.currentBestLocation.getTime()+1);
                                locationScene.deviceLocation.onLocationChanged(mLocation);

                                HashMap<String, String> marker;

                                marker = nearbyPlacesList.get(0);
                                addMarker1(marker.get("place_name"),Double.parseDouble(marker.get("lat")),Double.parseDouble(marker.get("lng")));
                                marker = nearbyPlacesList.get(1);
                                addMarker2(marker.get("place_name"),Double.parseDouble(marker.get("lat")),Double.parseDouble(marker.get("lng")));
                                marker = nearbyPlacesList.get(2);
                                addMarker3(marker.get("place_name"),Double.parseDouble(marker.get("lat")),Double.parseDouble(marker.get("lng")));
                                marker = nearbyPlacesList.get(3);
                                addMarker4(marker.get("place_name"),Double.parseDouble(marker.get("lat")),Double.parseDouble(marker.get("lng")));
                                marker = nearbyPlacesList.get(4);
                                addMarker5(marker.get("place_name"),Double.parseDouble(marker.get("lat")),Double.parseDouble(marker.get("lng")));
                                marker = nearbyPlacesList.get(5);
                                addMarker6(marker.get("place_name"),Double.parseDouble(marker.get("lat")),Double.parseDouble(marker.get("lng")));
                            }

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            if (locationScene != null) {
                                locationScene.processFrame(frame);
                            }
                        });

        return rootView;
    }

    public void addMarker1(String placeName,Double latitude,Double longitude){
        LocationMarker layoutLocationMarker = new LocationMarker(longitude, latitude, infoCardNode1);

        layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
            @Override
            public void render(LocationNode node) {
                View eView = infoCardRenderable1.getView();
                TextView placeNameTextView = eView.findViewById(R.id.textView);
                placeNameTextView.setText(placeName);
                TextView distanceTextView = eView.findViewById(R.id.textView2);
                distanceTextView.setText(node.getDistance() + "M");
            }
        });

        locationScene.mLocationMarkers.add(layoutLocationMarker);
    }
    public void addMarker2(String placeName,Double latitude,Double longitude){
        LocationMarker layoutLocationMarker = new LocationMarker(longitude, latitude, infoCardNode2);

        layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
            @Override
            public void render(LocationNode node) {
                View eView = infoCardRenderable2.getView();
                TextView placeNameTextView = eView.findViewById(R.id.textView);
                placeNameTextView.setText(placeName);
                TextView distanceTextView = eView.findViewById(R.id.textView2);
                distanceTextView.setText(node.getDistance() + "M");
            }
        });

        locationScene.mLocationMarkers.add(layoutLocationMarker);
    }
    public void addMarker3(String placeName,Double latitude,Double longitude){
        LocationMarker layoutLocationMarker = new LocationMarker(longitude, latitude, infoCardNode3);

        layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
            @Override
            public void render(LocationNode node) {
                View eView = infoCardRenderable3.getView();
                TextView placeNameTextView = eView.findViewById(R.id.textView);
                placeNameTextView.setText(placeName);
                TextView distanceTextView = eView.findViewById(R.id.textView2);
                distanceTextView.setText(node.getDistance() + "M");
            }
        });

        locationScene.mLocationMarkers.add(layoutLocationMarker);
    }
    public void addMarker4(String placeName,Double latitude,Double longitude){
        LocationMarker layoutLocationMarker = new LocationMarker(longitude, latitude, infoCardNode4);

        layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
            @Override
            public void render(LocationNode node) {
                View eView = infoCardRenderable4.getView();
                TextView placeNameTextView = eView.findViewById(R.id.textView);
                placeNameTextView.setText(placeName);
                TextView distanceTextView = eView.findViewById(R.id.textView2);
                distanceTextView.setText(node.getDistance() + "M");
            }
        });

        locationScene.mLocationMarkers.add(layoutLocationMarker);
    }
    public void addMarker5(String placeName,Double latitude,Double longitude){
        LocationMarker layoutLocationMarker = new LocationMarker(longitude, latitude, infoCardNode5);

        layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
            @Override
            public void render(LocationNode node) {
                View eView = infoCardRenderable5.getView();
                TextView placeNameTextView = eView.findViewById(R.id.textView);
                placeNameTextView.setText(placeName);
                TextView distanceTextView = eView.findViewById(R.id.textView2);
                distanceTextView.setText(node.getDistance() + "M");
            }
        });

        locationScene.mLocationMarkers.add(layoutLocationMarker);
    }
    public void addMarker6(String placeName,Double latitude,Double longitude){
        LocationMarker layoutLocationMarker = new LocationMarker(longitude, latitude, infoCardNode6);

        layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
            @Override
            public void render(LocationNode node) {
                View eView = infoCardRenderable6.getView();
                TextView placeNameTextView = eView.findViewById(R.id.textView);
                placeNameTextView.setText(placeName);
                TextView distanceTextView = eView.findViewById(R.id.textView2);
                distanceTextView.setText(node.getDistance() + "M");
            }
        });

        locationScene.mLocationMarkers.add(layoutLocationMarker);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        if (locationScene != null) {
            locationScene.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
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
        mLocation.setAccuracy(1);
    }
}
