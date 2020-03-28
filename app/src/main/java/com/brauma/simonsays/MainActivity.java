package com.brauma.simonsays;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView ivGreen, ivRed, ivYellow, ivBlue;
    TextView tvScore;
    Button btnRestart, btnSoundToggle;

    ArrayList<Integer> sequence;
    int currentIndex, score;

    boolean sound;

    Random r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        r = new Random(Calendar.getInstance().getTimeInMillis());

        ivGreen = findViewById(R.id.green_light);
        ivRed = findViewById(R.id.red_light);
        ivYellow = findViewById(R.id.yellow_light);
        ivBlue = findViewById(R.id.blue_light);

        tvScore = findViewById(R.id.score);
        btnRestart = findViewById(R.id.button_restart);
        btnSoundToggle = findViewById(R.id.button_sound_toggle);

        sequence = new ArrayList<>();
        currentIndex = 0;
        score = 0;

        sound = true;

        initSequence();

    }

    private void initSequence() {
        sequence.clear();
        for(int i = 0; i < 5; i++){
            sequence.add(r.nextInt(4) + 1);
        }
    }

    private void appendSequence(){
        for(int i = 0; i < 5; i++){
            sequence.add(r.nextInt(4) + 1);
        }
    }


    public void onClick(View view) {
        switch(view.getId()){
            case R.id.green_light:

                break;
            case R.id.red_light:

                break;
            case R.id.yellow_light:

                break;
            case R.id.blue_light:

                break;
        }
    }

    public void restart(View view) {
    }

    public void toggleSound(View view) {
        sound = !sound;
    }
}
