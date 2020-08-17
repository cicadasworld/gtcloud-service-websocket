package gtcloud.service.websocket.service;

import gtcloud.service.websocket.controller.WebSocketController;
import gtcloud.service.websocket.wsmanager.WsManager;
import gtcloud.service.websocket.wsmanager.listener.WsStatusListener;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsManagerService.class);

    private final String SITUATION_SERVER_ADDRESS = "ws://127.0.0.1:6010/1/tYfQIgQAdAQW7HMBAABEAAAAAAAAAAEAAACHAwCB";
    private static WsManagerService service;
    private static WsManager wsManager;

    private WsManagerService() {
    }

    public static WsManagerService getInstance() {
        return WsManagerServiceHolder.instance;
    }

    private static class WsManagerServiceHolder {
        private static final WsManagerService instance = new WsManagerService();
    }

    public void init(WebSocketController controller, WsStatusListener wsStatusListener) {
        String situationServerAddress = System.getProperty("situation.server.address", SITUATION_SERVER_ADDRESS);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .pingInterval(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        wsManager = new WsManager.Builder(controller).client(okHttpClient)
                .needReconnect(true)
                .wsUrl(situationServerAddress)
                .build();
        wsManager.setWsStatusListener(wsStatusListener);
        wsManager.startConnect();
    }

    public void disconnect() {
        if (wsManager != null)
            wsManager.stopConnect();
    }

    public void sendMessage(String content) {
        if (wsManager != null && wsManager.isWsConnected()) {
            boolean isSend = wsManager.sendMessage(content);
            if (isSend) {
                LOGGER.info("succeed to send message");
            } else {
                LOGGER.info("failed to send message");
            }
        }
    }
}
