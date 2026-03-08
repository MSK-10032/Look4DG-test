package xyz._100320721.ba4ihr.look4dg;

import xyz._100320721.ba4ihr.look4dg.models.Config;
import xyz._100320721.ba4ihr.look4dg.models.SatelliteData;
import xyz._100320721.ba4ihr.look4dg.models.DeviceOrientation;

public class RuleEngine {
    private Config config;

    public RuleEngine(Config config) {
        this.config = config;
    }

    public void updateConfig(Config config) {
        this.config = config;
    }

    /**
     * 计算方位角差 (0-180)
     */
    private float angleDifference(float a1, float a2) {
        float diff = Math.abs(a1 - a2);
        if (diff > 180) diff = 360 - diff;
        return diff;
    }

    /**
     * 根据卫星数据和手机方向计算输出强度
     * @return 强度值 (0-1000)，如果偏移小于阈值则返回0
     */
    public int computeStrength(SatelliteData satellite, DeviceOrientation device) {
        if (satellite == null || device == null) return 0;

        float azDiff = angleDifference(satellite.getAzimuth(), device.getAzimuth());
        float elDiff = Math.abs(satellite.getElevation() - device.getPitch());

        // 综合偏移：可自定义，这里使用欧几里得距离的近似
        float totalDiff = (float) Math.sqrt(azDiff * azDiff + elDiff * elDiff);

        if (totalDiff < config.getThreshold()) {
            return 0;
        }

        // 超出阈值部分计算强度
        float excess = totalDiff - config.getThreshold();
        int strength = config.getInitialStrength() + (int) (excess * config.getIncrementPerDegree());
        // 限制在0-1000之间（假设DG-LAB强度范围）
        return Math.max(0, Math.min(1000, strength));
    }
}