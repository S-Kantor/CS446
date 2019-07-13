package ca.uwaterloo.cs446.teamdroids.technosync;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    public Button startGroupSessionButton;
    public Button practiceButton;
    public Button recordCustomBeatButton;
    public Button changePresetsButton;
    public Button viewRecordingsButton;

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
        startGroupSessionButton = (Button) findViewById(R.id.createGroupButton);
        setupGroupSessionButton();
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
    }

    private void setupGroupSessionButton() {
        // TODO: Implement
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
