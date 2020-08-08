package gtcloud.service.websocket.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtcloud.service.websocket.domain.GetCurrentTargetResponse;
import gtcloud.service.websocket.service.JsonParserService;
import gtcloud.service.websocket.service.ResponseFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetCurrentTargetFilter implements ResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrentTargetFilter.class);

    private GetCurrentTargetFilter() {
    }

    public static GetCurrentTargetFilter getInstance() {
        return GetCurrentTargetFilterHolder.instance;
    }

    private static class GetCurrentTargetFilterHolder {
        private static final GetCurrentTargetFilter instance = new GetCurrentTargetFilter();
    }

    @Override
    public String filter(String serverMessage, String userId, Map<String, Map<String, List<String>>> userIdToCategoryTargetIds)
            throws IOException {
        LOGGER.info("original server message: {}", serverMessage);
        ObjectMapper mapper = JsonParserService.getInstance();
        GetCurrentTargetResponse originalResponse = mapper.readValue(serverMessage, GetCurrentTargetResponse.class);

        GetCurrentTargetResponse filteredResponse = new GetCurrentTargetResponse();
        filteredResponse.setCode(originalResponse.getCode());
        filteredResponse.setIdentifier(originalResponse.getIdentifier());
        filteredResponse.setResponse(originalResponse.getResponse());
        String originId = originalResponse.getOriginId();
        filteredResponse.setOriginId(originId);

        List<List<Object>> originalData = originalResponse.getData();
        for (List<Object> target : originalData) {
            String targetId = (String) target.get(0);  // first element is the objectId
            ResponseFilterService responseFilterService = ResponseFilterService.getInstance();
            boolean contains = responseFilterService.contain(targetId, originId, userId, userIdToCategoryTargetIds);
            if (contains) {
                filteredResponse.getData().add(target);
            }
        }

        String filteredJson = mapper.writeValueAsString(filteredResponse);
        LOGGER.info("filtered server message: {}", filteredJson);
        return filteredJson;
    }
}
