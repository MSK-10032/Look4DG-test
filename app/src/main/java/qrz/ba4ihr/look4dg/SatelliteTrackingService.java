package com.msktest.look4dg;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SatelliteTrackingService implements SensorEventListener {

    private final Context context;
    private final String look4SatUrl;
    private final String dgLabWsUrl;

    private Float satelliteAzimuth = null;
    private Float phoneAzimuth = null;

    private final SensorManager sensorManager;
    private final Sensor rotationSensor;

    private final OkHttpClient wsClient;
    private WebSocket dgLabWebSocket = null;

    public SatelliteTrackingService(Context context, String look4SatUrl, String dgLabWsUrl) {
        this.context = context;
        this.look4SatUrl = look4SatUrl;
        this.dgLabWsUrl = dgLabWsUrl;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        wsClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public void start() {
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        connectToDGLab();
        listenToLook4Sat();
    }

    public void stop() {
        sensorManager.unregisterListener(this);
        if (dgLabWebSocket != null) {
            dgLabWebSocket.close(1000, "App stopped");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientationAngles = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
            phoneAzimuth = (float) Math.toDegrees(orientationAngles[0]);
            checkDeviationAndShock();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // 与 Look4Sat 通信，假定 Look4Sat 通过 WebSocket 推送卫星位置，格式 { "azimuth": 123.0 }
    private void listenToLook4Sat() {
        Request request = new Request.Builder().url(look4SatUrl).build();
        wsClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject obj = new JSONObject(text);
                    satelliteAzimuth = (float) obj.getDouble("azimuth");
                    checkDeviationAndShock();
                } catch (Exception e) {
                    Log.e("Look4DG", "卫星数据异常: " + e.getMessage());
                }
            }
        });
    }

    private void connectToDGLab() {
        Request request = new Request.Builder().url(dgLabWsUrl).build();
        dgLabWebSocket = wsClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("Look4DG", "DG-LAB WebSocket已连接");
            }
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("Look4DG", "DG-LAB WebSocket连接失败: " + t.getMessage());
            }
        });
    }

    private void checkDeviationAndShock() {
        if (satelliteAzimuth != null && phoneAzimuth != null) {
            float diff = Math.abs(satelliteAzimuth - phoneAzimuth);
            float deviation = diff > 180 ? 360 - diff : diff;
            if (deviation > 10) {
                sendShockSignal();
            }
        }
    }

    private void sendShockSignal() {
        if (dgLabWebSocket == null) return;
        try {
            JSONObject command = new JSONObject();
            command.put("action", "shock");
            command.put("strength", 100);
            dgLabWebSocket.send(command.toString());
            Log.d("Look4DG", "已发送电击信号至DG-LAB");
        } catch (Exception e) {
            Log.e("Look4DG", "电击命令发送失败: " + e.getMessage());
        }
    }
}
