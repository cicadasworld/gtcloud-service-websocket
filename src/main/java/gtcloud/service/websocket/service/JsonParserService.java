package gtcloud.service.websocket.service;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParserService {

    private JsonParserService() {
    }

    public static ObjectMapper getInstance() {
        return JsonParserServiceHolder.instance;
    }

    private static class JsonParserServiceHolder {
        private static final ObjectMapper instance = new ObjectMapper()
            .configure(Feature.ESCAPE_NON_ASCII, true);
    }
}
