package com.example.oshinmundada.connecthometown;

/**
 * Created by oshinmundada on 13/04/17.
 */

public class UserInfo {
    public String nickname;
    public String email;

    public UserInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

