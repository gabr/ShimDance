package com.arekaga.shimdance;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;


public class GameActivity extends Activity {

    private Track mTrack;
    private MediaPlayer mPlayer;

    private TextView mTrackName;
    private TextView mTrackSubscription;
    private ImageButton mPlayPauseButton;
    private CanvasView mCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // get GUI
        mTrackName = (TextView) findViewById(R.id.trackName);
        mTrackSubscription = (TextView) findViewById(R.id.trackSubscription);
        mPlayPauseButton = (ImageButton) findViewById(R.id.PlayPauseButton);
        mCanvasView = (CanvasView) findViewById(R.id.CanvasView);

        // get track from TrackActivity
        mTrack = TracksActivity.getSelectedTrack();

        // rewrite track information
        mTrackName.setText(mTrack.name);
        mTrackSubscription.setText(mTrack.subscription);

        // create player
        mPlayer = MediaPlayer.create(this, mTrack.resource);

        // start playing
        mPlayer.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void onPlayPauseButton(View view) {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
        } else {
            mPlayer.start();
            mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
    }
}
