package com.example.oshinmundada.connecthometown;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{
    private FirebaseAuth auth;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        final ListView listView;
        setContentView(R.layout.activity_main);

        if (auth.getCurrentUser() != null) {
            email=auth.getCurrentUser().getEmail();

            Intent i=new Intent(this,UserHome.class);
            startActivity(i);
            finish();
        }
        listView =(ListView)findViewById(R.id.listview);
        final ArrayList<String> list = new ArrayList<String>();
        list.add("Existing User: Login");
        list.add("New User: Register");
        Login fragment = new Login();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, fragment,"com.example.oshinmundada.connecthometown.User Login")
                .commit();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                Bundle arguments = new Bundle();

                if (position == 0) {
                    Login fragment = new Login();
                    // fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment,"com.example.oshinmundada.connecthometown.User Login")
                            .commit();

                } else {
                    AddUser fragment = new AddUser();
                    //fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment,"AddUser com.example.oshinmundada.connecthometown.User")
                            .commit();
                }

            }
        });
    }


}
