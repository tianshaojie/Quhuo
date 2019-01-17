package cn.skyui.module.ugc.data.model.ugc;

import cn.skyui.module.ugc.data.model.user.SimpleUserVO;

import java.io.Serializable;

public class FeedVO implements Serializable {

    private SimpleUserVO user;
    private UgcItemVO ugcItem;

    private double distance;
    private long time;
    private String poiname;

    public String getPoiname() {
        return poiname;
    }

    public void setPoiname(String poiname) {
        this.poiname = poiname;
    }

    public SimpleUserVO getUser() {
        return user;
    }

    public void setUser(SimpleUserVO user) {
        this.user = user;
    }

    public UgcItemVO getUgcItem() {
        return ugcItem;
    }

    public void setUgcItem(UgcItemVO ugcItem) {
        this.ugcItem = ugcItem;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
