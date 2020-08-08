package gtcloud.service.websocket.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GetCurrentFormationResponse {

    private int code;
    private String identifier;
    private List<FormationInfo> data = new ArrayList<>();
    private String response;
    @JsonProperty("origin_id")
    private String originId;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<FormationInfo> getData() {
        return data;
    }

    public void setData(List<FormationInfo> data) {
        this.data = data;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }
}
