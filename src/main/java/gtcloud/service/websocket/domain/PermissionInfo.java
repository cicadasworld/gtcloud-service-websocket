package gtcloud.service.websocket.domain;

import java.util.HashMap;
import java.util.Map;

public class PermissionInfo {

    private int id;
    private String opDescription;
    private String sysPerm;
    private String objDomainCode;
    private String objMinorCode;
    private String creationTime;
    private String objMajorCode;
    private int objSecLevel;
    private String updateTime;
    private String objId;
    private String opCategory;
    private String opCode;
    private Map<String, String> objProps = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOpDescription() {
        return opDescription;
    }

    public void setOpDescription(String opDescription) {
        this.opDescription = opDescription;
    }

    public String getSysPerm() {
        return sysPerm;
    }

    public void setSysPerm(String sysPerm) {
        this.sysPerm = sysPerm;
    }

    public String getObjDomainCode() {
        return objDomainCode;
    }

    public void setObjDomainCode(String objDomainCode) {
        this.objDomainCode = objDomainCode;
    }

    public String getObjMinorCode() {
        return objMinorCode;
    }

    public void setObjMinorCode(String objMinorCode) {
        this.objMinorCode = objMinorCode;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getObjMajorCode() {
        return objMajorCode;
    }

    public void setObjMajorCode(String objMajorCode) {
        this.objMajorCode = objMajorCode;
    }

    public int getObjSecLevel() {
        return objSecLevel;
    }

    public void setObjSecLevel(int objSecLevel) {
        this.objSecLevel = objSecLevel;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public String getOpCategory() {
        return opCategory;
    }

    public void setOpCategory(String opCategory) {
        this.opCategory = opCategory;
    }

    public String getOpCode() {
        return opCode;
    }

    public void setOpCode(String opCode) {
        this.opCode = opCode;
    }

    public Map<String, String> getObjProps() {
        return objProps;
    }

    public void setObjProps(Map<String, String> objProps) {
        this.objProps = objProps;
    }
}
