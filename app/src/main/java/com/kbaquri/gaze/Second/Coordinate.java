package com.kbaquri.gaze.Second;

import com.google.android.gms.maps.model.LatLng;

public class Coordinate {
    LatLng latLng;

    public Coordinate(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
