package gtcloud.service.websocket.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GTGetCurrentFormationRequest {
    private String request;
    @JsonProperty("origin_id")
    private String originId;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }
}
