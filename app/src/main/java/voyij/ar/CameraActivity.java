package voyij.ar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
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
import android.hardware.camera2.CameraCharacteristics.*;
import android.location.Location;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SizeF;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.phenotype.Configuration;

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
    private File file;
    private CameraManager cameraManager;

    // Sensor Variables
    private SensorManager mSensorManager;
    private Sensor rotationSensor;
    private float[] mRotationMatrix;
    private float[] mOrientation;
    private float[] mRotationMatrix2;
    private float[] mOrientation2;
    private float[] mAngles;
    private Compass mCompassSensor;
    private LocationGPS mLocationSensor;
    private Location mCurrentLocation;
    private float[] mCurrentOrientation;

    // Screen Variables
    int bottomBarHeight;
    private Point activityScreenSize;
    private Point fullScreenSize;

    // POI Variables
    private POI[] points;
    private ImageView[] images;
    private TextView[] texts;
    private int POIRange;
    private boolean showStores;
    private boolean showRestaurants;
    private boolean showUtilities;
    private boolean showLandmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getScreenInfo();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        checkPermissions();
        restoreSettingsFromDisk();
        initializeSensors();
        initializeTextureView();
        loadPOIsFromJSON();
        createPoints();
        //createImages();
        createTexts();

    }

    private void loadPOIsFromJSON() {
        try {
            List<POI> points = JSONToPOIGenerator.unMarshallJSONFile(getAssets().open("JSONPOIs/twoPOIs.txt"));
            System.out.println(points);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPoints() {
        points = new POI[5];
        points[0] = new POI("Chapel", 36.001901, -78.940278, POI.TYPE_LANDMARK);
        points[1] = new POI("West Union",36.000798 ,-78.939011, POI.TYPE_RESTAURANT);
        points[2] = new POI("Cameron", 35.997592 , -78.942173, POI.TYPE_LANDMARK);
        points[3] = new POI("Fuqua", 35.998843, -78.947274, POI.TYPE_LANDMARK);
        points[4] = new POI("LSRC", 36.004361 , -78.941871, POI.TYPE_LANDMARK);
    }

    private void createTexts(){
        texts = new TextView[5];

        TextView tv1 = (TextView) findViewById(R.id.textView1);
        TextView tv2 = (TextView) findViewById(R.id.textView2);
        TextView tv3 = (TextView) findViewById(R.id.textView3);
        TextView tv4 = (TextView) findViewById(R.id.textView4);
        TextView tv5 = (TextView) findViewById(R.id.textView5);

        texts[0] = tv1;
        texts[1] = tv2;
        texts[2] = tv3;
        texts[3] = tv4;
        texts[4] = tv5;

    }

//    private void createImages(){
//        images = new ImageView[5];
//
//        ImageView iv1 = (ImageView) findViewById(R.id.imageView1);
//        ImageView iv2 = (ImageView) findViewById(R.id.imageView2);
//        ImageView iv3 = (ImageView) findViewById(R.id.imageView3);
//        ImageView iv4 = (ImageView) findViewById(R.id.imageView4);
//        ImageView iv5 = (ImageView) findViewById(R.id.imageView5);
//
//        images[0] = iv1;
//        images[1] = iv2;
//        images[2] = iv3;
//        images[3] = iv4;
//        images[4] = iv5;
////        for(int i = 0; i < 5; i++){
////            ImageView iv = new ImageView(this);
////            iv.setImageResource(R.drawable.building);
////            images[i] = iv;
////        }
//    }

    private double fov_x;
    private double fov_y;
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
        bottomBarHeight = fullScreenSize.y - activityScreenSize.y;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
    }

    private void checkPermissions(){
        // If the phone doesn't have sensors, exit the app (or do something else)
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            // Check to see if this actually works
            System.out.println("App exiting. No rotation vector sensor");
            this.finish();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation for
            // ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            System.out.println("FAILED");
//            return;
        }
    }

    private void initializeSensors(){
        mCompassSensor = new Compass(mSensorManager);
        mLocationSensor = new LocationGPS(this, this);
        mCurrentOrientation = new float[3];

        mRotationMatrix = new float[16];
        mRotationMatrix[0] = 1;
        mRotationMatrix[4] = 1;
        mRotationMatrix[8] = 1;
        mRotationMatrix[12] = 1;
        mOrientation = new float[9];

        mRotationMatrix2 = new float[16];
        mRotationMatrix2[0] = 1;
        mRotationMatrix2[4] = 1;
        mRotationMatrix2[8] = 1;
        mRotationMatrix2[12] = 1;
        mOrientation2 = new float[9];

        mAngles = new float[5];
    }

    private void initializeTextureView(){
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void onStart() {
        super.onStart();
        mLocationSensor.start();
        mSensorManager.registerListener(this, rotationSensor, 1);
    }

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
        mCompassSensor.registerLocationListener(this);
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
        mCompassSensor.unregisterLocationListener(this);
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
        System.out.println("Opening Settings");
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.STATE_POI_RANGE, POIRange);
        intent.putExtra(SettingsActivity.STATE_SHOW_STORES, showStores);
        intent.putExtra(SettingsActivity.STATE_SHOW_RESTAURANTS, showRestaurants);
        intent.putExtra(SettingsActivity.STATE_SHOW_UTILITIES, showUtilities);
        intent.putExtra(SettingsActivity.STATE_SHOW_LANDMARKS, showLandmarks);
        startActivityForResult(intent, SettingsActivity.SETTINGS_REQUEST);
    }

    private void restoreSettingsFromDisk() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.KEY_AR_PREFS, MODE_PRIVATE);
        POIRange = prefs.getInt(SettingsActivity.STATE_POI_RANGE, 1000);
        showStores = prefs.getBoolean(SettingsActivity.STATE_SHOW_STORES, true);
        showRestaurants = prefs.getBoolean(SettingsActivity.STATE_SHOW_RESTAURANTS, true);
        showUtilities = prefs.getBoolean(SettingsActivity.STATE_SHOW_UTILITIES, true);
        showLandmarks = prefs.getBoolean(SettingsActivity.STATE_SHOW_LANDMARKS, true);
        System.out.println("Restore Settings from Disk");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SettingsActivity.SETTINGS_REQUEST && resultCode == RESULT_OK && data != null) {
            POIRange = data.getIntExtra(SettingsActivity.STATE_POI_RANGE, 1000);
            showStores = data.getBooleanExtra(SettingsActivity.STATE_SHOW_STORES, true);
            showRestaurants = data.getBooleanExtra(SettingsActivity.STATE_SHOW_RESTAURANTS, true);
            showUtilities = data.getBooleanExtra(SettingsActivity.STATE_SHOW_UTILITIES, true);
            showLandmarks = data.getBooleanExtra(SettingsActivity.STATE_SHOW_LANDMARKS, true);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do we need to add stuff here?
    }

    final float alpha = 0.15f;
    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i=0; i<input.length; i++) {
            output[i] = input[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            mAngles = lowPass(event.values, mAngles);

            // Part 1 - used for Y axis calculations
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, mAngles);
            SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);
            mOrientation[0] = ARMath.normalize(Math.round(Math.toDegrees(mOrientation[0])));
            mOrientation[1] = (float) Math.round(Math.toDegrees(mOrientation[1]));
            mOrientation[2] = ARMath.normalize(Math.round(Math.toDegrees(mOrientation[2])));

            // Part 2 - used for X axis calculations
            SensorManager.getRotationMatrixFromVector(mRotationMatrix2, mAngles);
            SensorManager.remapCoordinateSystem(mRotationMatrix2, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRotationMatrix2);
            SensorManager.getOrientation(mRotationMatrix2, mOrientation2);
            mOrientation2[0] = ARMath.normalize(Math.round(Math.toDegrees(mOrientation2[0])));
            mOrientation2[1] = (float) Math.round(Math.toDegrees(mOrientation2[1]));
            mOrientation2[2] = ARMath.normalize(Math.round(Math.toDegrees(mOrientation2[2])));


            mCurrentOrientation[0] = ARMath.normalize(mOrientation2[0] - 90);
            mCurrentOrientation[1] = mOrientation[1];

            for (int i = 0; i < points.length; i++) {
                doMath(i);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        for (int i = 0; i < points.length; i++) {
            doMath(i);
        }
    }

    public void doMath(int i){
        if(mCurrentLocation != null){
            // X Calculations
            double direction = ARMath.getPOIDirection(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), points[i].getLongitude(), points[i].getLatitude());
            double differenceX = ARMath.getRelativeAngleOfPOI(mCurrentOrientation[0], direction);

            // Y Calculations
            double distance = ARMath.getPOIDistance(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), points[i].getLongitude(), points[i].getLatitude());
            double heightDifference = ARMath.getHeightDifference(0, 0);
            double absoluteHeightAngle = ARMath.getAbsoluteHeightAngle(distance, heightDifference);
            double differenceY = ARMath.getRelativeHeightAngle(mCurrentOrientation[1], absoluteHeightAngle);

