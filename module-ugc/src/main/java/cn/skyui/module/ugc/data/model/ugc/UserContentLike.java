package cn.skyui.module.ugc.data.model.ugc;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tiansj on 2018/4/14.
 */

public class UserContentLike implements Serializable {
    private Long id;

    private Long uid;

    private Long ugcId;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getUgcId() {
        return ugcId;
    }

    public void setUgcId(Long ugcId) {
        this.ugcId = ugcId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
