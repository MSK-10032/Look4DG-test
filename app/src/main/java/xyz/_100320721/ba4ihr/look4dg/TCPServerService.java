package xyz._100320721.ba4ihr.look4dg;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import xyz._100320721.ba4ihr.look4dg.models.SatelliteData;

public class TCPServerService extends Service {
    private static final String TAG = "TCPServerService";
    public static final int PORT = 5973;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private SatelliteData latestSatelliteData;
    private OnDataReceivedListener dataListener;

    public interface OnDataReceivedListener {
        void onSatelliteDataReceived(SatelliteData data);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        new Thread(this::startServer).start();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            Log.i(TAG, "TCP Server started on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                Log.i(TAG, "Client connected: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    Log.d(TAG, "Received: " + line);
                    parseAndNotify(line);
                }
                clientSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Server error", e);
        }
    }

    private void parseAndNotify(String line) {
        // 格式: "P $AZ $EL" 例如 "P 120 45"
        if (line.startsWith("P ")) {
            String[] parts = line.split(" ");
            if (parts.length == 3) {
                try {
                    float az = Float.parseFloat(parts[1]);
                    float el = Float.parseFloat(parts[2]);
                    latestSatelliteData = new SatelliteData(az, el);
                    if (dataListener != null) {
                        dataListener.onSatelliteDataReceived(latestSatelliteData);
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Parse error", e);
                }
            }
        }
    }

    public SatelliteData getLatestSatelliteData() {
        return latestSatelliteData;
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        this.dataListener = listener;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}