package ca.uwaterloo.cs446.teamdroids.technosync.api;

import android.util.Log;

public class WebApi {

    //Web API is just a facade for variety of Classes (POST, GET, PUT)

    //Post Data
    public void post(ApiPath webPath, String postData){
        //TO-DO -- Implement this using a new class that implements AsyncTask'
        Log.i("POST", postData);
    }

}
