package gtcloud.service.websocket.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class FormationInfo {

    @JsonProperty("formation_id")
    private String formationId;
    @JsonProperty("origin_id")
    private String originId;
    @JsonProperty("formation_data")
    private List<List<Object>> formationData = new ArrayList<>();
    @JsonProperty("formation_name")
    private String formationName;

    public String getFormationId() {
        return formationId;
    }

    public void setFormationId(String formationId) {
        this.formationId = formationId;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public List<List<Object>> getFormationData() {
        return formationData;
    }

    public void setFormationData(List<List<Object>> formationData) {
        this.formationData = formationData;
    }

    public String getFormationName() {
        return formationName;
    }

    public void setFormationName(String formationName) {
        this.formationName = formationName;
    }
}
