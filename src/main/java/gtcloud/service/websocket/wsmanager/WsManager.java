package gtcloud.service.websocket.wsmanager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gtcloud.service.websocket.controller.WebSocketController;
import gtcloud.service.websocket.wsmanager.listener.WsStatusListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WsManager implements IWsManager {

    private final static int RECONNECT_INTERVAL = 10 * 1000;    //重连自增步长
    private final static long RECONNECT_MAX_TIME = 120 * 1000;   //最大重连间隔
    private WebSocketController controller;
    private String wsUrl;
    private WebSocket webSocket;
    private OkHttpClient okHttpClient;
    private Request request;
    private int currentStatus = WsStatus.DISCONNECTED;     //websocket连接状态
    private boolean isNeedReconnect;          //是否需要断线自动重连
    private boolean isManualClose = false;         //是否为手动关闭websocket连接
    private WsStatusListener wsStatusListener;
    private Lock lock;
    private int reconnectCount = 0;   //重连次数
    private Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (wsStatusListener != null) {
                wsStatusListener.onReconnect();
            }
            buildConnect();
        }
    };
    private WebSocketListener mWebSocketListener = new WebSocketListener() {

        @Override
        public void onOpen(WebSocket webSocket, final Response response) {
            WsManager.this.webSocket = webSocket;
            setCurrentStatus(WsStatus.CONNECTED);
            connected();
            if (wsStatusListener != null) {
                wsStatusListener.onOpen(response);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, final ByteString bytes) {
            if (wsStatusListener != null) {
                wsStatusListener.onMessage(bytes);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            wsStatusListener.onMessage(text);
        }

        @Override
        public void onClosing(WebSocket webSocket, final int code, final String reason) {
            wsStatusListener.onClosing(code, reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, final int code, final String reason) {
            if (wsStatusListener != null) {
                wsStatusListener.onClosed(code, reason);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, final Throwable t, final Response response) {
            tryReconnect();
            if (wsStatusListener != null) {
                wsStatusListener.onFailure(t, response);
            }
        }
    };

    public WsManager(Builder builder) {
        this.controller = builder.controller;
        this.wsUrl = builder.wsUrl;
        this.isNeedReconnect = builder.needReconnect;
        this.okHttpClient = builder.okHttpClient;
        this.lock = new ReentrantLock();
    }

    private void initWebSocket() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();
        }
        if (request == null) {
            request = new Request.Builder()
                    .url(wsUrl)
                    .build();
        }
        okHttpClient.dispatcher().cancelAll();
        try {
            lock.lockInterruptibly();
            try {
                okHttpClient.newWebSocket(request, mWebSocketListener);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
        }
    }

    @Override
    public WebSocket getWebSocket() {
        return webSocket;
    }


    public void setWsStatusListener(WsStatusListener wsStatusListener) {
        this.wsStatusListener = wsStatusListener;
    }

    @Override
    public synchronized boolean isWsConnected() {
        return currentStatus == WsStatus.CONNECTED;
    }

    @Override
    public synchronized int getCurrentStatus() {
        return currentStatus;
    }

    @Override
    public synchronized void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public void startConnect() {
        isManualClose = false;
        buildConnect();
    }

    @Override
    public void stopConnect() {
        isManualClose = true;
        disconnect();
    }

    private void tryReconnect() {
        if (!isNeedReconnect | isManualClose) {
            return;
        }

        if (!isNetworkConnected(controller)) {
            setCurrentStatus(WsStatus.DISCONNECTED);
            return;
        }

        setCurrentStatus(WsStatus.RECONNECT);
        reconnectCount++;
    }

    private void cancelReconnect() {
        reconnectCount = 0;
    }

    private void connected() {
        cancelReconnect();
    }

    private void disconnect() {
        if (currentStatus == WsStatus.DISCONNECTED) {
            return;
        }
        cancelReconnect();
        if (okHttpClient != null) {
            okHttpClient.dispatcher().cancelAll();
        }
        if (webSocket != null) {
            boolean isClosed = webSocket.close(WsStatus.CODE.NORMAL_CLOSE, WsStatus.TIP.NORMAL_CLOSE);
            //非正常关闭连接
            if (!isClosed) {
                if (wsStatusListener != null) {
                    wsStatusListener.onClosed(WsStatus.CODE.ABNORMAL_CLOSE, WsStatus.TIP.ABNORMAL_CLOSE);
                }
            }
        }
        setCurrentStatus(WsStatus.DISCONNECTED);
    }

    private synchronized void buildConnect() {
        if (!isNetworkConnected(controller)) {
            setCurrentStatus(WsStatus.DISCONNECTED);
            return;
        }
        switch (getCurrentStatus()) {
            case WsStatus.CONNECTED:
            case WsStatus.CONNECTING:
                break;
            default:
                setCurrentStatus(WsStatus.CONNECTING);
                initWebSocket();
        }
    }

    //发送消息
    @Override
    public boolean sendMessage(String msg) {
        return send(msg);
    }

    @Override
    public boolean sendMessage(ByteString byteString) {
        return send(byteString);
    }

    private boolean send(Object msg) {
        boolean isSend = false;
        if (webSocket != null && currentStatus == WsStatus.CONNECTED) {
            if (msg instanceof String) {
                isSend = webSocket.send((String) msg);
            } else if (msg instanceof ByteString) {
                isSend = webSocket.send((ByteString) msg);
            }
            //发送消息失败，尝试重连
            if (!isSend) {
                tryReconnect();
            }
        }
        return isSend;
    }

    //检查网络是否连接
    private boolean isNetworkConnected(WebSocketController controller) {
        return controller != null;
    }

    public static final class Builder {

        private WebSocketController controller;
        private String wsUrl;
        private boolean needReconnect = true;
        private OkHttpClient okHttpClient;

        public Builder(WebSocketController controller) {
            this.controller = controller;
        }

        public Builder wsUrl(String wsUrl) {
            this.wsUrl = wsUrl;
            return this;
        }

        public Builder client(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        public Builder needReconnect(boolean needReconnect) {
            this.needReconnect = needReconnect;
            return this;
        }

        public WsManager build() {
            return new WsManager(this);
        }
    }
}
