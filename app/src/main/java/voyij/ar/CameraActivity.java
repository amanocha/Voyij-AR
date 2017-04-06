package voyij.ar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import android.util.Log;
import android.util.SizeF;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.phenotype.Configuration;

public class CameraActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

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

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float[] smoothed;

    private Compass mCompassSensor;
    private LocationGPS mLocationSensor;

    private Location mCurrentLocation;
    private float[] mCurrentOrientation;

    ImageView picture;
    int bottomBarHeight;
    private Point activityScreenSize;
    private Point fullScreenSize;
    private POI[] points;


    private void createPoints() {
        points = new POI[5];
        points[0] = new POI("Chapel", 36.001901, -78.940278);
        points[1] = new POI("West Union",36.000798 ,-78.939011);
        points[2] = new POI("Cameron",35.997592 ,-78.942173);
        points[3] = new POI("Fuqua", 35.998843,-78.947274);
        points[4] = new POI("LSRC",36.004361 ,-78.941871);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        picture = (ImageView) findViewById(R.id.imageView1);

        Display display = getWindowManager().getDefaultDisplay();
        fullScreenSize = new Point();
        display.getRealSize(fullScreenSize);
        activityScreenSize = new Point();
        display.getSize(activityScreenSize);
        bottomBarHeight = fullScreenSize.y - activityScreenSize.y;

//        picture.setX(fullScreenSize.x/2);
//        picture.setY(fullScreenSize.y/2 - bottomBarHeight);


        SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        // If the phone doesn't have sensors, exit the app (or do something else)
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null ||
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null){
            // Check to see if this actually works
            this.finish();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            System.out.println("FAILED");
//            return;
        }

        mCompassSensor = new Compass(mSensorManager);
        mLocationSensor = new LocationGPS(this, this);

        mGravity = new float[3];
        mGeomagnetic = new float[3];
        smoothed = new float[3];

        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        System.out.println("TV height:" + getWindow().getDecorView().getHeight());
        createPoints();
    }

    protected void onStart() {
        super.onStart();
        mLocationSensor.start();

    }

    protected void onStop() {
        super.onStop();
        mLocationSensor.stop();
    }

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

    // Compass changes
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        System.out.println("Accuracy changed: " + sensor.getName()+ ": " + accuracy);
    }

    final float alpha = 0.2f;

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i=0; i<input.length; i++) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }

        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // final float alpha = 0.97f;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int deviceOrientation = getResources().getConfiguration().orientation;
            System.out.println(deviceOrientation);
        }

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //mGravity = event.values;
//                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
//                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
//                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
                mGravity = lowPass(event.values.clone(), mGravity);
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                //mGeomagnetic = event.values;
//                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
//                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
//                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
                mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
            }

//            if(mGravity != null && mGeomagnetic != null){
//                float R[] = new float[9];
//                float I[] = new float[9];
//                SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
//                System.out.println((([0]*180)/Math.PI)+180);
//            }

            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    // at this point, orientation contains the azimuth, pitch and roll values.
                    // System.out.println("Azimuth: " + normalize(Math.toDegrees(orientation[0])));
                    //System.out.println("Pitch: " + Math.toDegrees(orientation[1]));
                    //System.out.println("Roll: " + Math.toDegrees(orientation[2]));

                    orientation[0] = normalize(Math.toDegrees(orientation[0])); //azimuth
                    orientation[1] = (float) Math.toDegrees(orientation[1]); //pitch
                    orientation[2] = normalize(Math.toDegrees(orientation[2])); //roll

                    mCurrentOrientation = orientation;
                    doMath();
                }
            }
        }
    }

