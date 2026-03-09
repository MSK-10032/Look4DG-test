package qrz.ba4ihr.look4dg.models;

public class SatelliteData {
    private float azimuth;   // 方位角 0-360
    private float elevation; // 仰角 0-90

    public SatelliteData(float azimuth, float elevation) {
        this.azimuth = azimuth;
        this.elevation = elevation;
    }

    public float getAzimuth() { return azimuth; }
    public float getElevation() { return elevation; }

    public void setAzimuth(float azimuth) { this.azimuth = azimuth; }
    public void setElevation(float elevation) { this.elevation = elevation; }
}