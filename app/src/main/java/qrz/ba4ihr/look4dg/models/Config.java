package qrz.ba4ihr.look4dg.models;

public class Config {
    private float threshold;      // 启用偏移阈值（度）
    private int initialStrength;  // 初始强度 (0-1000)
    private float incrementPerDegree; // 每偏移1度增加强度值
    private String wsUrl;         // WebSocket连接地址（二维码可能包含）

    public Config() {
        // 默认值
        this.threshold = 10.0f;
        this.initialStrength = 50;
        this.incrementPerDegree = 5.0f;
        this.wsUrl = "ws://example.com/dglab";
    }

    // Getters and Setters
    public float getThreshold() { return threshold; }
    public void setThreshold(float threshold) { this.threshold = threshold; }

    public int getInitialStrength() { return initialStrength; }
    public void setInitialStrength(int initialStrength) { this.initialStrength = initialStrength; }

    public float getIncrementPerDegree() { return incrementPerDegree; }
    public void setIncrementPerDegree(float incrementPerDegree) { this.incrementPerDegree = incrementPerDegree; }

    public String getWsUrl() { return wsUrl; }
    public void setWsUrl(String wsUrl) { this.wsUrl = wsUrl; }
}