//            float R[] = new float[9];
//            float I[] = new float[9];
//            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
//
//            if (success) {
//                float orientation[] = new float[3];
//                SensorManager.getOrientation(R, orientation);
//
//                float azimuth = normalize(Math.toDegrees(orientation[0])); // orientation contains: azimuth, pitch and roll
//                float pitch = normalize(Math.toDegrees(orientation[1]));
//                float roll = normalize(Math.toDegrees(orientation[2]));
//
//                //orientation[0] = azimuth;
//                System.out.println(azimuth);
//
//                orientation[0] = Math.round(azimuth); //Angle between device's current compass direction and magnetic north
//                orientation[1] = Math.round(pitch); //Angle between a plane parallel to device's screen and a plane parallel to ground
//                orientation[2] = Math.round(roll); //Angle between a plane perpendicular to device's screen and a plane perpendicular to ground
//
//                //System.out.println("Compass heading: " + orientation[0]);
//                //System.out.println(Double.toString(azimuth) + " " + Double.toString(pitch) + " " + Double.toString(roll));
//                //Toast.makeText(this, "Orientation Changed", Toast.LENGTH_SHORT).show();
//                mCurrentOrientation = orientation;
//                doMath();
//            }
        //}

    private float normalize(double value) {
        if (value < 0) {
            return (360 + (float)value);
        } else {
            return (float)value;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
        mCurrentLocation = location;
        doMath();
    }



    public void doMath(){
        if(mCurrentLocation != null){
            //Toast.makeText(this, Double.toString(ARMath.getAbsoluteAngleOfPOI(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), -78.940278, 36.001901)), Toast.LENGTH_SHORT).show();

//            Double absoluteAngle = ARMath.getAbsoluteAngleOfPOI(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), -78.940278, 36.001901);
//            Double relativeAngle = ARMath.getRelativeAngleOfPOI(mCurrentOrientation[0], absoluteAngle);
            //System.out.println("Absolute angle: " + Double.toString(absoluteAngle));

            double direction = ARMath.getPOIDirection(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), -78.940278, 36.001901);
            double differenceX = ARMath.getRelativeAngleOfPOI(mCurrentOrientation[0], direction);
            double fov_x = 45;

            //double directionHeight = ARMath.getPOIAltitude(mCurrentLocation.getAltitude(), 119);
            double distance = ARMath.getPOIDistance(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude(), -78.940278, 36.001901);
            double heightDifference = ARMath.getHeightDifference(0, 0);

            double absoluteHeightAngle = ARMath.getAbsoluteHeightAngle(distance, heightDifference);
            //System.out.println("absolute: " + Math.toDegrees(absoluteHeightAngle));
            double differenceY = ARMath.getRelativeHeightAngle(mCurrentOrientation[1], absoluteHeightAngle);
//            System.out.println("pitch: " + mCurrentOrientation[1]);
//            System.out.println("relative: " + relativeHeightAngle);
            //System.out.println("Relative Angle: " + relativeHeightAngle);
            //double differenceHeight = ARMath.getRelativeAltitudeOfPOI(mCurrentOrientation[1], 0.0);
            double fov_y = 90;

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

            System.out.println(mCurrentOrientation[1]);

            if(differenceX/fov_x <= 1 && differenceY/fov_y <= 1) {
                picture.setVisibility(View.VISIBLE);
                if (ARMath.getSide(mCurrentOrientation[0], direction, fov_x) == 0) {
                    picture.setX((float) (activityScreenSize.x*(0.5 + differenceX/fov_x/2) - picture.getWidth()/2));
                } else {
                    picture.setX((float) (activityScreenSize.x*(0.5 - differenceX/fov_x/2) - picture.getWidth()/2));
                }

                if (ARMath.getAboveBelow(mCurrentOrientation[1], absoluteHeightAngle) == 0) {
                    picture.setY((float) (activityScreenSize.y*(0.5 + differenceY/fov_y/2) - picture.getHeight()/2));
                } else {
                    picture.setY((float) (activityScreenSize.y*(0.5 - differenceY/fov_y/2) - picture.getHeight()/2));
                }
            } else {
                picture.setVisibility(View.INVISIBLE);
            }
        }
    }
}
