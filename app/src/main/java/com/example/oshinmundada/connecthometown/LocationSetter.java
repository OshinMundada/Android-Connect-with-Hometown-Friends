package com.example.oshinmundada.connecthometown;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Created by oshinmundada on 10/04/17.
 */


public class LocationSetter extends Fragment implements View.OnClickListener{
    private GoogleMap gMap;
    private com.google.android.gms.maps.MapView mapView;
    public Double lat = 32.7157;
    public Double lng =-117.1611;
    Button set,cancel;
    CameraPosition camppos;
    List<Address> address;
    LatLng location;
    Bundle bundle;

    public LocationSetter() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_location, container, false);
        bundle = this.getArguments();
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        set = (Button) view.findViewById(R.id.setLocation);
        set.setOnClickListener(this);
        mapView = (com.google.android.gms.maps.MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String addressText = bundle.getString("city")+", "+bundle.getString("state")+", "+bundle.getString("country");
        Geocoder locator = new Geocoder(getActivity());
        try {
            address = locator.getFromLocationName(addressText, 1);
            for (Address addressLocation : address) {
                if (addressLocation.hasLatitude()){
                    lat = addressLocation.getLatitude();}
                if (addressLocation.hasLongitude()){
                    lng = addressLocation.getLongitude();}

            }
            location = new LatLng(lat, lng);
            //gMap.addMarker(new MarkerOptions().position(location));
        } catch (Exception error) {
            error.getMessage();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(getActivity().getApplicationContext());
                gMap = googleMap;
                gMap.getUiSettings().setZoomControlsEnabled(true);
                gMap.getUiSettings().setRotateGesturesEnabled(false);
                gMap.getUiSettings().setScrollGesturesEnabled(true);
                gMap.getUiSettings().setTiltGesturesEnabled(false);
                //LatLng sanDiego = new LatLng(32.7157, -117.1611);
                gMap.addMarker(new MarkerOptions().position(location));
                camppos = CameraPosition.builder().target(location).build();
                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(camppos));
                gMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        gMap.clear();
                        camppos = CameraPosition.builder().target(latLng).build();
                        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(camppos));
                        gMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                        gMap.addMarker(markerOptions);
                        lat = latLng.latitude;
                        lng = latLng.longitude;
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setLocation:
                EditText lati= (EditText) getActivity().findViewById(R.id.latitude_display_label);
                lati.setText(String .valueOf(lat));
                EditText longi=(EditText)getActivity().findViewById(R.id.longitude_display_label);
                longi.setText(String.valueOf(lng));
                getFragmentManager().popBackStack();
                break;
            case R.id.cancel:
                getFragmentManager().popBackStack();
                break;
        }
    }

}

