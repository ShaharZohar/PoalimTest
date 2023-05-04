package com.poalim.exam;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Shahar on 27/08/2019.
 */

// Movie model

public class Movie extends RealmObject {

    @PrimaryKey
    @SerializedName("id")
    public Integer id;
    @SerializedName("title")
    public String title;
    @SerializedName("release_date")
    public String release_date;
    @SerializedName("poster_path")
    public String poster_path;
    @SerializedName("overview")
    public String overview;
    @SerializedName("vote_average")
    public float vote_average;

    public Movie() {
    }

    public Movie(Integer id, String title, String release_date, String poster_path, String overview, float vote_average) {
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.poster_path = poster_path;
        this.overview = overview;
        this.vote_average = vote_average;
    }
}