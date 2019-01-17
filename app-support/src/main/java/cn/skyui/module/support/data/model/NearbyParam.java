package cn.skyui.module.support.data.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class NearbyParam implements Serializable {

    private int type; // 0：附近5公里，1：同城
    private Long uid;
    private String city;
    private BigDecimal lon;
    private BigDecimal lat;
    private long time;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
