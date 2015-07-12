package com.arekaga.shimdance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class TracksActivity extends Activity {

    private TracksListView mTracksList;
    private String[][] mTracks = {{"test", "sub"}};

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
}
