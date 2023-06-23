package com.example.assignment;

public class CaptureData {
    private String imageUri;
    private String date;
    private String time;
    private int captureCount;
    private String location;
    private String batteryStatus;
    private String connectivityStatus;

    public CaptureData() {
        // Required empty constructor for Firebase
    }

    public CaptureData(String imageUri, String date, String time, int captureCount, String location,
                       String batteryStatus, String connectivityStatus) {
        this.imageUri = imageUri;
        this.date = date;
        this.time = time;
        this.captureCount = captureCount;
        this.location = location;
        this.batteryStatus = batteryStatus;
        this.connectivityStatus = connectivityStatus;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCaptureCount() {
        return captureCount;
    }

    public void setCaptureCount(int captureCount) {
        this.captureCount = captureCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getConnectivityStatus() {
        return connectivityStatus;
    }

    public void setConnectivityStatus(String connectivityStatus) {
        this.connectivityStatus = connectivityStatus;
    }

}
