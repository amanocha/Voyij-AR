package voyij.ar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
    private ImageView mPOIPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);
        mPOITitle = (TextView) findViewById(R.id.textViewTitle);
        mPOILatitude = (TextView) findViewById(R.id.textViewLatitude);
        mPOILongitude = (TextView) findViewById(R.id.textViewLongitude);
        mPOIType = (TextView) findViewById(R.id.textViewPOIType);
        mPOIDescription = (TextView) findViewById(R.id.textViewDescription);
        mPOIPicture = (ImageView) findViewById(R.id.imageViewPicture);
        Intent intent = getIntent();
        mPOITitle.setText(intent.getStringExtra(STATE_POI_NAME));
        mPOILatitude.setText(Double.toString(intent.getDoubleExtra(STATE_POI_LATITUDE, 0)));
        mPOILongitude.setText(Double.toString(intent.getDoubleExtra(STATE_POI_LONGITUDE, 0)));
        mPOIType.setText(intent.getStringExtra(STATE_POI_TYPE));
        mPOIDescription.setText(intent.getStringExtra(STATE_POI_DESCRIPTION));
        setPicture();
    }

    private void setPicture() {
        String poiType = getIntent().getStringExtra(STATE_POI_TYPE);
        if (poiType.equals(POI.TYPE_LANDMARK)) {
            mPOIPicture.setImageDrawable(getDrawable(R.drawable.landmark));
        }
        else if (poiType.equals(POI.TYPE_RESTAURANT)) {
            mPOIPicture.setImageDrawable(getDrawable(R.drawable.restaurant));
        }
        else if (poiType.equals(POI.TYPE_STORE)) {
            mPOIPicture.setImageDrawable(getDrawable(R.drawable.store));
        }
        else if (poiType.equals(POI.TYPE_UTILITY)) {
            mPOIPicture.setImageDrawable(getDrawable(R.drawable.utility));
        }
        else {
            mPOIPicture.setVisibility(View.INVISIBLE);
        }
    }

    public void onButtonCloseClickAction(View view) {
        this.finish();
    }
}
