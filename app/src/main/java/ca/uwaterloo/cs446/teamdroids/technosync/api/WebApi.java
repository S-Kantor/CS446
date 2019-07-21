package ca.uwaterloo.cs446.teamdroids.technosync.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class WebApi {

    private static WebApi instance = null;
    private static final String BASE_URL = "http://10.0.2.2:5000/";

    private TechnoSyncService technoSyncService;

    public static WebApi getInstance() {
        if (instance == null) {
            instance = new WebApi();
        }

        return instance;
    }

    private WebApi() {
        buildRetrofit();
    }

    private void buildRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        this.technoSyncService = retrofit.create(TechnoSyncService.class);
    }


    public TechnoSyncService getTechnoSyncService() {
        return this.technoSyncService;
    }
}
