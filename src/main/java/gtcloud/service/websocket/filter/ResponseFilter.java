package gtcloud.service.websocket.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ResponseFilter {

    String filter(String serverMessage, String userId, Map<String, Map<String, List<String>>> userIdToCategoryObjectIds) throws IOException;

}
