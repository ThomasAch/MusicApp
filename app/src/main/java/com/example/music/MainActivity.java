package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationBuilderWithBuilderAccessor;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.os.Vibrator;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //views that I need to be accessed throughout the class
    static MediaPlayer player;
    static ImageButton play_pause_btn;
    static TextView audioName;
    static TextView audioDurationText;
    static TextView audioCurrentTime;
    static SeekBar timeBar;
    static ImageView audioCoverImage;

    //variables that I need to be accessed throughout the class
    static int audioDuration;
    static int timeOfAudio;
    static boolean play = true;
    static boolean shuffle = false;
    static boolean loop = false;
    static boolean skip = false;
    static int audioFileIndex = 0;
    static File[] shuffleFiles;

    //get the files in the music folder
//      https://stackoverflow.com/questions/8646984/how-to-list-files-in-an-android-directory
    static String filepath = Environment.getExternalStorageDirectory().toString()+"/Music";
    //        Log.d("Files", "Path: " + path);
    static File directory = new File(filepath);
    static File[] files = directory.listFiles();
//        Log.d("Files", "Size: "+ files.length);
//        for (int i = 0; i < files.length; i++){Log.d("Files", "FileName:" + files[i].getName());}
//        Log.d("File", files[audioFileIndex].getPath());

    static NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // not entirely sure what this does, but i think it is used for getting the correct screen? just guessing tbh
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        v.setBackgroundColor(Color.argb(255, 0, 255, 255));

        // a bunch of views objects that i need to reference
        play_pause_btn = (ImageButton) findViewById(R.id.play_pause_btn);
        final Button back_btn = (Button) findViewById(R.id.back_btn);
        final Button next_btn = (Button) findViewById(R.id.next_btn);
        final Button shuffle_btn = (Button) findViewById(R.id.shuffle_btn);
        final Button loop_btn = (Button) findViewById(R.id.loop_btn);
        final Button testBtn = (Button) findViewById(R.id.testBtn);
        audioName = (TextView) findViewById(R.id.audio_name);
        audioDurationText = (TextView) findViewById(R.id.audioDuration);
        audioCurrentTime = (TextView) findViewById(R.id.audioCurrentTime);
        timeBar = (SeekBar) findViewById(R.id.timeBar);
        audioCoverImage = (ImageView) findViewById(R.id.audioCoverImage);


        // https://developer.android.com/training/notify-user/build-notification#Priority
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Audio Channel";
//            String description = "Used for audio based notifications";
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            NotificationChannel notificationChannel = new NotificationChannel("audioChannel", name, NotificationManager.IMPORTANCE_LOW);
//            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
//            notificationChannel.setSound(null, null);
//            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "audioChannel")
                .setSmallIcon(R.drawable.ic_drawing)
                .setContentTitle("textTitle1")
                .setContentText("textContent2")
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(1, builder.build());

        //button i sometimes use to test stuff
        testBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
