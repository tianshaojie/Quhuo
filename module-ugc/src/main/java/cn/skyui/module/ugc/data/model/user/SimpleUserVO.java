package cn.skyui.module.ugc.data.model.user;

import java.io.Serializable;

/**
 * Created by tiansj on 2018/5/6.
 */

public class SimpleUserVO implements Serializable {

    private Long id = 0L;
    private String nickname = "";
    private String avatar = "";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
