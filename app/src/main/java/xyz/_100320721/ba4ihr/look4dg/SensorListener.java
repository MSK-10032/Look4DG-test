package xyz._100320721.ba4ihr.look4dg;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import xyz._100320721.ba4ihr.look4dg.models.DeviceOrientation;

public class SensorListener implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private OnOrientationUpdateListener listener;

    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    public interface OnOrientationUpdateListener {
        void onOrientationUpdate(DeviceOrientation orientation);
    }

    public SensorListener(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void startListening(OnOrientationUpdateListener listener) {
        this.listener = listener;
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            // orientationAngles[0]: azimuth (radians) around -z axis (0=北)
            // orientationAngles[1]: pitch (radians) around x axis (-π/2 to π/2)
            // orientationAngles[2]: roll (radians) around y axis
            float azimuth = (float) Math.toDegrees(orientationAngles[0]);
            if (azimuth < 0) azimuth += 360;

            float pitch = (float) Math.toDegrees(orientationAngles[1]);

            // 简单映射：手机指向的仰角用pitch表示，但可能需要根据roll调整。此处简化。
            // 实际使用时可能需要根据具体手持方式校准。
            DeviceOrientation orientation = new DeviceOrientation(azimuth, pitch);
            if (listener != null) {
                listener.onOrientationUpdate(orientation);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}