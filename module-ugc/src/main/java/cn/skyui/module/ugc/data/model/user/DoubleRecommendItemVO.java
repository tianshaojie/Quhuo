package cn.skyui.module.ugc.data.model.user;

import java.io.Serializable;

/**
 * Created by tiansj on 2017/8/20.
 */
public class DoubleRecommendItemVO implements Serializable {

    RecommendItemVO item1;
    RecommendItemVO item2;

    public RecommendItemVO getItem1() {
        return item1;
    }

    public void setItem1(RecommendItemVO item1) {
        this.item1 = item1;
    }

    public RecommendItemVO getItem2() {
        return item2;
    }

    public void setItem2(RecommendItemVO item2) {
        this.item2 = item2;
    }
}
