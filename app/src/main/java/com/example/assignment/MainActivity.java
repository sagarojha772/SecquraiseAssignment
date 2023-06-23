package com.example.assignment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment.CaptureData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView date_text, time_text, captureCount, locationTextView, batteryStatus, batteryPercentageText,
            connectStatus, frequencyCnt;
    EditText frequencyCount;
    int count = 0, initialFrequency = 15;
    ImageView imageCaptured;
    Button btnCapture;
    FirebaseFirestore firestore;

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private DatabaseReference databaseRef;


    LocationManager locationManager;
    LocationListener locationListener;

    private final ActivityResultLauncher<Intent> captureImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null && extras.containsKey(MediaStore.EXTRA_OUTPUT)) {
                            Bitmap imageBitmap = (Bitmap) extras.get(MediaStore.EXTRA_OUTPUT);
                            if (imageBitmap != null) {
                                imageCaptured.setImageBitmap(imageBitmap);
                            } else {
                                Toast.makeText(this, "Failed to retrieve captured image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        date_text = findViewById(R.id.date_text);
        time_text = findViewById(R.id.time_text);
        captureCount = findViewById(R.id.cap_cnt_val);
        frequencyCnt = findViewById(R.id.frequencyCnt);
        frequencyCount = findViewById(R.id.freq_cnt_val);
        locationTextView = findViewById(R.id.Loc_val);
        batteryStatus = findViewById(R.id.batt_charging_val);
        batteryPercentageText = findViewById(R.id.batt_cnt_val);
        connectStatus = findViewById(R.id.conn_val);
        imageCaptured = findViewById(R.id.img_captured);
        btnCapture = findViewById(R.id.btn_to_capture);

        updateDateAndTime();
        updateCaptureCount();
        updateConnectivity();
        updateBatteryStatus();
        updateBatteryPercentage();
        updateLocationStatus();

        frequencyCnt.setText(String.format(getString(R.string.frequency_label), initialFrequency));

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDateAndTime();
                updateCaptureCount();
                updateFrequency();
                updateConnectivity();
                updateBatteryStatus();
                updateBatteryPercentage();
                updateLocationStatus();
                updateCaptureImage();

                CaptureData captureData = new CaptureData();
                String imageUrl = null;
                captureData.setImageUri(imageUrl);
                captureData.setDate(date_text.getText().toString());
                captureData.setTime(time_text.getText().toString());
                captureData.setCaptureCount(count);
                captureData.setLocation(locationTextView.getText().toString());
                captureData.setBatteryStatus(batteryStatus.getText().toString());
                captureData.setConnectivityStatus(connectStatus.getText().toString());

                DatabaseReference captureDataRef = databaseRef.child("captureData");

                captureDataRef.push().setValue(captureData)
                        .addOnSuccessListener(aVoid -> {
                            // Data upload success
                            Toast.makeText(MainActivity.this, "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Data upload failed
                            Toast.makeText(MainActivity.this, "Failed to upload data", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Toast.makeText(this, "Unable to obtain LocationManager", Toast.LENGTH_SHORT).show();
        } else {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String coordinates = latitude + "," + longitude;
                    locationTextView.setText(coordinates);

                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };
        }
    }

    private void updateDateAndTime() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(currentDate);

        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        String formattedTime = simpleTimeFormat.format(currentDate);

        date_text.setText(formattedDate);
        time_text.setText(formattedTime);
    }

    private void updateCaptureCount() {
        count++;
        captureCount.setText(String.valueOf(count));
    }

    private void updateConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        String status = isConnected ? "ON" : "OFF";
        connectStatus.setText(status);
    }

    private void updateBatteryStatus() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = registerReceiver(null, filter);

        int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        String chargingStatus = isCharging ? "ON" : "OFF";
        batteryStatus.setText(chargingStatus);
    }

    private void updateBatteryPercentage() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPercentage = (int) ((level / (float) scale) * 100);
        String percentageText = batteryPercentage + "%";
        batteryPercentageText.setText(percentageText);
    }

    private void updateLocationStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request location permission if not granted
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                // Check if location is enabled
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // Request location updates
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                } else {
                    // Show location settings prompt
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocationStatus();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateFrequency() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Frequency (in minutes)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(frequencyCount.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String frequencyStr = input.getText().toString();
                int frequency = Integer.parseInt(frequencyStr);

                frequencyCnt.setText(String.format(getString(R.string.frequency_label), frequency));
                frequencyCount.setText(frequencyStr);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateCaptureImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            captureImageLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }
}

