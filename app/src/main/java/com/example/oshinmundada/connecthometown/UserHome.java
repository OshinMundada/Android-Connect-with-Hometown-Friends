package com.example.oshinmundada.connecthometown;

/**
 * Created by oshinmundada on 10/04/17.
 */

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserHome extends AppCompatActivity implements ValueEventListener {
    private FirebaseAuth.AuthStateListener authstateL;
    private FirebaseAuth fbAuth;
    String mail;
    DatabaseReference dbsRef;
    String nick;
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userhome_act);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        tv =(TextView)findViewById(R.id.userHomeText);

        fbAuth = FirebaseAuth.getInstance();

        authstateL = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(UserHome.this, MainActivity.class));
                    finish();
                }
            }
        };

        mail =user.getEmail();

        dbsRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://connecthometown.firebaseio.com/userDB");
        Query q= dbsRef.orderByKey();
        q.addValueEventListener(this);


        final ListView listView =(ListView)findViewById(R.id.optionsList);
        ArrayList<String> list = new ArrayList<String>();
        list.add("Contacts");
        list.add("View Map Location");
        list.add("Chats");
        list.add("Logout");
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Bundle arguments = new Bundle();
                arguments.putString("email", mail);
                arguments.putString("nickname", nick);

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

               tv.setText(" ");
                switch (position) {

                    case 0:
                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                        Lists fragment = new Lists();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.option_detail_container, fragment, "List Display")
                                .commit();
//                        Toast.makeText(getBaseContext(), "Contacts", Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                        Toast.makeText(getBaseContext(), "View map location", Toast.LENGTH_SHORT).show();
                        MapDisplay mapFrag=new MapDisplay();
                        mapFrag.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction().replace(R.id.option_detail_container,mapFrag,"Map Display").commit();
                        break;

                    case 2:
                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                        Toast.makeText(getBaseContext(), "Chats", Toast.LENGTH_SHORT).show();
                        ChatRecords frag = new ChatRecords();
                        frag.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.option_detail_container, frag, "Chat List")
                                .commit();
                        break;
                    case 3:
                        fbAuth.signOut();
                        break;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        fbAuth.addAuthStateListener(authstateL);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authstateL != null) {
            fbAuth.removeAuthStateListener(authstateL);

        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (authstateL != null) {
            fbAuth.removeAuthStateListener(authstateL);

        }
    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        UserInfo u=new UserInfo();
        //Toast.makeText(this,"inside data change",Toast.LENGTH_SHORT).show();
        for (DataSnapshot d:dataSnapshot.getChildren()){
            u=d.getValue(UserInfo.class);
            if(u.email.equals(mail)){
                nick =u.nickname;
            }
            Log.e("rew",u.email);

        }
        tv.setText("Welcome "+ nick +"!! \n Connect with people from your hometown here");
        u.setEmail(mail);
        u.setNickname(nick);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
