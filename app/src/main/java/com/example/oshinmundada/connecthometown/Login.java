package com.example.oshinmundada.connecthometown;

/**
 * Created by oshinmundada on 10/04/17.
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class Login extends Fragment implements View.OnClickListener, ValueEventListener {
    private FirebaseAuth auth;
    EditText emailInput,passwordInput;
    Button login;
    String email_ip,nName,email;
    DatabaseReference mReference;
    public Login() {
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

        View v = inflater.inflate(R.layout.login_fra, container, false);
        emailInput = (EditText) v.findViewById(R.id.inputEmail);
        passwordInput = (EditText) v.findViewById(R.id.inputPassword);
        login = (Button) v.findViewById(R.id.loginBtn);
        login.setOnClickListener(this);
        auth = FirebaseAuth.getInstance();
        email_ip=emailInput.getText().toString();
        auth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://connecthometown.firebaseio.com/userDb");
        Query q=mReference.orderByKey();
        q.addValueEventListener(this);


        return v;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginBtn:
                login_user();
                break;

        }
    }

    public void login_user() {
        final String emailText = emailInput.getText().toString();
        final String passwordText = passwordInput.getText().toString();

        auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(),"Wrong username or password!"+emailText+" "+passwordText,Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(),"Logged In!",Toast.LENGTH_SHORT).show();

                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


                            auth = FirebaseAuth.getInstance();

                            FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                                @Override
                                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    email=user.getEmail();

                                }
                            };
                            Intent intent = new Intent(getActivity(), UserHome.class);
                            intent.putExtra("nickname",nName);
                            startActivity(intent);
                            getActivity().finish();

                        }
                    }
                });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        UserInfo u = new UserInfo();

        for(DataSnapshot d:dataSnapshot.getChildren()) {
            u= d.getValue(UserInfo.class);
            if (u.email.equals(email)) {
                nName = u.nickname;
            }


        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

}


