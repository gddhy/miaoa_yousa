package net.gddhy.miaoa;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;


import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class SoundPlayService extends Service {
    public SoundPlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new Notification.Builder(this)//,getPackageName())
                .setContentTitle("应用安装通知")
                .setContentText("正在播放声音")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playSound();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void playSound(){
        File file = new File(getFilesDir(),"music.mp3");
        MediaPlayer mediaPlayer;
        if(file.exists()){
            mediaPlayer = MediaPlayer.create(this, Uri.fromFile(file));
        } else {
            mediaPlayer = MediaPlayer.create(this, R.raw.yousa);
        }
        mediaPlayer.start();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //stopForeground(true);
                stopSelf();
            }
        },mediaPlayer.getDuration());
    }
}
