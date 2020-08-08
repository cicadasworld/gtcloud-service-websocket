package gtcloud.service.websocket;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient extends WebSocketListener {

    public static void main(String[] args) {
        new WebSocketClient().run();
    }

    private void run() {
        OkHttpClient client = new OkHttpClient();
        okhttp3.Request request = new Request.Builder()
            .url("ws://{ip}:{port}/mobile")
            .build();
        client.newWebSocket(request, this);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        String clientMessage = "{\"request\":\"get_current_formation\",\"identifier\":\"01\"}";
        webSocket.send(clientMessage);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println(text);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
    }
}
