package com.example.oshinmundada.connecthometown;

/**
 * Created by oshinmundada on 10/04/17.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Lists.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Lists#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Lists extends Fragment implements TextWatcher, AbsListView.OnScrollListener,AdapterView.OnItemClickListener {

    ArrayList<String> userList  = new ArrayList<String>();
    ArrayList<User> userDao;
    boolean flag_loading;
    ListView listView;

    private static CustomAdapter adapter;
    Spinner countrySpinner,stateSpinner;
    ArrayList<String> countries  = new ArrayList<String>();
    ArrayList<String> states =new ArrayList<String>();
    private String countryString="Select Country";
    private String stateString = "Select State";
    private String yearString = "";
    EditText year;
    private CharSequence s;
    SQLiteDatabase db;
    String nick;
    GetUserData info = new GetUserData();
    public Lists() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.lists_fra, container, false);
        listView=(ListView)v.findViewById(R.id.list);

        year=(EditText)v.findViewById(R.id.year_input);
        year.addTextChangedListener(this);
        info.setContextListViewDatabase(db,getContext(),listView);

        Bundle b=this.getArguments();
        nick =b.getString("nickname");
        listView.setOnItemClickListener(this);


        if(savedInstanceState!=null) {
            countryString = savedInstanceState.getString("country");
            stateString = savedInstanceState.getString("state");
            yearString =savedInstanceState.getString("year");
        }
        countrySpinner = (Spinner)v.findViewById(R.id.country_filter);
        stateSpinner = (Spinner)v.findViewById(R.id.state_filter);
        //fetchUsers("Select Country","Select State");
        fetchCountries();

        db=getContext().openOrCreateDatabase("Users", Context.MODE_PRIVATE, null);
        // sqldb.execSQL("CREATE TABLE IF NOT EXISTS student(rollno VARCHAR,name VARCHAR,marks VARCHAR);");
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
        //info.deleteAllFromDatabase(sqldb);
        info.setListView(listView);
        info.getLastIdFromServer();
        info.getLastIdFromDatabase(db);


        info.setUsersFromDatabase(getContext(),db);


        info.getFirstIdFromDatabase(db);
        //info.getLastIdMinFromDatabase(sqldb);

        //infouser = info.getAllUsersFromDatabase();

        // infouser.getUsersFromDatabase(getContext());
        return v;

    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        year = (EditText)getActivity().findViewById(R.id.year_input);

        yearString=year.getText().toString();
        info.setYear(yearString);
        if (yearString.equals("")) {
            if(countryString.equals("Select Country")){
                info.setUsersFromDatabase(getContext(),db);
            }
            else{
                fetchUsers(countryString,stateString);
            }

        }
        else{
            int yearEntered = Integer.valueOf(year.getText().toString());
            if (yearEntered < 1970 || yearEntered > 2017) {
                year.setError("Enter year between 1970 and 2017!");
            }
            else {
                fetchUsers(countryString,stateString);

            }
        }
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
                        //   Toast.makeText(getApplicationContext(), parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                        countryString = parent.getSelectedItem().toString();
                        info.setCountry(countryString);

                        if (countryString.equals("Select Country")) {

                            if(yearString.equals(""))
                            {
                                info.setUsersFromDatabase(getContext(),db);
                            }
                            states.add("Select State");

                        } else {


                            fetchStates();
                            fetchUsers(countryString,"Select State");
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
                        //            Toast.makeText(getApplicationContext(),parent.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
                        stateString=parent.getSelectedItem().toString();
                        info.setState(stateString);
                        fetchUsers(countryString,stateString);
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


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //Toast.makeText(getContext(),"--"+firstVisibleItem+"--"+visibleItemCount+"--total"+totalItemCount,Toast.LENGTH_LONG).show();
        if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
        {
            if(flag_loading == false)
            {
                flag_loading = true;
                Log.e("rew","--"+firstVisibleItem+"--"+visibleItemCount+"--total"+totalItemCount);
            }
        }

    }

    private void fetchUsers(String country,String state) {
        String url;
        int set_year;
        String year_i=year.getText().toString();
        if (year_i.equals("")){
            set_year = 0;
        }
        else{
            set_year = Integer.valueOf(year.getText().toString());
        }
        if (country=="Select Country"){
            url = "http://bismarck.sdsu.edu/hometown/users";
            if (set_year!=0)
                url = "http://bismarck.sdsu.edu/hometown/users?year=" +set_year;
        }else {
            url = "http://bismarck.sdsu.edu/hometown/users?country=" + country;
            if (state != "Select State") {
                try {
                    String encoded= URLEncoder.encode(state,"UTF-8");
                    url = "http://bismarck.sdsu.edu/hometown/users?country=" + country + "&state=" + encoded;
                    if (set_year!=0){
                        url = "http://bismarck.sdsu.edu/hometown/users?country=" + country + "&state=" + encoded + "&year=" +set_year;
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
                    userDao =new ArrayList<User>();
                    for (int i = response.length()-1; i >= 0; i--) {
                        try {
                            Log.i("rew", response.getString(i));
                            JSONObject user_data =response.getJSONObject(i);
                            userDao.add(new User(user_data.getString("nickname"),user_data.getString("country"),user_data.getString("state"),user_data.getString("city"),user_data.getInt("year"),user_data.getDouble("latitude"),user_data.getDouble("longitude")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    CustomAdapter adapter = new CustomAdapter(userDao, getContext());
                    listView.setAdapter(adapter);

                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("rew", "post fail " + new String(error.networkResponse.data));
                //Log.i("Response","nickname"+ ANickname+"pass"+APassword+"Country"+ACountryName+"State"+AState+"city"+ACity+"year"+AYear);

            }
        };

        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        VolleyQueue.instance(getContext()).add(getRequest);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle b=new Bundle();
        TextView t=(TextView)view.findViewById(R.id.nick_name);
        b.putString("nickname", nick);
        b.putString("chatWith",t.getText().toString());
        ChatFrame frag=new ChatFrame();
        frag.setArguments(b);
        getFragmentManager().beginTransaction().replace(R.id.option_detail_container,frag).commit();
    }
}

