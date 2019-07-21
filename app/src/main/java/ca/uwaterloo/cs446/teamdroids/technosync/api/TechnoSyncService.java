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
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface TechnoSyncService {

    @GET("health-check")
    Call<String> healthCheck();

    @POST("create-room")
    Call<String> createRoom();

    @POST("{room-id}/is-valid-room-id")
    Call<String> joinRoom(@Path(value = "room-id") String roomId);

    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadCustomAudio(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file
    );

    @POST("{room-id}/start-recording")
    Call<String> startRecording(@Path(value = "room-id") String roomId);

    @POST("{room-id}/stop-recording")
    Call<String> stopRecording(@Path(value = "room-id") String roomId);

    @Streaming
    @GET("{room-id}/get-composition")
    Call<ResponseBody> downloadComposedSongAsMp3(@Path(value = "room-id") String roomId);

}
