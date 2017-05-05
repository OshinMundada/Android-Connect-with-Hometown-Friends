package com.example.oshinmundada.connecthometown;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by oshinmundada on 13/04/17.
 */

public class ChatRecords extends Fragment implements ValueEventListener {
    ListView usersList;
    TextView noUsersText;
    DatabaseReference dbrefer;
    String nick;
    ArrayList<String> userlist;
    ArrayAdapter<String> conlist;

    public ChatRecords() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.chatrecords_frag, container, false);
        Bundle b = this.getArguments();
        nick = b.getString("nickname");

        usersList = (ListView) v.findViewById(R.id.displayChat);
        noUsersText = (TextView) v.findViewById(R.id.noUsersText);
        dbrefer = FirebaseDatabase.getInstance().getReferenceFromUrl("https://connecthometown.firebaseio.com/chatUser");

        Query q = dbrefer.orderByKey();
        q.addValueEventListener(this);
        userlist = new ArrayList<String>();

        usersList.setOnItemClickListener((new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) usersList.getItemAtPosition(position);
                String chatWith=itemValue;
                Fragment fragment = new ChatFrame();
                Bundle arguments=new Bundle();
                arguments.putString("nickname", nick);
                arguments.putString("chatWith",chatWith);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.option_detail_container, fragment, "Chat List Display")
                        .commit();

                Toast.makeText(getContext(), "Chat", Toast.LENGTH_SHORT).show();


            }

        }));

        return v;

    }



    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for(DataSnapshot d:dataSnapshot.getChildren()){
            String key=d.getKey().toString();
            String[] parts=key.split("_");
            String sender=parts[0];
            String receiver=parts[1];
            if(sender.equals(nick))
                userlist.add(receiver);

        }
        try {
            conlist = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, userlist);
            usersList.setAdapter(conlist);
        }catch (Exception e){

        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
