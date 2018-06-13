package com.kbaquri.gaze.Api;

import android.os.AsyncTask;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
/**
 * Created by kbaqu on 09-Feb-18.
 */

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {
    private String googlePlacesData;

    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d("GetNearbyPlacesData", "doInBackground entered");
            String url = (String) params[0];
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
            Log.d("GooglePlacesReadTask", "doInBackground Exit");
        } catch (Exception e) {
            Log.d("GooglePlacesReadTask", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");
        List<HashMap<String, String>> nearbyPlacesList = null;
        DataParser dataParser = new DataParser();
        nearbyPlacesList =  dataParser.parse(result);

        EventBus.getDefault().postSticky(new NearbyPlaces(nearbyPlacesList));

        Log.d("GooglePlacesReadTask", "onPostExecute Exit");
    }
}
