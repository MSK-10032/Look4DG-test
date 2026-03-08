package xyz._100320721.ba4ihr.look4dg;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private WebSocketClient webSocketClient;
    private String serverUri;
    private ConnectionListener listener;

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public WebSocketManager(String serverUri) {
        this.serverUri = serverUri;
    }

    public void connect(ConnectionListener listener) {
        this.listener = listener;
        try {
            webSocketClient = new WebSocketClient(new URI(serverUri)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.i(TAG, "WebSocket opened");
                    if (listener != null) listener.onConnected();
                }

                @Override
                public void onMessage(String message) {
                    Log.i(TAG, "Received: " + message);
                    // 可处理来自设备的消息
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.i(TAG, "WebSocket closed: " + reason);
                    if (listener != null) listener.onDisconnected();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                    if (listener != null) listener.onError(ex.getMessage());
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "WebSocket connection failed", e);
            if (listener != null) listener.onError(e.getMessage());
        }
    }

    public void sendStrength(int strength) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            // 构造DG-LAB可识别的指令，这里用JSON示例
            String command = String.format("{\"type\":\"strength\",\"value\":%d}", strength);
            webSocketClient.send(command);
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}