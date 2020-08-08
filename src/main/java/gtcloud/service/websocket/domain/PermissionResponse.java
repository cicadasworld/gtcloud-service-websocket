package gtcloud.service.websocket.domain;

import java.util.ArrayList;
import java.util.List;

public class PermissionResponse {

    private int retcode;
    private String retmsg;
    private List<PermissionInfo> retdata = new ArrayList<>();

    public int getRetcode() {
        return retcode;
    }

    public void setRetcode(int retcode) {
        this.retcode = retcode;
    }

    public String getRetmsg() {
        return retmsg;
    }

    public void setRetmsg(String retmsg) {
        this.retmsg = retmsg;
    }

    public List<PermissionInfo> getRetdata() {
        return retdata;
    }

    public void setRetdata(List<PermissionInfo> retdata) {
        this.retdata = retdata;
    }
}
