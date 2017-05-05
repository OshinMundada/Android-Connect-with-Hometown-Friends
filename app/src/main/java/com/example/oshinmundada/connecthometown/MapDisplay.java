package com.example.oshinmundada.connecthometown;

/**
 * Created by oshinmundada on 13/04/17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class MapDisplay extends Fragment implements TextWatcher,GoogleMap.OnInfoWindowClickListener {

    String nickname, state, sta, cntry, city, add,year1;
    // private static CustomAdapter adapter;
    private com.google.android.gms.maps.MapView mapView;
    Spinner countrySpinner,stateSpinner;
    int count, i, j;
    ArrayList<User> data;
    EditText year_input;
    CameraPosition cameraPosition;
    SQLiteDatabase db;
    int queryCount = 0;
    static int reslen;
    String nName,chatWith;
    GetUserData dao = new GetUserData();
    static int lastIdDtabase;
    static int lastIdServer;
    static int firstIdDatabase;
    ArrayList<String> countries  = new ArrayList<String>();
    ArrayList<String> states =new ArrayList<String>();
    private String countryString,stateString,yearString;
    int year;
    private GoogleMap myMap;
    Double latitude, lat,longitude, longi;
    Button loadMore;
    ArrayList<String> userdata = new ArrayList<String>();



    public MapDisplay() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.mapdisplay_frag, container, false);

        Bundle b=this.getArguments();
        nName=b.getString("nickname");

        mapView = (com.google.android.gms.maps.MapView) v.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        countryString = "Select Country";
        stateString = "Select State";

        year_input = (EditText) v.findViewById(R.id.year_input);
        if(savedInstanceState!=null) {
            countryString = savedInstanceState.getString("country");
            stateString = savedInstanceState.getString("state");
            yearString =savedInstanceState.getString("year");
        }
        countrySpinner = (Spinner)v.findViewById(R.id.country_filter);
        stateSpinner = (Spinner)v.findViewById(R.id.state_filter);
        loadMore = (Button)v.findViewById(R.id.more);
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get50MoreLocations();
            }
        });

        fetchCountries();
        year_input.addTextChangedListener(this);


        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(getActivity().getApplicationContext());
                myMap = googleMap;
                myMap.getUiSettings().setZoomControlsEnabled(true);
                myMap.getUiSettings().setRotateGesturesEnabled(false);
                myMap.getUiSettings().setScrollGesturesEnabled(true);
                myMap.getUiSettings().setTiltGesturesEnabled(false);
                myMap.getUiSettings().setCompassEnabled(true);
                myMap.getUiSettings().setAllGesturesEnabled(true);

            }
        });

        db=getContext().openOrCreateDatabase("Users", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + "USERS" + " ("
                + "nick_name" + " TEXT,"
                + "city" + " TEXT,"
                + "longitude" + " TEXT,"
                + "state" + " TEXT,"
                + "year" + " INTEGER,"
                + "id" + " INTEGER,"
                + "latitude" + " TEXT,"
                + "timestamp" + " TEXT,"
                + "country" + " TEXT"
                + ");");


        setUsersFromDatabaseToMap();
        return v;
    }

    public void clearMap() {
        if (myMap != null) {
            myMap.clear();
        } else {
            Log.d("rew", "myMap is null");
        }
    }

    public void displaymarker(Double lat, Double longi) {
        LatLng userLoc = new LatLng(lat, longi);
        myMap.addMarker(new MarkerOptions().position(userLoc).title(nickname));
        myMap.setOnInfoWindowClickListener(this);
    }

    public void getLastIdFromServer(){


        String url = "http://bismarck.sdsu.edu/hometown/nextid";
        Response.Listener<String> success = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response != null) {




                    try {
                        lastIdServer = Integer.parseInt(response);
                        lastIdServer--;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }

        };


        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("rew", "post fail " + new String(error.networkResponse.data));

            }
        };

        StringRequest getRequest = new StringRequest(url, success, failure);
        VolleyQueue.instance(getContext()).add(getRequest);
    }
    public void getFirstIdFromDatabase(SQLiteDatabase sqLiteDatabase){
        db = sqLiteDatabase;

        Cursor mCursor=db.rawQuery("SELECT id FROM USERS ORDER BY ID ASC LIMIT 1;",null);

        if (mCursor.moveToFirst()){
            firstIdDatabase = Integer.parseInt(mCursor.getString(mCursor.getColumnIndex("id")));

        }


        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

    }

    public void getMore100FromUsersMaps(SQLiteDatabase database){
        db = database;


        String url;
        getFirstIdFromDatabase(db);


        url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&beforeid="+firstIdDatabase;
     Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {

                if (response != null) {
                    data = new ArrayList<User>();
                    for (int i = 0; i< 30; i++) {
                        String latitude;
                        String nickname;
                        String longitude;
                        try {
                            JSONObject user_data = response.getJSONObject(i);
                            ContentValues newName = new ContentValues(1);
                            nickname = user_data.getString("nickname");
                            newName.put("nick_name", nickname);
                            newName.put("city", user_data.getString("city"));
                            newName.put("state", user_data.getString("state"));
                            newName.put("country", user_data.getString("country"));
                            longitude = user_data.getString("longitude");
                            latitude = user_data.getString("latitude");


                            newName.put("year", user_data.getString("year"));
                            newName.put("id", user_data.getString("id"));
                            newName.put("timestamp", user_data.getString("time-stamp"));
                            if (latitude.equals("0.0") && longitude == ("0.0")) {
                                String add = "" + user_data.getString("city") + ", " + user_data.getString("state") + ", " + user_data.getString("country");
                                Geocoder locator = new Geocoder(getContext());
                                try {
                                    List<Address> address =
                                            locator.getFromLocationName(add, 1);
                                    for (Address addressLocation : address) {
                                        if (addressLocation.hasLatitude())
                                            latitude = String.valueOf(addressLocation.getLatitude());
                                        if (addressLocation.hasLongitude())
                                            longitude = String.valueOf(addressLocation.getLongitude());
                                    }
                                } catch (Exception error) {
                                }
                            }
                            newName.put("longitude", longitude);
                            newName.put("latitude", latitude);

                            db.insert("USERS", null, newName);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setUsersFromDatabaseToMap();




            }


        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("rew", "post fail " + new String(error.networkResponse.data));

            }
        };

        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        VolleyQueue.instance(getContext()).add(getRequest);



    }



    void get50MoreLocations(){
        getMore100FromUsersMaps(db);
    }
    public void getAllUsersFromDatabaseMap(){

        data.clear();
        clearMap();
        Cursor mCursor =


                db.query(true, "USERS", new String[] {
                                "nick_name",
                                "longitude",
                                "latitude",
                                "timestamp",
                                "city",
                                "state",
                                "country",
                                "year",
                                "id",},null,
                        null,
                        null, null, "id" , null);


        if (mCursor.moveToFirst()) {
            do {
                User user = new User();
                user.setNickname(mCursor.getString(mCursor.getColumnIndexOrThrow("nick_name")));
                user.setYear(Integer.parseInt(mCursor.getString(mCursor.getColumnIndexOrThrow("year"))));
                user.setLongitude(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow("longitude"))));
                user.setLatitude(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow("latitude"))));
                data.add(user);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

        for (i = 0; i < data.size() ; i++) {
            nickname = data.get(i).getNickname();
            year = data.get(i).getYear();
            displaymarker(data.get(i).getLatitude(), data.get(i).getLongitude());
        }

    }



    public void zoomTo(String cnt) {
        Geocoder locator = new Geocoder(getContext());

        try {
            List<Address> address =
                    locator.getFromLocationName(cnt, 1);
            for (Address addressLocation : address) {
                if (addressLocation.hasLatitude())
                    lat = addressLocation.getLatitude();
                if (addressLocation.hasLongitude())
                    longi = addressLocation.getLongitude();
            }
        } catch (Exception error) {
            Log.e("rew", "Error", error);
        }
        LatLng zoomhere = new LatLng(lat, longi);
        myMap.moveCamera(CameraUpdateFactory.newLatLng(zoomhere));
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        year1 = year_input.getText().toString();

        if (year1.equals("")) {
            try {
                if(countryString.equals("Select Country")){
                    clearMap();
                    setUsersFromDatabaseToMap();
                }else{
                    fetchUsers(countryString, stateString);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            int yearEntered = Integer.valueOf(year_input.getText().toString());
            if (yearEntered < 1970 || yearEntered > 2017) {
                year_input.setError("Enter year between 1970 and 2017!");
            } else {
                try {
                    fetchUsers(countryString, stateString);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    public  void setUsersFromDatabaseToMap(){
        String url;
        getLastIdFromServer();
        getFirstIdFromDatabase(db);
        url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&afterid="+lastIdDtabase;
        Log.e("nan",url);
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                if (response != null) {
                    data = new ArrayList<User>();
                    if(response.length()>100)
                        reslen = 100;
                    else
                        reslen = response.length();
                    queryCount++;

                    for (int i = 0; i< reslen; i++) {
                        String latitude;
                        String nickname;
                        String longitude;
                        String state;
                        String country;
                        String city;
                        String add;
                        try {
                            JSONObject user_data = response.getJSONObject(i);
                            ContentValues newName = new ContentValues(1);
                            nickname = user_data.getString("nickname");
                            newName.put("nick_name", nickname);
                            city = user_data.getString("city");
                            state = user_data.getString("state");
                            country = user_data.getString("country");
                            newName.put("city",city );
                            newName.put("state", state);
                            newName.put("country", country);
                            longitude = user_data.getString("longitude");
                            latitude = user_data.getString("latitude");
                            newName.put("year", user_data.getString("year"));
                            newName.put("id", user_data.getString("id"));
                            if (latitude.equals("0.0") && longitude == ("0.0")) {

                                add = "" + city + ", " + state  + ", " + country;
                                Geocoder locator = new Geocoder(getContext());
                                try {
                                    List<Address> address =
                                            locator.getFromLocationName(add, 1);
                                    for (Address addressLocation : address) {
                                        if (addressLocation.hasLatitude())
                                            latitude = String.valueOf(addressLocation.getLatitude());
                                        if (addressLocation.hasLongitude())
                                            longitude = String.valueOf(addressLocation.getLongitude());
                                    }
                                } catch (Exception error) {
                                }
                            }
                            newName.put("longitude", longitude);
                            newName.put("latitude", latitude);
                            db.insert("USERS", null, newName);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    getAllUsersFromDatabaseMap();
                    getFirstIdFromDatabase(db);

                }
            }


        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("rew", "post fail " + new String(error.networkResponse.data));

            }
        };

        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        VolleyQueue.instance(getContext()).add(getRequest);
    }
    @Override
    public void afterTextChanged(Editable s) {

    }

    public void fetchCountries() {
        String url ="http://bismarck.sdsu.edu/hometown/countries";
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                countries.add("Select Country");
                if (response != null) {
                    for (int i=0;i<response.length();i++){
                        try {
                            countries.add(response.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item,countries);

                countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        countryString = parent.getSelectedItem().toString();

                        if (countryString.equals("Select Country")) {
                            clearMap();
                            setUsersFromDatabaseToMap();
                            states.clear();
                            states.add("Select State");

                        } else {

                            fetchStates();
                            try {
                                fetchUsers(countryString,"Select State");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                countrySpinner.setAdapter(dataAdapter);

                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        VolleyQueue.instance(getContext()).add(getRequest);
    }

    public void fetchStates() {
        states.clear();
        states.add("Select State");
        String url ="http://bismarck.sdsu.edu/hometown/states?country="+countryString;
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                if (response != null) {
                    for (int i=0;i<response.length();i++){
                        try {
                            states.add(response.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item,states);

                stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        stateString=parent.getSelectedItem().toString();
                        try {
                            fetchUsers(countryString,stateString);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                stateSpinner.setAdapter(dataAdapter);

                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        VolleyQueue.instance(getContext()).add(getRequest);
    }
    public void fetchUsers(String country, String state) throws UnsupportedEncodingException {

        userdata.clear();
        clearMap();

        String url;
        int set_year;
        String year_i=year_input.getText().toString();
        if (year_i.equals("")){
            set_year = 0;
        }
        else{
            set_year = Integer.valueOf(year_input.getText().toString());
        }
        if (countryString.equals("Select Country")){
            url = "http://bismarck.sdsu.edu/hometown/users";
            if (set_year!=0)
                url = "http://bismarck.sdsu.edu/hometown/users?year=" +set_year;
        }else {
            url = "http://bismarck.sdsu.edu/hometown/users?country=" + countryString;
            if (stateString != "Select State") {

                try {
                    String encoded= URLEncoder.encode(stateString,"UTF-8");
                    url = "http://bismarck.sdsu.edu/hometown/users?country=" + countryString + "&state=" + encoded;
                    if (set_year!=0){
                        url = "http://bismarck.sdsu.edu/hometown/users?country=" + countryString + "&state=" + encoded + "&year=" +set_year;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            else{
                if (set_year!=0){
                    url = "http://bismarck.sdsu.edu/hometown/users?country=" + country  + "&year=" +set_year;
                }
            }
        }


        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {

            public void onResponse(JSONArray response) {
                if (response != null) {
                    data = new ArrayList<User>();

                    for (i = 0; i < response.length(); i++) {
                        try {
                            userdata.add(response.getString(i));
                            JSONObject object = response.getJSONObject(i);
                            nickname = object.getString("nickname");
                            sta = object.getString("state");
                            year = object.getInt("year");
                            longitude = object.getDouble("longitude");
                            latitude = object.getDouble("latitude");
                            cntry = object.getString("country");
                            city = object.getString("city");

                            if (latitude == 0.0d && longitude == 0.0d) {
                                add = "" + city + ", " + sta + ", " + cntry;
                                Geocoder locator = new Geocoder(getContext());
                                try {
                                    List<Address> address =
                                            locator.getFromLocationName(add, 1);
                                    for (Address addressLocation : address) {
                                        if (addressLocation.hasLatitude())
                                            latitude = addressLocation.getLatitude();
                                        if (addressLocation.hasLongitude())
                                            longitude = addressLocation.getLongitude();
                                    }
                                } catch (Exception error) {
                                    Log.e("rew", "Error", error);
                                }
                            }
                            data.add(new User(nickname, sta, cntry, city, year, latitude, longitude));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                    for (i = 0; i < data.size(); i++) {
                        nickname = data.get(i).getNickname();
                        year = data.get(i).getYear();
                        displaymarker(data.get(i).getLatitude(), data.get(i).getLongitude());
                    }

                }
            }


        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };


        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        VolleyQueue.instance(getContext()).add(getRequest);


    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Bundle b=new Bundle();
        b.putString("nickname",nName);
        chatWith=marker.getTitle();
        b.putString("chatWith",chatWith);

        ChatFrame frag=new ChatFrame();
        frag.setArguments(b);
        getFragmentManager().beginTransaction().replace(R.id.option_detail_container,frag).commit();

    }
}