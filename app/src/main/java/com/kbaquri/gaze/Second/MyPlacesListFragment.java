package com.kbaquri.gaze.Second;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kbaquri.gaze.Api.NearbyPlaces;
import com.kbaquri.gaze.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

public class MyPlacesListFragment extends Fragment {

    ListView listView;
    CustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View rootView = inflater.inflate(R.layout.fragment_places_list, container, false);

        listView = rootView.findViewById(R.id.listView);

        return rootView;
    }

    public class CustomAdapter extends ArrayAdapter<HashMap<String, String>> {
        private final Activity _context;
        private List<HashMap<String, String>> nearbyPlacesList;

        public CustomAdapter(Activity context, List<HashMap<String, String>> nearbyPlacesList) {
            super(context, R.layout.fragment_places_list_item, nearbyPlacesList);
            this._context = context;
            this.nearbyPlacesList = nearbyPlacesList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = _context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.fragment_places_list_item, null, true);
            TextView txtLat = rowView.findViewById(R.id.lat);
            TextView txtLng = rowView.findViewById(R.id.lng);
            TextView txtTitle = rowView.findViewById(R.id.title);
            TextView txtSnippet = rowView.findViewById(R.id.snippet);

            HashMap<String, String> googlePlace = nearbyPlacesList.get(position);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");

            txtLat.setText(round(lat, 7) + "");
            txtLng.setText(round(lng, 7) + "");
            txtTitle.setText(placeName);
            txtSnippet.setText(vicinity);

            return rowView;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onNearbyPlaces(NearbyPlaces nearbyPlaces) {
        adapter = new CustomAdapter(getActivity(), nearbyPlaces.getNearbyPlacesList());
        listView.setAdapter(adapter);
    }

}
