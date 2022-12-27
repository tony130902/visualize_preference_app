package com.example.android.visualizerprefrences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.visualizerprefrences.AudioVisuals.AudioInputReader;
import com.example.android.visualizerprefrences.AudioVisuals.VisualizerView;

public class VisualizerActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE = 88;
    private VisualizerView mVisualizerView;
    private AudioInputReader mAudioInputReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizer);
        mVisualizerView = (VisualizerView) findViewById(R.id.activity_visualizer);
        setupSharedPreferences();
        setupPermissions();
    }

    private void setupSharedPreferences() {

        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mVisualizerView.setShowBass(sharedPreferences.getBoolean(getString(R.string.pref_show_bass_key),
                 getResources().getBoolean(R.bool.pref_show_bass_default)));

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mVisualizerView.setShowMid(sharedPreferences.getBoolean(getString(R.string.pref_show_midrange_rectangle_key),
                getResources().getBoolean(R.bool.pref_show_midrange_rectangle_default)));

        mVisualizerView.setShowTreble(sharedPreferences.getBoolean(getString(R.string.pref_show_treble_triangle_key),
                getResources().getBoolean(R.bool.pref_show_treble_triangle_default)));
        mVisualizerView.setMinSizeScale(1);

        loadSizeFromSharedPreferences(sharedPreferences);
        loadColorFromPreferences(sharedPreferences);
    }

    private void loadColorFromPreferences(SharedPreferences sharedPreferences) {
        mVisualizerView.setColor(sharedPreferences.getString(getString(R.string.pref_color_key),
                getString(R.string.pref_color_red_value)));
    }

    private void loadSizeFromSharedPreferences(SharedPreferences sharedPreferences) {
        float minSize = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_size_key),
                getString(R.string.pref_size_default)));
        mVisualizerView.setMinSizeScale(minSize);
    }

    // Updates the screen if the shared preferences change. This method is required when you make a
    // class implement OnSharedPreferenceChangedListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_show_bass_key))){
            mVisualizerView.setShowBass(sharedPreferences.getBoolean(getString(R.string.pref_show_bass_key),
                    getResources().getBoolean(R.bool.pref_show_bass_default)));
        }
        else if (key.equals(getString(R.string.pref_show_midrange_rectangle_key))){
            mVisualizerView.setShowBass(sharedPreferences.getBoolean(getString(R.string.pref_show_midrange_rectangle_key),
                    getResources().getBoolean(R.bool.pref_show_midrange_rectangle_default)));
        }
        else if (key.equals(getString(R.string.pref_show_treble_triangle_key))){
            mVisualizerView.setShowBass(sharedPreferences.getBoolean(getString(R.string.pref_show_treble_triangle_key),
                    getResources().getBoolean(R.bool.pref_show_treble_triangle_default)));
        }
        else if (key.equals(getString(R.string.pref_color_key))) {
            loadColorFromPreferences(sharedPreferences);
        }

        else if (key.equals(getString(R.string.pref_size_key))) {
            loadSizeFromSharedPreferences(sharedPreferences);
        }
    }

    /**
     * Methods for setting up the menu
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.settings, menu);
        /* Return true so that the visualizer_menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Below this point is code you do not need to modify; it deals with permissions
     * and starting/cleaning up the AudioInputReader
     **/

    /**
     * onPause Cleanup audio stream
     **/
    @Override
    protected void onPause() {
        super.onPause();
        if (mAudioInputReader != null) {
            mAudioInputReader.shutdown(isFinishing());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAudioInputReader != null) {
            mAudioInputReader.restart();
        }
    }

    /**
     * App Permissions for Audio
     **/
    private void setupPermissions() {
        // If we don't have the record audio permission...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                String[] permissionsWeNeed = new String[]{ Manifest.permission.RECORD_AUDIO };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE);
            }
        } else {
            // Otherwise, permissions were granted and we are ready to go!
            mAudioInputReader = new AudioInputReader(mVisualizerView, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The permission was granted! Start up the visualizer!
                    mAudioInputReader = new AudioInputReader(mVisualizerView, this);

                } else {
                    Toast.makeText(this, "Permission for audio not granted. Visualizer can't run.", Toast.LENGTH_LONG).show();
                    finish();
                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.
                }
            }
            // Other permissions could go down here

        }
    }

}