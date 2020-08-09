package gtcloud.service.websocket.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface ResponseFilter {

    String filter(String serverMessage, String userId, Map<String, Map<String, Set<String>>> userIdToCategoryObjectIds) throws IOException;

}
