package cn.skyui.module.ugc.data.model.user;

import cn.skyui.module.ugc.data.model.banner.BannerVO;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tiansj on 2017/8/29.
 */
public class RecommendVO implements Serializable {

    private List<RecommendItemVO> data;
    private List<BannerVO> banners;

    public List<RecommendItemVO> getData() {
        return data;
    }

    public void setData(List<RecommendItemVO> data) {
        this.data = data;
    }

    public List<BannerVO> getBanners() {
        return banners;
    }

    public void setBanners(List<BannerVO> banners) {
        this.banners = banners;
    }

}
