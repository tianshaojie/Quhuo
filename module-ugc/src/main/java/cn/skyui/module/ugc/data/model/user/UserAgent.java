package cn.skyui.module.ugc.data.model.user;

import java.io.Serializable;

/**
 User-Agent:{
    "deviceId" : "ABE35EC1-EE4D-4C91-913A-D1B47F29DD61",
    "model" : "iPhone",
    "sysVer" : "11.0",
    "appVer" : "1.0.0",
    "osType" : "iOS",
    "buildVer" : "10"
 }
 */
public class UserAgent implements Serializable {

    private String appVer;
    private String buildVer;
    private String osType;
    private String sysVer;
    private String model;
    private String deviceId;

    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public String getBuildVer() {
        return buildVer;
    }

    public void setBuildVer(String buildVer) {
        this.buildVer = buildVer;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getSysVer() {
        return sysVer;
    }

    public void setSysVer(String sysVer) {
        this.sysVer = sysVer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
