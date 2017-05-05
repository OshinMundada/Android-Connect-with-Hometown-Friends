package com.example.oshinmundada.connecthometown;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oshinmundada on 13/04/17.
 */

public class GetUserData {
    ArrayList<User> infouser;
    int slen;
    static SQLiteDatabase sqldb;
    int cntq = 0;
    ListView listView;
    static String stateString = "Select State";
    static String countryString = "Select Country";
    static String yearString = "";
    static Context thiscon;
    static int dblastid;
    static int serlastid;
    static int dbfirstid;
    int pos;

    ArrayList<User> reverse = new ArrayList<>();


    public void setCountry(String country){
        countryString = country;
    }
    public void setState(String state){
        stateString = state;
    } public void setYear(String year){
        yearString =year;
    }


    public void getMore100FromUsers(SQLiteDatabase database){
        sqldb = database;
        String url;
        url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&beforeid="+ dbfirstid;
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                if (response != null) {
                    infouser = new ArrayList<User>();
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
                                Geocoder locator = new Geocoder(thiscon);
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
                            sqldb.insert("USERS", null, newName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    getLastIdFromDatabase(sqldb);
                    getLastIdFromServer();
                    getFirstIdFromDatabase(sqldb);
                }
                setUsersFromDatabase(thiscon, sqldb);

            }


        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("rew", "post fail " + new String(error.networkResponse.data));
            }
        };

        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        VolleyQueue.instance(thiscon).add(getRequest);
    }

    public void getLastIdFromDatabase(SQLiteDatabase sqLiteDatabase){
        sqldb = sqLiteDatabase;
        Cursor mCursor= sqldb.rawQuery("SELECT id FROM USERS ORDER BY ID DESC LIMIT 1;",null);

        if (mCursor.moveToFirst()){
            dblastid = Integer.parseInt(mCursor.getString(mCursor.getColumnIndex("id")));
        }


        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

    }

    public void getFirstIdFromDatabase(SQLiteDatabase sqLiteDatabase){
        sqldb = sqLiteDatabase;
        Cursor mCursor= sqldb.rawQuery("SELECT id FROM USERS ORDER BY ID ASC LIMIT 1;",null);

        if (mCursor.moveToFirst()){
            dbfirstid = Integer.parseInt(mCursor.getString(mCursor.getColumnIndex("id")));
        }
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

    }

    public void getLastIdFromServer(){


        String url = "http://bismarck.sdsu.edu/hometown/nextid";
        Response.Listener<String> success = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response != null) {




                    try {

                        serlastid = Integer.parseInt(response);
                        serlastid--;



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
        VolleyQueue.instance(thiscon).add(getRequest);
    }
    public  void setUsersFromDatabase(final Context context1, SQLiteDatabase d) {
        sqldb = d;
        String url;
        thiscon = context1;
        getLastIdFromDatabase(sqldb);
        getLastIdFromServer();
        getFirstIdFromDatabase(sqldb);
        url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&afterid="+ dblastid;
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                if (response != null) {
                    infouser = new ArrayList<User>();
                    if(response.length()>100)
                        slen = 100;
                    else
                        slen = response.length();
                    cntq++;
                    for (int i = 0; i< slen; i++) {
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
                                Geocoder locator = new Geocoder(thiscon);
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
                            sqldb.insert("USERS", null, newName);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    getAllUsersFromDatabase();
                    getFirstIdFromDatabase(sqldb);
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
        VolleyQueue.instance(thiscon).add(getRequest);
    }




    public void setListView(ListView view){
        listView = view;
    }


    public ArrayList<User> getAllUsersFromDatabase(){

        if(infouser !=null){
            infouser.clear();
           }

        Cursor mCursor =
                sqldb.query(true, "USERS", new String[] {
                                "nick_name",
                                "longitude",
                                "longitude",
                                "timestamp",
                                "city",
                                "state",
                                "country",
                                "year",
                                "id",},null,
                        null,
                        null, null, "id" , null);
        cntq++;

        if (mCursor.moveToFirst()) {
            do {
                User user = new User();
                user.setCity(mCursor.getString(mCursor.getColumnIndexOrThrow("city")));
                user.setState(mCursor.getString(mCursor.getColumnIndexOrThrow("state")));
                user.setNickname(mCursor.getString(mCursor.getColumnIndexOrThrow("nick_name")));
                user.setCountry(mCursor.getString(mCursor.getColumnIndexOrThrow("country")));
                user.setYear(Integer.parseInt(mCursor.getString(mCursor.getColumnIndexOrThrow("year"))));
                infouser.add(user);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

        int i=0;
        reverse.clear();
        for(i = infouser.size()-1; i>=0; i--){
            reverse.add(infouser.get(i));
        }

        infouser.clear();
        infouser = reverse;
        CustomAdapter adapter = new CustomAdapter(infouser, thiscon);
        listView.setSelection(pos);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int firstVisibleItem1;
            int visibleItemCount1;
            int totalItemCount1;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(firstVisibleItem1+visibleItemCount1 == totalItemCount1 && totalItemCount1!=0 && scrollState==SCROLL_STATE_IDLE)
                {
                    if(countryString.equals("Select Country") && yearString.equals("") ) {
                        pos =totalItemCount1;
                        getMore100FromUsers(sqldb);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstVisibleItem1 = firstVisibleItem;
                visibleItemCount1 = visibleItemCount;
                totalItemCount1 = totalItemCount;
            }
        });
        return infouser;
    }

    public void setContextListViewDatabase(SQLiteDatabase db1, Context contextq, ListView listView1) {

        sqldb = db1;
        thiscon = contextq;
        listView = listView1;


    }


}