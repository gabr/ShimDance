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
    private Track[] mTracks = new Track[] {
            new Track("A Little Less Conversation", "Elvis Presley", R.raw.elvis_presley_vs_jxl_a_little_less_conversation)
            //new Track("Opa Opa Opa", "Antique", R.raw.antique_opaopaopa),
            //new Track("The boys does nothing", "Alesha Dixon", R.raw.aleshadixon_theboysdoesnothing),
            //new Track("Woda zryje banie", "BFF", R.raw.bff_wodazryjebanie),
            //new Track("Everybody needs somebody to love", "Blues Brothers", R.raw.bluesbrothers_everybodyneedssomebodytolove),
            //new Track("Bend it like bender!", "Devin Townsend Project", R.raw.devintownsendproject_benditlikebender),
            //new Track("Move in the right direction", "Gossip", R.raw.gossip_moveintherightdirection),
            //new Track("Mamma Mia", "In-Grid", R.raw.in_grid_mammiamia),
            //new Track("4 Minutes", "Madonna", R.raw.madonna_4minutes),
            //new Track("Supermassive Black Hole", "Muse", R.raw.muse_supermassiveblackhole),
            //new Track("I like U", "One track mind", R.raw.onetrackmind_ilikeu),
            //new Track("Cotton Eye Joe", "Rednex", R.raw.rednex_cottoneyejoe),
            //new Track("How much is the fish?", "Scooter", R.raw.scooter_howmuchisthefish),
            //new Track("Objection", "Shakira", R.raw.shakira_objection)
    };

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
