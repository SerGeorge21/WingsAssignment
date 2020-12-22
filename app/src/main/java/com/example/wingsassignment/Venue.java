package com.example.wingsassignment;

public class Venue {
    private String latitude;
    private String longitude;
    private String name;
    private String address;
    private String city;
    private String postalCode; //maybe make this an int?


    public Venue(String latitude, String longitude, String name, String address, String city, String postalCode){
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    public String toString(){
        return name + "\r\n" + address + "," + city + " " + postalCode + "\r\n"
                + "LAT: " + latitude + "\r\n"
                + "LNG: " + longitude;
    }

    public String getLatitude() { return latitude; }

    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }

    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }

    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}

