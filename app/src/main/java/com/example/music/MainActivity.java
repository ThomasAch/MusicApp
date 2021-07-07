package com.example.music;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.os.Vibrator;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //views that I need to be accessed throughout the class
    static MediaPlayer player;
    static TextView audioName;
    static TextView audioDurationText;
    static TextView audioCurrentTime;
    static SeekBar timeBar;

    //variables that I need to be accessed throughout the class
    static int audioDuration;

    //get the files in the music folder
//      https://stackoverflow.com/questions/8646984/how-to-list-files-in-an-android-directory
    static String filepath = Environment.getExternalStorageDirectory().toString()+"/Music";
    //        Log.d("Files", "Path: " + path);
    static File directory = new File(filepath);
    static File[] files = directory.listFiles();
//        Log.d("Files", "Size: "+ files.length);
//        for (int i = 0; i < files.length; i++){Log.d("Files", "FileName:" + files[i].getName());}
//        Log.d("File", files[0].getPath());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // not entirely sure what this does, but i think it is used for getting the correct screen? just guessing tbh
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // a bunch of views objects that i need to reference
        final ImageButton play_pause_btn = (ImageButton) findViewById(R.id.play_pause_btn);
        final Button testBtn = (Button) findViewById(R.id.testBtn);
        audioName = (TextView) findViewById(R.id.audio_name);
        audioDurationText = (TextView) findViewById(R.id.audioDuration);
        audioCurrentTime = (TextView) findViewById(R.id.audioCurrentTime);
        timeBar = (SeekBar) findViewById(R.id.timeBar);

        //button i sometimes use to test stuff
        testBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("printing", "to see in logcat");
            }
        });

        // for when the play/pause button is clicked
        play_pause_btn.setOnClickListener(new View.OnClickListener(){
            boolean play = true;
            public void onClick(View v) { //not sure what the "v" is for but it is a pain to take some stuff out of here and put it into other places
                if (play){
                    //changes btn icon from play to pause and visa versa
                    play_pause_btn.setImageResource(R.drawable.pause_img);

                    //https://www.youtube.com/watch?reload=9&v=C_Ka7cKwXW0
                    if (player == null){
                        player = MediaPlayer.create(v.getContext(), Uri.parse(files[0].getPath())); //I have to figure out how to put this in the setup function for later (not sure what "v.getContext()" does but it is preventing me from moving it)
//                        player = MediaPlayer.create(v.getContext(), R.raw.long_bruh); //the og sound i used for testing
                        setupAudio();
                    }
                    audioCurrentTime.post(mUpdateTime); //i think this just starts the new thread, but idk for sure (also not sure why it is tied to the current time view)

                    player.start();

                    play = false;
                    }

                else{
                    play_pause_btn.setImageResource(R.drawable.play_img);

                    if (player != null){
                        player.pause();
                    }

                    play = true;
                }
            }
        });

        //for when the user drags the timebar, update the song
        //https://www.geeksforgeeks.org/android-creating-a-seekbar/
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // increment 1 in progress and
                // increase the textsize
                // with the value of progress
                if (fromUser && player != null){
//                    Log.d("progress", "progress:" +  progress);
                    player.seekTo((int) ((float) progress / timeBar.getMax() * audioDuration)); //sets the player to the correct position
                    audioCurrentTime.setText(getTimeLayout(player.getCurrentPosition()));
                }
            }

            //the following 2 methods are usesless for now, but need to be here for setOnSeekBarChangeListener to work (if u check out the seekbar class, they are there, but idk why they need to be included always)
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // This method will automatically
                // called when the user touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // This method will automatically
                // called when the user
                // stops touching the SeekBar
            }
        });
    }

    // any time new audio is gonna be played, this function should be called
    public static void setupAudio() {
        audioName.setText(files[0].getName().substring(0, files[0].getName().lastIndexOf(".")));
        audioDuration = player.getDuration(); // gets length of audio clip
        audioDurationText.setText(getTimeLayout(audioDuration));
    }

    // formats time given (ms) into a more regular format for viewing (min and sec)
    static String getTimeLayout(int milliSeconds){
        int MinInt = milliSeconds / 60000;
        int SecInt = (milliSeconds % 60000) / 1000;
        String MinString = String.valueOf(MinInt);
        String SecString = String.valueOf(SecInt);

//        if (MinString.length() < 2){
//            MinString = "0" + MinString;
//        }

        if (SecString.length() < 2){
            SecString = "0" + SecString;
        }
        return MinString + ":" + SecString;
    }

    //creates a new thread to be constantly updating the current progress of the audio
    final Runnable mUpdateTime = new Runnable() {
        public void run() {
            if (player.isPlaying()) {
                //updates the text displaying the current time
                int timeOfAudio = player.getCurrentPosition();
                audioCurrentTime.setText(getTimeLayout(timeOfAudio));

                //finding how much of the song has been played and updating the progress bar
                float audioProgress = (float) timeOfAudio / audioDuration * timeBar.getMax();
                timeBar.setProgress((int) audioProgress);

                audioCurrentTime.postDelayed(this, 100); // how often this loop is done (the lower the speed, the smoother the progress bar)
                Log.d("Test", "player.getCurrentPosition():" + player.getCurrentPosition() + "    timeOfAudio:" + timeOfAudio + "     audioDuration:" + audioDuration + "      audioProgress:" + audioProgress);
            }else {
                audioCurrentTime.removeCallbacks(this); // litearlly no clue what this does. when i remove it, nothing seems to happen, but was par of stack code soooo yea
            }
        }
    };
}