package ca.uwaterloo.cs446.teamdroids.technosync;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import ca.uwaterloo.cs446.teamdroids.technosync.api.WebApi;
import ca.uwaterloo.cs446.teamdroids.technosync.recordingengine.RecordingList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenu extends AppCompatActivity {

    public Button createGroupSessionButton;
    public Button joinGroupSessionButton;
    public Button practiceButton;
    public Button recordCustomBeatButton;
    public Button changePresetsButton;
    public Button viewRecordingsButton;
    public Button startGroupSession;

    //Animate background
    private void animateBackground(){
        AnimationDrawable animationDrawable = (AnimationDrawable) findViewById(R.id.main_screen_layout).getBackground();
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }

        setContentView(R.layout.activity_main_menu);

        //Get Buttons
        createGroupSessionButton = (Button) findViewById(R.id.createGroupButton);
        setupCreateGroupSessionButton();
        joinGroupSessionButton = (Button) findViewById(R.id.joinGroupButton);
        setupJoinGroupSessionButton();
        practiceButton = (Button) findViewById(R.id.practiceButton);
        setupPracticeButton();
        recordCustomBeatButton = (Button) findViewById(R.id.recordAudioButton2);
        setupCustomBeatButton();
        changePresetsButton = (Button) findViewById(R.id.createPreset2) ;
        setupChangePresetsButton();
        viewRecordingsButton = (Button) findViewById(R.id.audioArchive);
        setupViewRecordingButton();

        //Animate Background
        animateBackground();

        //Check server
        checkServerConnection();
    }

    //Disabled server backed buttons
    private void disabledServerButtons(){
        //Disable
        findViewById(R.id.connection_error).setVisibility(View.VISIBLE);
        createGroupSessionButton.setEnabled(false);
        joinGroupSessionButton.setEnabled(false);
        recordCustomBeatButton.setEnabled(true);
        viewRecordingsButton.setEnabled(false);

        //Set transparency
        createGroupSessionButton.setAlpha(0.5f);
        joinGroupSessionButton.setAlpha(0.5f);
        recordCustomBeatButton.setAlpha(0.5f);
        viewRecordingsButton.setAlpha(0.5f);
    }

    //Check if server is up
    private void checkServerConnection(){
        try{
            //Test Upload
            WebApi webApi = WebApi.getInstance();
            Call<RecordingList> call = webApi.getTechnoSynchService().publishRecording("5");
            call.enqueue(new Callback<RecordingList>() {
                @Override
                public void onResponse(Call<RecordingList> call, Response<RecordingList> response) {
                    Log.i("TechnoSync", "Server check succeeded");
                }

                @Override
                public void onFailure(Call<RecordingList> call, Throwable t) {
                    Log.i("TechnoSync", "Server check failed");
                    disabledServerButtons();
                }
            });
        }
        catch (Exception ex){
            Log.i("Server Error" , "Can not connect! Restart App!");
            disabledServerButtons();
        }
    }

    private void setupCreateGroupSessionButton() {
        createGroupSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.create_group_popup_view, null);

                // create the popup window
                int width = 800;
                int height = 700;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                startGroupSession = (Button) popupView.findViewById(R.id.startGroupSession);

                // Closes the popup window when touch outside.
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                // Removes default background.
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Setup button to start the recording session
                setupStartGroupSessionButton(popupWindow);
            }
        });
    }

    private void setupJoinGroupSessionButton() {
        joinGroupSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.join_group_popup_view, null);

                // create the popup window
                int width = 800;
                int height = 700;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                //startGroupSession = (Button) popupView.findViewById(R.id.joinGroupSessionButton);

                // Closes the popup window when touch outside.
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                // Removes default background.
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Setup button to start the recording session
                setupStartGroupSessionButton(popupWindow);
            }
        });
    }

    private void setupStartGroupSessionButton(PopupWindow popupWindow) {
        // TODO: Implement
        startGroupSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Music  Creation Window
                Intent drumPadIntent = new Intent(getBaseContext(), CreationView.class);

                //Clear activity stack and start new activity
                startActivity(drumPadIntent);

                // Close the current popup
                popupWindow.dismiss();
            }
        });
    }

    private void setupPracticeButton() {
        //Setup Link to Create
        practiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Music  Creation Window
                Intent drumPadIntent = new Intent(getBaseContext(), CreationView.class);

                //Clear activity stack and start new activity
                startActivity(drumPadIntent);
            }
        });
    }

    private void setupCustomBeatButton() {
        //Setup Link to Create
        recordCustomBeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Music  Creation Window
                Intent recordCustomAudio = new Intent(getBaseContext(), CustomRecordingView.class);

                //Clear activity stack and start new activity
                startActivity(recordCustomAudio);
            }
        });
    }

    private void setupChangePresetsButton() {
        // TODO: Implement it
    }

    private void setupViewRecordingButton() {
        // TODO: Implement it #2
    }
}
