package gtcloud.service.websocket.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtcloud.service.websocket.domain.FormationInfo;
import gtcloud.service.websocket.domain.GetCurrentFormationResponse;
import gtcloud.service.websocket.service.JsonParserService;
import gtcloud.service.websocket.service.ResponseFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetCurrentFormationFilter implements ResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrentFormationFilter.class);

    private GetCurrentFormationFilter() {
    }

    public static GetCurrentFormationFilter getInstance() {
        return GetCurrentFormationFilterHolder.instance;
    }

    private static class GetCurrentFormationFilterHolder {
        private static final GetCurrentFormationFilter instance = new GetCurrentFormationFilter();
    }

    @Override
    public String filter(String serverMessage, String userId, Map<String, Map<String, List<String>>> userIdToCategoryTargetIds)
            throws IOException {
        LOGGER.info("original server message: {}", serverMessage);
        ObjectMapper mapper = JsonParserService.getInstance();
        GetCurrentFormationResponse originalResponse = mapper.readValue(serverMessage, GetCurrentFormationResponse.class);

        GetCurrentFormationResponse filteredResponse = new GetCurrentFormationResponse();
        filteredResponse.setCode(originalResponse.getCode());
        filteredResponse.setIdentifier(originalResponse.getIdentifier());
        filteredResponse.setResponse(originalResponse.getResponse());
        String originId = originalResponse.getOriginId();
        filteredResponse.setOriginId(originId);

        List<FormationInfo> originalData = originalResponse.getData();
        for (FormationInfo originalFormationInfo : originalData) {
            FormationInfo filteredFormationInfo = new FormationInfo();
            List<List<Object>> originalFormationData = originalFormationInfo.getFormationData();
            for (List<Object> target : originalFormationData) {
                String targetId = (String) target.get(1); // second element is objectId
                ResponseFilterService responseFilterService = ResponseFilterService.getInstance();
                boolean contains = responseFilterService.contain(targetId, originId, userId, userIdToCategoryTargetIds);
                if (contains) {
                    filteredFormationInfo.getFormationData().add(target);
                }
            }

            filteredFormationInfo.setFormationId(originalFormationInfo.getFormationId());
            filteredFormationInfo.setFormationName(originalFormationInfo.getFormationName());
            filteredFormationInfo.setOriginId(originalFormationInfo.getOriginId());

            filteredResponse.getData().add(filteredFormationInfo);
        }

        String filteredJson = mapper.writeValueAsString(filteredResponse);
        LOGGER.info("filtered server message: {}", filteredJson);
        return filteredJson;
    }
}
