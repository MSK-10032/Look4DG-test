package xyz._100320721.ba4ihr.look4dg;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import fi.iki.elonen.NanoHTTPD;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import xyz._100320721.ba4ihr.look4dg.models.Config;

public class HTTPServerService extends Service {
    private static final String TAG = "HTTPServerService";
    public static final int PORT = 8073;
    private WebServer server;
    private ConfigManager configManager;
    private Config currentConfig;
    private MainActivity mainActivityReference; // 用于获取最新二维码内容

    public void setMainActivity(MainActivity activity) {
        this.mainActivityReference = activity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configManager = new ConfigManager(this);
        currentConfig = configManager.loadConfig();

        server = new WebServer();
        try {
            server.start();
            Log.i(TAG, "HTTP Server started on port " + PORT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start HTTP server", e);
        }
    }

    private class WebServer extends NanoHTTPD {
        public WebServer() {
            super(PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            Method method = session.getMethod();

            if (Method.POST.equals(method) && "/save".equals(uri)) {
                // 处理配置保存
                try {
                    Map<String, String> params = session.getParms();
                    Config newConfig = new Config();
                    newConfig.setThreshold(Float.parseFloat(params.get("threshold")));
                    newConfig.setInitialStrength(Integer.parseInt(params.get("initialStrength")));
                    newConfig.setIncrementPerDegree(Float.parseFloat(params.get("increment")));
                    newConfig.setWsUrl(params.get("wsUrl"));
                    configManager.saveConfig(newConfig);
                    currentConfig = newConfig;
                    // 更新RuleEngine等（通过MainActivity）
                    if (mainActivityReference != null) {
                        mainActivityReference.onConfigUpdated(newConfig);
                    }
                    return newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, "<html><body>配置已保存。 <a href='/'>返回</a></body></html>");
                } catch (Exception e) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "参数错误");
                }
            } else {
                // 返回配置页面HTML
                String html = generateHtml();
                return newFixedLengthResponse(html);
            }
        }

        private String generateHtml() {
            // 生成二维码内容：根据当前配置的wsUrl，但实际应由MainActivity提供最新的连接信息
            String qrContent = currentConfig.getWsUrl();
            if (mainActivityReference != null) {
                // 可以自定义二维码内容，例如包含IP地址等
                qrContent = mainActivityReference.getQRContent();
            }
            String qrBase64 = "";
            Bitmap qrBitmap = QRCodeUtils.generateQRCode(qrContent, 200, 200);
            if (qrBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bytes = baos.toByteArray();
                qrBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
            }

            // 注意：HTML中的表单提交到/save
            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset='UTF-8'>\n" +
                    "    <title>Look4DG 配置</title>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial; margin: 20px; }\n" +
                    "        label { display: inline-block; width: 150px; }\n" +
                    "        input[type=number] { width: 100px; }\n" +
                    "        .qr { margin-top: 30px; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h2>Look4DG 配置</h2>\n" +
                    "    <form method='POST' action='/save'>\n" +
                    "        <p><label>启用阈值 (度):</label> <input type='number' name='threshold' step='0.1' value='" + currentConfig.getThreshold() + "'></p>\n" +
                    "        <p><label>初始强度 (0-1000):</label> <input type='number' name='initialStrength' min='0' max='1000' value='" + currentConfig.getInitialStrength() + "'></p>\n" +
                    "        <p><label>每度增量:</label> <input type='number' name='increment' step='0.1' value='" + currentConfig.getIncrementPerDegree() + "'></p>\n" +
                    "        <p><label>WebSocket URL:</label> <input type='text' name='wsUrl' size='40' value='" + currentConfig.getWsUrl() + "'></p>\n" +
                    "        <p><input type='submit' value='保存配置'></p>\n" +
                    "    </form>\n" +
                    "    <div class='qr'>\n" +
                    "        <h3>扫描二维码连接设备</h3>\n" +
                    "        <img src='data:image/png;base64," + qrBase64 + "' alt='QR Code'/>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";
        }
    }

    @Override
    public void onDestroy() {
        if (server != null) {
            server.stop();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Config getCurrentConfig() {
        return currentConfig;
    }
}