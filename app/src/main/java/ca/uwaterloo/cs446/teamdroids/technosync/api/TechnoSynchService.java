package ca.uwaterloo.cs446.teamdroids.technosync.api;

import ca.uwaterloo.cs446.teamdroids.technosync.recordingengine.RecordingList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Headers;

public interface TechnoSynchService {

    @GET("")
    Call<RecordingList> publishRecording(@Query("roomId") String roomId);

    @POST("create-room")
    Call<String> createRoom();

    @POST("{room-id}/is-valid-room-id")
    Call<String> joinRoom(@Path(value = "room-id") String roomId);

    @GET("getRecording")
    Call<RecordingList> getRecording(@Query("roomId") int roomId);

}
