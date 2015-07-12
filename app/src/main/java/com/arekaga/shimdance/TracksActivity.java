package com.arekaga.shimdance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class TracksActivity extends Activity {

    private TracksListView mTracksList;
    private Track[] mTracks = new Track[] { new Track("A Little Less Conversation", "Elvis Presley",
            R.raw.elvis_presley_vs_jxl_a_little_less_conversation)};

    private static Track mSelectedSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        mTracksList = (TracksListView) findViewById(R.id.TracksListView);
        mTracksList.addTracks(mTracks);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_calibration) {
            Intent calibration = new Intent(getApplicationContext(), CalibrationActivity.class);
            startActivity(calibration);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onPlayButton(View view) {
        Track selected = mTracksList.getSelected();
        if (selected == null) {
            Toast.makeText(this, "Select song", Toast.LENGTH_LONG).show();
            return;
        }

        mSelectedSong = selected;
        Intent game = new Intent(getApplicationContext(), GameActivity.class);
        startActivity(game);
    }

    public static Track getSelectedTrack() {
        return mSelectedSong;
    }
}
