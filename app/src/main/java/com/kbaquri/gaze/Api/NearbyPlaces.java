package com.kbaquri.gaze.Api;

import java.util.HashMap;
import java.util.List;

public class NearbyPlaces {

    List<HashMap<String, String>> nearbyPlacesList;

    public NearbyPlaces(List<HashMap<String, String>> nearbyPlacesList){
        this.nearbyPlacesList = nearbyPlacesList;
    }

    public List<HashMap<String, String>> getNearbyPlacesList(){
        return nearbyPlacesList;
    }
}
