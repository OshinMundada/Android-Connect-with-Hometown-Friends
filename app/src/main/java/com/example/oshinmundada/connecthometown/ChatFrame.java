package com.example.oshinmundada.connecthometown;

/**
 * Created by oshinmundada on 13/04/17.
 */

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class ChatFrame extends Fragment implements ValueEventListener {
    LinearLayout layout;
    ImageView sendmsg;
    EditText msg;
    ScrollView vScroll;
    Firebase fbase, fbase1;
    String nName,chatWith;

    public ChatFrame() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.chatframe_frag, container, false);
        layout = (LinearLayout)v.findViewById(R.id.layout1);
        sendmsg = (ImageView)v.findViewById(R.id.sendButton);
        msg = (EditText)v.findViewById(R.id.messageArea);
        vScroll = (ScrollView)v.findViewById(R.id.scrollView);

        Bundle b=this.getArguments();
        nName=b.getString("nickname");
        chatWith=b.getString("chatWith");
        Firebase.setAndroidContext(getContext());
        fbase = new Firebase("https://connecthometown.firebaseio.com/chatUser/" + nName + "_"+chatWith);
        fbase1 = new Firebase("https://connecthometown.firebaseio.com/chatUser/" + chatWith + "_" + nName);

        sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = msg.getText().toString();

                if (!messageText.equals("")) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", nName);
                    fbase.push().setValue(map);
                    fbase1.push().setValue(map);
                }
            }
        });

        fbase.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if (userName.equals(nName)) {
                    addMessageBox("You\n" + message, 1);
                } else {
                    addMessageBox(chatWith + "\n" + message, 2);
                }
            }

            @Override
            public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        return v;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void addMessageBox(String message, int type) {
        try {
            TextView textView = new TextView(getContext());
            textView.setText(message);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 10);
            textView.setLayoutParams(lp);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            if (type == 1) {
                textView.setGravity(Gravity.LEFT);
            } else {
                textView.setGravity(Gravity.RIGHT);
            }

            layout.addView(textView);
            vScroll.fullScroll(View.FOCUS_DOWN);
        }
        catch (Exception e){

        }
    }

}
