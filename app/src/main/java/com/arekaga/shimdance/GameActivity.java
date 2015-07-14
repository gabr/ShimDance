package com.arekaga.shimdance;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class GameActivity extends Activity implements CanvasView.SelectedArrowChangeListener{

    private static final int SCORE_INCREMETN = 3;

    private Track mTrack;
    private MediaPlayer mPlayer;

    private CountDownTimer mTimer;
    private long mCurrentDuration;

    private int mScore;
    private Arrow mSelectedArrow;

    private TextView mTrackName;
    private TextView mTrackSubscription;
    private TextView mTrackTime;
    private TextView mScoreView;
    private ImageButton mPlayPauseButton;
    private CanvasView mCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // get GUI
        mTrackName = (TextView) findViewById(R.id.trackName);
        mTrackSubscription = (TextView) findViewById(R.id.trackSubscription);
        mTrackTime = (TextView) findViewById(R.id.trackTime);
        mScoreView = (TextView) findViewById(R.id.score);
        mPlayPauseButton = (ImageButton) findViewById(R.id.PlayPauseButton);
        mCanvasView = (CanvasView) findViewById(R.id.CanvasView);

        // reset score
        mScore = 0;
        mScoreView.setText("Score: 0");

        // get track from TrackActivity
        mTrack = TracksActivity.getSelectedTrack();

        // rewrite track information
        mTrackName.setText(mTrack.name);
        mTrackSubscription.setText(mTrack.subscription);

        // create player
        mPlayer = MediaPlayer.create(this, mTrack.resource);

        // set duration
        mCurrentDuration = mPlayer.getDuration();

        // start the game
        start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void onPlayPauseButton(View view) {
        if (mPlayer.isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    private void initTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }

        mTimer = new CountDownTimer(mCurrentDuration, 1000) {
            @Override
            public void onTick(long l) {
                long min = l/(1000*60);
                long sec = l/1000 - min*60;
                mTrackTime.setText(min + ":" + ((sec < 10) ? "0" : "") + sec);

                mCurrentDuration = l;
            }

            @Override
            public void onFinish() {
                end();
            }
        };

        mTimer.start();
    }

    private void pause() {
        mTimer.cancel();
        mPlayer.pause();
        mCanvasView.pause();
        mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    private void start() {
        // init timer
        initTimer();

        mPlayer.start();
        mCanvasView.start();
        mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
    }

    private void end() {
        mPlayer.stop();
        mCanvasView.end(mScore);
        mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    private void acceptArrow() {
        mCanvasView.acceptArrow();
        mScore += SCORE_INCREMETN;
        mScoreView.setText("Score: " + mScore);
    }

    @Override
    public void onBackPressed() {
        if (mPlayer.isPlaying()) {
            acceptArrow();
            //pause();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onSelectedArrowChange(Arrow selectedArrow) {
        mSelectedArrow = selectedArrow;
    }
}
