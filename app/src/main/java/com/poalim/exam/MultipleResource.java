package com.poalim.exam;

/**
 * Created by Shahar on 27/08/2019.
 */

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MultipleResource {

    @SerializedName("page")
    public Integer page;
    @SerializedName("total_results")
    public Integer total;
    @SerializedName("total_pages")
    public Integer totalPages;
    @SerializedName("results")
    public List<Movie> results = null;

}