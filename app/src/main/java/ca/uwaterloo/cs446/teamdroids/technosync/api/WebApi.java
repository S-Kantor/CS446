package ca.uwaterloo.cs446.teamdroids.technosync.api;

import android.util.Log;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebApi {

    private static WebApi instance = null;
    private static final String BASE_URL = "http://thiserverdoesnotexist";

    private TechnoSynchService technoSynchService;

    public static WebApi getInstance() {
        if (instance == null) {
            instance = new WebApi();
        }

        return instance;
    }

    private WebApi () {
        buildRetrofit();
    }

    private void buildRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.technoSynchService = retrofit.create(TechnoSynchService.class);
    }


    public TechnoSynchService getTechnoSynchService() {
        return this.technoSynchService;
    }

    // USAGE:
    //

    //Post Data
    public void post(ApiPath webPath, String postData){
        //TO-DO -- Implement this using a new class that implements AsyncTask'
        Log.i("POST", postData);
    }

}
