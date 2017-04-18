package voyij.ar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class POIActivity extends AppCompatActivity {
    protected static final String STATE_POI_NAME = "POI_NAME";
    protected static final String STATE_POI_LATITUDE = "POI_LATITUDE";
    protected static final String STATE_POI_LONGITUDE = "POI_LONGITUDE";
    protected static final String STATE_POI_TYPE = "POI_TYPE";
    protected static final String STATE_POI_DESCRIPTION = "POI_DESCRIPTION";

    private TextView mPOITitle;
    private TextView mPOILatitude;
    private TextView mPOILongitude;
    private TextView mPOIType;
    private TextView mPOIDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);
        mPOITitle = (TextView) findViewById(R.id.textViewTitle);
        mPOILatitude = (TextView) findViewById(R.id.textViewLatitude);
        mPOILongitude = (TextView) findViewById(R.id.textViewLongitude);
        mPOIType = (TextView) findViewById(R.id.textViewPOIType);
        mPOIDescription = (TextView) findViewById(R.id.textViewDescription);
        Intent intent = getIntent();
        mPOITitle.setText(intent.getStringExtra(STATE_POI_NAME));
        mPOILatitude.setText(Double.toString(intent.getDoubleExtra(STATE_POI_LATITUDE, 0)));
        mPOILongitude.setText(Double.toString(intent.getDoubleExtra(STATE_POI_LONGITUDE, 0)));
        mPOIType.setText(intent.getStringExtra(STATE_POI_TYPE));
        mPOIDescription.setText(intent.getStringExtra(STATE_POI_DESCRIPTION));
    }

    public void onButtonCloseClickAction(View view) {
        this.finish();
    }
}
