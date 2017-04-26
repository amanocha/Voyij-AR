package voyij.ar;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private TextView textViewPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewPermissions = (TextView) findViewById(R.id.textViewPermissions);
        textViewPermissions.setVisibility(View.INVISIBLE);
        getSupportActionBar().hide();
    }

    private boolean checkHardwareExistence() {
        boolean hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        boolean hasGPS = getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        boolean hasRotationVector = ((SensorManager) getSystemService(SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null;
        if(!hasCamera) {
            Toast.makeText(this, "Sorry, pal. How are you gonna use AR without a camera on your device?", Toast.LENGTH_LONG).show();
        }
        if(!hasGPS) {
            Toast.makeText(this, "Sorry, pal. Can't use our app if your device doesn't have a GPS.", Toast.LENGTH_LONG).show();
        }
        if(!hasRotationVector) {
            Toast.makeText(this, "Sorry, pal. Can't use our app if your device doesn't have a built-in rotation vector (We don't know what a rotation vector is either...)", Toast.LENGTH_LONG).show();
        }
        if (!hasCamera || !hasGPS || !hasRotationVector) {
            return false;
        }
        return true;
    }

    private boolean checkPermissions() {
        System.out.println("in check permissions");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "Please give us access to your camera and location!", Toast.LENGTH_LONG).show();
            System.out.println("in permissions");
            textViewPermissions.setVisibility(View.VISIBLE);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            int grantedCount = 0;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedCount++;
                }
            }
            if (grantedCount == permissions.length) {
                textViewPermissions.setVisibility(View.INVISIBLE);
                startActivity(new Intent(this, CameraActivity.class));
            }
        }
    }

    public void openAR(View view) {
        boolean hasHardware = checkHardwareExistence();
        if (hasHardware) {
            boolean hasPermissions = checkPermissions();
            if (hasPermissions) {
                textViewPermissions.setVisibility(View.INVISIBLE);
                startActivity(new Intent(this, CameraActivity.class));
                return;
            }
        }
    }
}
