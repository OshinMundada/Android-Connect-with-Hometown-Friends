package com.example.oshinmundada.connecthometown;

/**
 * Created by oshinmundada on 10/04/17.
 */

public class User {
    String nickname, state, country, city,email;
    int year;
    Double latitude, longitude;
    public User(){}

    public User(String nickname, String state, String country, String city, int year, Double latitude, Double longitude) {
        this.nickname = nickname;
        this.state = state;
        this.country = country;
        this.city = city;
        this.year = year;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public User(String nickname,String email) {
        this.nickname = nickname;
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
