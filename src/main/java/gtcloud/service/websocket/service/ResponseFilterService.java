package gtcloud.service.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtcloud.service.websocket.filter.GetCurrentFormationFilter;
import gtcloud.service.websocket.filter.GetCurrentTargetFilter;
import gtcloud.service.websocket.filter.RealTimeTargetFilter;
import gtcloud.service.websocket.filter.ResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public String filter(String serverMessage, String userId, Map<String, Map<String, List<String>>> userIdToCategoryTargetIds)
            throws IOException {
        ObjectMapper mapper = JsonParserService.getInstance();
        Map<String, Object> map = mapper.readValue(serverMessage, Map.class);

        String response = (String) map.get("response");
        if ("get_current_target".equals(response)) {
            ResponseFilter getCurrentTargetFilter = GetCurrentTargetFilter.getInstance();
            return getCurrentTargetFilter.filter(serverMessage, userId, userIdToCategoryTargetIds);
        }
        if ("realtime_target".equals(response)) {
            RealTimeTargetFilter realTimeTargetFilter = RealTimeTargetFilter.getInstance();
            return realTimeTargetFilter.filter(serverMessage, userId, userIdToCategoryTargetIds);
        }
        if ("get_current_formation".equals(response)) {
            GetCurrentFormationFilter getCurrentFormationFilter = GetCurrentFormationFilter.getInstance();
            return getCurrentFormationFilter.filter(serverMessage, userId, userIdToCategoryTargetIds);
        }
        return serverMessage;
    }

    public boolean contain(String targetId, String originId, String userId, Map<String, Map<String, List<String>>> userIdToCategoryTargetIds) {
        String category = getCategory(originId);
        Map<String, List<String>> categoryToTargetIds = userIdToCategoryTargetIds.get(userId);
        List<String> objectIds = new ArrayList<>();
        List<String> targetIds = categoryToTargetIds.get(category);
        if (targetIds != null) {
            objectIds.addAll(targetIds);
        }
        return objectIds.contains(targetId);
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
                category = "unknown";
        }
        return category;
    }
}
