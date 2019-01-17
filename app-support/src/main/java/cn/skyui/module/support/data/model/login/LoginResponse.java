package cn.skyui.module.support.data.model.login;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    private String token;   // 登录Token
    private String imToken; // 融云 IM Token
    private Long userId;    // 业务用户ID，也是融云IM用户ID
    private int status;     // 用户资料完善状态，1：未完善，2：已完善；

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getImToken() {
        return imToken;
    }

    public void setImToken(String imToken) {
        this.imToken = imToken;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
