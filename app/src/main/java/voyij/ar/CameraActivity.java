package voyij.ar;

/**
 * Authors: Sam Toffler, Aninda Manocha, Chirag Tamboli
 * Date: March 23, 2017
 * Finished: April 26, 2017
 *
 * This class is the main class of the app and contains the camera functionality as well as
 * functions that direct the function of the app based on the behavior of the orientation and
 * location sensors.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SizeF;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.vision.text.Text;

public class CameraActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    // Camera Variables
    private String cameraId;
    private static final String TAG = "AndroidCameraApi";
    protected CameraDevice cameraDevice;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private TextureView textureView;
    private Size imageDimension;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private ImageReader imageReader;
    private CameraManager cameraManager;
    private RelativeLayout layout;
    private double fov_x;
    private double fov_y;
    private final int EXPERIMENTAL_FOV_X = 45;
    private final int EXPERIMENTAL_FOV_Y = 70;

    // Sensor Variables
    private SensorManager mSensorManager;
    private Sensor rotationSensor;
    private float[] mRotationMatrix_Y;
    private float[] mOrientation_Y;
    private float[] mRotationMatrix_X;
    private float[] mOrientation_X;
    private float[] mAngles;
    private LocationGPS mLocationSensor;
    private Location mCurrentLocation;
    private float[] mCurrentOrientation;

    // Screen Variables
    private Point activityScreenSize;
    private Point fullScreenSize;
    private int thumbnailTextSize = 20;
    private int thumbnailIconWidth = 175;
    private int thumbnailIconHeight = 175;
    private int spacingCounter = 0;
    private final double THUMBNAIL_STARTING_POINT_X = 0.5;
    private final double THUMBNAIL_STARTING_POINT_Y = 0.9;
    private final int THUMBNAIL_SPACING_AMOUNT = 175;

    // POI Variables
    private static final String JSON_POI_DIRECTORY = "JSONPOIs";
    private List<POI> points = new ArrayList<POI>();
    private Map<POI, TextView> POIsToTextViews = new HashMap<POI, TextView>();
    private int POIRange;
    private int maxPOIsToDisplay;
    private boolean showStores;
    private boolean showRestaurants;
    private boolean showUtilities;
    private boolean showLandmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getSupportActionBar().hide();
        getScreenInfo();
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        layout = (RelativeLayout) findViewById(R.id.cameralayout);
        restoreSettingsFromDisk();
        initializeSensors();
        initializeTextureView();
        loadPOIsFromJSON();
    }

    private void loadPOIsFromJSON() {
        try {
            String[] files = getAssets().list(JSON_POI_DIRECTORY);
            for (String file : files) {
                List<POI> listOfPOIs = JSONToPOIGenerator.unmarshallJSONFile(getAssets().open(JSON_POI_DIRECTORY +"/" + file));
                for(POI p : listOfPOIs) {
                    TextView textView = new TextView(this);
                    textView.setVisibility(View.INVISIBLE);
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(thumbnailTextSize);
                    if(p.getPOIType().equals(POI.TYPE_LANDMARK)){
                        Drawable dr = getDrawable(R.drawable.landmark);
                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, thumbnailIconWidth, thumbnailIconHeight, true));
                        textView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                    } else if (p.getPOIType().equals(POI.TYPE_RESTAURANT)){
                        Drawable dr = getDrawable(R.drawable.restaurant);
                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, thumbnailIconWidth, thumbnailIconHeight, true));
                        textView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                    } else if (p.getPOIType().equals(POI.TYPE_STORE)){
                        Drawable dr = getDrawable(R.drawable.store);
                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, thumbnailIconWidth, thumbnailIconHeight, true));
                        textView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                    } else if (p.getPOIType().equals(POI.TYPE_UTILITY)){
                        Drawable dr = getDrawable(R.drawable.utility);
                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, thumbnailIconWidth, thumbnailIconHeight, true));
                        textView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                    }

                    layout.addView(textView);
                    textView.setText(p.getTitle());
                    final POI finalPOI = p;
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(CameraActivity.this, POIActivity.class);
                            intent.putExtra(POIActivity.STATE_POI_NAME, finalPOI.getTitle());
                            intent.putExtra(POIActivity.STATE_POI_LATITUDE, finalPOI.getLatitude());
                            intent.putExtra(POIActivity.STATE_POI_LONGITUDE, finalPOI.getLongitude());
                            intent.putExtra(POIActivity.STATE_POI_TYPE, finalPOI.getPOIType());
                            intent.putExtra(POIActivity.STATE_POI_DESCRIPTION, finalPOI.getDescription());
                            startActivity(intent);
                        }
                    });
                    points.add(p);
                    POIsToTextViews.put(p, textView);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCameraInfo(){
        try {
            CameraCharacteristics manager = cameraManager.getCameraCharacteristics(cameraId);
            float focalLength = manager.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
            SizeF size = manager.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            float width = size.getWidth();
            fov_x = Math.toDegrees(2*Math.atan(width/2/focalLength));
            float height = size.getHeight();
            fov_y = Math.toDegrees(2*Math.atan(height/2/focalLength));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getScreenInfo(){
        Display display = getWindowManager().getDefaultDisplay();
        fullScreenSize = new Point();
        display.getRealSize(fullScreenSize);

        activityScreenSize = new Point();
        display.getSize(activityScreenSize);
    }

    private void initializeSensors(){
        mLocationSensor = new LocationGPS(this, this);
        mCurrentOrientation = new float[3];

        mRotationMatrix_Y = new float[16];
        mRotationMatrix_Y[0] = 1;
        mRotationMatrix_Y[4] = 1;
        mRotationMatrix_Y[8] = 1;
        mRotationMatrix_Y[12] = 1;
        mOrientation_Y = new float[9];

        mRotationMatrix_X = new float[16];
        mRotationMatrix_X[0] = 1;
        mRotationMatrix_X[4] = 1;
        mRotationMatrix_X[8] = 1;
        mRotationMatrix_X[12] = 1;
        mOrientation_X = new float[9];

        mAngles = new float[5];
    }

    private void initializeTextureView(){
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationSensor.start();
        mSensorManager.registerListener(this, rotationSensor, 1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationSensor.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
        mSensorManager.registerListener(this, rotationSensor, 1);
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void openSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.STATE_POI_RANGE, POIRange);
        intent.putExtra(SettingsActivity.STATE_MAX_POINTS_TO_DISPLAY, maxPOIsToDisplay);
        intent.putExtra(SettingsActivity.STATE_SHOW_STORES, showStores);
        intent.putExtra(SettingsActivity.STATE_SHOW_RESTAURANTS, showRestaurants);
        intent.putExtra(SettingsActivity.STATE_SHOW_UTILITIES, showUtilities);
        intent.putExtra(SettingsActivity.STATE_SHOW_LANDMARKS, showLandmarks);
        startActivityForResult(intent, SettingsActivity.SETTINGS_REQUEST);
    }

    private void restoreSettingsFromDisk() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.KEY_AR_PREFS, MODE_PRIVATE);
        POIRange = prefs.getInt(SettingsActivity.STATE_POI_RANGE, 1000);
        maxPOIsToDisplay = prefs.getInt(SettingsActivity.STATE_MAX_POINTS_TO_DISPLAY, 20);
        showStores = prefs.getBoolean(SettingsActivity.STATE_SHOW_STORES, true);
        showRestaurants = prefs.getBoolean(SettingsActivity.STATE_SHOW_RESTAURANTS, true);
        showUtilities = prefs.getBoolean(SettingsActivity.STATE_SHOW_UTILITIES, true);
        showLandmarks = prefs.getBoolean(SettingsActivity.STATE_SHOW_LANDMARKS, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SettingsActivity.SETTINGS_REQUEST && resultCode == RESULT_OK && data != null) {
            POIRange = data.getIntExtra(SettingsActivity.STATE_POI_RANGE, 1000);
            maxPOIsToDisplay = data.getIntExtra(SettingsActivity.STATE_MAX_POINTS_TO_DISPLAY, 20);
            showStores = data.getBooleanExtra(SettingsActivity.STATE_SHOW_STORES, true);
            showRestaurants = data.getBooleanExtra(SettingsActivity.STATE_SHOW_RESTAURANTS, true);
            showUtilities = data.getBooleanExtra(SettingsActivity.STATE_SHOW_UTILITIES, true);
            showLandmarks = data.getBooleanExtra(SettingsActivity.STATE_SHOW_LANDMARKS, true);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            mAngles = event.values;

            // Part 1 - used for Y axis calculations
            SensorManager.getRotationMatrixFromVector(mRotationMatrix_Y, mAngles);
            SensorManager.remapCoordinateSystem(mRotationMatrix_Y, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix_Y);
            SensorManager.getOrientation(mRotationMatrix_Y, mOrientation_Y);
            mOrientation_Y[1] = (float) (Math.toDegrees(mOrientation_Y[1]));

            // Part 2 - used for X axis calculations
            SensorManager.getRotationMatrixFromVector(mRotationMatrix_X, mAngles);
            SensorManager.remapCoordinateSystem(mRotationMatrix_X, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRotationMatrix_X);
            SensorManager.getOrientation(mRotationMatrix_X, mOrientation_X);
            mOrientation_X[0] = ARMath.normalize(Math.toDegrees(mOrientation_X[0]));

            mCurrentOrientation[0] = ARMath.normalize(mOrientation_X[0] - 90);
            mCurrentOrientation[1] = mOrientation_Y[1];
            for (POI poi : points) {
                checkSettingsAndDisplay(poi);
            }
            spacingCounter = 0;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        sortPOIsOnDistance();
        for (POI poi : points) {
            checkSettingsAndDisplay(poi);
            POIsToTextViews.get(poi).setText(String.format(poi.getTitle() + "\n" + "%.2f" + "km", poi.getDistanceFromCurrentLocation()));
        }
        spacingCounter = 0;
    }

    private void checkSettingsAndDisplay(POI poi){
        if(poi.getDistanceFromCurrentLocation() <= (POIRange/1000.0)){      //To convert from meters to kilometers
            if(showStores && poi.getPOIType().equals(POI.TYPE_STORE)){
                calculateAndPlaceThumbnails(poi);
                return;
            }
            if(showLandmarks && poi.getPOIType().equals(POI.TYPE_LANDMARK)){
                calculateAndPlaceThumbnails(poi);
                return;
            }
            if(showRestaurants && poi.getPOIType().equals(POI.TYPE_RESTAURANT)){
                calculateAndPlaceThumbnails(poi);
                return;
            }
            if(showUtilities && poi.getPOIType().equals(POI.TYPE_UTILITY)){
                calculateAndPlaceThumbnails(poi);
                return;
            }
        }
        POIsToTextViews.get(poi).setVisibility(View.INVISIBLE);
    }

    private void sortPOIsOnDistance(){
        for (POI poi : points) {
            double distance = ARMath.getPOIDistance(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), poi.getLongitude(), poi.getLatitude());
            poi.setDistanceFromCurrentLocation(distance);
        }
        Collections.sort(points, POI.getIncreasingDistanceComparator());
    }

    public void calculateAndPlaceThumbnails(POI poi){
        if(mCurrentLocation != null){
            // X Calculations
            double direction = ARMath.getPOIDirection(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), poi.getLongitude(), poi.getLatitude());
            double differenceX = ARMath.getRelativeAngleOfPOI(mCurrentOrientation[0], direction);

            // Y Calculations
            //double distance = ARMath.getPOIDistance(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), points.get(i).getLongitude(), points.get(i).getLatitude());
            double heightDifference = ARMath.getHeightDifference(0, 0);     //0,0 means that height is not included in calculations
            double absoluteHeightAngle = ARMath.getAbsoluteHeightAngle(poi.getDistanceFromCurrentLocation(), heightDifference);
            double differenceY = ARMath.getRelativeHeightAngle(mCurrentOrientation[1], absoluteHeightAngle);

            // Add TextViews
            // These numbers are still in the experimental phase and were picked via trial and error. Ideally, values will be based on the FOV of any camera in x and y directions
            TextView textView = POIsToTextViews.get(poi);
            if(differenceX/EXPERIMENTAL_FOV_X <= 1 && differenceY/EXPERIMENTAL_FOV_Y <= 1) {
                if (ARMath.getSide(mCurrentOrientation[0], direction, fov_x) == 0) {
                    textView.setX((float) (layout.getWidth()*(THUMBNAIL_STARTING_POINT_X + differenceX/EXPERIMENTAL_FOV_X) - textView.getWidth()/2));
                } else {
                    textView.setX((float) (layout.getWidth()*(THUMBNAIL_STARTING_POINT_X - differenceX/EXPERIMENTAL_FOV_X) - textView.getWidth()/2));
                }

                if (ARMath.getAboveBelow(mCurrentOrientation[1], absoluteHeightAngle) == 0) {
                    textView.setY((float) (layout.getHeight()*(THUMBNAIL_STARTING_POINT_Y + differenceY/EXPERIMENTAL_FOV_Y) - textView.getHeight()/2));
                } else {
                    textView.setY((float) (layout.getHeight()*(THUMBNAIL_STARTING_POINT_Y - differenceY/EXPERIMENTAL_FOV_Y) - textView.getHeight()/2));
                }

                if (maxPOIsToDisplay > checkHowManyPOIOnScreen(poi)) {
                    textView.setY(textView.getY() - spacingCounter*THUMBNAIL_SPACING_AMOUNT);
                    textView.setVisibility(View.VISIBLE);
                    spacingCounter++;
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }
            } else {
                textView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private int checkHowManyPOIOnScreen(POI p) {
        int howManyVisible = 0;
        Rect scrollBounds = new Rect();
        layout.getHitRect(scrollBounds);
        for (POI poi : points) {
            if (poi != p && POIsToTextViews.get(poi).getLocalVisibleRect(scrollBounds) && POIsToTextViews.get(poi).getVisibility() == View.VISIBLE) {
                howManyVisible++;
            }
        }
        return howManyVisible;
    }



    // HERE BE CAMERA STUFF
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
            getCameraInfo();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };


    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA,} , REQUEST_CAMERA_PERMISSION);
                return;
            }
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(CameraActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}