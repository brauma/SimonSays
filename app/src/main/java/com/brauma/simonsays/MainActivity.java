package com.brauma.simonsays;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    // Views
    ImageView ivGreen, ivRed, ivYellow, ivBlue;
    TextView tvScore;
    ImageView btnRestart, btnSoundToggle;

    // Sound Variables
    float initVolume, volume;
    int greenSound, redSound, yellowSound, blueSound;
    SoundPool soundPool;

    // Pattern Variables
    ArrayList<Integer> sequence;
    int currentIndex, currentSequenceLength, score;

    // Thread
    PlaybackThread playbackThread;

    // For feedback
    Vibrator vibration;

    // Random number generator
    Random random;

    AudioManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializing and seeding the random number generator
        random = new Random(Calendar.getInstance().getTimeInMillis());

        if (savedInstanceState != null) {
            this.currentIndex = savedInstanceState.getInt("CURRENTINDEX");
            this.currentSequenceLength = savedInstanceState.getInt("CURRENTSEQUENCELENGTH");
            this.score = savedInstanceState.getInt("SCORE");
            this.sequence = savedInstanceState.getIntegerArrayList("SEQUENCE");
        } else {
            // Initializing variables
            sequence = new ArrayList<>();
            currentIndex = 0;
            currentSequenceLength = 1;
            score = 0;
            initSequence();
        }

        // Make it full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        
        // Some more initializing
        initViews();
        initSounds();

        // For feedback
        vibration = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        // Setting initial score
        tvScore.setText(String.valueOf(score));

        // Setting icons for the buttons
        btnRestart.setImageResource(R.drawable.ic_reload);
        btnSoundToggle.setImageResource(R.drawable.ic_mute);

        // Play the first part of the first sequence
        playbackSequence();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("CURRENTINDEX", this.currentIndex);
        outState.putInt("CURRENTSEQUENCELENGTH", this.currentSequenceLength);
        outState.putInt("SCORE", this.score);
        outState.putIntegerArrayList("SEQUENCE", this.sequence);

        // Call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        playbackThread.interrupt();
    }

    private void initViews() {
        ivGreen = findViewById(R.id.green_light);
        ivRed = findViewById(R.id.red_light);
        ivYellow = findViewById(R.id.yellow_light);
        ivBlue = findViewById(R.id.blue_light);

        tvScore = findViewById(R.id.score);
        btnRestart = findViewById(R.id.button_restart);
        btnSoundToggle = findViewById(R.id.toggleButton);
    }

    private void initSounds() {
        soundPool = new SoundPool(4, AudioManager.STREAM_SYSTEM, 0);

        // IDs and loading for the sounds
        greenSound = soundPool.load(this, R.raw.green, 1);
        redSound = soundPool.load(this, R.raw.red, 1);
        yellowSound = soundPool.load(this, R.raw.yellow, 1);
        blueSound = soundPool.load(this, R.raw.blue, 1);

        // This is for trying to set the volume to the system volume
        mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        if (mgr != null) {
            initVolume = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
            initVolume = initVolume / mgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        } else {
            initVolume = 0.05f;
        }

        volume = initVolume;
    }

    private void initSequence() {
        sequence.clear();
        for(int i = 0; i < 100; i++){
            sequence.add(random.nextInt(4) + 1);
        }
    }

    private void playbackSequence() {
        // Starting new thread for the playback, so the Thread.sleep() function doesn't freeze the UI thread
        playbackThread = new PlaybackThread(sequence, currentSequenceLength);
        playbackThread.start();
    }

    private boolean play(int pressedButton){
        // Check if it was the correct button to press
        if(sequence.get(currentIndex) == pressedButton){
            // If not at the end of the current sequence part, increment the index variable
            if(currentIndex+1 != currentSequenceLength){
                currentIndex++;
            }
            // If at the end, increment score and the length of the sequence part. Then play back the new, longer sequence
            else{
                currentSequenceLength++;
                score++;
                tvScore.setText(String.valueOf(score));
                currentIndex = 0;
                playbackSequence();
            }
            return true;
        }
        // If not, vibrate, then restart the game
        else {
            vibration.vibrate(200);
            restart();
            return false;
        }
    }

    private void activateButton(int buttonId) {
        // Play the button specific effects
        switch(buttonId){
            case 1:
                highlightGreen();
                break;
            case 2:
                highlightRed();
                break;
            case 3:
                highlightYellow();
                break;
            case 4:
                highlightBlue();
                break;
        }
    }

    public void onClick(View view) {
        switch(view.getId()){
            case R.id.green_light:
                if(play(1))
                    highlightGreen();
                break;
            case R.id.red_light:
                if(play(2))
                    highlightRed();
                break;
            case R.id.yellow_light:
                if(play(3))
                    highlightYellow();
                break;
            case R.id.blue_light:
                if(play(4))
                    highlightBlue();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            if (mgr != null) {
                initVolume = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
                initVolume = initVolume / mgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            }

            volume = initVolume;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void highlightGreen(){
        ValueAnimator colorAnim = ObjectAnimator.ofInt(ivGreen, "backgroundColor",
                getResources().getColor(R.color.colorGreen), getResources().getColor(R.color.colorWhite));
        colorAnim.setDuration(250);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(1);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        colorAnim.start();

        soundPool.play(greenSound, volume, volume,0,0,1);
    }

    private void highlightRed(){
        ValueAnimator colorAnim = ObjectAnimator.ofInt(ivRed, "backgroundColor",
                getResources().getColor(R.color.colorRed), getResources().getColor(R.color.colorWhite));
        colorAnim.setDuration(250);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(1);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        colorAnim.start();

        soundPool.play(redSound, volume, volume,0,0,1);
    }

    private void highlightYellow(){
        ValueAnimator colorAnim = ObjectAnimator.ofInt(ivYellow, "backgroundColor",
                getResources().getColor(R.color.colorYellow), getResources().getColor(R.color.colorWhite));
        colorAnim.setDuration(250);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(1);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        colorAnim.start();

        soundPool.play(yellowSound, volume, volume,0,0,1);
    }

    private void highlightBlue(){
        ValueAnimator colorAnim = ObjectAnimator.ofInt(ivBlue, "backgroundColor",
                getResources().getColor(R.color.colorBlue), getResources().getColor(R.color.colorWhite));
        colorAnim.setDuration(250);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(1);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        colorAnim.start();

        soundPool.play(blueSound, volume, volume,0,0,1);
    }

    public void restart() {
        playbackThread.interrupt();
        initSequence();
        score = 0;
        currentIndex = 0;
        currentSequenceLength = 1;
        tvScore.setText(String.valueOf(score));
        playbackSequence();
    }

    public void restartClick(View view) {
        restart();
        vibration.vibrate(100);
    }

    public void toggleSound(View view) {
        if(volume == 0) {
            volume = initVolume;
            btnSoundToggle.setImageResource(R.drawable.ic_mute);
        } else {
            volume = 0;
            btnSoundToggle.setImageResource(R.drawable.ic_volume);
        }
        vibration.vibrate(50);
    }

    // Thread for playing back the sequence
    public class PlaybackThread extends Thread {

        private static final String TAG = "PlaybackThread";
        private static final int DELAY = 1000; // a second

        private ArrayList<Integer> sequence;
        private int currentSequenceLength;
        private int i;

        PlaybackThread(ArrayList<Integer> sequence, int currentSequenceLength){
            this.sequence = sequence;
            this.currentSequenceLength = currentSequenceLength;
            i = 0;
        }

        @Override
        public void run() {
            Log.v(TAG, "doing work in Playback Thread");
            while (i < currentSequenceLength) {
                if(Thread.interrupted()){
                    break;
                }
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Playback Thread interrupted");
                    return;
                }
                progress(sequence.get(i));
                i++;
            }
        }

        private void progress(final int index) {
            Log.v(TAG, "calling ImageView functions from Playback Thread");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activateButton(index);
                }
            });
        }
    }

}
