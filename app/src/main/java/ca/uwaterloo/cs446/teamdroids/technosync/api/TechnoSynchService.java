package ca.uwaterloo.cs446.teamdroids.technosync.api;

import ca.uwaterloo.cs446.teamdroids.technosync.recordingengine.RecordingList;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface TechnoSynchService {

    @GET("publishRecording")
    Call<RecordingList> publishRecording(@Query("roomId") String roomId);

    @GET("createRoom")
    Call<RoomResponse> createRoom(@Query("roomId") int roomId);

    @GET("joinRoom")
    Call<RoomResponse> joinRoom(@Query("roomId") int roomId);

    @GET("getRecording")
    Call<RecordingList> getRecording(@Query("roomId") int roomId);

    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadCustomAudio (
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file
    );

}
