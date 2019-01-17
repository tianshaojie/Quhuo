package cn.skyui.module.ugc.data.model.user;

import cn.skyui.library.data.model.UserVO;
import cn.skyui.module.ugc.data.model.banner.BannerVO;

import java.io.Serializable;
import java.util.List;

public class UserBannerVO implements Serializable {

    private List<UserVO> users;
    private List<BannerVO> banners;

    public List<UserVO> getUsers() {
        return users;
    }

    public void setUsers(List<UserVO> users) {
        this.users = users;
    }

    public List<BannerVO> getBanners() {
        return banners;
    }

    public void setBanners(List<BannerVO> banners) {
        this.banners = banners;
    }
}