//                Log.d("printing", "player:" + player);
            }
        });

        // for when the play/pause button is clicked
        play_pause_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) { //not sure what the "v" is for but it is a pain to take some stuff out of here and put it into other places
                if (play){
                    //changes btn icon from play to pause and visa versa
                    play_pause_btn.setImageResource(R.drawable.pause_img);

                    //https://www.youtube.com/watch?reload=9&v=C_Ka7cKwXW0
                    if (player == null){
//                        player = MediaPlayer.create(MainActivity.this, Uri.parse(files[audioFileIndex].getPath())); //I have to figure out how to put this in the setup function for later (not sure what "v.getContext()" does but it is preventing me from moving it)
//                        player = MediaPlayer.create(v.getContext(), Uri.parse(files[audioFileIndex].getPath()));
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

        back_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                skip = true;
                audioFileIndex = getNextorPriviousIndex(-1, audioFileIndex, files.length -1);
                setupAudio();
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                skip = true;
                audioFileIndex = getNextorPriviousIndex(1, audioFileIndex, files.length -1);
                setupAudio();
            }
        });

        shuffle_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (!shuffle) {
                    shuffle = true;
                    shuffle_btn.setForegroundTintList(ColorStateList.valueOf(Color.argb(255, 255, 255, 255)));

                    shuffleFiles = files;
                    Collections.shuffle(Arrays.asList(shuffleFiles));
                    setupAudio();
                }
                else {
                    shuffle = false;
                    shuffle_btn.setForegroundTintList(ColorStateList.valueOf(Color.argb(255, 140, 140, 140)));
                }
            }
        });

        loop_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (!loop && player != null) {
                    loop = true;
                    loop_btn.setForegroundTintList(ColorStateList.valueOf(Color.argb(255, 255, 255, 255)));

//                    player.setLooping(true);
                }
                else {
                    loop = false;
                    loop_btn.setForegroundTintList(ColorStateList.valueOf(Color.argb(255, 140, 140, 140)));

//                    player.setLooping(false);
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

        audioName.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("audio title", audioName.getText());
                clipboard.setPrimaryClip(clip);

                Toast toast = Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT);
                toast.show();

            }
        });

    }

    @Override
    protected  void onDestroy() {

        super.onDestroy();
        notificationManager.cancel(1);
    }


    //any time new audio is gonna be played, this function should be called
    public void setupAudio() {
        if (player != null) {
            player.pause();
            play_pause_btn.setImageResource(R.drawable.play_img);
            play = true;
        }
        //used for swaping back and forth between
        File[] setUpFiles;
        if (shuffle) {
            setUpFiles = shuffleFiles;
        }
        else {
            setUpFiles = files;
        }

        Log.d("setup", "audioFileIndex:" + audioFileIndex);
        player = MediaPlayer.create(MainActivity.this, Uri.parse(setUpFiles[audioFileIndex].getPath())); //sets up the player
        audioName.setText(setUpFiles[audioFileIndex].getName().substring(0, setUpFiles[audioFileIndex].getName().lastIndexOf(".")));
        audioDuration = player.getDuration(); // gets length of audio clip
        audioDurationText.setText(getTimeLayout(audioDuration));
        getAudioPic(setUpFiles[audioFileIndex].getPath());
        audioCurrentTime.setText(getTimeLayout(0));
        timeBar.setProgress(0);

        //https://stackoverflow.com/a/7369642
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {
                Log.d("abc", "timeOfAudio:" + timeOfAudio + "    audioDuration:" + audioDuration);
//                if (timeOfAudio >= audioDuration - 200){
                if (!skip) {
                    if (loop) {
                        player.seekTo(0);
                        player.start();
                    }
                    else{
                        Log.d("abc","next");
                        audioFileIndex = getNextorPriviousIndex(1, audioFileIndex, setUpFiles.length -1);
                        setupAudio();
                        play_pause_btn.performClick();
                    }
                }
                skip = false;
            }

        });
    }

    //method to get the albumcover from mp3 files
    public static void getAudioPic(String songPath) {
        //https://stackoverflow.com/a/21549403
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songPath);

        byte [] data = mmr.getEmbeddedPicture();
        //coverart is an Imageview object

        // convert the byte array to a bitmap
        if(data != null)
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            audioCoverImage.setImageBitmap(bitmap); //associated cover art in bitmap
        }
        else
        {
            audioCoverImage.setImageResource(R.drawable.ic_drawing); //any default cover resourse folder
        }

//        audioCoverImage.setAdjustViewBounds(true);
//        coverart.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
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

    //used to go to the next/previous song by going to the next index in the list
    static int getNextorPriviousIndex(int forwardOrBackwards, int currentIndex, int lengthOfList) {
        currentIndex += forwardOrBackwards;
        if (currentIndex < 0){
            currentIndex = lengthOfList;
        }
        else if (currentIndex > lengthOfList){
            currentIndex = 0;
        }
        return currentIndex;
    }

    //creates a new thread to be constantly updating the current progress of the audio
    final Runnable mUpdateTime = new Runnable() {
        public void run() {
            if (player.isPlaying()) {
//            if (!play) {
                //updates the text displaying the current time
                timeOfAudio = player.getCurrentPosition();
                audioCurrentTime.setText(getTimeLayout(timeOfAudio));

                //finding how much of the song has been played and updating the progress bar
                float audioProgress = (float) timeOfAudio / audioDuration * timeBar.getMax();
                timeBar.setProgress((int) audioProgress);

                audioCurrentTime.postDelayed(this, 100); // how often this loop is done (the lower the speed, the smoother the progress bar)
//                Log.d("Test", "player.getCurrentPosition():" + player.getCurrentPosition() + "    timeOfAudio:" + timeOfAudio + "     audioDuration:" + audioDuration + "      audioProgress:" + audioProgress);
            }else {
                audioCurrentTime.removeCallbacks(this); // litearlly no clue what this does. when i remove it, nothing seems to happen, but was par of stack code soooo yea
            }
        }
    };
}