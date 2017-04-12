package voyij.ar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    protected static final int SETTINGS_REQUEST = 1;
    protected static final String KEY_AR_PREFS = "KEY_VOYIJ_AR_PREFS";
    protected static final String STATE_POI_RANGE = "POI_RANGE";
    protected static final String STATE_SHOW_STORES = "SHOW_STORES";
    protected static final String STATE_SHOW_RESTAURANTS = "SHOW_RESTAURANTS";
    protected static final String STATE_SHOW_UTILITIES = "SHOW_UTILITIES";
    protected static final String STATE_SHOW_LANDMARKS = "SHOW_LANDMARKS";

    private SeekBar mPOIRangeSlider;
    private CheckBox mCheckBoxPOIStores;
    private CheckBox mCheckBoxPOIRestaurants;
    private CheckBox mCheckBoxPOIUtilities;
    private CheckBox mCheckBoxPOILandmarks;
    private TextView mPOIRangeIndicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPOIRangeSlider = (SeekBar) findViewById(R.id.seekBarRange);
        mCheckBoxPOIStores = (CheckBox) findViewById(R.id.checkBoxStores);
        mCheckBoxPOIRestaurants = (CheckBox) findViewById(R.id.checkBoxRestaurants);
        mCheckBoxPOIUtilities = (CheckBox) findViewById(R.id.checkBoxUtilities);
        mCheckBoxPOILandmarks = (CheckBox) findViewById(R.id.checkBoxLandmarks);
        mPOIRangeIndicator = (TextView) findViewById(R.id.textViewRangeIndicator);
        mPOIRangeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getProgress() == 1) {
                    mPOIRangeIndicator.setText("" + seekBar.getProgress() + " Meter");
                } else {
                    mPOIRangeIndicator.setText("" + seekBar.getProgress() + " Meters");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        loadPreviousSettings();
    }

    private void loadPreviousSettings() {
        Intent intent = getIntent();
        if (intent != null) {
            mPOIRangeSlider.setProgress(intent.getIntExtra(STATE_POI_RANGE, 1000));
            mPOIRangeIndicator.setText(""+intent.getIntExtra(STATE_POI_RANGE, 1000));
            mCheckBoxPOIStores.setChecked(intent.getBooleanExtra(STATE_SHOW_STORES, true));
            mCheckBoxPOIRestaurants.setChecked(intent.getBooleanExtra(STATE_SHOW_RESTAURANTS, true));
            mCheckBoxPOIUtilities.setChecked(intent.getBooleanExtra(STATE_SHOW_UTILITIES, true));
            mCheckBoxPOILandmarks.setChecked(intent.getBooleanExtra(STATE_SHOW_LANDMARKS, true));
        }
    }

    public void saveAndCloseButtonAction(View view) {
        Intent toSend = new Intent();
        toSend.putExtra(STATE_POI_RANGE, mPOIRangeSlider.getProgress());
        toSend.putExtra(STATE_SHOW_STORES, mCheckBoxPOIStores.isChecked());
        toSend.putExtra(STATE_SHOW_RESTAURANTS, mCheckBoxPOIRestaurants.isChecked());
        toSend.putExtra(STATE_SHOW_UTILITIES, mCheckBoxPOIUtilities.isChecked());
        toSend.putExtra(STATE_SHOW_LANDMARKS, mCheckBoxPOILandmarks.isChecked());
        saveSettingsToDisk();
        setResult(RESULT_OK, toSend);
        this.finish();
        return;
    }

    private void saveSettingsToDisk() {
        SharedPreferences prefs = getSharedPreferences(KEY_AR_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(STATE_POI_RANGE, mPOIRangeSlider.getProgress());
        editor.putBoolean(STATE_SHOW_STORES, mCheckBoxPOIStores.isChecked());
        editor.putBoolean(STATE_SHOW_RESTAURANTS, mCheckBoxPOIRestaurants.isChecked());
        editor.putBoolean(STATE_SHOW_UTILITIES, mCheckBoxPOIUtilities.isChecked());
        editor.putBoolean(STATE_SHOW_LANDMARKS, mCheckBoxPOILandmarks.isChecked());
        editor.commit();
        System.out.println("Saved Persistent State to Disk");
    }
}
