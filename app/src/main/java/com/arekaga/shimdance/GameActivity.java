package com.arekaga.shimdance;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import static com.arekaga.shimdance.Arrow.Type.*;


public class GameActivity extends Activity implements CanvasView.SelectedArrowChangeListener{

    private static final int SCORE_INCREMETN = 3;

    private static MediaPlayer mPlayer;
    private Track mTrack;

    private CountDownTimer mTimer;

    private int mScore;
    private boolean mIsEnd = false;
    private Arrow mSelectedArrow;

    private TextView mTrackName;
    private TextView mTrackSubscription;
    private TextView mTrackTime;
    private TextView mScoreView;
    private ImageButton mPlayPauseButton;
    private CanvasView mCanvasView;

    private static final int MOTION_DATA_SIZE = 10;
    private static String[] mMotionData = new String[] {"", ""};
    private Handler mHanlder = new Handler() {

        public void handleMessage(Message msg) {
            // obtain data from msg
            CalibratedData[] data = (CalibratedData[]) msg.obj;

            for (int i = 0; i < 2; i++) {
                if (data[i] == null || data[i].direction == null) {
                    mMotionData[i] = mMotionData[i].substring(1);
                    continue;
                }

                mMotionData[i] += data[i].direction;

                char prev = '\0';
                char curr = '\0';
                String result = "";
                for (int j =0; j < mMotionData[i].length(); j++) {
                    curr = mMotionData[i].charAt(j);
                    if (curr == 'N' || curr == 'P') {
                        if (prev != curr) {
                            result += curr;
                        }
                        prev = curr;
                    } else {
                        result += curr;
                    }
                }

                mMotionData[i] = result;

                if (mMotionData[i].length() > MOTION_DATA_SIZE) {
                    mMotionData[i] = mMotionData[i].substring(mMotionData[i].length() - MOTION_DATA_SIZE);
                }
            }

            if (mSelectedArrow != null) {
                String letter = getLetterRepresentation(mSelectedArrow.getType());
                for (int i = 0; i < 2; i++) {
                    if (mMotionData[i].contains(letter) && mMotionData[i].contains("P") && mMotionData[i].contains("N")) {
                        acceptArrow();
                        mSelectedArrow = null;
                    }
                }
            }
        }
    };


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
        if (mPlayer == null) {
            mPlayer = MediaPlayer.create(this, mTrack.resource);
        }

        DataProcessor.removeHandler(mHanlder);
        DataProcessor.addHandler(mHanlder);

        // start the game
        start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void onPlayPauseButton(View view) {
        if (mIsEnd) {
            onBackPressed();
        }

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

        int current = mPlayer.getDuration() - mPlayer.getCurrentPosition();
        mTimer = new CountDownTimer(current, 1000) {
            @Override
            public void onTick(long l) {
                long min = l/(1000*60);
                long sec = l/1000 - min*60;
                mTrackTime.setText(min + ":" + ((sec < 10) ? "0" : "") + sec);
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

        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            mCanvasView.setOnSelectedArrowChangeListener(this);
        }

        mCanvasView.start();
        mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
    }

    private void end() {
        mIsEnd = true;
        mTrackTime.setVisibility(View.INVISIBLE);
        mScoreView.setVisibility(View.INVISIBLE);
        mPlayer.stop();
        mPlayer = null;
        mCanvasView.end(mScore);
        mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_previous);
    }

    private void acceptArrow() {
        mCanvasView.acceptArrow();
        mScore += SCORE_INCREMETN;
        mScoreView.setText("Score: " + mScore);
    }

    @Override
    public void onBackPressed() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            pause();
            return;
        }

        end();
        super.onBackPressed();
    }

    @Override
    public void onSelectedArrowChange(Arrow selectedArrow) {
        mSelectedArrow = selectedArrow;
    }
}
