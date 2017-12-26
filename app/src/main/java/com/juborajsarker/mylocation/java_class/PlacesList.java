package com.juborajsarker.mylocation.java_class;


import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jubor on 12/27/2017.
 */

public class PlacesList implements Serializable {

    @Key
    public String status;

    @Key
    public List<Place> results;
}
