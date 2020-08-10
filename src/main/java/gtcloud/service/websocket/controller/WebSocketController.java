package gtcloud.service.websocket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

import gtcloud.service.websocket.domain.GetCurrentFormationRequest;
import gtcloud.service.websocket.service.JsonParserService;
import gtcloud.service.websocket.service.PermissionInfoService;
import gtcloud.service.websocket.service.ResponseFilterService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

@ServerEndpoint(value = "/mobile/{userId}/{token}")
@Component
public class WebSocketController extends WebSocketListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);

    private Session session;
    private String userId;
    private WebSocket webSocket;

    // userId -> (category -> objectId)
    private final Map<String, Map<String, Set<String>>> userIdToCategoryObjectIds = new HashMap<>();
    private boolean pass; // true for all pass

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        LOGGER.info("===httpclient onOpen===");
        this.webSocket = webSocket;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        String filteredText;
        try {
            if (pass) {
                sendMessage(text); // send original TS server message
            } else {
                ResponseFilterService responseFilterService = ResponseFilterService.getInstance();
                filteredText = responseFilterService.filter(text, userId, userIdToCategoryObjectIds);
                sendMessage(filteredText != null ? filteredText : ""); // send filtered TS server message
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void sendMessage(String text) throws IOException {
        session.getBasicRemote().sendText(text); // send TS server message to GT client
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("token") String token) {
        LOGGER.info("===GTClient onOpen===");
        LOGGER.info("userId={}, token={}", userId, token);
        this.session = session;
        this.userId = userId;
        OkHttpClient client = new OkHttpClient.Builder().build();
        String situationServerAddress = System.getProperty("situation.server.address", "ws://localhost:8090/ws");
        LOGGER.info("situation server address: {}", situationServerAddress);
        Request request = new Request.Builder().url(situationServerAddress).build();
        client.newWebSocket(request, this);

        try {  // get and save permission
            savePermission(userId, token);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            try {
                if (session != null) {
                    session.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void savePermission(String userId, String token) throws Exception {
        Map<String, Set<String>> categoryToTargetIds = new HashMap<>();
        String[] categories = new String[] {"BDTS-1", "BDTS-2"};
        for (String category : categories) {
            PermissionInfoService permissionInfoService = PermissionInfoService.getInstance();
            Set<String> targets = permissionInfoService.getTargets(userId, token, category);
            categoryToTargetIds.putIfAbsent(category, targets);
        }
        userIdToCategoryObjectIds.putIfAbsent(userId, categoryToTargetIds);
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("===GTClient onClose===");
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
            webSocket.send(clientMessage);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage);
        }
    }
}
