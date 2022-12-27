package com.example.music;

import static com.example.music.MainActivity.notificationManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

//https://stackoverflow.com/a/49267522
public class ExitService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);

        notificationManager.cancel(1); //removes notification
        //stop service
        this.stopSelf();
    }

}
