package com.example.oshinmundada.connecthometown;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.oshinmundada.connecthometown.R.id.city_input;
import static com.example.oshinmundada.connecthometown.R.id.done_button;
import static com.example.oshinmundada.connecthometown.R.id.email_input;
import static com.example.oshinmundada.connecthometown.R.id.latitude_display_label;
import static com.example.oshinmundada.connecthometown.R.id.longitude_display_label;
import static com.example.oshinmundada.connecthometown.R.id.nickname_input;
import static com.example.oshinmundada.connecthometown.R.id.password_input;
import static com.example.oshinmundada.connecthometown.R.id.year_input;

/**
 * Created by oshinmundada on 10/04/17.
 */

public class AddUser extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {
    Button getLocation;
    Spinner countrySpinner,stateSpinner;
    Double a_latitude, a_longitude;
    ArrayList<String> countries  = new ArrayList<String>();
    ArrayList<String> states =new ArrayList<String>();

    FirebaseAuth auth;
    String user_email, user_password;
    DatabaseReference dbRef;

    private EditText nickname,city,latitude,longitude,password,year,email;
    String nickname_user;
    String ACountryName,AState,ANickname,APassword,ACity;
    int AYear;



    public AddUser() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_register, container, false);
        Button button = (Button) v.findViewById(done_button);

        getLocation = (Button)v.findViewById(R.id.setmap);
        getLocation.setOnClickListener(this);

        nickname = (EditText)v.findViewById(nickname_input);
        nickname.setOnFocusChangeListener((View.OnFocusChangeListener) this);
        city = (EditText)v.findViewById(city_input);
        city.setOnFocusChangeListener(this);
        year =(EditText)v.findViewById(year_input);
        year.setOnFocusChangeListener(this);
        password = (EditText)v.findViewById(password_input);
        password.setOnFocusChangeListener(this);
        latitude = (EditText)v.findViewById(latitude_display_label);
        latitude.setOnFocusChangeListener(this);
        longitude = (EditText)v.findViewById(longitude_display_label);
        longitude.setOnFocusChangeListener(this);
        email=(EditText)v.findViewById(email_input);
        email.setOnFocusChangeListener(this);
        fetchCountries();
        countrySpinner=(Spinner)v.findViewById(R.id.country_input);
        stateSpinner=(Spinner)v.findViewById(R.id.state_input);
        button.setOnClickListener(this);
        auth = FirebaseAuth.getInstance();
        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case done_button:
                validate();
                user_email = email.getText().toString().trim();
                user_password = password.getText().toString().trim();
                auth.createUserWithEmailAndPassword(user_email, user_password)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(),"Registered new user",Toast.LENGTH_SHORT).show();
                                    addUser(task.getResult().getUser());
                                    Intent intent = new Intent(getActivity(), UserHome.class);
                                    startActivity(intent);
                                }
                            }
                        });

                Toast.makeText(getContext(),"Done!",Toast.LENGTH_SHORT);


                break;

            case R.id.setmap:
                Fragment mapLocation =new LocationSetter();
                Bundle bundle=new Bundle();
                bundle.putString("city",city.getText().toString());
                bundle.putString("state", stateSpinner.getSelectedItem().toString());
                bundle.putString("country",countrySpinner.getSelectedItem().toString());
                mapLocation.setArguments(bundle);
                FragmentManager fm=getFragmentManager();
                FragmentTransaction t=fm.beginTransaction();
                t.hide(getFragmentManager().findFragmentByTag("AddUser com.example.oshinmundada.connecthometown.User"));
                t.add(R.id.item_detail_container,mapLocation,"setLocation");
                t.addToBackStack("setLocation");
                t.commit();
                break;
        }

    }

    public void addUser(FirebaseUser user){
        String  nickname_ip = nickname.getText().toString().trim();
        addDataToServer(user.getUid(),nickname_ip,user.getEmail());


    }
    public void addDataToServer(String uid,String nickname,String email){
        User user=new User(nickname,email);
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://connecthometown.firebaseio.com/userDB");
        dbRef.child(uid).setValue(user);
    }

    @Override
    public void onFocusChange(View view, boolean Focus) {
        switch(view.getId()){
            case R.id.nickname_input:
                if(!Focus){
                    if( nickname.getText().toString().length()==0 ){
                        nickname.setError( "Please enter Nickname!" );
                    }
                    else{
                        nickname_user =nickname.getText().toString();
                        dupCheckNickName();
                    }
                }
                break;

            case R.id.city_input:
                if (!Focus) {
                    if (city.getText().toString().length()==0) {
                        city.setError("Please enter City!");
                    }
                }
                break;
            case R.id.year_input:
                if(!Focus){
                    if(year.getText().toString().length()==0)
                        year.setError("Please enter Year!");
                    if(year.getText().toString().length()>0) {
                        int yearEntered=Integer.valueOf(year.getText().toString());
                        if (yearEntered < 1970 || yearEntered > 2017)
                            year.setError("Enter year between 1970 and 2017!");
                    }
                }
                break;

            case R.id.password_input:
                if (!Focus) {
                    if ( password.getText().toString().length() == 0) {
                        password.setError("Please enter Password!");
                    }
                    else if (password.getText().toString().length() < 3){
                        password.setError("Password should contain atleast 3 characters!");
                    }
                }
                break;



        }
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
                countrySpinner = (Spinner) getActivity().findViewById(R.id.country_input);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,countries);

                countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ACountryName = parent.getSelectedItem().toString();

                        if (ACountryName.equals("Select Country")) {
                            states.add("Select State");
                            stateSpinner = (Spinner) getActivity().findViewById(R.id.state_input);
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,states);

                            stateSpinner.setAdapter(dataAdapter);
                            fetchStates();

                        } else {

                            fetchStates();
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
        String url ="http://bismarck.sdsu.edu/hometown/states?country="+ACountryName;
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
                stateSpinner = (Spinner) getActivity().findViewById(R.id.state_input);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,states);

                stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        AState=parent.getSelectedItem().toString();
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

    public void dupCheckNickName() {

        String url ="http://bismarck.sdsu.edu/hometown/nicknameexists?name="+ nickname_user;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    public void onResponse(String response) {
                        if(response.startsWith("true")){
                            nickname.setError( "Nickname Already Exists!" );
                        }
                    }
                }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
            }
        });
        VolleyQueue.instance(getContext()).add(stringRequest);
    }
    private void registerUser() throws JSONException {
        final String url = "http://bismarck.sdsu.edu/hometown/adduser";
        JSONObject data = new JSONObject();
        ANickname = nickname.getText().toString();
        ACity =city.getText().toString();
        APassword =password.getText().toString();
        AYear =Integer.valueOf(year.getText().toString());
        a_latitude =Double.valueOf(latitude.getText().toString());
        a_longitude = Double.valueOf(longitude.getText().toString());
        data.put("nickname", ANickname);
        data.put("password", APassword);
        data.put("country", ACountryName);
        data.put("state",AState);
        data.put("city",ACity);
        data.put("year",AYear);
        data.put("latitude", a_latitude);
        data.put("longitude", a_longitude);


        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse (JSONObject response){
                Log.i("rew", response.toString());

            }

        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("rew", "post fail " + new String(error.networkResponse.data));

            }
        };
        JsonObjectRequest postRequest = new JsonObjectRequest(url, data, success, failure);
        VolleyQueue.instance(getContext()).add(postRequest);
    }


    public String viewToString(View v){
        EditText view = (EditText) v;
        return view.getText().toString();
    }

    public int viewToInt(View v){
        EditText view = (EditText) v;
        return Integer.parseInt(view.getText().toString());
    }

    public void validate(){
        if (viewToString(nickname).length() == 0) {
            nickname.requestFocus();
        }
        else if (viewToString(password).length() == 0) {
            password.requestFocus();
        } else if (viewToString(city).length() == 0) {
            city.requestFocus();
        } else if (viewToString(year).length() == 0) {
            year.requestFocus();
        } else if (countrySpinner.getSelectedItemPosition() == 0) {
            TextView i = (TextView) countrySpinner.getSelectedView();
            i.setError("Select Country");
            countrySpinner.requestFocus();
        } else if (stateSpinner.getSelectedItemPosition() == 0) {
            TextView i = (TextView) stateSpinner.getSelectedView();
            i.setError("Select State");
            stateSpinner.requestFocus();
        }else if (viewToString(year)!=null) {
            if (viewToInt(year) < 1970 || viewToInt(year) > 2017) {
                year.setError("Enter year between 1970 and 2017!");
            }
            else if(nickname.getError()== null && password.getError()== null && city.getError()== null
                    && year.getError()== null){
                try {
                    registerUser();
                    Toast.makeText(getContext(), "User added successfully!", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(getContext(), "Invalid data!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
