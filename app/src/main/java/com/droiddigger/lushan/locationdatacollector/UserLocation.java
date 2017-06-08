package com.droiddigger.lushan.locationdatacollector;

/**
 * Created by mihodihasan on 6/5/17.
 */

public class UserLocation {

    String username;
    String timeStamp;
    String latitude;
    String longitude;

    public UserLocation() {
    }

    public UserLocation(String username, String timeStamp, String latitude, String longitude) {
        this.username = username;
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