//            Add ImageViews
//            if(differenceX/fov_x <= 1 && differenceY/fov_y <= 1) {
//                images[i].setVisibility(View.VISIBLE);
//                if (ARMath.getSide(mCurrentOrientation[0], direction, fov_x) == 0) {
//                    images[i].setX((float) (activityScreenSize.x*(0.5 + differenceX/fov_x/2) - images[i].getWidth()/2));
//                } else {
//                    images[i].setX((float) (activityScreenSize.x*(0.5 - differenceX/fov_x/2) - images[i].getWidth()/2));
//                }
//
//                if (ARMath.getAboveBelow(mCurrentOrientation[1], absoluteHeightAngle) == 0) {
//                    images[i].setY((float) (activityScreenSize.y*(0.5 + differenceY/fov_y/2) - images[i].getHeight()/2));
//                } else {
//                    images[i].setY((float) (activityScreenSize.y*(0.5 - differenceY/fov_y/2) - images[i].getHeight()/2));
//                }
//                System.out.println(points[i].getTitle() + ": " + images[i].getX() + " " + images[i].getY());
//            } else {
//                images[i].setVisibility(View.INVISIBLE);
//            }

            // Add TextViews
            if(differenceX/fov_x <= 1 && differenceY/fov_y <= 1) {
                texts[i].setVisibility(View.VISIBLE);
                if (ARMath.getSide(mCurrentOrientation[0], direction, fov_x) == 0) {
                    texts[i].setX((float) (activityScreenSize.x*(0.5 + differenceX/45) - texts[i].getWidth()/2));
                } else {
                    texts[i].setX((float) (activityScreenSize.x*(0.5 - differenceX/45) - texts[i].getWidth()/2));
                }

                if (ARMath.getAboveBelow(mCurrentOrientation[1], absoluteHeightAngle) == 0) {
                    texts[i].setY((float) (activityScreenSize.y*(0.5 + differenceY/fov_y/2) - texts[i].getHeight()/2) + i*50);
                } else {
                    texts[i].setY((float) (activityScreenSize.y*(0.5 - differenceY/fov_y/2) - texts[i].getHeight()/2) + i*50);
                }

            } else {
                texts[i].setVisibility(View.INVISIBLE);
            }
        }
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
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