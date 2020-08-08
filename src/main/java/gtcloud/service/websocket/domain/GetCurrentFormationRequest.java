package gtcloud.service.websocket.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetCurrentFormationRequest {
    private String request;
    private String identifier;
    @JsonProperty("origin_id")
    private String originId;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }
}
