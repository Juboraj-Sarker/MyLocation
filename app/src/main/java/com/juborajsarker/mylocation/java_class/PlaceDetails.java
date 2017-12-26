package com.juborajsarker.mylocation.java_class;


import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * Created by jubor on 12/27/2017.
 */

public class PlaceDetails implements Serializable {

    @Key
    public String status;

    @Key
    public Place result;

    @Override
    public String toString() {
        if (result != null) {
            return result.toString();
        }
        return super.toString();
    }
}
