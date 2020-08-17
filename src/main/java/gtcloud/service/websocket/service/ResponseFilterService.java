package gtcloud.service.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtcloud.service.websocket.filter.GetCurrentFormationFilter;
import gtcloud.service.websocket.filter.GetCurrentTargetFilter;
import gtcloud.service.websocket.filter.RealTimeTargetFilter;
import gtcloud.service.websocket.filter.ResponseFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseFilterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFilterService.class);

    private ResponseFilterService() {
    }

    public static ResponseFilterService getInstance() {
        return FilterServiceHolder.instance;
    }

    private static class FilterServiceHolder {
        private static final ResponseFilterService instance = new ResponseFilterService();
    }

    @SuppressWarnings("unchecked")
    public String filter(String serverMessage, String userId,
        Map<String, Map<String, Set<String>>> userIdToCategoryObjectIds) throws IOException {
        boolean isResponse = serverMessage.contains("response");
        if (!isResponse) {
            return serverMessage;
        }
        ObjectMapper mapper = JsonParserService.getInstance();
        Map<String, Object> map = mapper.readValue(serverMessage, Map.class);
        String response = (String) map.get("response");
        if ("get_current_target".equals(response)) {
            ResponseFilter getCurrentTargetFilter = GetCurrentTargetFilter.getInstance();
            return getCurrentTargetFilter.filter(serverMessage, userId, userIdToCategoryObjectIds);
        }
        else if ("realtime_target".equals(response)) {
            RealTimeTargetFilter realTimeTargetFilter = RealTimeTargetFilter.getInstance();
            return realTimeTargetFilter.filter(serverMessage, userId, userIdToCategoryObjectIds);
        }
        else if ("get_current_formation".equals(response)) {
            GetCurrentFormationFilter getCurrentFormationFilter = GetCurrentFormationFilter.getInstance();
            return getCurrentFormationFilter.filter(serverMessage, userId, userIdToCategoryObjectIds);
        }
        return serverMessage;
    }

    public boolean contain(String objectId, String originId, String userId,
        Map<String, Map<String, Set<String>>> userIdToCategoryObjectIds) {

        if ("2".equals(originId)) { // Let HKQ pass
            return true;
        }

        String category = getCategory(originId);
        Map<String, Set<String>> categoryToObjectIds = userIdToCategoryObjectIds.get(userId);

        if (categoryToObjectIds.containsKey(category)) {
            Set<String> objectIds = categoryToObjectIds.get(category);
            return objectIds.contains(objectId);
        }
        return false;
    }

    private String getCategory(String originId) {
        String category;
        switch (originId) {
            case "1": // BD
                category = "BDTS-1";
                break;
            case "2": // HKQ
                category = "BDTS-2";
                break;
            default:
                category = "UNKNOWN";
        }
        return category;
    }
}
