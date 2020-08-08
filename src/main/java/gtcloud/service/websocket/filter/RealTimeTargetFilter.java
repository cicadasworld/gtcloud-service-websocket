package gtcloud.service.websocket.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtcloud.service.websocket.domain.RealTimeTargetResponse;
import gtcloud.service.websocket.service.JsonParserService;
import gtcloud.service.websocket.service.ResponseFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RealTimeTargetFilter implements ResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeTargetFilter.class);

    private RealTimeTargetFilter() {
    }

    public static RealTimeTargetFilter getInstance() {
        return RealTimeTargetFilterHolder.instance;
    }

    private static class RealTimeTargetFilterHolder {
        private static final RealTimeTargetFilter instance = new RealTimeTargetFilter();
    }

    @Override
    public String filter(String serverMessage, String userId, Map<String, Map<String, List<String>>> userIdToCategoryObjectIds)
            throws IOException {
        LOGGER.info("original server message: {}", serverMessage);
        ObjectMapper mapper = JsonParserService.getInstance();
        RealTimeTargetResponse originalResponse = mapper.readValue(serverMessage, RealTimeTargetResponse.class);

        RealTimeTargetResponse filteredResponse = new RealTimeTargetResponse();
        filteredResponse.setResponse(originalResponse.getResponse());
        String originId = originalResponse.getOriginId();
        filteredResponse.setOriginId(originId);

        List<List<Object>> originalData = originalResponse.getData();
        for (List<Object> targets : originalData) {
            String objectId = (String) targets.get(0); // first item is objectId
            ResponseFilterService responseFilterService = ResponseFilterService.getInstance();
            boolean contains = responseFilterService.contain(objectId, originId, userId, userIdToCategoryObjectIds);
            if (contains) {
                filteredResponse.getData().add(targets);
            }
        }

        String filteredJson = mapper.writeValueAsString(filteredResponse);
        LOGGER.info("filtered server message: {}", filteredJson);
        return filteredJson;
    }
}
