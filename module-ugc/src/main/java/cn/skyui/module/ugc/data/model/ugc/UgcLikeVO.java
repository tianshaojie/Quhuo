package cn.skyui.module.ugc.data.model.ugc;

import java.io.Serializable;

/**
 * Created by tiansj on 2018/4/14.
 */

public class UgcLikeVO implements Serializable {
    private UserContentLike userContentLike;
    private UgcItemVO ugcItem;

    public UserContentLike getUserContentLike() {
        return userContentLike;
    }

    public void setUserContentLike(UserContentLike userContentLike) {
        this.userContentLike = userContentLike;
    }

    public UgcItemVO getUgcItem() {
        return ugcItem;
    }

    public void setUgcItem(UgcItemVO ugcItem) {
        this.ugcItem = ugcItem;
    }
}
