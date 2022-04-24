package com.jcrawley.webradio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private Button startPauseButton;
    /**
     * help to toggle between play and pause.
     */
    private boolean playPause;
    private MediaPlayer mediaPlayer;
    private MediaPlayer player;
    /**
     * remain false till media is not completed, inside OnCompletionListener make it true.
     */
    private boolean initialStage = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startPauseButton = (Button) findViewById(R.id.button1);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
       // btn.setOnClickListener(pausePlay);
        startPauseButton.setOnClickListener((View view)->{ initializeMediaPlayer();});

    }



    private void log(String msg){
        System.out.println("^^^MainActivity: " + msg);
    }

    private final View.OnClickListener pausePlay = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!playPause) {
                startPauseButton.setBackgroundResource(R.drawable.pause_button_background);
                if (initialStage) {
                    log("onClick() entered initialStage, about to execute new Player url");
                    new Player()
                            .execute("");
                }
                else {
                    log("Not in initial stage, about to start playing!");
                    if (!mediaPlayer.isPlaying())
                        mediaPlayer.start();
                }
                playPause = true;
            } else {
                startPauseButton.setBackgroundResource(R.drawable.play_button_background);
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                playPause = false;
            }
        }
    };
    /**
     * preparing mediaplayer will take sometime to buffer the content so prepare it inside the background thread and starting it on UI thread.
     * @author piyush
     *
     */

    @SuppressLint("StaticFieldLeak")
    class Player extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog progress;

        @Override
        protected Boolean doInBackground(String... params) {
            boolean prepared;
            try {

                mediaPlayer.setDataSource(params[0]);

                mediaPlayer.setOnCompletionListener(mp -> {
                    initialStage = true;
                    playPause=false;
                    startPauseButton.setBackgroundResource(R.drawable.play_button_background);
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                });
                mediaPlayer.prepare();
                prepared = true;
            } catch (IllegalArgumentException e) {
                Log.i("IllegalArgument", e.getMessage());
                prepared = false;
                e.printStackTrace();
            } catch (SecurityException | IllegalStateException | IOException e) {
                prepared = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (progress.isShowing()) {
                progress.cancel();
            }
            Log.i("^^^ Prepared", "//" + result);
            mediaPlayer.start();

            initialStage = false;
        }

        public Player() {
            progress = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progress.setMessage("^^^ Buffering...");
            this.progress.show();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    private void initializeMediaPlayer() {
        player = new MediaPlayer();
        String url = "";
        player.setAudioAttributes( new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());

        try {
            //change with setDataSource(Context,Uri);
            player.setDataSource(this, Uri.parse(url));
            player.prepareAsync();
            player.setOnPreparedListener(mp -> {
                //mp.start();
                player.start();
            });
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }
}