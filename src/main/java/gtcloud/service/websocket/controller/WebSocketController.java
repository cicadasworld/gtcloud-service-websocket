package gtcloud.service.websocket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtcloud.service.websocket.domain.GetCurrentFormationRequest;
import gtcloud.service.websocket.service.JsonParserService;
import gtcloud.service.websocket.service.PermissionInfoService;
import gtcloud.service.websocket.service.ResponseFilterService;
import gtcloud.service.websocket.service.WsManagerService;
import gtcloud.service.websocket.wsmanager.listener.WsStatusListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ServerEndpoint("/{userId}/{token}")
@Component
public class WebSocketController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);

    private Session session;
    private String userId;

    // userId -> (category -> objectId)
    private final Map<String, Map<String, Set<String>>> userIdToCategoryObjectIds = new HashMap<>();
    private boolean pass; // true for pass

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("token") String token) {
        LOGGER.info("===GTMapClient onOpen===");
        LOGGER.info("userId={}, token={}", userId, token);
        this.session = session;
        this.userId = userId;
        WsManagerService wsManagerService = WsManagerService.getInstance();
        wsManagerService.init(this, wsStatusListener);

        try {  // get and save permission
            savePermission(userId, token);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            onClose(session);
        }
    }

    private WsStatusListener wsStatusListener = new WsStatusListener() {
        @Override
        public void onMessage(String text) {
            try {
                if (pass) {
                    sendMessage(text); // send original TS server message
                } else {
                    ResponseFilterService responseFilterService = ResponseFilterService.getInstance();
                    String filteredText = responseFilterService.filter(text, userId, userIdToCategoryObjectIds);
                    sendMessage(filteredText != null ? filteredText : ""); // send filtered TS server message
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    };

    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    private void savePermission(String userId, String token) throws Exception {
        Map<String, Set<String>> categoryToObjectIds = new HashMap<>();
        String[] categories = new String[] {"BDTS-1", "BDTS-2"};
        for (String category : categories) {
            PermissionInfoService permissionInfoService = PermissionInfoService.getInstance();
            Set<String> targets = permissionInfoService.getTargets(userId, token, category);
            categoryToObjectIds.putIfAbsent(category, targets);
        }
        userIdToCategoryObjectIds.putIfAbsent(userId, categoryToObjectIds);
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("===GTMapClient onClose===");
        WsManagerService wsManagerService = WsManagerService.getInstance();
        wsManagerService.disconnect();
    }

    @OnError
    public void onError(Throwable t) {
        LOGGER.error(t.getMessage(), t);
    }

    @OnMessage
    @SuppressWarnings("unchecked")
    public void onMessage(String message, Session session) {
        LOGGER.info("client message: {}", message);
        String clientMessage = message;
        ObjectMapper mapper = JsonParserService.getInstance();
        try {
            Map<String, Object> map = mapper.readValue(message, Map.class);
            String request = (String) map.get("request");
            String originId = (String) map.get("origin_id");
            if ("formation_all".equals(request) && "1".equals(originId)) {
                pass = true;
                GetCurrentFormationRequest getCurrentFormationRequest = new GetCurrentFormationRequest();
                getCurrentFormationRequest.setRequest("get_current_formation");
                getCurrentFormationRequest.setIdentifier(UUID.randomUUID().toString());
                getCurrentFormationRequest.setOriginId("1");

                clientMessage = mapper.writeValueAsString(getCurrentFormationRequest);
            } else if ("formation_all".equals(request) && "2".equals(originId)) {
                pass = true;
                GetCurrentFormationRequest getCurrentFormationRequest = new GetCurrentFormationRequest();
                getCurrentFormationRequest.setRequest("get_current_formation");
                getCurrentFormationRequest.setIdentifier(UUID.randomUUID().toString());
                getCurrentFormationRequest.setOriginId("2");

                clientMessage = mapper.writeValueAsString(getCurrentFormationRequest);
            }
            WsManagerService wsManagerService = WsManagerService.getInstance();
            wsManagerService.sendMessage(clientMessage); // send GTMap client message to TS server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

