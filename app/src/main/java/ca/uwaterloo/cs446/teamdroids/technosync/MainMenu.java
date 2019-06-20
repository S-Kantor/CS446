package ca.uwaterloo.cs446.teamdroids.technosync;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    public Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Get Buttons
        startButton = (Button) findViewById(R.id.startButton);

        //Setup Link to Create
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Music  Creation Window
                Intent drumPadIntent = new Intent(getBaseContext(), CreationView.class);

                //Clear activity stack and start new activity
                drumPadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(drumPadIntent);
            }
        });

    }
}
