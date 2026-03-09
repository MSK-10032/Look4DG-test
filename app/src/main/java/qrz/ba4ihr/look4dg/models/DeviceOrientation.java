package qrz.ba4ihr.look4dg.models;

public class DeviceOrientation {
    private float azimuth;   // 方位角 0-360
    private float pitch;     // 俯仰角 -90~90 (向上为正)

    public DeviceOrientation(float azimuth, float pitch) {
        this.azimuth = azimuth;
        this.pitch = pitch;
    }

    public float getAzimuth() { return azimuth; }
    public float getPitch() { return pitch; }

    public void setAzimuth(float azimuth) { this.azimuth = azimuth; }
    public void setPitch(float pitch) { this.pitch = pitch; }
}