package com.poalim.exam;

/**
 * Created by Shahar on 27/08/2019.
 */

// All TMDb api's

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface APIInterface {

    String API_KEY = "?api_key=d9dabdfc65794362dd93d55a8a011869";

    @GET("discover/movie" + API_KEY)
    Call<MultipleResource> getMovies(@Query("primary_release_date.gte") String gte, @Query("primary_release_date.lte") String lte, @Query("page") int page);
}
