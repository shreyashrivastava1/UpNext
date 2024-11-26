package com.example.upnext.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Source implements Serializable {
    @SerializedName("id")
    private String id; // Allow null
    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
