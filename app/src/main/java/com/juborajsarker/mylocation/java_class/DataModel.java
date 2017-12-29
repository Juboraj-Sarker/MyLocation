package com.juborajsarker.mylocation.java_class;

/**
 * Created by jubor on 12/28/2017.
 */

public class DataModel {

    String address;
    String city;
    String subCity;
    String posterCode;
    String division;
    String country;
    String countryCode;
    String latitude;
    String longitude;


    public DataModel(String address, String city, String subCity, String posterCode,
                     String division, String country, String countryCode, String latitude,
                     String longitude) {

        this.address = address;
        this.city = city;
        this.subCity = subCity;
        this.posterCode = posterCode;
        this.division = division;
        this.country = country;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getSubCity() {
        return subCity;
    }

    public String getPosterCode() {
        return posterCode;
    }

    public String getDivision() {
        return division;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
