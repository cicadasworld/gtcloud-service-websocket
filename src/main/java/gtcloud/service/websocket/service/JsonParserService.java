package gtcloud.service.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParserService {

    private JsonParserService() {
    }

    public static ObjectMapper getInstance() {
        return JsonParserServiceHolder.instance;
    }

    private static class JsonParserServiceHolder {
        private static final ObjectMapper instance = new ObjectMapper();
    }
}
