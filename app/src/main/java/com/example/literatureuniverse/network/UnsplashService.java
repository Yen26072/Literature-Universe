package com.example.literatureuniverse.network;

import com.example.literatureuniverse.model.unsplash.UnsplashResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UnsplashService {
    @GET("search/photos")
    Call<UnsplashResponse> searchPhotos(
            @Query("query") String query,
            @Query("per_page") int perPage,
            @Query("client_id") String clientId
    );
}
