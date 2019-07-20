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
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import ca.uwaterloo.cs446.teamdroids.technosync.api.WebApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenu extends AppCompatActivity {

    public Button createGroupSessionButton;
    public Button joinGroupSessionButton;
    public Button practiceButton;
    public Button recordCustomBeatButton;
    public Button changePresetsButton;
    public Button viewComposedSongsButton;
    public Button startGroupSession;
    public TextView connectionErrorText;
    public EditText groupCodeEditText;

    public String newGroupId;
    public WebApi webApi;

    //Animate background
    private void animateBackground() {
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
        changePresetsButton = (Button) findViewById(R.id.createPreset2);
        setupChangePresetsButton();
        viewComposedSongsButton = (Button) findViewById(R.id.audioArchive);
        setupViewComposedSongsButton();
        connectionErrorText = (TextView) findViewById(R.id.connection_error);
        connectionErrorText.setVisibility(View.GONE);

        // Initialize WebAPI
        webApi = WebApi.getInstance();

        //Animate Background
        animateBackground();

        //Check server
        checkServerConnection();
    }

    //Disabled server backed buttons
    private void disabledServerButtons() {
        //Disable
        findViewById(R.id.connection_error).setVisibility(View.VISIBLE);
        createGroupSessionButton.setEnabled(false);
        joinGroupSessionButton.setEnabled(false);
        recordCustomBeatButton.setEnabled(false);
        viewComposedSongsButton.setEnabled(false);

        connectionErrorText.setVisibility(View.VISIBLE);

        //Set transparency
        createGroupSessionButton.setAlpha(0.5f);
        joinGroupSessionButton.setAlpha(0.5f);
        recordCustomBeatButton.setAlpha(0.5f);
        viewComposedSongsButton.setAlpha(0.5f);
    }

    public void redoServerCheck(View view) {
        Toast.makeText(view.getContext(), "checking server again",
                Toast.LENGTH_LONG).show();
        checkServerConnection();
    }

    //Check if server is up
    private void checkServerConnection() {
        try {
            Call<String> call = webApi.getTechnoSyncService().healthCheck();
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.body() != null && response.body().equals("I'm Alive!")) {
                        Log.i("TechnoSync", "Server health check succeeded");
                    } else {
                        Log.i("TechnoSync", "Server check failed, garbage health check");
                        disabledServerButtons();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("TechnoSync", "Server check failed",t);
                    disabledServerButtons();
                }
            });
        } catch (Exception ex) {
            Log.i("Server Error", "Can not connect! Restart App!");
            disabledServerButtons();
        }
    }

    private void createRoom(PopupWindow popupWindow) {
        //Open Music  Creation Window
        Intent drumPadIntent = new Intent(getBaseContext(), CreationView.class);

        // Put groupId as Extra
        drumPadIntent.putExtra("group_id", newGroupId);

        //Clear activity stack and start new activity
        startActivity(drumPadIntent);

        // Close the current popup
        popupWindow.dismiss();
    }

    private void joinRoom(PopupWindow popupWindow) {
        String groupId = groupCodeEditText.getText().toString();
        if (groupId.length() == 0) {
            return;
        }

        try {
            Call<String> call = webApi.getTechnoSyncService().joinRoom(groupId);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.i("TechnoSync", "Room available to join room");
                    String is_valid = response.body();

                    if (is_valid.equals("True")) {
                        //Open Music  Creation Window
                        Intent drumPadIntent = new Intent(getBaseContext(), CreationView.class);

                        // Put groupId as Extra
                        drumPadIntent.putExtra("group_id", groupId);

                        //Clear activity stack and start new activity
                        startActivity(drumPadIntent);

                        // Close the current popup
                        popupWindow.dismiss();
                    } else {
                        String message = "Incorrect Group Code. Please try again!";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.i("TechnoSync", "Failed to join the room");

                    String message = "Failed to join the group. Please try again!";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    // Close the current popup
                    popupWindow.dismiss();
                }
            });
        } catch (Exception e) {
            Log.i("Server Error", "Can not connect! Restart App!");

            // Close the current popup
            popupWindow.dismiss();
        }
    }

    private void setupCreateGroupSessionButton() {
        createGroupSessionButton.setOnClickListener(v -> {
            try {
                Call<String> call = webApi.getTechnoSyncService().createRoom();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        newGroupId = response.body();

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
                        TextView groupCode = (TextView) popupView.findViewById(R.id.joinGroupCode);
                        groupCode.setText(newGroupId);

                        // Closes the popup window when touch outside.
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.setFocusable(true);
                        // Removes default background.
                        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        // Setup button to start the recording session
                        setupStartGroupSessionButton(Boolean.TRUE, popupWindow);
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.i("TechnoSync", "Server connection failed");
                        disabledServerButtons();
                    }
                });
            } catch (Exception ex) {
                Log.i("Server Error", "Can not connect! Restart App!");
                disabledServerButtons();
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

                startGroupSession = (Button) popupView.findViewById(R.id.joinGroupSessionButton);
                groupCodeEditText = (EditText) popupView.findViewById(R.id.groupCodeEditText);

                // Closes the popup window when touch outside.
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                // Removes default background.
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Setup button to start the recording session
                setupStartGroupSessionButton(Boolean.FALSE, popupWindow);
            }
        });
    }

    private void setupStartGroupSessionButton(Boolean isCreating, PopupWindow popupWindow) {
        // TODO: Implement
        startGroupSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCreating == Boolean.TRUE) {
                    // Create a Group
                    createRoom(popupWindow);
                } else {
                    // Join a group
                    joinRoom(popupWindow);
                }
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
        changePresetsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Music  Creation Window
                Intent changePreset = new Intent(getBaseContext(), ChangePresetView.class);

                //Clear activity stack and start new activity
                startActivity(changePreset);
            }
        });
    }

    private void setupViewComposedSongsButton() {

        viewComposedSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewComposedSongsIntent = new Intent(getBaseContext(), ComposedSongsView.class);

                startActivity(viewComposedSongsIntent);
            }
        });
    }
}
