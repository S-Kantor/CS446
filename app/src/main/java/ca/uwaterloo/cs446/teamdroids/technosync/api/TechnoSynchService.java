package ca.uwaterloo.cs446.teamdroids.technosync.api;

import ca.uwaterloo.cs446.teamdroids.technosync.recordingengine.RecordingList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TechnoSynchService {

    @GET("publishRecording")
    Call<RecordingList> publishRecording(@Query("roomId") int roomId);

    @GET("createRoom")
    Call<RoomResponse> createRoom(@Query("roomId") int roomId);

    @GET("joinRoom")
    Call<RoomResponse> joinRoom(@Query("roomId") int roomId);

    @GET("getRecording")
    Call<RecordingList> getRecording(@Query("roomId") int roomId);

}
